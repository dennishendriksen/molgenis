package org.molgenis.data.annotation.test;

import java.util.Optional;
import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import org.molgenis.data.annotation.Attribute;
import org.molgenis.data.annotation.Entity;

@Entity
public abstract class Id implements org.molgenis.data.Entity {
  @Attribute
  public abstract String getId();
}