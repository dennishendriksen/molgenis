package org.molgenis.data.elasticsearch.admin;

import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.ElasticsearchService;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
	public AggregationResponse aggregate(AggregateRequest aggregateRequest)
	{
		// get three top hits
		QueryStringQueryBuilder query = QueryBuilders.queryStringQuery(aggregateRequest.query());
		AbstractAggregationBuilder aggregation = AggregationBuilders.terms("types").field("_type");
		org.elasticsearch.action.search.SearchResponse searchResponse = client.prepareSearch(DEFAULT_INDEX_NAME)
				.setSize(0).setQuery(query).addAggregation(aggregation).get();

		Aggregation typesAggregation = searchResponse.getAggregations().get("types");
		List<Terms.Bucket> buckets = ((Terms) typesAggregation).getBuckets();
		List<AggregationBucket> aggregationBuckets = new ArrayList<>();
		for (Terms.Bucket bucket : buckets)
		{
			EntityType entityType = documentIdMapper.getEntityType(bucket.getKey());
			long documentCount = bucket.getDocCount();
			aggregationBuckets.add(AggregationBucket
					.create(entityType.getFullyQualifiedName(), entityType.getLabel(), documentCount));
		}
		return AggregationResponse.create(aggregationBuckets);
	}

	@Override
	public SearchResponse search(SearchRequest searchRequest)
	{
		String documentType = toDocumentType(searchRequest.entityTypeId());
		org.elasticsearch.action.search.SearchResponse searchResponse = client.prepareSearch(DEFAULT_INDEX_NAME)
				.setTypes(documentType).setQuery(QueryBuilders.queryStringQuery(searchRequest.query())).get();
		SearchHits hits = searchResponse.getHits();
		List<SearchHit> searchHitsList = stream(hits.hits()).map(this::toSearchHit).collect(toList());
		return SearchResponse.create(searchHitsList, hits.getTotalHits());
	}

	private String toDocumentType(String s)
	{
		return null; // FIXME
	}

	private SearchHit toSearchHit(org.elasticsearch.search.SearchHit searchHit)
	{
		EntityType entityType = documentIdMapper.getEntityType(searchHit.getType());
		String entityTypeId = entityType.getFullyQualifiedName();
		String entityTypeLabel = entityType.getLabel(); // TODO i18n

		return SearchHit.create(entityTypeId, entityTypeLabel, searchHit.getId());
	}
}
