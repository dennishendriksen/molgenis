package org.molgenis.compute.ui.executor.cluster;

import org.molgenis.compute.ui.executor.WorkflowExecutor;
import org.molgenis.compute.ui.model.Analysis;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by hvbyelas on 10/15/14.
 */
public class ClusterManager implements WorkflowExecutor
{

	@Autowired
	private ClusterExecutor clusterExecutor;

	/* (non-Javadoc)
	 * @see org.molgenis.compute.ui.clusterexecutor.WorkflowExecutor#executeAnalysis(org.molgenis.compute.ui.model.Analysis)
	 */
	@Override
	public void executeAnalysis(Analysis analysis)
	{
		(new Thread(new ClusterThread(clusterExecutor, analysis, ClusterThread.SUBMIT))).start();
	}

	/* (non-Javadoc)
	 * @see org.molgenis.compute.ui.clusterexecutor.WorkflowExecutor#cancelRunJobs(org.molgenis.compute.ui.model.Analysis)
	 */
	@Override
	public void cancelRunJobs(Analysis analysis)
	{
			(new Thread(new ClusterThread(clusterExecutor, analysis, ClusterThread.CANCEL))).start();
	}

	/* (non-Javadoc)
	 * @see org.molgenis.compute.ui.clusterexecutor.WorkflowExecutor#reRunJobs(org.molgenis.compute.ui.model.Analysis)
	 */
	@Override
	public void reRunJobs(Analysis analysis)
	{
		(new Thread(new ClusterThread(clusterExecutor, analysis, ClusterThread.RERUN))).start();
	}

}
