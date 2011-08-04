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

package org.mobicents.ext.javax.sip;

import gov.nist.core.LogLevels;
import gov.nist.javax.sip.ListeningPointImpl;
import gov.nist.javax.sip.MobicentsSipProviderImpl;
import gov.nist.javax.sip.SipStackImpl;

import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.SipProvider;
import javax.sip.SipStack;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class MobicentsSipProviderFactory implements SipProviderFactory {

	private SipStackImpl sipStack;
	
	public void setSipStack(SipStack sipStack) {
		this.sipStack = (SipStackImpl) sipStack;
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.ext.javax.sip.SipProviderFactory#createSipProvider(javax.sip.ListeningPoint)
	 */
	public SipProvider createSipProvider(ListeningPoint listeningPoint)
		throws ObjectInUseException {
		if (listeningPoint == null)
			throw new NullPointerException("null listeningPoint");
		if (sipStack.isLoggingEnabled(LogLevels.TRACE_DEBUG))
			sipStack.getStackLogger().logDebug(
					"createSipProvider: " + listeningPoint);
		ListeningPointImpl listeningPointImpl = (ListeningPointImpl) listeningPoint;
		if (listeningPointImpl.getProvider() != null)
			throw new ObjectInUseException("Provider already attached!");

		MobicentsSipProviderImpl provider = new MobicentsSipProviderImpl(sipStack);

		provider.setListeningPoint(listeningPointImpl);
		listeningPointImpl.setSipProvider(provider);
		((SipStackExtension)sipStack).addSipProvider(provider);
		return provider;
	}

}