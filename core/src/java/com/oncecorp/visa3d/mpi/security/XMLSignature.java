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

import java.io.FileInputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.InvalidAlgorithmParameterException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Set;
import java.util.List;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;
import java.security.cert.Certificate;
import java.security.cert.PKIXParameters;
import java.security.cert.CertPathValidator;
import java.security.cert.CertificateFactory;
import java.security.cert.CertPath;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.CertPathValidatorException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ibm.xml.dsig.KeyInfo;
import com.ibm.xml.dsig.SignatureContext;
import com.ibm.xml.dsig.SignatureStructureException;
import com.ibm.xml.dsig.Validity;
import com.ibm.xml.dsig.XSignature;
import com.ibm.xml.dsig.XSignatureException;
import com.ibm.xml.dsig.CertUtil;
import com.ibm.xml.dsig.util.AdHocIDResolver;
import com.oncecorp.visa3d.mpi.domain.payment.ErrorCodes;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.MessagingException;
import com.oncecorp.visa3d.mpi.utility.XMLUtil;

/**
 * Description: The XMLSignature class is a facade class that encapsulated
 * the functinality of the selected XML Signature Library. The corresponding
 * result is encapsulated into the corresponding XMLSignatureResult interface.
 *
 * @version 0.1 July 29, 2002
 * @author	Alan Zhang
 */
public class XMLSignature {
    /**
     * Keystore Type
     * */
    protected final static String KEYSTORE_TYPE = "JKS";

    /**
     * XML Signature method (DSA) ID
     */
    protected final static String SIGNATURE_METHOD_DSA = "DSA";

    /**
     * XML Signature method (RSA) ID
     */
    protected final static String SIGNATURE_METHOD_RSA = "RSA";

    /**
     * Local Log4J logger
     */
    protected static Logger logger =
            MPILogger.getLogger(XMLSignature.class.getName());

    /**
     * XML Signature Signer
     *
     * @param keystorepath The Key Store URI
     * @param alias The certificate alaias
     * @param keypass The key pass of private key
     * @param methodID The Signature method ID
     * @param doc The document to be signed
     * @param xpath The XPath of element to be signed
     * @return The XMLSignatureResult
     * @deprecated Direct access will be prohibited in the futuew. Please use SecurityManager.getInstance().sign(...) instead
     */
    public static XMLSignatureResult sign(
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
        return doSignature(
                keystorepath,
                alias,
                storepass,
                keypass,
                methodID,
                doc,
                xpath);

    }

    /**
     * Mirror signature to be used within the SecurityManager only. This is why it has a package-access of default
     */
    static XMLSignatureResult doSignature(
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
            SAXException {
        //Get element with ID
        Element idElement = (Element) XMLUtil.getFirstNodeByXPath(doc, xpath);

        //Get Signature template with ID set
        Element sigTemplate =
                XMLSignature.getSignatureTemplate(
                        methodID,
                        idElement.getAttribute("id"));

        //Get parent element of Signature
        Element msg =
                (Element) XMLUtil.getFirstNodeByXPath(doc, "/ThreeDSecure/Message");

        //Import node (deep copy)
        Element dup = (Element) doc.importNode(sigTemplate, true);

        //Insert Signature template
        msg.appendChild(dup);

        //Instantiate KeyStore
        KeyStore keystore = KeyStore.getInstance(KEYSTORE_TYPE);

        //Load keystore
        keystore.load(new FileInputStream(keystorepath), storepass);

        //Get certificate
        X509Certificate cert = (X509Certificate) keystore.getCertificate(alias);

        //Get private key
        Key key = keystore.getKey(alias, keypass);

        //Sanity check
        if (key == null) {
            logger.error("Could not get a key: " + alias);
            throw new KeyStoreException("Key not available.");
        }
        else {
            logger.debug("Key retrieved from KeyStore.");
        }

        //Retrieve Signature element
        NodeList list =
                doc.getElementsByTagNameNS(
                        XSignature.XMLDSIG_NAMESPACE,
                        "Signature");
        if (list.getLength() == 0) {
            logger.error("No Signature Element.");
            throw new KeyStoreException("No Signature Element.");
        }

        //Instantiate Signature Context
        SignatureContext sigContext = new SignatureContext();

        //Set up ID resovler
        sigContext.setIDResolver(new AdHocIDResolver(doc));

        //Create KeyInfo
        KeyInfo keyInfo = new KeyInfo();
        KeyInfo.X509Data x5data = new KeyInfo.X509Data();
        x5data.setCertificate(cert);
        x5data.setParameters(cert, true, true, true);
        keyInfo.setX509Data(new KeyInfo.X509Data[]{x5data});
        keyInfo.setKeyValue(cert.getPublicKey());

        //Insert all keys
        for (int i = 0; i < list.getLength(); i++) {
            Element signatureElement = (Element) list.item(i);

            //Insert key
            keyInfo.insertTo(signatureElement);

            //Sign it
            sigContext.sign(signatureElement, key);
        }

        //Set XMLSignatureResult
        XMLSignatureResult xsr = new XMLSignatureResult();
        xsr.setSignedDoc(doc);

        //return result
        return xsr;

    }

    /**
     * Verify XML Signature
     *
     * @param doc The Document to be verified
     * @return The XMLSignatureResult
     */
    static XMLSignatureResult verify(Document doc, String keystorePath, char [] storePass) throws Exception {
		// Create XMLSignatureResult
		XMLSignatureResult xsr = new XMLSignatureResult();

        // Check syntax of XML-signed document first
        String id = checkSignature(doc);

        // Searches for a Signature element
        NodeList nodeList = doc.getElementsByTagNameNS(XSignature.XMLDSIG_NAMESPACE,
                                                       "Signature");
        // Get Signature element
        Element signature = (Element) nodeList.item(0);

        // Create signature context
        SignatureContext sigContext = new SignatureContext();

        // Set up ID Resolver
        sigContext.setIDResolver(new AdHocIDResolver(doc));

        /*
         * [Martin's Note - June 30, 2003 - 10:57am
         * Before we were checking the XML signature against the Trust Store directly.
         * This was not good because we could have accepted any key sign by a valid CA....
         * Now we check only with the Signing Store...
         *
         * Extract the key information from the signature and try to find it within
         * the SigningKeystore setup in the Core Server. If found, verify the signature
         * with it.
         */
        // Instantiate KeyStore
        KeyStore keystore = KeyStore.getInstance(KEYSTORE_TYPE);

        // Load keystore
        keystore.load(new FileInputStream(keystorePath), storePass);

        // Extract signing key

		// Original extraction key algorithm
		// Key key = extractSigningKey(signature, keystore, id);

		Key key = extractSigningKey2(signature, keystore, id);
        if (key != null) {
			logger.debug("Extracted signing key format is:" + key.getFormat());

			// Verify signature
			Validity validity = sigContext.verify(signature, key);

			// Do logging work
			logger.debug(
					validity.getSignedInfoValidity()
					? "Signature Validity: OK"
					: "Signature Validity: NOT VALID (" + validity.getSignedInfoMessage() + ")");

			logger.debug(
					validity.getCoreValidity()
					? "Core Validity: OK"
					: "Core Validity: NOT VALID");

			for (int i = 0; i < validity.getNumberOfReferences(); i++) {
				logger.debug("Ref: \"" + validity.getReferenceURI(i) + "\"\t");
				if (validity.getReferenceValidity(i)) {
					logger.debug("  Validity: OK.");
				}
				else {
					logger.debug("NOT VALID: " + validity.getReferenceMessage(i));
				}
			}


			// Set verify flag
			xsr.setVerified(validity.getCoreValidity());
		}
		else {
			// Unable to retrieve a key that means we cannot validate
			xsr.setVerified(false);
		}

		return xsr;
    }

    private static Key extractSigningKey(Element signature, KeyStore store, String msgId) throws Exception {
		//Look for key in KeyInfo element
		//Search for KeyInfo element
		//Create signature context
		SignatureContext sigContext = new SignatureContext();
		Element keyInfoElement = KeyInfo.searchForKeyInfo(signature);

		// Look for the key in the signature within our Signing KeyStore
		Key key = null;
		KeyInfo keyInfo = new KeyInfo(keyInfoElement);
		Key keyInKeyValue = keyInfo.getKeyValue();
		if (keyInKeyValue != null) {
			logger.debug("The signature has a KeyValue element.");
			key = keyInKeyValue;
		}
		else {
			// Now try to look for key in X509Data elements
			KeyInfo.X509Data[] x5data = keyInfo.getX509Data();
			if (x5data != null && x5data.length > 0) {
				logger.debug("The signature has one or more X509Data elements.");
				for (int i = 0; i < x5data.length; i++) {
					Key xkey = getKeyFromX509Data(x5data[i]);
					if (key == null)
						key = xkey;
				}
			}
			else {
				// Last try: look for key in RetrievalMethod elements
				Element[] retrievals = keyInfo.getRetrievalMethods();
				if (retrievals != null && retrievals.length > 0) {
					logger.debug("The signature has one or more RetrievalMethod elements.");
					for (int i = 0; i < retrievals.length; i++) {
						String type = retrievals[i].getAttribute("Type");
						logger.debug("Type: " + type);
						if (type.equals(KeyInfo.X509DATA)) {
							KeyInfo.X509Data x5d = (KeyInfo.X509Data) sigContext.retrieve(retrievals[i]);
							Key xkey = getKeyFromX509Data(x5d);
							if (key == null) {
								logger.debug("Uses a key from a RetrievalMethod.");
								key = xkey;
							}
						}
					}
				}
			}
		}

		return key;
    }

	private static Key extractSigningKey2(Element signature, KeyStore store, String msgId) throws Exception {
		// Search for KeyInfo element
		SignatureContext sigContext = new SignatureContext();
		Element keyInfoElement = KeyInfo.searchForKeyInfo(signature);
		KeyInfo keyInfo = new KeyInfo(keyInfoElement);

		// Look for the key in the signature within our Signing KeyStore
		Key key = null;
		if (keyInfoElement != null) {
			// Retrieve all the key names
			logger.debug("Extracting signing key information from signature.");
			String[] nn = new KeyInfo(keyInfoElement).getKeyNames();
			if (nn != null) {
				logger.debug("We got " + nn.length + " keys for this signature");
				for (int i = 0; key == null && i < nn.length; i++) {
					logger.debug("Retrieved key name: " + nn[i]);
					Certificate c = store.getCertificate(nn[i]);
					if (c != null) {
						logger.debug("\tfrom certificate: " + c.toString());
						key = c.getPublicKey();
						break;
					}
				}
			}
			else {
				// Now try to look for key in X509Data elements
				KeyInfo.X509Data[] x5data = keyInfo.getX509Data();
				if (x5data != null && x5data.length > 0) {
					logger.debug("The signature has " + x5data.length + " X509Data elements.");
					for (int i = 0; i < x5data.length; i++) {
						// For each X509 data retrieved, we must check if the certificate is in our keytore as well
						if (checkCertsInKeystore(x5data[i], store)) {
							Key xkey = getKeyFromX509Data(x5data[i]);
							if (key == null) {
				                logger.debug("Uses a key from the X509 data element");
								key = xkey;
								break;
							}
						}
					}
				}
				else {
					// Last try: look for key in RetrievalMethod elements
					Element[] retrievals = keyInfo.getRetrievalMethods();
					if (retrievals != null && retrievals.length > 0) {
						logger.debug("The signature has one or more RetrievalMethod elements.");
						for (int i = 0; i < retrievals.length; i++) {
							String type = retrievals[i].getAttribute("Type");
							logger.debug("Type: " + type);
							if (type.equals(KeyInfo.X509DATA)) {
								KeyInfo.X509Data x5d = (KeyInfo.X509Data) sigContext.retrieve(retrievals[i]);
								if (checkCertsInKeystore(x5d, store)) {
									Key xkey = getKeyFromX509Data(x5d);
									if (key == null) {
										logger.debug("Uses a key from a RetrievalMethod.");
										key = xkey;
										break;
									}
								}
							}
						}
					}
				}
			}
		}

		// Check if we were able to retrieve the key from our signing cert keystore. If not then
		// validation fails and we indicate it appropriately
		//if (key == null) {
		//	throw new MessagingException(
		//			msgId,
		//			ErrorCodes.ERROR_CODE_5,
		//			ErrorCodes.ERROR_MESSAGE_5,
		//			"Signature.key",
		//			"Message is signed with a key that is NOT trusted by the MPI component",
		//			"Message is signed with a key that is NOT trusted by the MPI component");
		//
		//}

		return key;
	}

	private static boolean checkCertsInKeystore(KeyInfo.X509Data x5d, KeyStore aStore) throws CertificateException {
		boolean certIsValid = true;
		X509Certificate[] certs = x5d.getCertificates();

		// Printout the certs we are checking
		logger.debug("Checking certificates for validity:");
		for (int i = 0; i < certs.length; i++) {
			X509Certificate cert = certs[i];
			logger.debug("\tCert #:" + i);
			logger.debug("\tVersion: " 		+ cert.getVersion());
			logger.debug("\tSubjectDN: " 	+ cert.getSubjectDN());
			logger.debug("\tIssuerDN: " 	+ cert.getIssuerDN());
			logger.debug("\tNot before:"	+ cert.getNotBefore());
			logger.debug("\tNot after:"		+ cert.getNotAfter());
			logger.debug("\tSerial#: 0x" 	+ cert.getSerialNumber().toString(16));
 		}

		// Check each certificate to ensure that they are present in the keystore
		try {
			   // Convert chain to a List
			   List certList = Arrays.asList(certs);

			   // Instantiate a CertificateFactory for X.509
			   CertificateFactory cf = CertificateFactory.getInstance("X.509");

			   // Extract the certification path from
			   // the List of Certificates
			   CertPath cp = cf.generateCertPath(certList);

			   // Create CertPathValidator that implements the "PKIX" algorithm
			   CertPathValidator cpv = CertPathValidator.getInstance("PKIX");

			   // Set the PKIX parameters to trust everything in the keystore.
			   PKIXParameters params = new PKIXParameters(aStore);
               params.setRevocationEnabled(false);

			   // Validate and obtain results
			   try {
				   PKIXCertPathValidatorResult result = (PKIXCertPathValidatorResult) cpv.validate(cp, params);
				}
			   catch (CertPathValidatorException cpve) {
				   logger.debug("Validation failure, cert[" + cpve.getIndex() + "] :" + cpve.getMessage(), cpve.getCause());
				   certIsValid = false;
			   }
		}
		catch (KeyStoreException e) {
			logger.error("Keystore error while validating certificate presence.", e);
			certIsValid = false;
		}
		catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();  //To change body of catch statement use Options | File Templates.
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();  //To change body of catch statement use Options | File Templates.
		}

		// Everything is cool means the certificate is in our keystore.
		return certIsValid;
	}

	/**
     * Convenient method to create signature template.
     * VISA 3D-Secure specifies the use of detached signature, hernce we have to create
     * templates reflecting the signature element.
     *
     * @param method The Signature method ID
     * @param id The reference ID
     * @return The Signature Element
     */
    private static Element getSignatureTemplate(String method, String id)
            throws SAXException {
        String template = null;

        //Check signature method
        if (method.equals(SIGNATURE_METHOD_RSA)) {
            template =
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">"
                    + "<SignedInfo>"
                    + "<CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\"/>"
                    + "<SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\"/>"
                    + "<Reference>"
                    + "<Transforms>"
                    + "<Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/>"
                    + "</Transforms>"
                    + "<DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/>"
                    + "<DigestValue></DigestValue>"
                    + "</Reference>"
                    + "</SignedInfo>"
                    + "<SignatureValue></SignatureValue>"
                    + "</Signature>";
        }
        else {
            //Default to DSA
            template =
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">"
                    + "<SignedInfo>"
                    + "<CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\"/>"
                    + "<SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#dsa-sha1\"/>"
                    + "<Reference>"
                    + "<Transforms>"
                    + "<Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/>"
                    + "</Transforms>"
                    + "<DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/>"
                    + "<DigestValue></DigestValue>"
                    + "</Reference>"
                    + "</SignedInfo>"
                    + "<SignatureValue></SignatureValue>"
                    + "</Signature>";
        }

        //Create template document
        Document doc = null;
        try {
            doc = XMLUtil.createDocument(template, false);
        }
        catch (Exception e) {
            logger.error("Create Signature Template failed.", e);
            throw new SAXException(e);
        }

        //Set Reference URI
        Element root = doc.getDocumentElement();
        Element ref = (Element) root.getElementsByTagName("Reference").item(0);
        ref.setAttribute("URI", "#" + id);

        return root;
    }

    /**
     * Convenient method to get key from an X509Data.
     *
     * @param x5data The X509Data
     * @return The key
     */
    static Key getKeyFromX509Data(KeyInfo.X509Data x5data) throws Exception {
        //Prepare key
        Key key = null;

        //Check Certificate Revokation List (CRL)
        X509CRL crl = x5data.getCRL();
        if (crl != null) {
            logger.debug("CRL Information:");
            logger.debug("\tVersion: " + crl.getVersion());
            logger.debug("\tIssuer: " + crl.getIssuerDN());
            logger.debug("\tUpdated: " + crl.getThisUpdate());
            logger.debug("\tNext update: " + crl.getNextUpdate());
            Set certs = crl.getRevokedCertificates();
            logger.debug(
                    "\tNumber of revoked certs: "
                    + (certs != null ? certs.size() : 0));
        }

        //Get certificates
        X509Certificate[] certs = x5data.getCertificates();
        if (certs == null || certs.length <= 0) {
            logger.debug("No certificates.");
            return null;
        }
        logger.debug(certs.length + " certificate(s).");

        //Check certificates
        for (int j = 0; j < certs.length; j++) {
            boolean revoked = false;
            X509Certificate cert = certs[j];
            logger.debug("Certificate validity:");
            String validity = "OK";

            // Check validity of certificate
            try {
                cert.checkValidity();
            }
            catch (CertificateException certex) {
                validity = certex.getMessage();
                logger.error("\tCertificate in not valid.");
                throw certex;
            }

            // Log info
            logger.debug("\tCertificate validity: " + validity);

            // Check revocation status
            if (crl != null && crl.getIssuerDN().equals(cert.getIssuerDN())) {
                if (crl.getRevokedCertificate(cert.getSerialNumber())
                        != null) {
                    logger.debug("\tThis certificate was revoked!!!");
                    revoked = true;
                }
            }

            // Get key
            if (!revoked && key == null)
                key = cert.getPublicKey();
        }
        return key;
    }

    /**
     * Checks XML signature elements and attributes. Whenever an error occurred
     * a notification message would be sent to ACS (to Visa Dir in compliance test).
     * Element / Attribute existence:
     * <ul>
     * <li>Signature.xmlns</li>
     * <li>Signature.SignedInfo.Reference.URI</li>
     * <li>Signature.SignedInfo.Reference.DigestValue</li>
     * <li>Signature.SignatureValue</li>
     * <li>Signature.KeyInfo</li>
     * </ul>
     *
     * Attribute value:
     * <ul>
     * <li>Signature.xmlns</li>
     * <li>Signature.SignedInfo.xmlns</li>
     * <li>Signature.CanonicalizationMethod.xmlns</li>
     * <li>Signature.SignatureMethod.xmlns</li>
     * </ul>
     *
     * @param paresDoc The document object of PARes message
     */
    public static String checkSignature(Document paresDoc) throws Exception {
        Element root = paresDoc.getDocumentElement();
        Element child = null;
        Element e = (Element) root.getElementsByTagName("Message").item(0);

        Element paresEle =
                (Element) XMLUtil.getFirstNodeByXPath(
                        paresDoc,
                        "/ThreeDSecure/Message/PARes");
        if (paresEle == null) {
            logger.error("PARes is null. Stop signature check.");
            throw new RuntimeException("PARes is null. Stop signature check.");
        }

        String paresId = paresEle.getAttribute("id");
        if (paresId == null) {
            logger.error("PARes ID is null. Stop signature check.");
            throw new RuntimeException("PARes ID is null. Stop signature check.");
        }

        if (e == null) {
            logger.error("Message element missing.");
            throw new MessagingException(
                    "UNKNOWN",
                    ErrorCodes.ERROR_CODE_3,
                    ErrorCodes.ERROR_MESSAGE_3,
                    "Message",
                    "Signature check: Message element missing.",
                    "Signature check: Message element missing.");
        }

        String id = e.getAttribute("id");
        id = (id == null) ? "UNKNOWN" : id;

        String xmlns = null;
        e = (Element) e.getElementsByTagName("Signature").item(0);
        if (e == null) {
            logger.error("Signature element missing.");
            throw new MessagingException(
                    id,
                    ErrorCodes.ERROR_CODE_3,
                    ErrorCodes.ERROR_MESSAGE_3,
                    "Signature",
                    "Signature check: Signature element missing.",
                    "Signature check: Signature element missing.");
        }
        else {
            xmlns = e.getAttribute("xmlns");
            logger.debug("xmlns: " + xmlns);
            if (e.getAttributeNode("xmlns") == null) {
                logger.error("Signature xmlns attribute missing.");
                throw new MessagingException(
                        id,
                        ErrorCodes.ERROR_CODE_3,
                        ErrorCodes.ERROR_MESSAGE_3,
                        "Signature.xmlns",
                        "Signature check: Signature xmlns attribute element missing.",
                        "Signature check: Signature xmlns attribute element missing.");
            }
            else {
                if ((xmlns != null) && !xmlns.equals(XSignature.XMLDSIG_NAMESPACE)) {
                    logger.error("Wrong value in Signature.xmlns.");
                    throw new MessagingException(
                            id,
                            ErrorCodes.ERROR_CODE_5,
                            ErrorCodes.ERROR_MESSAGE_5,
                            "Signature.xmlns",
                            "Signature check: wrong value in Signature.xmlns. Value: "
                            + xmlns,
                            "Signature check: wrong value in Signature.xmlns. Value: "
                            + xmlns);
                }
            }
        }

        child = (Element) e.getElementsByTagName("SignatureValue").item(0);
        if (child == null) {
            logger.error("SignatureValue element missing.");
            throw new MessagingException(
                    id,
                    ErrorCodes.ERROR_CODE_3,
                    ErrorCodes.ERROR_MESSAGE_3,
                    "SignatureValue",
                    "Signature check: SignatureValue element missing.",
                    "Signature check: SignatureValue element missing.");
        }

        child = (Element) e.getElementsByTagName("KeyInfo").item(0);
        if (child == null) {
            logger.error("KeyInfo element missing.");
            throw new MessagingException(
                    id,
                    ErrorCodes.ERROR_CODE_3,
                    ErrorCodes.ERROR_MESSAGE_3,
                    "KeyInfo",
                    "Signature check: KeyInfo element missing.",
                    "Signature check: KeyInfo element missing.");
        }

        e = (Element) e.getElementsByTagName("SignedInfo").item(0);
        if (e == null) {
            logger.error("SignedInfo element missing.");
            throw new MessagingException(
                    id,
                    ErrorCodes.ERROR_CODE_3,
                    ErrorCodes.ERROR_MESSAGE_3,
                    "SignedInfo",
                    "Signature check: SignedInfo element missing.",
                    "Signature check: SignedInfo element missing.");
        }
        else {
            xmlns = e.getAttribute("xmlns");
            logger.debug("xmlns: " + xmlns);
            if ((xmlns != null) && (e.getAttributeNode("xmlns") != null)) {
                if (!xmlns.equals(XSignature.XMLDSIG_NAMESPACE)) {
                    logger.error("Wrong value in Signature.SignedInfo.xmlns.");
                    throw new MessagingException(
                            id,
                            ErrorCodes.ERROR_CODE_5,
                            ErrorCodes.ERROR_MESSAGE_5,
                            "SignedInfo.xmlns",
                            "Signature check: wrong value in SignedInfo.xmlns. Value: "
                            + xmlns,
                            "Signature check: wrong value in SignedInfo.xmlns. Value: "
                            + xmlns);
                }
            }
        }

        child =
                (Element) e.getElementsByTagName("CanonicalizationMethod").item(0);
        if (child == null) {
            logger.error("CanonicalizationMethod element missing.");
            throw new MessagingException(
                    id,
                    ErrorCodes.ERROR_CODE_3,
                    ErrorCodes.ERROR_MESSAGE_3,
                    "CanonicalizationMethod",
                    "Signature check: CanonicalizationMethod element missing.",
                    "Signature check: CanonicalizationMethod element missing.");
        }
        else {
            xmlns = child.getAttribute("xmlns");
            logger.debug("xmlns: " + xmlns);

            if ((xmlns != null) && (child.getAttributeNode("xmlns") != null)) {
                if (!xmlns.equals(XSignature.XMLDSIG_NAMESPACE)) {
                    logger.error("Wrong value in CanonicalizationMethod.xmlns.");
                    throw new MessagingException(
                            id,
                            ErrorCodes.ERROR_CODE_5,
                            ErrorCodes.ERROR_MESSAGE_5,
                            "CanonicalizationMethod.xmlns",
                            "Signature check: wrong value in CanonicalizationMethod.xmlns. Value: "
                            + xmlns,
                            "Signature check: wrong value in CanonicalizationMethod.xmlns. Value: "
                            + xmlns);
                }
            }

        }

        child = (Element) e.getElementsByTagName("SignatureMethod").item(0);
        if (child == null) {
            logger.error("SignatureMethod element missing.");
            throw new MessagingException(
                    id,
                    ErrorCodes.ERROR_CODE_3,
                    ErrorCodes.ERROR_MESSAGE_3,
                    "SignatureMethod",
                    "Signature check: SignatureMethod element missing.",
                    "Signature check: SignatureMethod element missing.");
        }
        else {
            xmlns = child.getAttribute("xmlns");
            logger.debug("xmlns: " + xmlns);

            if ((xmlns != null) && (child.getAttributeNode("xmlns") != null)) {
                if (!xmlns.equals(XSignature.XMLDSIG_NAMESPACE)) {
                    logger.error("Wrong value in SignatureMethod.xmlns.");
                    throw new MessagingException(
                            id,
                            ErrorCodes.ERROR_CODE_5,
                            ErrorCodes.ERROR_MESSAGE_5,
                            "SignatureMethod.xmlns",
                            "Signature check: wrong value in SignatureMethod.xmlns. Value: "
                            + xmlns,
                            "Signature check: wrong value in SignatureMethod.xmlns. Value: "
                            + xmlns);
                }
            }

        }

        e = (Element) e.getElementsByTagName("Reference").item(0);
        if (e == null) {
            logger.error("Reference element missing.");
            throw new MessagingException(
                    id,
                    ErrorCodes.ERROR_CODE_3,
                    ErrorCodes.ERROR_MESSAGE_3,
                    "Reference",
                    "Signature check: Reference element missing.",
                    "Signature check: Reference element missing.");
        }
        else {
            String uri = e.getAttribute("URI");
            logger.debug("uri: " + uri);

            if ((e.getAttributeNode("URI") == null) || (uri == null)) {
                logger.error("Reference URI attribute missing.");
                throw new MessagingException(
                        id,
                        ErrorCodes.ERROR_CODE_3,
                        ErrorCodes.ERROR_MESSAGE_3,
                        "Reference.URI",
                        "Signature check: Reference URI attribute element missing.",
                        "Signature check: Reference URI attribute element missing.");
            }
            else {
                //Check URI diff with PARes id
                if (!uri.equals("#" + paresId)) {
                    logger.error("Reference.URI diff with PARes.id.");
                    throw new MessagingException(
                            id,
                            ErrorCodes.ERROR_CODE_5,
                            ErrorCodes.ERROR_MESSAGE_5,
                            "Reference.URI, PARes.id",
                            "Signature check: Reference.URI diff with PARes.id.. Value: "
                            + uri
                            + "(URI), "
                            + paresId
                            + "(id).",
                            "Signature check: Reference.URI diff with PARes.id.. Value: "
                            + uri
                            + "(URI), "
                            + paresId
                            + "(id).");
                }
            }

        }

        child = (Element) e.getElementsByTagName("DigestMethod").item(0);
        if (child == null) {
            logger.error("DigestMethod element missing.");
            throw new MessagingException(
                    id,
                    ErrorCodes.ERROR_CODE_3,
                    ErrorCodes.ERROR_MESSAGE_3,
                    "DigestMethod",
                    "Signature check: DigestMethod element missing.",
                    "Signature check: DigestMethod element missing.");
        }
        else {
            xmlns = child.getAttribute("xmlns");
            logger.debug("xmlns: " + xmlns);

            if ((xmlns != null) && (child.getAttributeNode("xmlns") != null)) {
                if (!xmlns.equals(XSignature.XMLDSIG_NAMESPACE)) {
                    logger.error("Wrong value in DigestMethod.xmlns.");
                    throw new MessagingException(
                            id,
                            ErrorCodes.ERROR_CODE_5,
                            ErrorCodes.ERROR_MESSAGE_5,
                            "DigestMethod.xmlns",
                            "Signature check: wrong value in DigestMethod.xmlns. Value: "
                            + xmlns,
                            "Signature check: wrong value in DigestMethod.xmlns. Value: "
                            + xmlns);
                }
            }
        }

        child = (Element) e.getElementsByTagName("DigestValue").item(0);
        if (child == null) {
            logger.error("DigestValue element missing.");
            throw new MessagingException(
                    id,
                    ErrorCodes.ERROR_CODE_3,
                    ErrorCodes.ERROR_MESSAGE_3,
                    "DigestValue",
                    "Signature check: DigestValue element missing.",
                    "Signature check: DigestValue element missing.");
        }

        logger.info("Signature check finished. No error found.");
        return id;
    }
}