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
import com.oncecorp.visa3d.mpi.messaging.Message;
import com.oncecorp.visa3d.mpi.messaging.MessageValidator;
import com.oncecorp.visa3d.mpi.messaging.MessagingException;
import com.oncecorp.visa3d.mpi.utility.XMLUtil;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Description: Implement message class for Visa Error. The Error message
 * shall be returned when the incoming request or response cannot be 
 * successfully processed at a protocol level (such as bad XML)
 *  
 * @version $Revision: 9 $
 * @author	Jun Shi
 */

public class ErrorMessage extends Message {

	// Message type
	public final static String MSG_TYPE = "Error";

	// Message version
	public final static String MSG_VERSION = "1.0.2";

	/**
	 * XML Message structure
	 */
	private static final String ERROR_XML_STRING =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<ThreeDSecure>"
			+ "<Message>"
			+ "<Error>"
			+ "<version/>"
			+ "<errorCode/>"
			+ "<errorMessage/>"
			+ "<errorDetail/>"
			+ "</Error>"
			+ "</Message>"
			+ "</ThreeDSecure>";

	/**
	 * Error Message Fields
	 */
	private String errorCode; // Mandatory
	private String errorMessage; // Mandatory
	private String errorDetail; // Mandatory
	private String vendorCode; // Optional

	// Constant error messages
	private static final String CODE_ERRMSG =
		"Null, size is 0 or size exceeds 2.";
	private static final String CODE_ERRMSG_LONG =
		"Invalid field: [Error.errorCode] - " + CODE_ERRMSG;

	private static final String VCODE_ERRMSG = "Size exceeds 256.";
	private static final String VCODE_ERRMSG_LONG =
		"Invalid field: [Error.vendorCode] - " + VCODE_ERRMSG;

	private static final String[] ERROR_CODE_LIST =
		new String[] { "1", "2", "3", "4", "5", "6", "98", "99" };

	// Local Log4J logger
	private Logger logger = MPILogger.getLogger(ErrorMessage.class.getName());

	/** Constructor: set type and version for this object */
	public ErrorMessage() throws MessagingException {
		//Set message type
		setType(MSG_TYPE);

		//Set messsage version
		setVersion(MSG_VERSION);
	}

	/** 
	 * [Getter and Setter methods]
	 * [Setters] validates Error message fields to make sure all mandatory 
	 * fields presented and with correct sizes. A MessagingException will be 
	 * thrown in case any invalid field exists.
	 */

	// override super class to do id validation for this msg
	public void setId(String id) throws MessagingException {
		/** validate input id */
		MessageValidator.validateField(
			"UNKNOWN",
			id,
			"[Message.id]",
			1,
			30,
			true);
		super.setId(id);
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) throws MessagingException {
		/*
		 * [Alan's Note: Nov 6, 2002]
		 * No action taken if received invalid Error code
		if ((errorCode == null)
			|| (errorCode.length() == 0)
			|| (errorCode.length() > 2)
			|| isNotInList(errorCode)) {
			logger.error(CODE_ERRMSG_LONG + " Value: {" + errorCode + "}");
			throw new MessagingException(
				getId(),
				ErrorCodes.ERROR_CODE_5,
				ErrorCodes.ERROR_MESSAGE_5,
				"[Error.errorCode]",
				CODE_ERRMSG,
				CODE_ERRMSG_LONG + " Value: {" + errorCode + "}");
		}
		*/

		this.errorCode = errorCode;
	}

	private boolean isNotInList(String code) {
		for (int i = 0; i < ERROR_CODE_LIST.length; i++) {
			if (code.equals(ERROR_CODE_LIST[i]))
				return false;
		}

		logger.error("Error code is not in the code list: " + code);
		return true;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage)
		throws MessagingException {
		MessageValidator.validateField(
			getId(),
			errorMessage,
			"[Error.errorCode]",
			1,
			-1,
			true);
		this.errorMessage = errorMessage;
	}
	public String getErrorDetail() {
		return errorDetail;
	}
	public void setErrorDetail(String errorDetail) throws MessagingException {
		MessageValidator.validateField(
			getId(),
			errorDetail,
			"[Error.errorDetail]",
			1,
			-1,
			true);
		this.errorDetail = errorDetail;
	}
	public String getVendorCode() {
		return vendorCode;
	}
	public void setVendorCode(String vendorCode) throws MessagingException {
		MessageValidator.validateField(
			getId(),
			vendorCode,
			"[Error.errorDetail]",
			1,
			256,
			false);
		this.vendorCode = vendorCode;
	}

	/**
	 * Overrides super class validate()
	 */
	public boolean validate() throws MessagingException {
		//Mandatory field check
		MessageValidator.validateField(
			"UNKNOWN",
			getId(),
			"[Message.id]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getErrorCode(),
			"[Error.errorCode]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getErrorMessage(),
			"[Error.errorMessage]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getErrorDetail(),
			"[Error.errorDetail]",
			1,
			-1,
			true);
		return true;
	}

	/**
	 * XMLSerializable interface method: toXML()
	 * This method consturcts a DOM document which represents this message.
	 *
	 * @return DOM document object
	 */
	public Document toXML() {
		try {

			//Create document template with no validation
			Document dom = XMLUtil.createDocument(ERROR_XML_STRING, false);

			//Sanity check
			if (dom == null) {
				logger.error("Failed to create document template.");
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
					"/ThreeDSecure/Message/Error/version");
			element.appendChild(dom.createTextNode(getVersion()));

			//Set errorCode (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/ThreeDSecure/Message/Error/errorCode");
			element.appendChild(dom.createTextNode(getErrorCode()));

			//Set errorMessage (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/ThreeDSecure/Message/Error/errorMessage");
			element.appendChild(dom.createTextNode(getErrorMessage()));

			//Set errorDetail (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/ThreeDSecure/Message/Error/errorDetail");
			element.appendChild(dom.createTextNode(getErrorDetail()));

			//Set vendorCode (Optional)
			if ((getVendorCode() != null) && (getVendorCode().length() != 0)) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/ThreeDSecure/Message/Error");
				Element child = dom.createElement("vendorCode");
				child.appendChild(dom.createTextNode(getVendorCode()));
				element.appendChild(child);
			}

			logger.debug("[ErrorMessage.toXML] completed!");
			return dom;
		} catch (Exception e) {
			logger.error("Failed to convert Error message to DOM document.", e);
			return null;
		}

	}

	/**
	 * Represents this message as XML string
	 *
	 * @return The XML string
	 */
	public String toString() {
		try {
			StringBuffer sb = new StringBuffer();

			sb.append(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ "<ThreeDSecure>"
					+ "<Message id=\"");

			sb.append(XMLUtil.filterSpecialChars(getId()));
			sb.append("\"><Error><version>");
			sb.append(XMLUtil.filterSpecialChars(getVersion()));
			sb.append("</version><errorCode>");
			sb.append(XMLUtil.filterSpecialChars(getErrorCode()));
			sb.append("</errorCode><errorMessage>");
			sb.append(XMLUtil.filterSpecialChars(getErrorMessage()));
			sb.append("</errorMessage><errorDetail>");
			sb.append(XMLUtil.filterSpecialChars(getErrorDetail()));
			sb.append("</errorDetail>");

			// add vendorCode (Optional)
			if ((getVendorCode() != null) && (getVendorCode().length() != 0)) {
				sb.append("<vendorCode>");
				sb.append(XMLUtil.filterSpecialChars(getVendorCode()));
				sb.append("</vendorCode>");
			}

			sb.append("</Error></Message></ThreeDSecure>");

			return sb.toString();
		} catch (Exception e) {
			logger.error("Failed to convert Error message to XML string.", e);
			return null;
		}

	}
}
