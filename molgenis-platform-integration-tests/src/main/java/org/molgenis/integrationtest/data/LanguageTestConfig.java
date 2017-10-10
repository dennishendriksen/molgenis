package org.molgenis.integrationtest.data;

import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.i18n.LocalizationService;
import org.molgenis.data.i18n.model.L10nStringFactory;
import org.molgenis.data.i18n.model.L10nStringMetaData;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ LanguageService.class, LocalizationService.class, L10nStringFactory.class, L10nStringMetaData.class })
public class LanguageTestConfig
{
}
