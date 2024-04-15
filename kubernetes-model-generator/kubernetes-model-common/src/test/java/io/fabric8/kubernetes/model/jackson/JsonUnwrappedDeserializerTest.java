/*
 * Copyright (C) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.kubernetes.model.jackson;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JsonUnwrappedDeserializerTest {

  private static final String EXPECTED_VALUE_A = "Value A";
  private static final String EXPECTED_VALUE_B = "Value B";
  private static final String EXPECTED_VALUE_C = "Value C";

  @Nested
  class Deserialize {
    @Test
    @DisplayName("Single @JsonUnwrapped polymorphic type")
    void singleInterfaceWithJsonWrapped() throws JsonProcessingException {
      ObjectMapper mapper = new ObjectMapper();
      RootClass instance = mapper.readValue("{ \"stringField\": \"" + EXPECTED_VALUE_A + "\", "
        + "\"extendedField\": \"" + EXPECTED_VALUE_B + "\", "
        + "\"nestedField\": \"" + EXPECTED_VALUE_C + "\" }", RootClass.class);
      // Verify normal fields works along to the json-wrapped fields
      assertEquals(EXPECTED_VALUE_A, instance.stringField);

      // Verify interfaces are supported at root level
      assertNotNull(instance.rootInterface, "Interface was not deserialized!");
      assertInstanceOf(RootImplementation.class, instance.rootInterface);
      RootImplementation rootImplementation = ((RootImplementation) instance.rootInterface);
      assertEquals(EXPECTED_VALUE_B, rootImplementation.extendedField);

      // Verify nested interfaces are also supported
      assertInstanceOf(NestedImplementation.class, rootImplementation.nestedInterface);
      assertEquals(EXPECTED_VALUE_C, ((NestedImplementation) rootImplementation.nestedInterface).nestedField);
    }

    @Test
    @DisplayName("multiple @JsonUnwrapped polymorphic types")
    void multipleInterfacesJsonWrappedFields() throws JsonProcessingException {
      ObjectMapper mapper = new ObjectMapper();
      RootClassWithMultiplePolymorphicJsonUnwrappedFields instance = mapper.readValue("{" +
        "\"foo\": \"" + EXPECTED_VALUE_C + "\"," +
        "\"bar\": \"" + EXPECTED_VALUE_B + "\"" +
        "}", RootClassWithMultiplePolymorphicJsonUnwrappedFields.class);

      // Verify interfaces are supported at root level
      assertNotNull(instance.fooInterface, "Interface was not deserialized!");
      assertInstanceOf(FooImplementation.class, instance.fooInterface);
      FooImplementation fooImplementation = ((FooImplementation) instance.fooInterface);
      assertNotNull(instance.barInterface, "Interface was not deserialized!");
      BarImplementation barImplementation = ((BarImplementation) instance.barInterface);
      assertInstanceOf(BarImplementation.class, instance.barInterface);
      assertEquals(EXPECTED_VALUE_C, fooImplementation.foo);
      assertEquals(EXPECTED_VALUE_B, barImplementation.bar);
    }

    @Test
    @DisplayName("Multiple @JsonUnwrapped non-polymorphic types")
    void multipleNonPolymorphicJsonUnwrappedFields() throws JsonProcessingException {
      ObjectMapper mapper = new ObjectMapper();
      RootClassWithMultipleNonPolymorphicJsonUnwrappedFields instance = mapper.readValue("{" +
        "\"foo\": \"" + EXPECTED_VALUE_A + "\"," +
        "\"bar\": \"" + EXPECTED_VALUE_B + "\"" +
        "}", RootClassWithMultipleNonPolymorphicJsonUnwrappedFields.class);

      // Verify interfaces are supported at root level
      assertEquals(EXPECTED_VALUE_A, instance.foo.getFoo());
      assertEquals(EXPECTED_VALUE_B, instance.bar.getBar());
    }

    @Test
    @DisplayName("multiple @JsonUnwrapped polymorphic and non polymorphic types")
    void multipleNonPolymorphicAndPolymorphicUnwrappedFields() throws JsonProcessingException {
      ObjectMapper mapper = new ObjectMapper();
      RootClassWithMultipleNonPolymorphicAndPolymorphicFields instance = mapper.readValue("{" +
        "\"foo\": \"" + EXPECTED_VALUE_C + "\"," +
        "\"bar\": \"" + EXPECTED_VALUE_B + "\"," +
        "\"nestedField\": \"" + EXPECTED_VALUE_C + "\"," +
        "\"stringField\": \"" + EXPECTED_VALUE_A + "\"" +
        "}", RootClassWithMultipleNonPolymorphicAndPolymorphicFields.class);

      // Verify interfaces are supported at root level
      assertNotNull(instance.fooInterface, "Interface was not deserialized!");
      assertInstanceOf(FooImplementation.class, instance.fooInterface);
      FooImplementation fooImplementation = ((FooImplementation) instance.fooInterface);
      assertNotNull(instance.barInterface, "Interface was not deserialized!");
      BarImplementation barImplementation = ((BarImplementation) instance.barInterface);
      assertInstanceOf(BarImplementation.class, instance.barInterface);
      assertEquals(EXPECTED_VALUE_C, fooImplementation.foo);
      assertEquals(EXPECTED_VALUE_B, barImplementation.bar);
      assertEquals(EXPECTED_VALUE_C, instance.nestedImplementation.getNestedField());
      assertEquals(EXPECTED_VALUE_A, instance.getStringField());
    }
  }

  @Getter
  @Setter
  public static class RootClassWithMultipleJsonUnwrappedFields {
    @JsonUnwrapped
    private FooImplementation fooImpl;
    @JsonUnwrapped
    private BarImplementation barImpl;
  }

  @Getter
  @Setter
  @JsonDeserialize(using = io.fabric8.kubernetes.model.jackson.JsonUnwrappedDeserializer.class)
  public static class RootClassWithMultiplePolymorphicJsonUnwrappedFields {
    @JsonUnwrapped
    private FooInterface fooInterface;
    @JsonUnwrapped
    private BarInterface barInterface;
  }

  @Getter
  @Setter
  public static class RootClassWithMultipleNonPolymorphicJsonUnwrappedFields {
    // Replacing these types with FooImplementation / BarImplementation causes error
    @JsonUnwrapped
    private FooNoParentImpl foo;
    @JsonUnwrapped
    private BarNoParentImpl bar;
  }

  @Getter
  @Setter
  @JsonDeserialize(using = io.fabric8.kubernetes.model.jackson.JsonUnwrappedDeserializer.class)
  public static class RootClassWithMultipleNonPolymorphicAndPolymorphicFields {
    @JsonUnwrapped
    private FooInterface fooInterface;
    @JsonUnwrapped
    private BarInterface barInterface;
    @JsonUnwrapped
    private NestedImplementation nestedImplementation;
    private String stringField;
  }

  @JsonDeserialize(using = io.fabric8.kubernetes.model.jackson.JsonUnwrappedDeserializer.class)
  public static class RootClass {

    private String stringField;

    @JsonUnwrapped
    private RootInterface rootInterface;

    public RootClass() {

    }

    public String getStringField() {
      return stringField;
    }

    public void setStringField(String stringField) {
      this.stringField = stringField;
    }

    public RootInterface getRootInterface() {
      return rootInterface;
    }

    public void setRootInterface(RootInterface rootInterface) {
      this.rootInterface = rootInterface;
    }
  }

  @JsonSubTypes(@JsonSubTypes.Type(RootImplementation.class))
  @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
  interface RootInterface {

  }

  @JsonSubTypes(@JsonSubTypes.Type(BarImplementation.class))
  @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
  interface BarInterface {

  }

  @JsonSubTypes(@JsonSubTypes.Type(FooImplementation.class))
  @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
  interface FooInterface {

  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class FooImplementation implements FooInterface {
    private String foo;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class BarImplementation implements BarInterface {
    private String bar;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class FooNoParentImpl {
    private String foo;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class BarNoParentImpl {
    private String bar;
  }

  @JsonDeserialize(using = io.fabric8.kubernetes.model.jackson.JsonUnwrappedDeserializer.class)
  public static class RootImplementation implements RootInterface {

    private String extendedField;
    @JsonUnwrapped
    private NestedInterface nestedInterface;

    public RootImplementation() {

    }

    public String getExtendedField() {
      return extendedField;
    }

    public void setExtendedField(String extendedField) {
      this.extendedField = extendedField;
    }
  }

  @JsonSubTypes(@JsonSubTypes.Type(NestedImplementation.class))
  @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
  interface NestedInterface {

  }

  public static class NestedImplementation implements NestedInterface {
    private String nestedField;

    public NestedImplementation() {

    }

    public String getNestedField() {
      return nestedField;
    }

    public void setNestedField(String nestedField) {
      this.nestedField = nestedField;
    }
  }
}
