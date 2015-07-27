package org.molgenis.data.annotation.utils;

import autovalue.shaded.com.google.common.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by charbonb on 11/03/15.
 */
public class AnnotatorUtils
{
	public static DefaultAttributeMetaData getCompoundResultAttribute(RepositoryAnnotator annotator,
			EntityMetaData entityMetaData)
	{
		DefaultAttributeMetaData compoundAttributeMetaData = new DefaultAttributeMetaData(annotator.getFullName(),
				MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
		compoundAttributeMetaData.setLabel(annotator.getSimpleName());

		List<AttributeMetaData> outputAttrs = annotator.getOutputMetaData();

		if (outputAttrs.size() == 1
				&& Iterables.get(outputAttrs, 0).getDataType().getEnumType()
						.equals(MolgenisFieldTypes.FieldTypeEnum.COMPOUND))
		{
			compoundAttributeMetaData = (DefaultAttributeMetaData) outputAttrs.get(0);
		}
		else
		{
			for (AttributeMetaData currentAmd : outputAttrs)
			{
				String currentAttributeName = currentAmd.getName();
				if (entityMetaData.getAttribute(currentAttributeName) == null)
				{
					compoundAttributeMetaData.addAttributePart(currentAmd);
				}
			}
		}
		return compoundAttributeMetaData;
	}

	public static Entity getAnnotatedEntity(RepositoryAnnotator annotator, Entity entity, Map<String, Object> resultMap)
	{
		DefaultEntityMetaData resultEntityMetadata = new DefaultEntityMetaData(entity.getEntityMetaData());
		resultEntityMetadata.addAttributeMetaData(AnnotatorUtils.getCompoundResultAttribute(annotator,
				entity.getEntityMetaData()));

		MapEntity resultEntity = new MapEntity(entity, resultEntityMetadata);
		for (AttributeMetaData attributeMetaData : annotator.getOutputMetaData())
		{
			resultEntity.set(attributeMetaData.getName(), resultMap.get(attributeMetaData.getName()));
		}

		return resultEntity;
	}
}
