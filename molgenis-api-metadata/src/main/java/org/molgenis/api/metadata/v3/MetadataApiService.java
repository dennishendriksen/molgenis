package org.molgenis.api.metadata.v3;

import java.util.List;
import org.molgenis.api.model.Query;
import org.molgenis.api.model.Sort;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

public interface MetadataApiService {

  void createEntityType(EntityType entityType);

  EntityType findEntityType(String entityTypeId);

  EntityTypes findEntityTypes(Query orElse, Sort sort, int size, int page);

  Attributes findAttributes(String entityTypeId, Query orElse, Sort sort, int size, int page);

  /**
   * @param attributeId attribute identifier
   * @param entityTypeId
   * @throws UnknownAttributeException if no attribute exists for the given identifier
   * @throws UnknownEntityTypeException if no entityType exists for the given identifier
   */
  Attribute findAttribute(String attributeId, String entityTypeId);

  Void deleteAttribute(String attributeId, String entityTypeId);

  Void deleteAttributes(List<String> attributeIds, String entityTypeId);

  /**
   * @param entityTypeId entity type identifier
   * @throws UnknownEntityTypeException if no entity type exists for the given identifier
   */
  Void deleteEntityType(String entityTypeId);

  /**
   * @param entityTypeIds entity type identifiers
   * @throws UnknownEntityTypeException if no entity type exists for a given identifier
   */
  Void deleteEntityTypes(List<String> entityTypeIds);

  /**
   * Updates entity type.
   *
   * @param entityType updated entity type
   */
  Void updateEntityType(EntityType entityType);
}
