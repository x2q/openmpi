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

package com.oncecorp.visa3d.mpi.intf.payment;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.Message;
import com.oncecorp.visa3d.mpi.messaging.MessageValidator;
import com.oncecorp.visa3d.mpi.messaging.MessagingException;
import com.oncecorp.visa3d.mpi.utility.XMLUtil;

/**
 * Implement message class for MPIError. The MPIError 
 * message shall be returned to caller when an internal exception 
 * occured in MPI Component computing or recieved an ErrorMessage
 * back from Visa Directory after sending VEReq  
 *  
 * @version $Revision: 12 $
 * @author	ONCEmpi Engineering Team
 */

public class MPIErrorMessage extends Message {

	// Message type
	public final static String MSG_TYPE = "MPIError";

	// Message version
	/* 
	 * [Martin's Note: Dec 12, 2002 1:06:27 PM]
	 * 
	 * Moved to version 1.1 as per new interface definition
	 */
	// public final static String MSG_VERSION = "1.0";
	public final static String MSG_VERSION = "1.1";

	/**
	 * MPIError Message Fields
	 */
	private String merchantID; //Mandatory
	private String errorCode; // Mandatory
	private String errorMessage; // Mandatory
	private String errorDetail; // Optional
	private String vendorCode; // Optional

	/**
	 * XML message structure
	 */
	private final static String MPIERROR_XML_STRING =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<MPI_Interface>"
			+ "<MPIError>"
			+ "<version/>"
			+ "<merchantID/>"
			+ "<errorCode/>"
			+ "<errorMessage/>"
			+ "</MPIError>"
			+ "</MPI_Interface>";

	// Local Log4J logger
	private Logger logger =
		MPILogger.getLogger(MPIErrorMessage.class.getName());

	/** Constructor: set type and version for this object */
	public MPIErrorMessage() {
		//Set message type
		setType(MSG_TYPE);

		//Set messsage version
		setVersion(MSG_VERSION);
	}

	public MPIErrorMessage(
		String anId,
		String aCode,
		String aMsg,
		String aDetail,
		String aVendorCode) {
		this();
		this.id = anId;
		this.errorCode = aCode;
		this.errorMessage = aMsg;
		this.errorDetail = aDetail;
		this.vendorCode = aVendorCode;
	}

	/** 
	 * [Getter and Setter methods]
	 * [Setters] validates MPIError message fields to make sure all mandatory 
	 * fields presented and with correct sizes. A MessagingException will be 
	 * thrown in case any invalid field exists.
	 */

	// override super class to do id validation for this msg
	public void setId(String id) throws MessagingException {
		// validating id
		MessageValidator.validateField(
			"UNKNOWN",
			id,
			"[MPIError.id]",
			1,
			-1,
			true);
		super.setId(id);
	}

	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) throws MessagingException {
		// validating errorCode
		MessageValidator.validateField(
			getId(),
			errorCode,
			"[MPIError.errorCode]",
			1,
			-1,
			true);
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage)
		throws MessagingException {
		// validating errorMessage 
		MessageValidator.validateField(
			getId(),
			errorMessage,
			"[MPIError.errorMessage]",
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
			errorMessage,
			"[MPIError.errorDetail]",
			1,
			-1,
			false);
		this.errorDetail = errorDetail;
	}

	public String getVendorCode() {
		return vendorCode;
	}
	public void setVendorCode(String vendorCode) throws MessagingException {
		// validating vendorCode (Optional) 
		MessageValidator.validateField(
			getId(),
			vendorCode,
			"[MPIError.vendorCode]",
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
			getId(),
			merchantID,
			"[MPIError.merchantID]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			errorMessage,
			"[MPIError.id]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			errorCode,
			"[MPIError.errorCode]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			errorMessage,
			"[MPIError.errorMessage]",
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
			//Create document template
			Document dom = XMLUtil.createDocument(MPIERROR_XML_STRING, false);

			//Sanity check
			if (dom == null) {
				logger.error(
					"[MPIErrorMessage.toXML] Failed to create document template.");
				return null;
			}

			//Shared attributes
			Element element;

			//Add message id attribute (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/MPIError");
			element.setAttribute("id", getId());

			//Set version (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/MPIError/version");
			element.appendChild(dom.createTextNode(getVersion()));

			//Set merchantID (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/MPIError/merchantID");
			element.appendChild(dom.createTextNode(getMerchantID()));

			//Set errorCode (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/MPIError/errorCode");
			element.appendChild(dom.createTextNode(getErrorCode()));

			//Set errorMessage (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/MPIError/errorMessage");
			element.appendChild(dom.createTextNode(getErrorMessage()));

			//Set errorDetail (Optional)
			if ((getErrorDetail() != null)
				&& (getErrorDetail().length() > 0)) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/MPI_Interface/MPIError");
				Element child = dom.createElement("errorDetail");
				child.appendChild(dom.createTextNode(getErrorDetail()));
				element.appendChild(child);
			}

			//Set vendorCode (Optional)
			if ((getVendorCode() != null) && (getVendorCode().length() > 0)) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/MPI_Interface/MPIError");
				Element child = dom.createElement("vendorCode");
				child.appendChild(dom.createTextNode(getVendorCode()));
				element.appendChild(child);
			}

			// do logging
			logger.debug("[MPIErrorMessage.toXML] completed!");
			return dom;
		} catch (Exception e) {
			logger.error(
				"Failed to convert MPIErrorMessage message to DOM document.",
				e);
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
					+ "<MPI_Interface>"
					+ "<MPIError id=\"");

			sb.append(XMLUtil.filterSpecialChars(getId()));
			sb.append("\"><version>");
			sb.append(XMLUtil.filterSpecialChars(getVersion()));
			sb.append("</version><merchantID>");
			sb.append(XMLUtil.filterSpecialChars(getMerchantID()));
			sb.append("</merchantID><errorCode>");
			sb.append(XMLUtil.filterSpecialChars(getErrorCode()));
			sb.append("</errorCode><errorMessage>");
			sb.append(XMLUtil.filterSpecialChars(getErrorMessage()));
			sb.append("</errorMessage>");

			// add errorDetail (Optional)
			if ((getErrorDetail() != null)
				&& (getErrorDetail().length() > 0)) {
				sb.append("<errorDetail>");
				sb.append(XMLUtil.filterSpecialChars(getErrorDetail()));
				sb.append("</errorDetail>");
			}

			// add vendorCode (Optional)
			if ((getVendorCode() != null) && (getVendorCode().length() > 0)) {
				sb.append("<vendorCode>");
				sb.append(XMLUtil.filterSpecialChars(getVendorCode()));
				sb.append("</vendorCode>");
			}

			sb.append("</MPIError></MPI_Interface>");

			return sb.toString();
		} catch (Exception e) {
			logger.error(
				"Failed to convert MPIError message to XML string.",
				e);
			return null;
		}

	}

	/**
	 * Method setAll.
	 * @param mexcep
	 */
	public void setAll(MessagingException mexcep) {
		try {
			setId((mexcep.getId() == null) ? "UNKNOWN" : mexcep.getId());
			setErrorCode(mexcep.getErrorCode());
			setErrorMessage(mexcep.getErrorMsg());
			setErrorDetail(mexcep.getErrorDetail());
			setVendorCode(mexcep.getVendorCode());
		} catch (MessagingException e) {
			// If we get here, that means that some values in the 
			// MessagingException passed as a parameter do not properly
			// validate against the setters.....So we take that NEW
			// MessagingException (in this current catch block) and initialize
			// the MPIErrorMessage with these attributes, hoping that this
			// time MessagingException is OK.
			// If not then we have an endless loop!!!!! and we are fleeing to an 
			// isolated island....
			setAll(e);
		}
	}
	/**
	 * Returns the merchantID.
	 * @return String
	 */
	public String getMerchantID() {
		return merchantID;
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
			"[MPIError.merchantID]",
			1,
			24,
			true);
		this.merchantID = merchantID;
	}

}
