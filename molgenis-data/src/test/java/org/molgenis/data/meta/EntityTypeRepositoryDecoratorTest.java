package org.molgenis.data.meta;

import com.google.common.collect.Lists;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.testng.Assert.assertEquals;

public class EntityTypeRepositoryDecoratorTest extends AbstractMockitoTest
{
	@Mock
	private Repository<EntityType> delegateRepository;
	@Mock
	private DataService dataService;
	@Mock
	private EntityTypeDependencyResolver entityTypeDependencyResolver;

	private EntityTypeRepositoryDecorator entityTypeRepositoryDecorator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		entityTypeRepositoryDecorator = new EntityTypeRepositoryDecorator(delegateRepository, dataService,
				entityTypeDependencyResolver);
	}

	private MetaDataService createMockMetaDataService()
	{
		MetaDataService metaDataService = mock(MetaDataService.class);
		when(dataService.getMeta()).thenReturn(metaDataService);
		return metaDataService;
	}

	@Test
	public void addWithKnownBackend()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("EntityTypeId").getMock();
		RepositoryCollection repositoryCollection = mock(RepositoryCollection.class);
		MetaDataService metaDataService = createMockMetaDataService();
		when(metaDataService.getBackend(entityType)).thenReturn(repositoryCollection);

		entityTypeRepositoryDecorator.add(entityType);
		verify(delegateRepository).add(entityType);
		verify(repositoryCollection).createRepository(entityType);
		verifyNoMoreInteractions(delegateRepository, dataService, entityTypeDependencyResolver);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Unknown backend \\[backend\\]")
	public void addWithUnknownBackend()
	{
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn("EntityTypeId").getMock();
		when(entityType.getBackend()).thenReturn("backend");
		createMockMetaDataService();

		entityTypeRepositoryDecorator.add(entityType);
	}

	@Test
	public void delete()
	{
		EntityType entityType = mock(EntityType.class);
		RepositoryCollection repositoryCollection = mock(RepositoryCollection.class);
		MetaDataService metaDataService = createMockMetaDataService();
		when(metaDataService.getBackend(entityType)).thenReturn(repositoryCollection);

		Attribute attr0 = mock(Attribute.class);
		when(attr0.getChildren()).thenReturn(emptyList());
		Attribute attrCompound = mock(Attribute.class);
		Attribute attr1a = mock(Attribute.class);
		when(attr1a.getChildren()).thenReturn(emptyList());
		Attribute attr1b = mock(Attribute.class);
		when(attr1b.getChildren()).thenReturn(emptyList());
		when(attrCompound.getChildren()).thenReturn(newArrayList(attr1a, attr1b));
		when(entityType.getOwnAttributes()).thenReturn(newArrayList(attr0, attrCompound));

		entityTypeRepositoryDecorator.delete(entityType);

		verify(delegateRepository).delete(entityType);
		verify(repositoryCollection).deleteRepository(entityType);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Attribute>> attrCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(dataService).delete(eq(ATTRIBUTE_META_DATA), attrCaptor.capture());
		assertEquals(attrCaptor.getValue().collect(toList()), newArrayList(attr0, attrCompound, attr1a, attr1b));
	}

	@Test
	public void deleteAbstract()
	{
		EntityType entityType = mock(EntityType.class);

		when(entityType.isAbstract()).thenReturn(true);
		Attribute attr0 = mock(Attribute.class);
		when(attr0.getChildren()).thenReturn(emptyList());
		when(entityType.getOwnAttributes()).thenReturn(singletonList(attr0));

		entityTypeRepositoryDecorator.delete(entityType);

		verify(delegateRepository).delete(entityType);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Attribute>> attrCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(dataService).delete(eq(ATTRIBUTE_META_DATA), attrCaptor.capture());
		assertEquals(attrCaptor.getValue().collect(toList()), singletonList(attr0));
	}

	@Test
	public void addRemoveAttributeAbstractEntityType()
	{
		String entityTypeId1 = "EntityType1";
		String entityTypeId2 = "EntityType2";
		String entityTypeId3 = "EntityType3";
		EntityType entityType1 = when(mock(EntityType.class).getId()).thenReturn(entityTypeId1).getMock();
		EntityType entityType2 = when(mock(EntityType.class).getId()).thenReturn(entityTypeId2).getMock();
		EntityType entityType3 = when(mock(EntityType.class).getId()).thenReturn(entityTypeId3).getMock();
		EntityType currentEntityType1 = mock(EntityType.class);
		EntityType currentEntityType2 = mock(EntityType.class);
		EntityType currentEntityType3 = mock(EntityType.class);
		doReturn(currentEntityType1).when(delegateRepository).findOneById(entityTypeId1);
		doReturn(currentEntityType2).when(delegateRepository).findOneById(entityTypeId2);
		doReturn(currentEntityType3).when(delegateRepository).findOneById(entityTypeId3);

		Attribute attributeStays = mock(Attribute.class);
		when(attributeStays.getName()).thenReturn("attributeStays");
		Attribute attributeRemoved = mock(Attribute.class);
		when(attributeRemoved.getName()).thenReturn("attributeRemoved");
		Attribute attributeAdded = mock(Attribute.class);
		when(attributeAdded.getName()).thenReturn("attributeAdded");

		MetaDataService metaDataService = createMockMetaDataService();

		when(currentEntityType1.isAbstract()).thenReturn(true);
		when(currentEntityType1.getOwnAllAttributes()).thenReturn(Lists.newArrayList(attributeStays, attributeRemoved));
		when(entityType1.getOwnAllAttributes()).thenReturn(Lists.newArrayList(attributeStays, attributeAdded));
		when(metaDataService.getConcreteChildren(entityType1)).thenReturn(Stream.of(entityType2, entityType3));
		RepositoryCollection backend2 = mock(RepositoryCollection.class);
		RepositoryCollection backend3 = mock(RepositoryCollection.class);
		doReturn(backend2).when(metaDataService).getBackend(entityType2);
		doReturn(backend3).when(metaDataService).getBackend(entityType3);

		entityTypeRepositoryDecorator.update(entityType1);

		// verify that attributes got added and deleted in concrete extending entities
		verify(backend2).addAttribute(currentEntityType2, attributeAdded);
		verify(backend2).deleteAttribute(currentEntityType2, attributeRemoved);
		verify(backend3).addAttribute(currentEntityType3, attributeAdded);
		verify(backend3).deleteAttribute(currentEntityType3, attributeRemoved);
		verify(backend2, never()).updateRepository(any(), any());
		verify(backend3, never()).updateRepository(any(), any());
	}

	@Test
	public void updateConcreteEntityType()
	{
		String entityTypeId1 = "EntityType1";
		EntityType entityType1 = mock(EntityType.class);

		RepositoryCollection repositoryCollection = mock(RepositoryCollection.class);
		MetaDataService metaDataService = createMockMetaDataService();
		when(metaDataService.getBackend(entityType1)).thenReturn(repositoryCollection);

		when(entityType1.isAbstract()).thenReturn(false);
		when(delegateRepository.findOneById(entityTypeId1)).thenReturn(entityType1);
		when(entityType1.getOwnAllAttributes()).thenReturn(emptyList());

		EntityType updatedEntityType1 = mock(EntityType.class);
		when(updatedEntityType1.getId()).thenReturn(entityTypeId1);
		when(updatedEntityType1.getOwnAllAttributes()).thenReturn(emptyList());
		when(metaDataService.getConcreteChildren(updatedEntityType1)).thenReturn(Stream.empty());

		entityTypeRepositoryDecorator.update(updatedEntityType1);

		verify(repositoryCollection).updateRepository(entityType1, updatedEntityType1);
	}

	@Test
	public void deleteEntityTypesWithOneToMany()
	{
		String entityTypeId1 = "EntityType1";
		String entityTypeId2 = "EntityType2";
		EntityType entityType1 = when(mock(EntityType.class).getId()).thenReturn(entityTypeId1).getMock();
		EntityType entityType2 = when(mock(EntityType.class).getId()).thenReturn(entityTypeId2).getMock();

		MetaDataService metaDataService = createMockMetaDataService();
		RepositoryCollection repositoryCollection = mock(RepositoryCollection.class);
		doReturn(repositoryCollection).when(metaDataService).getBackend(entityType1);
		doReturn(repositoryCollection).when(metaDataService).getBackend(entityType2);

		Attribute mappedByAttribute = mock(Attribute.class);
		when(mappedByAttribute.getEntity()).thenReturn(entityType2);
		when(entityType1.getMappedByAttributes()).thenReturn(Stream.of(mappedByAttribute));
		when(entityType2.getMappedByAttributes()).thenReturn(Stream.empty());
		when(entityTypeDependencyResolver.resolve(any())).thenReturn(asList(entityType1, entityType2));
		InOrder repositoryCollectionInOrder = inOrder(repositoryCollection);
		InOrder decoratedRepoInOrder = inOrder(delegateRepository);

		entityTypeRepositoryDecorator.delete(Stream.of(entityType1, entityType2));

		verify(dataService).delete(ATTRIBUTE_META_DATA, mappedByAttribute);
		repositoryCollectionInOrder.verify(repositoryCollection).deleteRepository(entityType2);
		repositoryCollectionInOrder.verify(repositoryCollection).deleteRepository(entityType1);
		decoratedRepoInOrder.verify(delegateRepository).delete(entityType2);
		decoratedRepoInOrder.verify(delegateRepository).delete(entityType1);
	}
}