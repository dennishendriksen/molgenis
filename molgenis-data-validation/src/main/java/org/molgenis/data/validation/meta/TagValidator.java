package org.molgenis.data.validation.meta;

import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.validation.EntityValidator;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import static java.lang.String.format;
import static org.molgenis.data.meta.model.TagMetadata.RELATION_IRI;

/**
 * {@link org.molgenis.data.meta.model.Tag Tag} validator
 */
@Component
public class TagValidator extends EntityValidator<Tag>
{
	protected TagValidator()
	{
		super(Tag.class);
	}

	@Override
	public void validateEntity(Tag tag, Errors errors)
	{
		validateRelationIriExistsConstraint(tag, errors);
	}

	private void validateRelationIriExistsConstraint(Tag tag, Errors errors)
	{
		String relationIri = tag.getRelationIri();
		Relation relation = Relation.forIRI(relationIri);
		if (relation == null)
		{
			errors.rejectValue(RELATION_IRI, "V20", new Object[] { tag.getId() }, format("id:%s", tag.getId()));
		}
	}
}
