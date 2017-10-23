package org.molgenis.integrationtest.utils.config;

import com.google.gson.Gson;
import org.molgenis.integrationtest.ui.UiTestConfig;
import org.molgenis.ui.MolgenisWebAppConfig;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ GsonHttpMessageConverter.class, Gson.class, UiTestConfig.class })
public class WebAppITConfig extends MolgenisWebAppConfig
{
}
