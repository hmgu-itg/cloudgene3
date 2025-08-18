package cloudgene.mapred.database.util;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.core.User;
import cloudgene.mapred.database.TemplateDao;
import cloudgene.mapred.database.UserDao;
import cloudgene.mapred.util.HashUtil;
import cloudgene.mapred.util.Template;

public class Fixtures {

	private static final Log log = LogFactory.getLog(Fixtures.class);

	public static String USERNAME = "admin";

	public static String PASSWORD = "admin1978";

	public static void insert(Database database) {

		// insert user
		UserDao dao = new UserDao(database);
		User user = dao.findByUsername(USERNAME);
		if (user == null) {
			log.info("Creating Admin user");
			user = new User();
			user.setUsername(USERNAME);
			PASSWORD = HashUtil.hashPassword(PASSWORD);
			user.setPassword(PASSWORD);
			user.makeAdmin();
			user.setFullName("Admin");
			user.setMail("admin@example.com");
			user.setInstituteEmail("supervisor@example.com");
			user.setInstituteName("Institute");
			user.setInstituteAddress1("Institute Address1");
			user.setInstituteCity("City");
			user.setInstitutePostCode("Postcode");
			user.setInstituteCountry("Germany");
			user.setAcceptedTandC(new Date());
			user.setAcceptedCountry(new Date());

			dao.insert(user);
			log.info("User " + USERNAME + " created.");
		} else {

			log.info("User " + USERNAME + " already exists.");

			if (!user.isAdmin()) {
				user.makeAdmin();
				dao.update(user);
				log.info("User " + USERNAME + " has admin rights now.");
			}
		}

		// insert template messages
		TemplateDao htmlSnippetDao = new TemplateDao(database);

		for (Template defaultSnippet : Template.SNIPPETS) {

			Template snippet = htmlSnippetDao
					.findByKey(defaultSnippet.getKey());
			if (snippet == null) {
				htmlSnippetDao.insert(defaultSnippet);
				log.info("Template " + defaultSnippet.getKey() + " created.");
			} else {
				log.info("Template " + defaultSnippet.getKey()
						+ " already exists.");
			}

		}

	}

}
