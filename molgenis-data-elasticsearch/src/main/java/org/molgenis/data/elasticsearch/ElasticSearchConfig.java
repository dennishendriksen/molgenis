package org.molgenis.data.elasticsearch;

import org.molgenis.data.elasticsearch.client.ElasticSearchClientConfig;
import org.molgenis.data.elasticsearch.generator.ElasticSearchGeneratorConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ ElasticSearchClientConfig.class, ElasticSearchGeneratorConfig.class, ElasticsearchService.class })
public class ElasticSearchConfig
{
}
