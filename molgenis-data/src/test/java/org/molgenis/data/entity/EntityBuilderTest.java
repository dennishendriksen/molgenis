package org.molgenis.data.entity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.STRING;

import java.util.Arrays;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.testng.annotations.Test;

public class EntityBuilderTest
{
	@Test
	public void EntityBuilder()
	{
		String idAttrName = "id";
		String attr0Name = "attr0";
		String attr1Name = "attr1";
		AttributeMetaData idAttr = when(mock(AttributeMetaData.class).getName()).thenReturn(idAttrName).getMock();
		when(idAttr.getDataType()).thenReturn(STRING);
		when(idAttr.isIdAtrribute()).thenReturn(true);
		when(idAttr.isAuto()).thenReturn(true);
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn(attr0Name).getMock();
		when(attr0.getDataType()).thenReturn(STRING);
		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn(attr1Name).getMock();
		when(attr1.getDataType()).thenReturn(STRING);

		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("MyEntity").getMock();
		when(entityMeta.getIdAttribute()).thenReturn(idAttr);
		when(entityMeta.getAttribute(idAttrName)).thenReturn(idAttr);
		when(entityMeta.getAttribute(attr0Name)).thenReturn(attr0);
		when(entityMeta.getAttribute(attr1Name)).thenReturn(attr1);
		when(entityMeta.getAtomicAttributes()).thenReturn(Arrays.asList(idAttr, attr0, attr1));

		Entity entity = new EntityBuilder(entityMeta).set(attr0, "val0").set(attr1, "notanint").build();
		System.out.println(entity);
	}
}
