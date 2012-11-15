/**
 * Copyright 2003, 2004  ONCE Corporation
 *
 * LICENSE:
 * This file is part of BuilditMPI. It may be redistributed and/or modified
 * under the terms of the Common Public License, version 1.0.
 * You should have received a copy of the Common Public License along with this
 * software. See LICENSE.txt for details. Otherwise, you may find it online at:
 *   http://www.oncecorp.com/CPL10/ or http://opensource.org/licenses/cpl.php
 *
 * DISCLAIMER OF WARRANTIES AND LIABILITY:
 * THE SOFTWARE IS PROVIDED "AS IS".  THE AUTHOR MAKES NO REPRESENTATIONS OR
 * WARRANTIES, EITHER EXPRESS OR IMPLIED.  TO THE EXTENT NOT PROHIBITED BY LAW,
 * IN NO EVENT WILL THE AUTHOR BE LIABLE FOR ANY DAMAGES, INCLUDING WITHOUT
 * LIMITATION, LOST REVENUE, PROFITS OR DATA, OR FOR SPECIAL, INDIRECT,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS
 * OF THE THEORY OF LIABILITY, ARISING OUT OF OR RELATED TO ANY FURNISHING,
 * PRACTICING, MODIFYING OR ANY USE OF THE SOFTWARE, EVEN IF THE AUTHOR HAVE
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * -----------------------------------------------------
 * $Id$
 */

package com.oncecorp.visa3d.mpi.controller;

import com.oncecorp.visa3d.mpi.configuration.MPIConfigDefinition;


/**
 * This class contains all the session specific information that is needed in
 * order to correctly perform an authentication transaction.
 * It can be accessed through the singleton pattern from anywhere in the 
 * Core Server and implements all values as ThreadLocal attributes
 * 
 * @author Martin Dufort (mdufort@oncecorp.com)
 */
public class AuthenticatorSession {

	// Definition of the available payment interfaces
	public static final String HTML_INTERFACE 	= "HTML";
	public static final String XML_INTERFACE 	= "XML";

	// Singleton accessor variable
	private static AuthenticatorSession instance = null;

	// Thread specific values that we need for session processing
	private ThreadLocal merchantId; 		// Merchant ID associated with this thread
	private ThreadLocal protocol; 			// Protocol (VbV, SecureCode) associated with this thread
	private ThreadLocal paymentInterface; 	// Interface used for authentication (HTML, XML)

	// Can only construct from singleton access point
	private AuthenticatorSession() {
		// Create the proper ThreadLocal variables
		merchantId 			= new ThreadLocal();
		protocol 			= new ThreadLocal();
		paymentInterface 	= new ThreadLocal();
	}

	/**
	 * Singleton accessor method for the AuthenticatorSession
	 */
	public static synchronized AuthenticatorSession instance() {
		// If no instance then we create a new one
		if (instance == null) {
			instance = new AuthenticatorSession();
		}

		return instance;
	}

	/**
	 * Reset all the thread local variables
	 * 
	 * @param aMerchantID
	 */
	public void reset() {
		merchantId.set(null);
		protocol.set(null);
		paymentInterface.set(null);
	}
	
	/**
	 * Setter for the thread-specific merchant ID
	 * @param aMerchantID	Merchant ID value to set
	 */
	public void setMerchantID(String aMerchantID) {
		merchantId.set(aMerchantID);
	}

	/**
	 * Getter for the thread-specific merchant ID
	 * @return String
	 */
	public String getMerchantID() {
		Object value = merchantId.get();
		return (value == null) ? "UNKNOWN" : (String) value;
	}

	/**
	 * Setter for the thread-specific protocol value
	 * @param aProtocol		Protocol value to set
	 */
	public void setProtocol(String aProtocol) {
		if (aProtocol != null) {
			if ((aProtocol.equalsIgnoreCase(MPIConfigDefinition.PROTOCOL_VISA_3D_SECURE)) || 
				(aProtocol.equalsIgnoreCase(MPIConfigDefinition.PROTOCOL_MASTERCARD_SECURE_CODE))) {
				protocol.set(aProtocol);
			}
		}
		else {
			protocol.set(null);
		}
	}

	/**
	 * Getter for the thread-specific protocol value
	 * @return int
	 */
	public String getProtocol() {
		Object value = protocol.get();
		return (value == null) ? "UNKNOWN" : (String) value;
	}

	/**
	 * Setter for the intergration interface use by the merchant application
	 * @param anInterface	Interface to be set
	 */
	public void setPaymentInterface(String anInterface) {
		if (anInterface != null) {
			if (anInterface.equalsIgnoreCase(XML_INTERFACE) || anInterface.equalsIgnoreCase(HTML_INTERFACE)) {
				// Set the proper interface defined
				paymentInterface.set(anInterface);
			}
		}
		else {
			// By default we use the XML interface
			paymentInterface.set(null);
		}
	}

	/**
	 * Getter for the integration interface to use
	 * @return String
	 */
	public String getPaymentInterface() {
		Object value = paymentInterface.get();
		return (value == null)? "UNKNOWN" : (String) value;
	}
}
