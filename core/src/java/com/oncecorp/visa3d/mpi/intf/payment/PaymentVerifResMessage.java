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

import com.oncecorp.visa3d.mpi.domain.payment.ErrorCodes;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.Message;
import com.oncecorp.visa3d.mpi.messaging.MessageValidator;
import com.oncecorp.visa3d.mpi.messaging.MessagingException;
import com.oncecorp.visa3d.mpi.utility.XMLUtil;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Description: Implement message class for PaymentVerifRes.
 * If VERes - enrolled is "Y", the PaymentVerifRes message is sent
 * back to called with <HTMLForm> encapsulated PAReqMessage after 
 * it is created by MPI. On the other hand, it will return iReqCode
 * and iReqDetails included in VEResMessage if enrolled is marked "N";
 * In case of enrolled is equals to "U",this message just set enrolled
 * field to "U" and return, no further information included
 *  
 * @version $Revision: 8 $
 * @author	Jun Shi
 */

public class PaymentVerifResMessage extends Message {

	// Message type
	public final static String MSG_TYPE = "PaymentVerifRes";

	/**
	 * Message Version
	 */
	// public final static String MSG_VERSION = "1.0";
	/* 
	 * [Martin's Note: Dec 12, 2002 1:06:27 PM]
	 * 
	 * Moved to version 1.1 as per new interface definition
	 */
	public final static String MSG_VERSION = "1.1";

	/**
	 * PaymentVerifRes Message Fields
	 */
	private String enrolled; // Mandatory
	private String merchantID; //Mandatory
	private String invalidReq; // Conditional
	private String invalidDetails; // Optional
	private String vendorCode; // Optional
	private String htmlForm; // Conditional

	/**
	 * XML message structure
	 */
	private final static String PAYMENTVERIFRES_XML_STRING =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<MPI_Interface>"
			+ "<PaymentVerifRes>"
			+ "<version/>"
			+ "<merchantID/>"
			+ "<enrolled/>"
			+ "</PaymentVerifRes>"
			+ "</MPI_Interface>";

	// Error messages
	private static final String ENRL_ERRMSG =
		"Null, size is 0 or >1 or not [Y/N/U] ";
	private static final String ENRL_ERRMSG_LONG =
		"Invalid field: [PaymentVerifRes.enrolled] - " + ENRL_ERRMSG;

	private static final String ENROLLED1_ERRMSG =
		"[HTMLForm] should not be null if [enrolled] = 'Y'";
	private static final String ENROLLED1_ERRMSG_LONG =
		"Invalid message: [PaymentVerifReq] - " + ENROLLED1_ERRMSG;

	private static final String ENROLLED2_ERRMSG =
		"[HTMLForm] should be null if [enrolled] = 'N'";
	private static final String ENROLLED2_ERRMSG_LONG =
		"Invalid message: [PaymentVerifReq] - " + ENROLLED1_ERRMSG;

	private static final String VCODE1_ERRMSG =
		"[InvalidDetails] is not empty but no value specified in [InvalidReq]";
	private static final String VCODE1_ERRMSG_LONG =
		"Invalid message: [PaymentVerifReq] - " + VCODE1_ERRMSG;

	private static final String VCODE2_ERRMSG =
		"[vendorCode] is not empty but no value specified in [InvalidReq]";
	private static final String VCODE2_ERRMSG_LONG =
		"Invalid message: [PaymentVerifReq] - " + VCODE2_ERRMSG;

	// Local Log4J logger
	private static Logger logger =
		MPILogger.getLogger(PaymentVerifResMessage.class.getName());

	/** Constructor: set type and version for this object */
	public PaymentVerifResMessage() throws MessagingException {
		//Set message type
		setType(MSG_TYPE);

		//Set messsage version
		setVersion(MSG_VERSION);
	}

	/** 
	 * [Getter and Setter methods]
	 * [Setters] validates PaymentVerifResMessage fields to make sure all 
	 * mandatory fields presented and with correct sizes. 
	 * A MessagingException will be thrown in case any invalid field exists.
	 */

	// override super class to do id validation for this msg
	public void setId(String id) throws MessagingException {
		// validating id 
		MessageValidator.validateField(
			"UNKNOWN",
			id,
			"[Message.id]",
			1,
			-1,
			true);
		super.setId(id);
	}

	public String getEnrolled() {
		return enrolled;
	}

	public void setEnrolled(String enrolled) throws MessagingException {
		// Check Field: enrolled 
		if (((enrolled == null)
			|| (enrolled.length() == 0)
			|| (enrolled.length() > 1))
			|| (!enrolled.equalsIgnoreCase("Y")
				&& !enrolled.equalsIgnoreCase("N")
				&& !enrolled.equalsIgnoreCase("U"))) {
			logger.error(ENRL_ERRMSG_LONG + " Value: {" + enrolled + "}");
			throw new MessagingException(
				getId(),
				ErrorCodes.ERROR_CODE_5,
				ErrorCodes.ERROR_MESSAGE_5,
				"[PaymentVerifRes.enrolled]",
				ENRL_ERRMSG,
				ENRL_ERRMSG_LONG + " Value: {" + enrolled + "}");
		}

		this.enrolled = enrolled;
	}
	public String getInvalidReq() {
		return invalidReq;
	}
	public void setInvalidReq(String invalidReq) throws MessagingException {
		this.invalidReq = invalidReq; // checked in validate()
	}
	public String getInvalidDetails() {
		return invalidDetails;
	}
	public void setInvalidDetails(String invalidDetails)
		throws MessagingException {
		this.invalidDetails = invalidDetails; // checked in validate()
	}
	public String getVendorCode() {
		return vendorCode;
	}
	public void setVendorCode(String vendorCode) throws MessagingException {
		this.vendorCode = vendorCode; // checked in validate()
	}
	public String getHtmlForm() {
		return htmlForm;
	}
	public void setHtmlForm(String htmlForm) throws MessagingException {
		this.htmlForm = htmlForm; // checked in validate()
	}

	/**
	* Overrides super class validate()
	*/
	public boolean validate() throws MessagingException {
		//Mandatory field check
		MessageValidator.validateField(
			"UNKNOWN",
			getId(),
			"[PaymentVerifRes:Message.id]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getEnrolled(),
			"[PaymentVerifRes.enrolled]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getMerchantID(),
			"[PaymentVerifRes.merchantID]",
			1,
			-1,
			true);
		//Mandatory field check END

		//Check Field: htmlForm
		if (getEnrolled().equalsIgnoreCase("Y")) {
			if ((getHtmlForm() == null || getHtmlForm().length() == 0)
				|| (getInvalidReq() != null && getInvalidReq().length() != 0)) {
				logger.error(ENROLLED1_ERRMSG_LONG);
				throw new MessagingException(
					getId(),
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"[PaymentVerifRes.enrolled, PaymentVerifRes.htmlForm]",
					ENROLLED1_ERRMSG,
					ENROLLED1_ERRMSG_LONG);
			}
		}
		if (getEnrolled().equalsIgnoreCase("N")) {
			if (getHtmlForm() != null && getHtmlForm().length() != 0) {
				logger.error(ENROLLED2_ERRMSG_LONG);
				throw new MessagingException(
					getId(),
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"[PaymentVerifRes.enrolled, PaymentVerifRes.htmlForm]",
					ENROLLED2_ERRMSG,
					ENROLLED2_ERRMSG_LONG);
			}
		}

		//Check Field: invalidReq (if invalidReq is not null, enrolled must be "N")
		if (getEnrolled().equalsIgnoreCase("N")) {
			// if enrolled = "N", invalidReq still can be null unless IReq is not null !!
			MessageValidator.validateDigitField(
				getId(),
				getInvalidReq(),
				"[PaymentVerifRes.invalidReq]",
				1,
				3,
				false);
		}

		//Check Field: invalidDetails
		if ((getInvalidReq() == null || getInvalidReq().length() == 0)
			&& (getInvalidDetails() != null
				&& getInvalidDetails().length() != 0)) {
			logger.error(VCODE1_ERRMSG_LONG);
			throw new MessagingException(
				getId(),
				ErrorCodes.ERROR_CODE_5,
				ErrorCodes.ERROR_MESSAGE_5,
				"[PaymentVerifRes.invalidReq, PaymentVerifRes.invalidDetails]",
				VCODE1_ERRMSG,
				VCODE1_ERRMSG_LONG);
		}

		//Check Field: vendorCode
		if ((getInvalidReq() == null || getInvalidReq().length() == 0)
			&& (getVendorCode() != null && getVendorCode().length() != 0)) {
			logger.error(VCODE2_ERRMSG_LONG);
			throw new MessagingException(
				getId(),
				ErrorCodes.ERROR_CODE_5,
				ErrorCodes.ERROR_MESSAGE_5,
				"[PaymentVerifRes.invalidReq, PaymentVerifRes.vendorCode]",
				VCODE2_ERRMSG,
				VCODE2_ERRMSG_LONG);
		}

		if (getEnrolled().equalsIgnoreCase("N")
			&& (getInvalidReq() != null && getInvalidReq().length() != 0)) {
			// if enrolled = "N" and InvalidReq is not null, vendorCode is still Optional 
			MessageValidator.validateField(
				getId(),
				getVendorCode(),
				"[PaymentVerifRes.vendorCode]",
				1,
				256,
				false);
		}

		return true;
	}

	/**
	 * XMLSerializable interface method: toXML()
	 * This method consturcts a DOM document which represents this message.
	 
	 * @return DOM document object
	 */
	public Document toXML() {
		try {
			//Create document template
			Document dom =
				XMLUtil.createDocument(PAYMENTVERIFRES_XML_STRING, false);

			//Sanity check
			if (dom == null) {
				logger.error(
					"[PaymentVerifResMessage.toXML] Failed to create document template.");
				return null;
			}

			//Shared attributes
			Element element;

			//Add message id attribute (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/PaymentVerifRes");
			element.setAttribute("id", getId());

			//Set version
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/PaymentVerifRes/version");
			element.appendChild(dom.createTextNode(getVersion()));

			//Set merchantID
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/PaymentVerifRes/merchantID");
			element.appendChild(dom.createTextNode(getMerchantID()));

			//Set enrolled
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/PaymentVerifRes/enrolled");
			element.appendChild(dom.createTextNode(getEnrolled()));

			//Set invalidReq
			if (getEnrolled().equalsIgnoreCase("N")) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/MPI_Interface/PaymentVerifRes");
				Element child = dom.createElement("invalidReq");

				if (getInvalidReq() != null && getInvalidReq().length() != 0) {
					child.appendChild(dom.createTextNode(getInvalidReq()));
				} else {
					// Note: "set value - 00" mean VERes has no iReqCode element
					child.appendChild(dom.createTextNode("00"));
				}
				element.appendChild(child);
			}

			//Set invalidDetails
			if (getInvalidDetails() != null
				&& getInvalidDetails().length() != 0) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/MPI_Interface/PaymentVerifRes");
				Element child = dom.createElement("invalidDetails");
				child.appendChild(dom.createTextNode(getInvalidDetails()));
				element.appendChild(child);
			}

			//Set vendorCode
			if (getVendorCode() != null && getVendorCode().length() != 0) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/MPI_Interface/PaymentVerifRes");
				Element child = dom.createElement("vendorCode");
				child.appendChild(dom.createTextNode(getVendorCode()));
				element.appendChild(child);
			}

			//Set htmlForm (Conditional)
			if (getEnrolled().equalsIgnoreCase("Y")) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/MPI_Interface/PaymentVerifRes");
				Element child = dom.createElement("htmlForm");
				child.appendChild(dom.createTextNode(getHtmlForm()));
				element.appendChild(child);
			}

			// do logging
			logger.debug("[PaymentVerifResMessage.toXML] completed!");
			return dom;
		} catch (Exception e) {
			logger.error(
				"Failed to convert PaymentVerifRes message to DOM document.",
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
					+ "<PaymentVerifRes id=\"");

			sb.append(XMLUtil.filterSpecialChars(getId()));
			sb.append("\"><version>");
			sb.append(XMLUtil.filterSpecialChars(getVersion()));
			sb.append("</version><merchantID>");
			sb.append(XMLUtil.filterSpecialChars(getMerchantID()));
			sb.append("</merchantID><enrolled>");
			sb.append(XMLUtil.filterSpecialChars(getEnrolled()));
			sb.append("</enrolled>");

			// add invalidReq
			if (getEnrolled().equalsIgnoreCase("N")) {
				sb.append("<invalidReq>");
				if (getInvalidReq() != null && getInvalidReq().length() != 0) {
					sb.append(XMLUtil.filterSpecialChars(getInvalidReq()));
				} else {
					// Note: "set value - 00" mean VERes has no iReqCode element
					sb.append("00");
				}
				sb.append("</invalidReq>");
			}

			// add invalidDetails
			if (getInvalidDetails() != null
				&& getInvalidDetails().length() != 0) {
				sb.append("<invalidDetails>");
				sb.append(XMLUtil.filterSpecialChars(getInvalidDetails()));
				sb.append("</invalidDetails>");
			}

			//Set vendorCode
			if (getVendorCode() != null && getVendorCode().length() != 0) {
				sb.append("<vendorCode>");
				sb.append(XMLUtil.filterSpecialChars(getVendorCode()));
				sb.append("</vendorCode>");
			}

			// add htmlForm (Conditional)
			if (getEnrolled().equalsIgnoreCase("Y")) {
				sb.append("<htmlForm>");
				sb.append(XMLUtil.filterSpecialChars(getHtmlForm()));
				sb.append("</htmlForm>");
			}

			sb.append("</PaymentVerifRes></MPI_Interface>");

			return sb.toString();
		} catch (Exception e) {
			logger.error(
				"Failed to convert PaymentVerifRes message to XML string.",
				e);
			return null;
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
			"[PaymentVerifRes.merchantID]",
			1,
			24,
			true);
		this.merchantID = merchantID;
	}

}