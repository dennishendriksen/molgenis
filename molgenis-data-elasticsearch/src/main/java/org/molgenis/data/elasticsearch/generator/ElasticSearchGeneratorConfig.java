package org.molgenis.data.elasticsearch.generator;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ IndexGenerator.class, DocumentIdGenerator.class, ContentGenerators.class, MappingGenerator.class,
		QueryContentGenerators.class, QueryGenerator.class, SortGenerator.class, AggregationGenerator.class,
		DocumentContentBuilder.class })
public class ElasticSearchGeneratorConfig
{
}
