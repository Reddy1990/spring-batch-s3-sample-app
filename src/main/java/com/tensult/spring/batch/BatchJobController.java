package com.tensult.spring.batch;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
/**
 * Rest API for managing the batch jobs
 * @author Dilip <dev@tensult.com> 
 */
@RestController
public class BatchJobController {

	@Autowired
	JobRepository jobRepository;

	@Bean
	public JobLauncher aysncJobLauncher() {
		final SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
		jobLauncher.setJobRepository(jobRepository);
		final SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
		simpleAsyncTaskExecutor.setConcurrencyLimit(1);
		jobLauncher.setTaskExecutor(simpleAsyncTaskExecutor);
		return jobLauncher;
	}

	@Autowired
	Job job;

	@RequestMapping(value = "/start", params = { "s3Folder", "numRecordsInBatch" }, method = RequestMethod.GET)
	public String startJob(@RequestParam("s3Folder") String s3Folder,
			@RequestParam("numRecordsInBatch") String numRecordsInBatch) throws Exception {
		Map<String, JobParameter> jobParams = new LinkedHashMap<String, JobParameter>();
		jobParams.put("s3Folder", new JobParameter(s3Folder));
		jobParams.put("numRecordsInBatch", new JobParameter(numRecordsInBatch));
		aysncJobLauncher().run(job, new JobParameters(jobParams));
		System.out.println("Job started");
		return "started";
	}

	@RequestMapping(value = "/status", params = { "s3Folder", "numRecordsInBatch" }, method = RequestMethod.GET)
	public String getJobStatus(@RequestParam("s3Folder") String s3Folder,
			@RequestParam("numRecordsInBatch") String numRecordsInBatch) throws Exception {
		Map<String, JobParameter> jobParams = new LinkedHashMap<String, JobParameter>();
		jobParams.put("s3Folder", new JobParameter(s3Folder));
		jobParams.put("numRecordsInBatch", new JobParameter(numRecordsInBatch));
		JobExecution jobExecution = jobRepository.getLastJobExecution(job.getName(), new JobParameters(jobParams));
		return jobExecution != null ? jobExecution.getExitStatus().getExitCode() : ExitStatus.UNKNOWN.getExitCode();
	}
}