package org.molgenis.data.elasticsearch.admin;

import org.molgenis.data.DataService;
import org.molgenis.data.Fetch;
import org.molgenis.data.elasticsearch.util.DocumentIdGenerator;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;

@Component
public class DocumentIdMapper
{
	private final DocumentIdGenerator documentIdGenerator;
	private final DataService dataService;

	@Autowired
	public DocumentIdMapper(DocumentIdGenerator documentIdGenerator, DataService dataService)
	{
		this.documentIdGenerator = documentIdGenerator;
		this.dataService = requireNonNull(dataService);
	}

	public EntityType getEntityType(String documentType)
	{
		return dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)
				.fetch(new Fetch().field(EntityTypeMetadata.FULL_NAME).field(EntityTypeMetadata.SIMPLE_NAME)
						.field(EntityTypeMetadata.LABEL)).findAll()
				.filter(anEntityType -> documentIdGenerator.generateId(anEntityType).equals(documentType)).findFirst()
				.orElse(null);
	}
}
