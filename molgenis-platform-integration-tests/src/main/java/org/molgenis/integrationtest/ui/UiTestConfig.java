package org.molgenis.integrationtest.ui;

import org.molgenis.data.settings.AppSettings;
import org.molgenis.ui.jobs.JobsController;
import org.molgenis.ui.menu.MenuReaderService;
import org.molgenis.ui.menu.MenuReaderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(JobsController.class)
public class UiTestConfig
{
	@Autowired
	private AppSettings appSettings;

	@Bean
	public MenuReaderService menuReaderService()
	{
		return new MenuReaderServiceImpl(appSettings);
	}

}
