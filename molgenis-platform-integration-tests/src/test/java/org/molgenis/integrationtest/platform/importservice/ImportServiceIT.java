package org.molgenis.integrationtest.platform.importservice;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.security.auth.UserMetadata.USER;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.csv.CsvDataConfig;
import org.molgenis.data.file.FileRepositoryCollectionFactory;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.importer.ImportServiceRegistrar;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserFactory;
import org.molgenis.data.vcf.VcfDataConfig;
import org.molgenis.data.vcf.importer.VcfImporterService;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.molgenis.ontology.core.OntologyDataConfig;
import org.molgenis.ontology.core.config.OntologyTestConfig;
import org.molgenis.ontology.core.importer.OntologyImportService;
import org.molgenis.security.core.runas.RunAsSystemAspect;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.molgenis.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@ContextConfiguration(classes = {PlatformITConfig.class, ImportServiceIT.Config.class})
@Transactional
public abstract class ImportServiceIT extends AbstractMockitoSpringContextTests {
  private static final Logger LOG = LoggerFactory.getLogger(ImportServiceIT.class);

  static final String ROLE_SU = "SU";

  @Autowired ApplicationContext applicationContext;

  @Autowired UserFactory userFactory;

  @Autowired ImportServiceFactory importServiceFactory;

  @Autowired FileRepositoryCollectionFactory fileRepositoryCollectionFactory;

  @Autowired ImportServiceRegistrar importServiceRegistrar;

  @Autowired DataService dataService;

  @BeforeEach
  public void beforeEach() {
    ContextRefreshedEvent contextRefreshedEvent = Mockito.mock(ContextRefreshedEvent.class);
    Mockito.when(contextRefreshedEvent.getApplicationContext()).thenReturn(applicationContext);
    importServiceRegistrar.register(contextRefreshedEvent);
    RunAsSystemAspect.runAsSystem(() -> dataService.add(USER, getTestUser()));
  }

  abstract User getTestUser();

  static void validateImportReport(
      EntityImportReport importReport,
      Map<String, Integer> entityTypeCountMap,
      Set<String> addedEntityTypeIds) {
    assertEquals(addedEntityTypeIds, copyOf(importReport.getNewEntities()));
    assertEquals(entityTypeCountMap, importReport.getNrImportedEntitiesMap());
  }

  static File getFile(String resourceName) {
    requireNonNull(resourceName);

    try {
      File file = ResourceUtils.getFile(ImportServiceIT.class, resourceName);
      LOG.trace("emx import integration test file: [{}]", file);
      return file;
    } catch (Exception e) {
      LOG.error("File name: [{}]", resourceName);
      throw new MolgenisDataAccessException(e);
    }
  }

  void verifyFirstAndLastRows(
      String entityName,
      Map<String, Object> expectedFirstRow,
      Map<String, Object> expectedLastRow) {
    EntityType entityType = dataService.getEntityType(entityName);
    String idAttributeName = entityType.getIdAttribute().getName();

    Map<Object, Entity> importedEntities =
        findAllAsList(entityName).stream().collect(toMap(Entity::getIdValue, Function.identity()));
    assertEquals(
        expectedFirstRow, entityToMap(importedEntities.get(expectedFirstRow.get(idAttributeName))));
    assertEquals(
        expectedLastRow, entityToMap(importedEntities.get(expectedLastRow.get(idAttributeName))));
  }

  List<Entity> findAllAsList(String entityName) {
    return dataService.findAll(entityName).collect(Collectors.toList());
  }

  static Map<String, Object> entityToMap(Entity entity) {
    Map<String, Object> entityMap = newHashMap();
    Iterable<Attribute> attributes = entity.getEntityType().getAllAttributes();

    for (Attribute attribute : attributes) {
      if (attribute.getDataType().equals(COMPOUND)) {
        continue;
      }

      String attributeName = attribute.getName();
      Object value = null;
      switch (attribute.getDataType()) {
        case CATEGORICAL:
        case FILE:
        case XREF:
          if (entity.getEntity(attributeName) != null) {
            value = entity.getEntity(attributeName).getIdValue();
          }
          break;
        case CATEGORICAL_MREF:
        case MREF:
        case ONE_TO_MANY:
          value = getIdsAsSet(entity.getEntities(attributeName));
          break;
        default:
          value = entity.get(attributeName);
          break;
      }
      entityMap.put(attributeName, value);
    }

    return entityMap;
  }

  static Set<Object> getIdsAsSet(Iterable<? extends Entity> entities) {
    return stream(entities).map(Entity::getIdValue).collect(toSet());
  }

  @Import(
      value = {
        VcfDataConfig.class,
        VcfImporterService.class,
        VcfAttributes.class,
        OntologyDataConfig.class,
        OntologyTestConfig.class,
        OntologyImportService.class,
        CsvDataConfig.class
      })
  static class Config {}
}
