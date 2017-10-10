package org.molgenis.integrationtest.data;

import org.molgenis.data.elasticsearch.ElasticSearchConfig;
import org.molgenis.data.platform.config.PlatformConfig;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.integrationtest.data.aggregation.AggregationTestConfig;
import org.molgenis.integrationtest.data.cache.CacheTestConfig;
import org.molgenis.integrationtest.data.meta.MetaTestConfig;
import org.molgenis.integrationtest.data.transaction.TransactionTestConfig;
import org.molgenis.integrationtest.data.validation.ValidationTestConfig;
import org.molgenis.integrationtest.jobs.JobsTestConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ LanguageTestConfig.class, RepositoryTestConfig.class, JobsTestConfig.class, MetaTestConfig.class,
		TransactionTestConfig.class, ValidationTestConfig.class, CacheTestConfig.class, AggregationTestConfig.class,
		ElasticSearchConfig.class, PlatformConfig.class })
public class DataTestConfig
{
	@Bean
	public AppSettings appSettings()
	{
		return new TestAppSettings();
	}

}
