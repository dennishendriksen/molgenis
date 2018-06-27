package org.molgenis.semanticsearch.service;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.semanticsearch.explain.bean.ExplainedAttribute;
import org.molgenis.semanticsearch.semantic.Hits;

import java.util.Collection;
import java.util.Set;

import static java.util.Collections.emptySet;

public interface SemanticSearchService
{
	/**
	 * Find {@link Attribute attributes} in a {@link EntityType entity type} that match the given target attribute in
	 * the target entity type.
	 */
	default Hits<ExplainedAttribute> findAttributes(EntityType sourceEntityType, EntityType targetEntityType,
			Attribute targetAttribute)
	{
		return findAttributes(sourceEntityType, targetEntityType, targetAttribute, emptySet());
	}

	/**
	 * Find {@link Attribute attributes} in an {@link EntityType entity type} that match the given target attribute in
	 * the target entity type. Optionally constrain the search using one or more search terms.
	 */
	Hits<ExplainedAttribute> findAttributes(EntityType sourceEntityType, EntityType targetEntityType, Attribute targetAttribute, Set<String> searchTerms);

	/**
	 * Finds {@link OntologyTerm ontology terms} in the given ontologies that can be used to tag an attribute.
	 */
	Hits<OntologyTerm> findOntologyTerms(Attribute attribute, Collection<Ontology> ontologies);
}
