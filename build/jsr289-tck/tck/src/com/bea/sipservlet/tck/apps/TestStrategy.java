/**
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved.
 * 
 * TestStrategy is an annotation used to indicate how the method can be 
 * processed on server side.
 */
package com.bea.sipservlet.tck.apps;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target( { METHOD })
@Retention(RUNTIME)
public @interface TestStrategy {
	int strategy() default BaseServlet.TESTSTRATEGY_NORMAL;
}