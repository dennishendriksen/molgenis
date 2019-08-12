package org.molgenis.data;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.time.Instant;
import java.time.LocalDate;
import org.mockito.Mock;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EntityTest extends AbstractMockitoTest {
  private static final String ATTRIBUTE_NAME = "myAttribute";

  @Mock private Entity entity;
  @Mock private Attribute attribute;

  @BeforeMethod
  public void setUpBeforeMethod() {
    when(attribute.getName()).thenReturn(ATTRIBUTE_NAME);
  }

  @Test
  public void testGet() {
    doCallRealMethod().when(entity).get(attribute);
    Object value = mock(Object.class);
    doReturn(value).when(entity).get(ATTRIBUTE_NAME);
    assertEquals(entity.get(attribute), value);
  }

  @Test
  public void testGetString() {
    doCallRealMethod().when(entity).getString(attribute);
    String value = "str";
    doReturn(value).when(entity).getString(ATTRIBUTE_NAME);
    assertEquals(entity.getString(attribute), value);
  }

  @Test
  public void testGetInt() {
    doCallRealMethod().when(entity).getInt(attribute);
    Integer value = 1;
    doReturn(value).when(entity).getInt(ATTRIBUTE_NAME);
    assertEquals(entity.getInt(attribute), value);
  }

  @Test
  public void testGetLong() {
    doCallRealMethod().when(entity).getLong(attribute);
    Long value = 1L;
    doReturn(value).when(entity).getLong(ATTRIBUTE_NAME);
    assertEquals(entity.getLong(attribute), value);
  }

  @Test
  public void testGetBoolean() {
    doCallRealMethod().when(entity).getBoolean(attribute);
    Boolean value = Boolean.TRUE;
    doReturn(value).when(entity).getBoolean(ATTRIBUTE_NAME);
    assertEquals(entity.getBoolean(attribute), value);
  }

  @Test
  public void testGetDouble() {
    doCallRealMethod().when(entity).getDouble(attribute);
    Double value = 1.23;
    doReturn(value).when(entity).getDouble(ATTRIBUTE_NAME);
    assertEquals(entity.getDouble(attribute), value);
  }

  @Test
  public void testGetInstant() {
    doCallRealMethod().when(entity).getInstant(attribute);
    Instant value = Instant.now();
    doReturn(value).when(entity).getInstant(ATTRIBUTE_NAME);
    assertEquals(entity.getInstant(attribute), value);
  }

  @Test
  public void testGetLocalDate() {
    doCallRealMethod().when(entity).getLocalDate(attribute);
    LocalDate value = LocalDate.now();
    doReturn(value).when(entity).getLocalDate(ATTRIBUTE_NAME);
    assertEquals(entity.getLocalDate(attribute), value);
  }

  @Test
  public void testGetEntity() {
    doCallRealMethod().when(entity).getEntity(attribute);
    Entity value = mock(Entity.class);
    doReturn(value).when(entity).getEntity(ATTRIBUTE_NAME);
    assertEquals(entity.getEntity(attribute), value);
  }

  @Test
  public void testGetEntityAttributeClass() {
    Class<Attribute> clazz = Attribute.class;
    doCallRealMethod().when(entity).getEntity(attribute, clazz);
    Attribute value = mock(Attribute.class);
    doReturn(value).when(entity).getEntity(ATTRIBUTE_NAME, clazz);
    assertEquals(entity.getEntity(attribute, clazz), value);
  }

  @Test
  public void testGetEntities() {
    doCallRealMethod().when(entity).getEntities(attribute);
    @SuppressWarnings("unchecked")
    Iterable<Entity> value = mock(Iterable.class);
    doReturn(value).when(entity).getEntities(ATTRIBUTE_NAME);
    assertEquals(entity.getEntities(attribute), value);
  }

  @Test
  public void testGetEntitiesAttributeClass() {
    Class<Attribute> clazz = Attribute.class;
    doCallRealMethod().when(entity).getEntities(attribute, clazz);
    @SuppressWarnings("unchecked")
    Iterable<Attribute> value = mock(Iterable.class);
    doReturn(value).when(entity).getEntities(ATTRIBUTE_NAME, clazz);
    assertEquals(entity.getEntities(attribute, clazz), value);
  }

  @Test
  public void testSet() {
    Object value = mock(Object.class);
    doCallRealMethod().when(entity).set(attribute, value);
    entity.set(attribute, value);
    verify(entity).set(ATTRIBUTE_NAME, value);
  }
}
