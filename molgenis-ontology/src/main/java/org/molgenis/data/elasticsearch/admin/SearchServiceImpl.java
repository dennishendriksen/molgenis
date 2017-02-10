package org.molgenis.data.elasticsearch.admin;

import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.ElasticsearchService;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.elasticsearch.factory.EmbeddedElasticSearchServiceFactory.DEFAULT_INDEX_NAME;

@Component
public class SearchServiceImpl implements SearchService
{
	private final Client client;
	private final DocumentIdMapper documentIdMapper;
	private final DataService dataService;

	@Autowired
	public SearchServiceImpl(ElasticsearchService elasticsearchService, DocumentIdMapper documentIdMapper,
			DataService dataService)
	{
		this.client = requireNonNull(elasticsearchService).getClient();
		this.documentIdMapper = requireNonNull(documentIdMapper);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public SearchResponse search(SearchRequest searchRequest)
	{
		org.elasticsearch.action.search.SearchResponse searchResponse = client.prepareSearch(DEFAULT_INDEX_NAME)
				.setQuery(QueryBuilders.queryStringQuery(searchRequest.query()).lenient(true)).setExplain(true).get();
		SearchHits hits = searchResponse.getHits();
		List<SearchHit> searchHitsList = stream(hits.hits()).map(this::toSearchHit).collect(toList());
		return SearchResponse.create(searchHitsList, hits.getTotalHits());
	}

	private SearchHit toSearchHit(org.elasticsearch.search.SearchHit searchHit)
	{
		EntityType entityType = documentIdMapper.getEntityType(searchHit.getType());
		String entityTypeId = entityType.getFullyQualifiedName();
		String entityTypeLabel = entityType.getLabel(); // TODO i18n

		return SearchHit.create(entityTypeId, entityTypeLabel, searchHit.getId());
	}
}
