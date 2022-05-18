package cloudgene.mapred.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

import cloudgene.mapred.server.Application;
import cloudgene.mapred.util.Config;
import cloudgene.mapred.util.Settings;
import genepi.base.Tool;
import genepi.hadoop.HadoopCluster;
import io.micronaut.context.env.Environment;
import io.micronaut.runtime.Micronaut;

public class StartServer extends Tool {

	public static final String DEFAULT_HADOOP_USER = "cloudgene";

	private String[] args;

	public StartServer(String[] args) {
		super(args);
		this.args = args;
	}

	@Override
	public void createParameters() {
		addOptionalParameter("user", "Hadoop username [default: " + DEFAULT_HADOOP_USER + "]", Tool.STRING);
		addOptionalParameter("port", "running webinterface on this port [default: 8082]", Tool.STRING);
		addOptionalParameter("conf", "Hadoop configuration folder", Tool.STRING);
	}

	@Override
	public int run() {

		if (getValue("conf") != null) {

			String conf = getValue("conf").toString();

			String username = null;
			if (getValue("user") != null) {
				username = getValue("user").toString();
			}
			System.out.println(
					"Use Haddop configuration folder " + conf + (username != null ? " with username " + username : ""));
			HadoopCluster.setConfPath("Unknown", conf, username);

		} else {
			// if (settings.getCluster() == null) {
			// System.out.println("No external Haddop cluster set.");
			// }
		}

		try {

			// load cloudgene.conf file. contains path to settings, db, apps, ..
			Application.config = new Config();
			if (new File(Config.CONFIG_FILENAME).exists()) {
				YamlReader reader = new YamlReader(new FileReader(Config.CONFIG_FILENAME));
				Application.config = reader.read(Config.class);
			}

			// load setting.yaml. contains applications, server configuration, ...
			Application.settings = loadSettings(Application.config);

			String port = Application.settings.getPort();
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put("micronaut.server.port", port);
			if (Application.settings.getUploadLimit() != -1) {
				properties.put("micronaut.server.multipart.maxFileSize", Application.settings.getUploadLimit() + "MB");
			}
			
			String secretKey = Application.settings.getSecretKey();
			if (secretKey == null || secretKey.isEmpty() || secretKey.equals(Settings.DEFAULT_SECURITY_KEY)) {
				secretKey = RandomStringUtils.randomAlphabetic(64);
				Application.settings.setSecretKey(secretKey);
				Application.settings.save();
			}

			
			properties.put("micronaut.security.token.jwt.signatures.secret.generator.secret",
					Application.settings.getSecretKey());
			properties.put("micronaut.autoRetireInterval", Application.settings.getAutoRetireInterval() + "h");

			// TODO: urlPrefix

			if (new File("webapp").exists()) {

				Micronaut.build(args).mainClass(Application.class).properties(properties).start();

			} else {

				System.out.println("Start in DEVELOPMENT mode");
				Micronaut.build(args).mainClass(Application.class).properties(properties)
						.defaultEnvironments(Environment.DEVELOPMENT).start();

			}

			// TODO: check why we need this?
			System.out.println();
			System.out.println("Server is running");
			System.out.println();
			System.out.println("Please press ctrl-c to stop.");
			while (true) {
				Thread.sleep(5000000);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
	}

	@Override
	public void init() {

	}

	protected Settings loadSettings(Config config) throws FileNotFoundException, YamlException {
		String settingsFilename = config.getSettings();

		// load default settings when not yet loaded
		Settings settings;
		if (new File(settingsFilename).exists()) {
			System.out.println("Loading settings from " + settingsFilename + "...");
			settings = Settings.load(config);
		} else {
			settings = new Settings(config);
		}

		if (!settings.testPaths()) {
			System.exit(1);
		}

		if (settings.getHostname() == null || settings.getHostname().trim().isEmpty()) {
			System.out.println("Hostname not set. Please set hostname in file '" + settingsFilename + "'");
			System.exit(1);
		}

		return settings;
	}

}