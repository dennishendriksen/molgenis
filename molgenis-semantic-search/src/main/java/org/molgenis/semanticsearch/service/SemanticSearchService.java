package org.molgenis.semanticsearch.service;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.semanticsearch.explain.bean.ExplainedAttribute;
import org.molgenis.semanticsearch.semantic.Hit;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SemanticSearchService
{
	/**
	 * A decision tree for getting the relevant attributes
	 * <p>
	 * 1. First find attributes based on searchTerms. 2. Second find attributes based on ontology terms from tags 3.
	 * Third find attributes based on target attribute label.
	 *
	 * @return Attribute of resembling attributes, sorted by relevance
	 */
	Map<Attribute, ExplainedAttribute> findAttributes(EntityType sourceEntityType, EntityType targetEntityType,
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
