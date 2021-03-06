package dev.fiki.forgehax.api.mapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodMapping {
  /**
   * COMPILE ONLY
   * <p>
   * The parent class of the mapping this object represents.
   * If not changed, the code generator will use the class provided by the parent classes {@code @ClassMapping}
   *
   * @return parent class
   */
  Class<?> parentClass() default Dummy.class;
  ClassMapping parent() default @ClassMapping;

  /**
   * COMPILE ONLY
   * <p>
   * The name of the method to attempt to lookup mappings for.
   *
   * @return method name
   */
  String value() default "";

  /**
   * COMPILE ONLY
   * <p>
   * The arguments for the method. This can be omitted if the {@code value} and/or {@code ret} is unique.
   *
   * @return Array of argument types
   */
  Class<?>[] args() default {Dummy.class};
  ClassMapping[] argsm() default @ClassMapping;

  /**
   * COMPILE ONLY
   * <p>
   * The return type of the method. This can be omitted if the {@code value} and/or {@code args} is unique.
   *
   * @return Return type
   */
  Class<?> ret() default Dummy.class;
  ClassMapping[] retm() default @ClassMapping;

  /**
   * To help the code generator know if the method is static or not.
   *
   * @return if the method is static or not
   */
  boolean isStatic() default false;

  /**
   * If {@code value} is not a mcp mapping name, the type can be changed to srg or obfuscated.
   *
   * @return the type of mapping name provided
   */
  MappedFormat format() default MappedFormat.MAPPED;

  ClassMapping _parentClass() default @ClassMapping(void.class);

  String _name() default "";
  String _obfName() default "";
  String _srgName() default "";

  String _descriptor() default "";
  String _obfDescriptor() default "";
}
