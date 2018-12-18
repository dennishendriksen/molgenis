package org.molgenis.data.annotation;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EntityProcessorTest {
  private EntityProcessor entityProcessor;

  @BeforeMethod
  public void setUpBeforeMethod() {
    entityProcessor = new EntityProcessor();
  }

  @Test
  public void testCompileWithAnnotationProcessors() {
    Compilation compilation =
        javac().withProcessors(entityProcessor).compile(JavaFileObjects.forResource("Id.java"));
    assertThat(compilation).succeeded();
    assertThat(compilation)
        .generatedSourceFile("Generated_Id")
        .hasSourceEquivalentTo(JavaFileObjects.forResource("Generated_Id.java"));
  }
}
