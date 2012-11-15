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

package com.oncecorp.visa3d.mpi.domain.payment;

import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.Extension;
import com.oncecorp.visa3d.mpi.messaging.Message;
import com.oncecorp.visa3d.mpi.messaging.MessageValidator;
import com.oncecorp.visa3d.mpi.messaging.MessagingException;
import com.oncecorp.visa3d.mpi.utility.XMLUtil;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of Visa 3-D CRReq message.
 * The CRReq (Card Range Request) message is sent from the Merchant Plug-in (MPI) 
 * to the Visa Directory to request the list of participationg card ranges in order 
 * to update the MPI's internal cache information.
 * 
 * @version $Revision: 11 $
 * @author	Alan Zhang
 */
public class CRReqMessage extends Message {
	/**
	 * Message Type
	 */
	public static final String MSG_TYPE = "CRReq";

	/**
	 * Message Version
	 */
	public static final String MSG_VERSION = "1.0.2";

	/** 
	 * XML Message Structure 
	 */
	private static String CRREQ_XML_STRING =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<ThreeDSecure>"
			+ "<Message>"
			+ "<CRReq>"
			+ "<version/>"
			+ "<Merchant>"
			+ "<acqBIN/>"
			+ "<merID/>"
			+ "<password/>"
			+ "</Merchant>"
			+ "</CRReq>"
			+ "</Message>"
			+ "</ThreeDSecure>";

	// Define the constant error messages. Will facilitate future i18n tasks 
	private final static String CRITICAL_ERRMSG =
		"Critical extension not supported";
	private final static String CRITICAL_ERRMSG_LONG =
		"Invalid field: [CRReq.Extension] - " + CRITICAL_ERRMSG;

	/**
	 * Local Log4J logger
	 */
	private transient Logger logger =
		MPILogger.getLogger(CRReqMessage.class.getName());

	/**
	 * CRReq Message Fields
	 */
	private String merchantAcqBIN;
	private String merchantID;
	private String merchantPassword;
	private String serialNumber;
	private Extension[] extension; // Optional	

	/**
	 * constructor
	 */
	public CRReqMessage() throws MessagingException {
		setType(MSG_TYPE);
		setVersion(MSG_VERSION);
	}

	/**
	 * Convert message into XML Document in conformance with Visa 3-D DTD
	 * 
	 * @return The document
	 */
	public Document toXML() {
		try {
			//Create document template
			Document dom = XMLUtil.createDocument(CRREQ_XML_STRING, false);

			//Sanity check
			if (dom == null) {
				this.logger.error(
					"Failed to create document template for CRReq message.");
				return null;
			}

			//Shared attributes
			Element element;

			//Add message id attribute (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/ThreeDSecure/Message");
			element.setAttribute("id", getId());

			//Set version (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/ThreeDSecure/Message/CRReq/version");
			element.appendChild(dom.createTextNode(getVersion()));

			//Set Merchant acquirer BIN (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/ThreeDSecure/Message/CRReq/Merchant/acqBIN");
			element.appendChild(dom.createTextNode(getMerchantAcqBIN()));

			//Set Merchant ID (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/ThreeDSecure/Message/CRReq/Merchant/merID");
			element.appendChild(dom.createTextNode(getMerchantID()));

			//Set Merchant Password (Optional)
			if (getMerchantPassword() != null && getMerchantPassword().length() != 0) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/ThreeDSecure/Message/CRReq/Merchant");
				Element child = dom.createElement("password");
				child.appendChild(dom.createTextNode(getMerchantPassword()));
				element.appendChild(child);
			}

			//Set serial number (Optional)
			if ((getSerialNumber() != null)
				&& (getSerialNumber().length() != 0)) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/ThreeDSecure/Message/CRReq");
				Element sn = dom.createElement("serialNumber");
				sn.appendChild(dom.createTextNode(getSerialNumber()));
				element.appendChild(sn);
			}

			return dom;
		} catch (Exception e) {
			this.logger.error(
				"Failed to convert CRReq message to DOM document.",
				e);
			return null;
		}
	}

	/**
	 * Represents this message as XML string
	 * 
	 * @return The XML String
	 */
	public String toString() {
		try {
			StringBuffer sb = new StringBuffer();

			sb.append(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ "<ThreeDSecure>"
					+ "<Message id=\"");

			sb.append(XMLUtil.filterSpecialChars(getId()));
			sb.append("\"><CRReq><version>");
			sb.append(XMLUtil.filterSpecialChars(getVersion()));
			sb.append("</version><Merchant><acqBIN>");
			sb.append(XMLUtil.filterSpecialChars(getMerchantAcqBIN()));
			sb.append("</acqBIN><merID>");
			sb.append(XMLUtil.filterSpecialChars(getMerchantID()));
			sb.append("</merID>");
			
			// Add password if specified (optional)
			if (isNotNull(getMerchantPassword())) {
				sb.append("<password>");
				sb.append(XMLUtil.filterSpecialChars(getMerchantPassword()));
				sb.append("</password>");
			}	
			
			sb.append("</Merchant>");

			// add serial number (Optional)
			if ((getSerialNumber() != null)
				&& (getSerialNumber().length() != 0)) {
				sb.append("<serialNumber>");
				sb.append(XMLUtil.filterSpecialChars(getSerialNumber()));
				sb.append("</serialNumber>");
			}

			sb.append("</CRReq></Message></ThreeDSecure>");

			return sb.toString();
		} catch (Exception e) {
			this.logger.error(
				"Failed to convert CRReq message to XML string.",
				e);
			return null;
		}
	}

	/**
	 * Validate message attributes against Visa 3-D DTD definition.
	 * 
	 * @return 	True if valid
	 * @throws	Exception containing validation error information
	 */
	public boolean validate() throws MessagingException {
		logger.debug("Validating current CRReq message...");

		//Check all the mandatory field existence
		MessageValidator.validateField(
			"UNKNOWN",
			getId(),
			"[Message.id]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getMerchantAcqBIN(),
			"[CRReq.Merchant.acqBIN]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getMerchantID(),
			"[CRReq.Merchant.merchantID]",
			1,
			-1,
			true);
		/* 
		 * [Martin's Note: Feb 27, 2003 4:31:26 PM]
		 * Password is no longer a mandatory field for this message
		 * 
		 */
//		MessageValidator.validateField(
//			getId(),
//			getMerchantPassword(),
//			"[CRReq.Merchant.password]",
//			1,
//			-1,
//			false);

		// Validate optional extension part
		if ((extension != null) && (extension.length > 0)) {
			for (int i = 0; i < extension.length; i++) {
				// Iterate over all extension and throw an exception for any 
				// critical extension that we do not support
				if (!Extension
					.isSupported(getType(), getVersion(), extension[i])) {
					throw new MessagingException(
						getId(),
						ErrorCodes.ERROR_CODE_4,
						ErrorCodes.ERROR_MESSAGE_4,
						"[CRReq.Extension]",
						CRITICAL_ERRMSG,
						CRITICAL_ERRMSG_LONG);
				}
			}
		}
		logger.debug("CRReq is valid.");
		return true;
	}

	/**
	 * Sets the id. Overrides super setId()
	 * @param in The id to set
	 */
	public void setId(String in) throws MessagingException {
		//Check Field: id
		MessageValidator.validateField(
			"UNKNOWN",
			in,
			"[Message.id]",
			1,
			30,
			true);
		super.setId(in);
	}

	/**
	 * Returns the merchantAcqBIN.
	 * @return String
	 */
	public String getMerchantAcqBIN() {
		return merchantAcqBIN;
	}

	/**
	 * Returns the merchantID.
	 * @return String
	 */
	public String getMerchantID() {
		return merchantID;
	}

	/**
	 * Returns the merchantPassword.
	 * @return String
	 */
	public String getMerchantPassword() {
		return merchantPassword;
	}

	/**
	 * Returns the serialNumber.
	 * @return String
	 */
	public String getSerialNumber() {
		return serialNumber;
	}

	/**
	 * Sets the merchantAcqBIN.
	 * @param merchantAcqBIN The merchantAcqBIN to set
	 */
	public void setMerchantAcqBIN(String merchantAcqBIN)
		throws MessagingException {
		//Check Field: Acquirer BIN
		MessageValidator.validateField(
			getId(),
			merchantAcqBIN,
			"[CRReq.Merchant.acqBIN]",
			1,
			11,
			true);
		this.merchantAcqBIN = merchantAcqBIN;
	}

	/**
	 * Sets the merchantID.
	 * @param merchantID The merchantID to set
	 */
	public void setMerchantID(String merchantID) throws MessagingException {
		//Check Field: Merchant ID
		MessageValidator.validateField(
			getId(),
			merchantID,
			"[CRReq.Merchant.merID]",
			1,
			24,
			true);
		this.merchantID = merchantID;
	}

	/**
	 * Sets the merchantPassword.
	 * @param merchantPassword The merchantPassword to set
	 */
	public void setMerchantPassword(String merchantPassword)
		throws MessagingException {
		//Check filed: password
		MessageValidator.validateField(
			getId(),
			merchantPassword,
			"[CRReq.Merchant.password]",
			8,
			8,
			false);
		this.merchantPassword = merchantPassword;
	}

	/**
	 * Sets the serialNumber.
	 * @param serialNumber The serialNumber to set
	 */
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	/**
	 * Returns the extension.
	 * @return Extension[]
	 */
	public Extension[] getExtension() {
		return extension;
	}

	/**
	 * Sets the extension.
	 * @param extension The extension to set
	 */
	public void setExtension(Extension[] extension) {
		this.extension = extension;
	}
	
	private boolean isNotNull(String in) {
		if ((in == null) || (in.length() == 0))
			return false;
		else
			return true;
	}
}
