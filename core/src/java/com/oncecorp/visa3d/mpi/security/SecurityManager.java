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

package com.oncecorp.visa3d.mpi.security;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.ibm.xml.dsig.SignatureStructureException;
import com.ibm.xml.dsig.XSignatureException;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.MessagingException;

/**
 * Description: The SecurityManager class is a Singleton and facade calss
 * that provides Security functionality to the other components of the
 * Merchant PlugIn. It provides: 
 * 		- Connection initialization
 * 		- Certificate managerment
 * 		- XML Signature signing and verification procedure
 * 
 * @version 0.1 July 26, 2002
 * @author	Alan Zhang
 */
public class SecurityManager {
	/**
	 * Singleton instance
	 */
	private static SecurityManager instance = null;

	/**
	 * Connection initialization flag
	 */
	private static boolean isConfigured = false;

	/**
	 * Local Log4J logger
	 */
	private static Logger logger =
		MPILogger.getLogger(SecurityManager.class.getName());

	/**
	 * Private constructor to prevent other components from instantiating this class
	 */
	private SecurityManager() {
	}

	/**
	 * Other components get SecurityManager by calling this method.
	 * 
	 * @return The SecurityManager acquired.
	 */
	public static synchronized SecurityManager getInstance() {
		//Singleton check
		if (instance == null) {
			logger.info("Instantiate SecurityManager.");
			instance = new SecurityManager();
		}

		return instance;
	}

	/**
	 * Initialize Connection protocol handler and security providers
	 */
	public synchronized boolean initializeConnection() {
		if (!isConfigured) {
			//Get configurator
			ConnectionConfigurator cc = new ConnectionConfigurator();
			isConfigured = true;

			//initialize connection and forward response.
			return cc.setConnectionParameters();
		} else {
			return ConnectionConfigurator.SUCCESS;
		}
	}

	/**
	 * Verify XML Signature.
	 * 
	 * @param doc The document to be verified.
	 * @return The validity result. True means valid.
	 */
	public boolean verifySignature(Document doc, String keyStorePath, char[] storePass) throws MessagingException {
		//Verify signature
		try {
			XMLSignatureResult xsr = XMLSignature.verify(doc, keyStorePath, storePass);

			//Check result
			if (xsr.getVerified())
				logger.info("Signature validity: OK.");
			else
				logger.info("Signature validity: NG.");

			return xsr.getVerified();
		} catch (MessagingException me) {
			logger.error("Delegate MessagingException to processor...");
			throw me;
		} catch (Exception e) {
			logger.error(
				"Block any exception other than MessagingException. Signature verify failed.",
				e);
			return false;
		}
	}

	public XMLSignatureResult sign(
		String keystorepath,
		String alias,
		char[] storepass,
		char[] keypass,
		String methodID,
		Document doc,
		String xpath)
		throws
			XSignatureException,
			IOException,
			KeyStoreException,
			SignatureStructureException,
			NoSuchAlgorithmException,
			CertificateException,
			UnrecoverableKeyException,
			ParserConfigurationException,
			SAXException {
		return XMLSignature.doSignature(
			keystorepath,
			alias,
			storepass,
			keypass,
			methodID,
			doc,
			xpath);
	}
}