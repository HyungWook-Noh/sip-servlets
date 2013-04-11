/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable.
 * All rights reserved.
 *
 * TargetApplication is a annotation used by client to indicate the target
 *  application name
 */
package com.bea.sipservlet.tck.agents;

import com.bea.sipservlet.tck.common.TestConstants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
public @interface TargetApplication {
  public ApplicationName value() default ApplicationName.APITESTAPP;
}
