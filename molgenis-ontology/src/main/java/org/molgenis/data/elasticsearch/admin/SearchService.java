package org.molgenis.data.elasticsearch.admin;

public interface SearchService
{
	SearchResponse search(SearchRequest searchRequest);
}
