package org.molgenis.data.meta;

import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;

public class AttributeRepositoryDecoratorTest extends AbstractMockitoTest
{
	@Mock
	private Repository<Attribute> delegateRepository;
	@Mock
	private DataService dataService;

	private AttributeRepositoryDecorator attributeRepositoryDecorator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		attributeRepositoryDecorator = new AttributeRepositoryDecorator(delegateRepository, dataService);
	}

	@Test
	public void delete()
	{
		Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
		attributeRepositoryDecorator.delete(attribute);
		verify(delegateRepository).delete(attribute);
		verifyNoMoreInteractions(delegateRepository, dataService);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void deleteCompoundAttribute()
	{
		Attribute compoundAttribute = when(mock(Attribute.class).getDataType()).thenReturn(COMPOUND).getMock();
		Attribute compoundAttributePart = mock(Attribute.class);
		when(compoundAttribute.getChildren()).thenReturn(singletonList(compoundAttributePart));
		when(compoundAttributePart.getParent()).thenReturn(compoundAttribute);

		Repository attributeRepository = mock(Repository.class);
		MetaDataService metaDataService = mock(MetaDataService.class);
		when(dataService.getMeta()).thenReturn(metaDataService);
		when(metaDataService.getRepository(ATTRIBUTE_META_DATA)).thenReturn(attributeRepository);

		attributeRepositoryDecorator.delete(compoundAttribute);

		verify(compoundAttributePart).setParent(null);
		verify(delegateRepository).delete(compoundAttribute);
	}

	@Test
	public void deleteStream()
	{
		Attribute attribute0 = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
		Attribute attribute1 = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
		attributeRepositoryDecorator.delete(Stream.of(attribute0, attribute1));
		verify(delegateRepository).delete(attribute0);
		verify(delegateRepository).delete(attribute1);
		verifyNoMoreInteractions(delegateRepository, dataService);
	}

	@Test
	public void updateNonSystemAbstractEntity()
	{
		String attributeId = "SDFSADFSDAF";
		Attribute attribute = mock(Attribute.class);
		EntityType abstractEntityType = mock(EntityType.class);
		EntityType concreteEntityType1 = mock(EntityType.class);
		EntityType concreteEntityType2 = mock(EntityType.class);
		RepositoryCollection backend1 = mock(RepositoryCollection.class);
		RepositoryCollection backend2 = mock(RepositoryCollection.class);
		MetaDataService metadataService = mock(MetaDataService.class);

		when(dataService.getMeta()).thenReturn(metadataService);
		when(metadataService.getConcreteChildren(abstractEntityType)).thenReturn(
				Stream.of(concreteEntityType1, concreteEntityType2));
		doReturn(backend1).when(metadataService).getBackend(concreteEntityType1);
		doReturn(backend2).when(metadataService).getBackend(concreteEntityType2);
		when(attribute.getIdentifier()).thenReturn(attributeId);

		Attribute currentAttribute = mock(Attribute.class);
		when(delegateRepository.findOneById(attributeId)).thenReturn(currentAttribute);
		when(currentAttribute.getEntity()).thenReturn(abstractEntityType);

		attributeRepositoryDecorator.update(attribute);

		verify(delegateRepository).update(attribute);
		verify(backend1).updateAttribute(concreteEntityType1, currentAttribute, attribute);
		verify(backend2).updateAttribute(concreteEntityType2, currentAttribute, attribute);
	}
}