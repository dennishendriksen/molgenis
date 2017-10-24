package org.molgenis.integrationtest.jobs;

import org.molgenis.data.jobs.JobExecutionConfig;
import org.molgenis.data.jobs.model.JobPackage;
import org.molgenis.scheduler.SchedulerConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ SchedulerConfig.class, JobExecutionConfig.class, JobPackage.class })
public class JobsTestConfig
{

}
