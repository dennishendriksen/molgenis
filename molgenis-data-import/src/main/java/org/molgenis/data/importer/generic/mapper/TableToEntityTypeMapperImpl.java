package org.molgenis.data.importer.generic.mapper;

import com.google.common.collect.ImmutableList;
import org.molgenis.data.importer.table.Table;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Component
public class TableToEntityTypeMapperImpl implements TableToEntityTypeMapper
{
	private final EntityTypeFactory entityTypeFactory;
	private final TableHeaderToAttributeMapper tableHeaderToAttrMapper;

	@Autowired
	public TableToEntityTypeMapperImpl(EntityTypeFactory entityTypeFactory,
			TableHeaderToAttributeMapper tableHeaderToAttrMapper)
	{
		this.entityTypeFactory = requireNonNull(entityTypeFactory);
		this.tableHeaderToAttrMapper = requireNonNull(tableHeaderToAttrMapper);
	}

	@Override
	public MappedEntityType create(Table table)
	{
		String entityName = "entity" + System.nanoTime();
		ImmutableList<MappedAttribute> mappedAttributes = create(table.getColumnHeaderStream());

		EntityType entityType = entityTypeFactory.create();
		entityType.setLabel(table.getName());
		entityType.setSimpleName(entityName);
		entityType.addAttributes(mappedAttributes.stream().map(MappedAttribute::getAttribute).collect(toList()));

		return MappedEntityType.create(entityType, mappedAttributes);
	}

	private ImmutableList<MappedAttribute> create(List<String> headers)
	{
		ImmutableList.Builder<MappedAttribute> mappedAttributeBuilder = new ImmutableList.Builder<>();
		int nrHeaders = headers.size();
		for (int i = 0; i < nrHeaders; i++)
		{
			MappedAttribute mappedAttribute = tableHeaderToAttrMapper.create(i, headers.get(i));
			mappedAttributeBuilder.add(mappedAttribute);
		}
		return mappedAttributeBuilder.build();
	}
}
