package org.molgenis.compute.ui.executor;

import org.molgenis.compute.ui.model.Analysis;
import org.springframework.scheduling.annotation.Async;

public interface WorkflowExecutor
{
	@Async
	void executeAnalysis(Analysis analysis);

	@Async
	void cancelRunJobs(Analysis analysis);

	@Async
	void reRunJobs(Analysis analysis);

}