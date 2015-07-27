package org.molgenis.compute.ui.executor.cluster;

import java.io.IOException;

import org.molgenis.compute.ui.executor.WorkflowExecutor;
import org.molgenis.data.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by hvbyelas on 12/16/14.
 */

@Configuration
public class ClusterExecutorConfig
{
	@Autowired
	private DataService dataService;

	@Bean
	public WorkflowExecutor clusterManager()
	{
		return new ClusterManager();
	}

	@Bean
	public ClusterExecutor clusterExecutor()
	{
		return new ClusterExecutorImpl();
	}

	@Bean
	public ClusterCurlBuilder clusterCurlBuilder() throws IOException
	{
		return new ClusterCurlBuilder();
	}

}
