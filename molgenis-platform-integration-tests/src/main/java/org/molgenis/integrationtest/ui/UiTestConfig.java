package org.molgenis.integrationtest.ui;

import org.molgenis.data.settings.AppSettings;
import org.molgenis.ui.menu.MenuReaderService;
import org.molgenis.ui.menu.MenuReaderServiceImpl;
import org.molgenis.ui.menumanager.MenuManagerServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ MenuManagerServiceImpl.class })
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
