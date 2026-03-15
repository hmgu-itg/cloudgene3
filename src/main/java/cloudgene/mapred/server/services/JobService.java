package cloudgene.mapred.server.services;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Vector;

import cloudgene.mapred.database.ParameterDao;
import cloudgene.mapred.jobs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloudgene.mapred.apps.ApplicationRepository;
import cloudgene.mapred.core.User;
import cloudgene.mapred.database.DownloadDao;
import cloudgene.mapred.database.JobDao;
import cloudgene.mapred.jobs.workspace.IWorkspace;
import cloudgene.mapred.jobs.workspace.WorkspaceFactory;
import cloudgene.mapred.server.Application;
import cloudgene.mapred.server.exceptions.JsonHttpStatusException;
import cloudgene.mapred.util.FormUtil.Parameter;
import cloudgene.mapred.util.Page;
import cloudgene.mapred.util.Settings;
import cloudgene.mapred.wdl.WdlApp;
import genepi.io.FileUtil;
import io.micronaut.http.HttpStatus;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@Singleton
public class JobService {

	private static final Logger log = LoggerFactory.getLogger(JobService.class);

	@Inject
	protected Application application;

	@Inject
	protected WorkspaceFactory workspaceFactory;

	public AbstractJob getById(String id) {

		// TODO: better to go via database? only load from engine when running?

		AbstractJob job = application.getWorkflowEngine().getJobById(id);

		if (job == null) {
			// finished job is in database
			JobDao dao = new JobDao(application.getDatabase());
			job = dao.findById(id, true);

		} else {

			if (job instanceof CloudgeneJob) {
				((CloudgeneJob) job).updateProgress();
			}
		}

		if (job == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, "Job " + id + " not found.");
		}

		return job;
	}

	public AbstractJob getByIdAndUser(String id, User user) {

		if (user == null) {
			throw new JsonHttpStatusException(HttpStatus.UNAUTHORIZED, "Access denied.");
		}

		AbstractJob job = getById(id);

		// admin has access to all jobs. Other users only to their own jobs.
		if (!user.isAdmin() && job.getUser().getId() != user.getId()) {
			throw new JsonHttpStatusException(HttpStatus.FORBIDDEN, "Access denied.");
		}

		return job;
	}

	public AbstractJob submitJob(String appId, List<Parameter> form, User user, String userAgent) {
		if (user == null) {
			throw new JsonHttpStatusException(HttpStatus.UNAUTHORIZED, "Access denied.");
		}

		WorkflowEngine engine = this.application.getWorkflowEngine();
		Settings settings = this.application.getSettings();

		int maxPerUser = settings.getMaxRunningJobsPerUser();
		if (!user.isAdmin() && engine.getJobsByUser(user).size() >= maxPerUser) {
			throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST,
					"Only " + maxPerUser + " jobs per user can be executed simultaneously.");
		}

		ApplicationRepository repository = settings.getApplicationRepository();
		cloudgene.mapred.apps.Application application = repository.getByIdAndUser(appId, user);
		if (application == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, "Application '" + appId + "' not found.");
		}
		WdlApp app = application.getWdlApp();
		if (app.getWorkflow() == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND,
					"Application '" + appId + "' has no workflow section.");
		}

		Map<String,String> tmp_params=null;
		try{
		    tmp_params=JobParameterParser.parse0(form);
		    log.debug("=== tmp_params ===");
		    for (String key:tmp_params.keySet()){
			log.debug(key+" : "+tmp_params.get(key));
		    }
		    log.debug("");
		} catch (Exception e) {
		    throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}

		String total_chunks=tmp_params.get("total_chunks");
		String cur_chunk=tmp_params.get("cur_chunk");
		String jobid=tmp_params.get("jobid");
		log.debug("jobid="+jobid);
		log.debug("total_chunks="+total_chunks);
		log.debug("cur_chunk="+cur_chunk);
		String id=jobid;
		boolean need_WS=false;
		if (id.equals("NA")){
		    id = createId();
		    need_WS=true;
		}
		log.debug("id="+id);

		Map<String, String> inputParams = null;
		IWorkspace workspace = workspaceFactory.getDefault();
		try {
			// setup workspace
			workspace.setJob(id);
			if (need_WS){
			    workspace.setup();
			}

			// parse input params
			inputParams = JobParameterParser.parse(form, app, workspace);

		} catch (Exception e) {
			throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}

		String name = id;
		String jobName = inputParams.get("job-name");
		if (jobName != null && !jobName.trim().isEmpty()) {
			name = jobName;
		}

		if (cur_chunk.equals(total_chunks)){
		    // TODO: remove and solve via workspace!
		    String localWorkspace = FileUtil.path(settings.getLocalWorkspace(), id);
		    FileUtil.createDirectory(localWorkspace);
		    mergeFileParts(inputParams.get("files"));
		    log.debug("files: "+inputParams.get("files"));
		    log.debug("localWorkspace: "+localWorkspace);
		    HashMap<String,Integer> nsamples=getDirNsamples(inputParams.get("files"));
		    for (Map.Entry<String,Integer> entry : nsamples.entrySet()) {
			String fname=entry.getKey();
			int n=entry.getValue();
			log.debug("File: "+fname+" : "+n+" samples");
		    }
		    CloudgeneJob job = new CloudgeneJob(user, id, app, inputParams);
		    job.setId(id);
		    job.setName(name);
		    job.setLocalWorkspace(localWorkspace);
		    job.setWorkspace(workspace);
		    job.setSettings(settings);
		    job.setApplication(app.getName() + " " + app.getVersion());
		    job.setApplicationId(appId);
		    job.setUserAgent(userAgent);
		    engine.submit(job);
		    return job;
		}
		else{
		    CloudgeneJob job=new CloudgeneJob();
		    job.setId("temp_"+cur_chunk+"_"+total_chunks+"_"+id);
		    return job;
		}
	}

    private boolean mergeFileList(List <String> files,String output){
	log.info("Merging "+files+" to "+output);
	try{
	    DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(output))));
	    for (String fname: files.stream().sorted().collect(Collectors.toList())){
		log.info("File: "+fname);
		try{
		    byte fileBytes [] = FileUtils.readFileToByteArray(new File(fname));
		    out.write(fileBytes);
		    out.flush();
		}
		catch (IOException ex){
		    log.error(ex.toString());
		    return false;
		}
	    }
	    try{
		out.close();
		log.info("");
	    }
	    catch (IOException ex){
		log.error(ex.toString());
		return false;
	    }
	}
	catch (FileNotFoundException ex){
	    log.error(ex.toString());
	    return false;
	}
	return true;
    }

    private int getFileNsamples(String fname){
	BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(fname))));
	String line;
	int n=0;
	while ((line=in.readLine()) != null){
	    if(line.startsWith("#CHROM")){
		String[] ar=line.split("\t");
		return ar.length-9;
	    }
	}
	return n;
    }
    
    // for all *.vcf.gz files in dir, return map : fileName --> Nsamples
    private HashMap<String,Integer> getDirNsamples(String dir){
	HashMap <String,Integer> H=new HashMap <String,Integer>();
	File D=new File(dir);
	FileFilter fileFilter = new WildcardFileFilter("*.vcf.gz");
	File flist [] = D.listFiles(fileFilter);
	log.info("Found "+flist.length+" files matching *.vcf.gz");
	for(File file : flist) {
	    String fname=file.getName();
	    int n=getFileNsamples(fname);
	    log.info("File name: "+fname+", Nsamples="+n);
	    H.put(fname,new Integer(n));
	}
	log.info("");
	return H;
    }
    
    private boolean mergeFileParts(String dir){
	File D=new File(dir);
	FileFilter fileFilter = new WildcardFileFilter("*.vcf.gz.part*");
	File flist [] = D.listFiles(fileFilter);
	log.info("Found "+flist.length+" files matching *.vcf.gz.part*");
	for(File file : flist) {
	    log.info("File name: "+file.getName());
	}
	log.info("");
	Pattern pattern = Pattern.compile("(.*)\\.vcf\\.gz\\.part\\d+$");
	Map<String, List<String>> M = new HashMap<String, List<String>>();
	for (File f:flist){
	    Matcher matcher = pattern.matcher(f.getName());
	    while (matcher.find()) {
		String p=matcher.group(1);
		if (!M.containsKey(p)) {
		    M.put(p, new ArrayList<String>());
		}
		M.get(p).add(f.getAbsolutePath());
		log.info(p+" : "+f.getName());
	    }
	}
	for (Map.Entry<String, List <String>> entry: M.entrySet()){
	    log.info(entry.getKey()+" -- "+entry.getValue());
	    mergeFileList(entry.getValue(),FileUtil.path(dir,entry.getKey()+".vcf.gz"));	    
	}
	// delete all *part* files
	for (File f: flist){
	    if (f.delete())
		log.info("Deleted "+f.getName());
	    else
		log.error("Deleting "+f.getName()+" failed");
	}
	// report MD5 sums
	// log.info("Saving MD5 checksums of the input files");
	// try{
	//     MessageDigest mdigest = MessageDigest.getInstance("MD5");
	//     fileFilter = new WildcardFileFilter("*.vcf.gz");
	//     flist = D.listFiles(fileFilter);
	//     log.info("Found "+flist.length+" files matching *.vcf.gz");
	//     for(File file : flist) {
	// 	try{
	// 	    log.info("checksum: "+file.getName()+" "+checksum(mdigest,file));
	// 	}catch (IOException ex) {
	// 	    log.error(ex.toString());
	// 	}
	// 	log.info("");
	//     }
	// }catch (NoSuchAlgorithmException ex) {
	//     log.error(ex.toString());
	//     return false;
	// }

	return true;
    }

    
	public Page<AbstractJob> getAllByUserAndPage(User user, Integer page, int pageSize) {

		int offset = 0;
		if (page != null) {

			offset = page;
			if (offset < 1) {
				offset = 1;
			}
			offset = (offset - 1) * pageSize;
		}

		// find all jobs by user
		JobDao dao = new JobDao(application.getDatabase());

		// count all jobs
		int count = dao.countAllByUser(user);

		List<AbstractJob> jobs = null;
		if (page != null) {
			jobs = dao.findAllByUser(user, offset, pageSize);
		} else {
			jobs = dao.findAllByUser(user);
			page = 1;
			pageSize = count;

		}

		// if job is running, use in memory instance
		List<AbstractJob> finalJobs = new Vector<AbstractJob>();
		for (AbstractJob job : jobs) {
			AbstractJob runningJob = application.getWorkflowEngine().getJobById(job.getId());
			if (runningJob != null) {
				finalJobs.add(runningJob);
			} else {
				finalJobs.add(job);
			}

		}

		Page<AbstractJob> result = new Page<AbstractJob>();
		result.setCount(count);
		result.setPage(page);
		result.setPageSize(pageSize);
		result.setData(finalJobs);

		return result;

	}

	public AbstractJob delete(AbstractJob job) {
		Settings settings = application.getSettings();

		// delete local directory
		String localOutput = FileUtil.path(settings.getLocalWorkspace(), job.getId());
		FileUtil.deleteDirectory(localOutput);

		// delete job from database
		job.setState(AbstractJob.STATE_DELETED);

		JobDao dao = new JobDao(application.getDatabase());
		dao.update(job);

		// When a user manually deletes a job, clear sensitive data immediately
		ParameterDao parameterDao = new ParameterDao(application.getDatabase());
		parameterDao.deleteSensitiveByJob(job);

		// delete all results that are stored on external workspaces

		IWorkspace workspace = workspaceFactory.getByJob(job);
		try {
			workspace.delete(job.getId());
		} catch (Exception e) {
			log.error("Deleting " + job.getId() + " form workspace failed.", e);
		}

		return job;
	}

	public AbstractJob cancel(AbstractJob job) {
		application.getWorkflowEngine().cancel(job);
		return job;
	}

	public AbstractJob restart(AbstractJob job) {

		Settings settings = application.getSettings();

		if (job.getState() != AbstractJob.STATE_DEAD) {
			throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST, "Job " + job.getId() + " is not pending.");
		}

		String localWorkspace = FileUtil.path(settings.getLocalWorkspace(), job.getId());

		job.setLocalWorkspace(localWorkspace);
		job.setSettings(settings);

		String appId = job.getApplicationId();

		ApplicationRepository repository = settings.getApplicationRepository();
		cloudgene.mapred.apps.Application application = repository.getByIdAndUser(appId, job.getUser());
		if (application == null) {
			throw new JsonHttpStatusException(HttpStatus.NOT_FOUND, "Application '" + appId + "' not found.");

		}

		IWorkspace workspace = workspaceFactory.getDefault();

		try {
			// setup workspace
			workspace.setJob(job.getId());
			workspace.setup();
		} catch (Exception e) {
			throw new JsonHttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
		job.setWorkspace(workspace);

		((CloudgeneJob) job).loadApp(application.getWdlApp());

		this.application.getWorkflowEngine().restart(job);

		return job;

	}

	public int reset(AbstractJob job, int maxDownloads) {

		DownloadDao downloadDao = new DownloadDao(application.getDatabase());
		int count = 0;
		for (CloudgeneParameterOutput param : job.getOutputParams()) {
			if (param.isDownload()) {
				List<Download> downloads = param.getFiles();

				for (Download download : downloads) {
					download.setCount(maxDownloads);
					downloadDao.update(download);
					count++;
				}

			}
		}

		return count;

	}

	public AbstractJob changePriority(AbstractJob job, long priority) {
		application.getWorkflowEngine().updatePriority(job, priority);
		return job;
	}

	public String archive(AbstractJob job) {
		Settings settings = application.getSettings();

		JobDao dao = new JobDao(application.getDatabase());

		if (job.getState() != AbstractJob.STATE_SUCCESS && job.getState() != AbstractJob.STATE_FAILED
				&& job.getState() != AbstractJob.STATE_CANCELED) {
			return "Job " + job.getId() + " has wrong state for this operation.";
		}

		try {

			// delete local directory and hdfs directory
			String localOutput = FileUtil.path(settings.getLocalWorkspace(), job.getId());
			FileUtil.deleteDirectory(localOutput);

			job.setState(AbstractJob.STATE_RETIRED);
			dao.update(job);

			// When an admin manually deletes a job, clear sensitive data immediately
			ParameterDao parameterDao = new ParameterDao(application.getDatabase());
			parameterDao.deleteSensitiveByJob(job);

			IWorkspace workspace = workspaceFactory.getByJob(job);

			try {
				workspace.delete(job.getId());
			} catch (Exception e) {
				log.error("Deleting " + job.getId() + " from workspace failed.", e);
			}

			return "Retired job " + job.getId();

		} catch (Exception e) {
			return "Retire " + job.getId() + " failed.";
		}

	}

	public String increaseRetireDate(AbstractJob job, int days) {

		JobDao dao = new JobDao(application.getDatabase());
		if (job.getState() == AbstractJob.STATE_SUCESS_AND_NOTIFICATION_SEND
				|| job.getState() == AbstractJob.STATE_FAILED_AND_NOTIFICATION_SEND) {

			try {

				job.setDeletedOn(job.getDeletedOn() + (days * 24 * 60 * 60 * 1000));

				dao.update(job);

				return "Update delete on date for job " + job.getId() + ".";

			} catch (Exception e) {

				return "Update delete date for job " + job.getId() + " failed.";
			}

		} else {
			return "Job " + job.getId() + " has wrong state for this operation.";
		}

	}

	public String createId() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
		return "job-" + sdf.format(new Date());
	}


	public List<AbstractJob> getJobs(String state) {

		List<AbstractJob> jobs = new Vector<AbstractJob>();

		WorkflowEngine engine = application.getWorkflowEngine();
		JobDao dao = new JobDao(application.getDatabase());

		if (state != null) {
			switch (state) {

			case "running-ltq":

				jobs = engine.getAllJobsInLongTimeQueue();
				break;

			case "running-stq":

				// TODO: remove!
				jobs = new Vector<AbstractJob>();
				break;

			case "current":

				jobs = dao.findAllNotRetiredJobs();
				List<AbstractJob> toRemove = new Vector<AbstractJob>();
				for (AbstractJob job : jobs) {
					if (engine.isInQueue(job)) {
						toRemove.add(job);
					}
				}
				jobs.removeAll(toRemove);
				break;

			case "retired":

				jobs = dao.findAllByState(AbstractJob.STATE_RETIRED);
				break;

			}
		}
		return jobs;
	}

	public String getJobLog(AbstractJob job, String name) throws IOException {
		if (job.isRunning()) {
			// files are locally when job is running
			return job.getLog(name);
		} else {
			IWorkspace workspace = workspaceFactory.getByJob(job);
			return workspace.downloadLog(name);
		}
	}

}
