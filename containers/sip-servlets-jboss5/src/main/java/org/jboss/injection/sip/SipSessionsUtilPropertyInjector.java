/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.injection.sip;

import org.jboss.ejb3.BeanContext;
import org.jboss.injection.AbstractPropertyInjector;
import org.jboss.injection.InjectionContainer;
import org.jboss.injection.lang.reflect.BeanProperty;
import org.jboss.web.tomcat.service.sip.TomcatConvergedSipInjectionContainer;
import org.mobicents.servlet.sip.core.SipContext;

/**
 * Injects a SipSessionsUtil into a bean property.
 *
 * @author jean.deruelle@gmail.com
 */
public class SipSessionsUtilPropertyInjector extends AbstractPropertyInjector
{
   private BeanProperty property;
   private InjectionContainer container;

   public SipSessionsUtilPropertyInjector(BeanProperty property, InjectionContainer container)
   {
	  super(property);
      this.property = property;
      this.container = container;
   }

   public void inject(Object instance)
   {
	   property.set(instance, ((SipContext)((TomcatConvergedSipInjectionContainer)container).getCatalinaContext()).getSipSessionsUtil());
   }

   public void inject(BeanContext ctx)
   {
      property.set(ctx.getInstance(), ((SipContext)((TomcatConvergedSipInjectionContainer)container).getCatalinaContext()).getSipSessionsUtil());
   }

   public Class getInjectionClass()
   {
      return property.getType();
   }
}
