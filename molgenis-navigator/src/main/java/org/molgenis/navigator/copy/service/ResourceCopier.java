package org.molgenis.navigator.copy.service;

import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static org.molgenis.data.meta.model.EntityType.AttributeCopyMode.SHALLOW_COPY_ATTRS;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.navigator.copy.service.RelationTransformer.transformExtends;
import static org.molgenis.navigator.copy.service.RelationTransformer.transformMappedBys;
import static org.molgenis.navigator.copy.service.RelationTransformer.transformPackage;
import static org.molgenis.navigator.copy.service.RelationTransformer.transformRefEntities;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import org.molgenis.data.AbstractEntityDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.UnknownPackageException;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaMetaData;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.util.EntityTypeUtils;
import org.molgenis.data.util.PackageUtils;
import org.molgenis.data.util.PackageUtils.PackageTreeTraverser;
import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.jobs.Progress;
import org.molgenis.navigator.copy.exception.RecursiveCopyException;
import org.molgenis.navigator.util.ResourceCollection;
import org.springframework.context.i18n.LocaleContextHolder;

// TODO document
@SuppressWarnings({"squid:S1854", "squid:S1481", "squid:S3958"}) // TODO REMOVE ME
public class ResourceCopier {

  private static final String POSTFIX = " (Copy)";
  private static final int BATCH_SIZE = 1000;

  private final List<Package> packages;
  private final List<EntityType> entityTypes;
  @Nullable private final Package targetPackage;
  private final Progress progress;

  private final DataService dataService;
  private final MetaDataService metaDataService;
  private final IdGenerator idGenerator;
  private final EntityTypeDependencyResolver entityTypeDependencyResolver;
  private final AttributeFactory attributeFactory;

  private final List<EntityType> entityTypesInPackages = newArrayList();
  private final Map<String, Package> copiedPackageMap = newHashMap();
  private final Map<String, EntityType> copiedEntityTypeMap = newHashMap();
  private final Map<String, Attribute> copiedAttributesMap = newHashMap();
  private final Map<String, String> copiedIdsMap = newHashMap();
  private final Map<String, String> referenceDefaultValues = newHashMap();

  ResourceCopier(
      ResourceCollection resourceCollection,
      @Nullable Package targetPackage,
      Progress progress,
      DataService dataService,
      IdGenerator idGenerator,
      EntityTypeDependencyResolver entityTypeDependencyResolver,
      AttributeFactory attributeFactory) {
    requireNonNull(resourceCollection);
    this.packages = resourceCollection.getPackages();
    this.entityTypes = resourceCollection.getEntityTypes();
    this.targetPackage = targetPackage;
    this.progress = requireNonNull(progress);

    this.dataService = requireNonNull(dataService);
    this.idGenerator = requireNonNull(idGenerator);
    this.entityTypeDependencyResolver = requireNonNull(entityTypeDependencyResolver);
    this.attributeFactory = requireNonNull(attributeFactory);

    this.metaDataService = this.dataService.getMeta();
  }

  public void copy() {
    progress.setProgressMax(calculateMaxProgress());
    progress.progress(0, getMessage("progress-copy-started"));

    if (!packages.isEmpty()) {
      progress.status(getMessage("progress-copy-packages"));
      packages.forEach(this::copyPackage);
    }

    if (!entityTypes.isEmpty() || !entityTypesInPackages.isEmpty()) {
      progress.status(getMessage("progress-copy-entity-types"));
      copyEntityTypes();
    }

    progress.status(getMessage("progress-copy-success"));
  }

  @NotNull
  private String getMessage(String key) {
    return MessageSourceHolder.getMessageSource()
        .getMessage(key, new Object[0], LocaleContextHolder.getLocale());
  }

  private void copyEntityTypes() {
    entityTypes.forEach(this::assignUniqueLabel);

    List<EntityType> copiedEntityTypes =
        concat(entityTypes.stream(), entityTypesInPackages.stream())
            .map(this::copyEntityType)
            .collect(toList());

    copiedEntityTypes.forEach(this::transformRelations);

    entityTypeDependencyResolver
        .resolve(copiedEntityTypes)
        .stream()
        .map(this::cutDefaultValues)
        .map(this::persistEntityType)
        .collect(toList())
        .stream()
        .map(this::copyEntities)
        .map(this::pasteDefaultValues)
        .forEach(e -> progress.increment(1));
  }

  private void transformRelations(EntityType entityType) {
    transformPackage(entityType, copiedPackageMap);
    transformExtends(entityType, copiedEntityTypeMap);
    transformRefEntities(entityType, copiedEntityTypeMap);
    transformMappedBys(entityType, copiedAttributesMap);
  }

  private EntityType cutDefaultValues(EntityType copy) {
    stream(copy.getAtomicAttributes())
        .filter(EntityTypeUtils::isReferenceType)
        .forEach(
            attr -> {
              referenceDefaultValues.put(attr.getIdentifier(), attr.getDefaultValue());
              attr.setDefaultValue(null);
            });
    return copy;
  }

  private EntityType pasteDefaultValues(EntityType copy) {
    stream(copy.getAtomicAttributes())
        .filter(EntityTypeUtils::isReferenceType)
        .forEach(
            attr ->
                attr.setDefaultValue(
                    referenceDefaultValues.getOrDefault(attr.getIdentifier(), null)));

    metaDataService.updateEntityType(copy);
    return copy;
  }

  private EntityType copyEntityType(EntityType original) {
    EntityType copy = EntityType.newInstance(original, SHALLOW_COPY_ATTRS, attributeFactory);
    Map<String, Attribute> copiedAttributes =
        EntityType.deepCopyAttributes(original, copy, attributeFactory);

    String newId = idGenerator.generateId();
    copy.setId(newId);

    copiedEntityTypeMap.put(original.getId(), copy);
    copiedIdsMap.put(newId, original.getId());
    copiedAttributesMap.putAll(copiedAttributes);

    return copy;
  }

  private EntityType persistEntityType(EntityType copy) {
    metaDataService.addEntityType(copy);
    return copy;
  }

  private EntityType copyEntities(EntityType copy) {
    String originalEntityTypeId = copiedIdsMap.get(copy.getId());
    if (!copy.isAbstract()) {
      dataService
          .getRepository(originalEntityTypeId)
          .forEachBatched(
              batch -> dataService.add(copy.getId(), batch.stream().map(PretendingEntity::new)),
              BATCH_SIZE);
    }
    return copy;
  }

  private void copyPackage(Package pack) {
    validateNotContainsItself(pack);
    assignUniqueLabel(pack);
    copyPackageRecursive(pack, targetPackage);
  }

  private void copyPackageRecursive(Package pack, Package parent) {
    entityTypesInPackages.addAll(newArrayList(pack.getEntityTypes()));
    assignNewId(pack);
    pack.setParent(parent);
    dataService.add(PACKAGE, pack);
    progress.increment(1);
    pack.getChildren().forEach(child -> copyPackageRecursive(getPackage(child.getId()), pack));
  }

  private void assignNewId(Package pack) {
    String newId = idGenerator.generateId();
    copiedPackageMap.put(pack.getId(), pack);
    pack.setId(newId);
  }

  /**
   * Checks if there's a Package in the target location with the same label. If so, keeps adding a
   * postfix until the label is unique.
   */
  private void assignUniqueLabel(Package pack) {
    Set<String> existingLabels;
    if (targetPackage != null) {
      existingLabels = stream(targetPackage.getChildren()).map(Package::getLabel).collect(toSet());
    } else {
      existingLabels =
          dataService
              .query(PACKAGE, Package.class)
              .eq(PackageMetadata.PARENT, null)
              .findAll()
              .map(Package::getLabel)
              .collect(toSet());
    }
    pack.setLabel(generateUniqueLabel(existingLabels, pack.getLabel()));
  }

  /**
   * Checks if there's an EntityType in the target location with the same label. If so, keeps adding
   * a postfix until the label is unique.
   */
  private void assignUniqueLabel(EntityType entityType) {
    Set<String> existingLabels;
    if (targetPackage != null) {
      existingLabels =
          stream(targetPackage.getEntityTypes()).map(EntityType::getLabel).collect(toSet());
    } else {
      existingLabels =
          dataService
              .query(ENTITY_TYPE_META_DATA, EntityType.class)
              .eq(EntityTypeMetadata.PACKAGE, null)
              .findAll()
              .map(EntityType::getLabel)
              .collect(toSet());
    }
    entityType.setLabel(generateUniqueLabel(existingLabels, entityType.getLabel()));
  }

  private String generateUniqueLabel(Set<String> existingLabels, String label) {
    StringBuilder newLabel = new StringBuilder(label);
    while (existingLabels.contains(newLabel.toString())) {
      newLabel.append(POSTFIX);
    }
    return newLabel.toString();
  }

  private void validateNotContainsItself(Package pack) {
    if (PackageUtils.contains(pack, targetPackage)) {
      throw new RecursiveCopyException();
    }
  }

  private Package getPackage(String id) {
    return metaDataService.getPackage(id).orElseThrow(() -> new UnknownPackageException(id));
  }

  private int calculateMaxProgress() {
    AtomicInteger maxProgress = new AtomicInteger();
    maxProgress.addAndGet(entityTypes.size());
    maxProgress.addAndGet(packages.size());

    packages.forEach(
        packToCopy ->
            new PackageTreeTraverser()
                .postOrderTraversal(packToCopy)
                .forEach(
                    pack -> {
                      maxProgress.addAndGet(size(pack.getChildren()));
                      maxProgress.addAndGet(size(pack.getEntityTypes()));
                    }));

    return maxProgress.get();
  }

  /**
   * When copying rows from one repository to another, the metadata of these entities will not fit
   * the copied repository and its references because the metadatas will have different IDs. The
   * PretendingEntity acts like it's the newly copied entity by returning the metadata of the copied
   * repository instead of the original.
   */
  private class PretendingEntity extends AbstractEntityDecorator {

    PretendingEntity(Entity entity) {
      super(entity);
    }

    @Override
    public EntityType getEntityType() {
      if (delegate().getEntityType() != null) {
        String id = delegate().getEntityType().getId();
        if (copiedEntityTypeMap.containsKey(id)) {
          return copiedEntityTypeMap.get(id);
        } else {
          return delegate().getEntityType();
        }
      } else {
        return null;
      }
    }

    @Override
    public Entity getEntity(String attributeName) {
      Entity entity = delegate().getEntity(attributeName);
      return entity != null ? new PretendingEntity(entity) : null;
    }

    /**
     * Because the File datatype has a reference to {@link FileMetaMetaData} it can happen that a
     * typed FileMeta Entity is requested.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <E extends Entity> E getEntity(String attributeName, Class<E> clazz) {
      Entity entity = delegate().getEntity(attributeName);
      if (clazz.equals(FileMeta.class)) {
        return entity != null ? (E) new FileMeta(new PretendingEntity(entity)) : null;
      } else {
        throw new UnsupportedOperationException("Can't return typed pretending entities");
      }
    }

    @Override
    public Iterable<Entity> getEntities(String attributeName) {
      return stream(delegate().getEntities(attributeName))
          .map(PretendingEntity::new)
          .collect(toList());
    }

    /**
     * Because the File datatype has a reference to {@link FileMetaMetaData} it can happen that a
     * typed FileMeta Entity is requested.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz) {
      Iterable<E> entities = delegate().getEntities(attributeName, clazz);
      if (clazz.equals(FileMeta.class)) {
        return stream(entities)
            .filter(Objects::nonNull)
            .map(PretendingEntity::new)
            .map(e -> (E) new FileMeta(e))
            .collect(toList());
      } else {
        throw new UnsupportedOperationException("Can't return typed pretending entities");
      }
    }
  }
}
