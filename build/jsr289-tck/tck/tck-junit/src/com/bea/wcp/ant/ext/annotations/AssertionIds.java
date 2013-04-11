package com.bea.wcp.ant.ext.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is the annotation decorate the testXXX() methods or 
 * @author hujin
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AssertionIds {
  String[] ids();
  String desc() default "";
}

	  
