package org.molgenis.data.elasticsearch.admin;

public interface SearchService
{
	AggregationResponse aggregate(AggregateRequest aggregateRequest);

	SearchResponse search(SearchRequest searchRequest);
}
