package org.molgenis.semanticsearch.service;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.semanticsearch.explain.bean.AttributeSearchHits;
import org.molgenis.semanticsearch.semantic.Hit;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SemanticSearchService
{
	/**
	 * Find {@link Attribute attributes} in a {@link EntityType entity type} that match the given target attribute in
	 * the target entity type. Optionally constrain the search using one or more search terms.
	 */
	AttributeSearchHits findAttributes(EntityType sourceEntityType, EntityType targetEntityType,
			Attribute targetAttribute, Set<String> searchTerms);

	/**
	 * Finds {@link OntologyTerm}s that can be used to tag an attribute.
	 *
	 * @param entity      name of the entity
	 * @param ontologyIds IDs of ontologies to take the {@link OntologyTerm}s from.
	 * @return {@link Map} of {@link Hit}s for {@link OntologyTerm} results
	 */
	Map<Attribute, Hit<OntologyTerm>> findTags(String entity, List<String> ontologyIds);
}
