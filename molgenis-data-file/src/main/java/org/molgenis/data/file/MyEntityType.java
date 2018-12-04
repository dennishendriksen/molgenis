package org.molgenis.data.file;

import java.util.Optional;
import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import org.molgenis.data.annotation.Attribute;
import org.molgenis.data.annotation.Entity;

@Entity
public abstract class MyEntityType implements org.molgenis.data.Entity {
  @Attribute
  public abstract String getAttr0();

  public abstract MyEntityType setAttr0(String attr0);

  @Attribute(name = "attr1")
  public abstract String getAttr1();

  public abstract MyEntityType setAttr1(String attr1);

  @Attribute(name = "attr2")
  public abstract String getAttr2DifferentName();

  public abstract MyEntityType setAttr2DifferentName(String attr2DifferentName);

  @Attribute
  public abstract int getAttr3Integer();

  public abstract MyEntityType setAttr3Integer(int attrInteger0);

  @Attribute(nullable = true)
  public abstract Optional<String> getAttr4Nullable();

  public abstract MyEntityType setAttr4Nullable(@Nullable String attrNullable);

  public static MyEntityType create() {
    return new Generated_MyEntityType();
  }

  @Attribute
  public abstract Iterable<Entity> getAttr5Entities0();

  public abstract MyEntityType setAttr5Entities0(Iterable<Entity> entities);

  @Attribute(nullable = true)
  public abstract @NotEmpty Iterable<Entity> getAttr6Entities0();

  public abstract MyEntityType setAttr6Entities0(@NotEmpty Iterable<Entity> entities);
}
