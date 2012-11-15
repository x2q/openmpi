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
 * Implement message class for PaymentAuthReq. The PaymentAuthReq
 * (Payment Authentication Request) message is sent by the Merchant Application
 * providing wrapped PARes response from ACS server.
 * 
 * @version $Revision: 14 $
 * @author	Alan Zhang
 */

public class PaymentAuthReqMessage extends Message {
	// Message Type
	public final static String MSG_TYPE = "PaymentAuthReq";

	// Message Version
	/* 
	 * [Martin's Note: Dec 12, 2002 1:06:27 PM]
	 * 
	 * Moved to version 1.1 as per new interface definition
	 */
	// public final static String MSG_VERSION = "1.0";
	public final static String MSG_VERSION = "1.1";

	// Local Log4J logger
	private Logger logger =
		MPILogger.getLogger(PaymentAuthReqMessage.class.getName());

	// Error messages
	private static final String ID_ERRMSG = "Null, size is 0.";
	private static final String ID_ERRMSG_LONG =
		"Invalid field: [PaymentAuthReq.id] - " + ID_ERRMSG;

	private static final String AUTHMSG_ERRMSG = "Null, size is 0.";
	private static final String AUTHMSG_ERRMSG_LONG =
		"Invalid field: [PaymentAuthReq.PaymentAuthMsg] - " + AUTHMSG_ERRMSG;

	private static final String MERCHANT_ERRMSG =
		"[merchantID] must be up to 24 characters with up to 15 byte alphanumeric Card Acceptor ID"
			+ " and optionally followed by a hypher and an up to 8 byte alphanumeric Card Acceptor Terminal ID";
	private static final String MERCHANT_ERRMSG_LONG =
		"Invalid message: [PaymentAuthReq] - " + MERCHANT_ERRMSG;

	/**
	 * Message Field: merchantID
	 */
	private String merchantID;

	/**
	 * Message Field: paymentAuthMsg
	 */
	private String paymentAuthMsg;

	/**
	 * Message Field: MD
	 */
	private String merchantData;

	/**
	 * XML message structure
	 */
	private static final String PAYMENTAUTHREQ_XML_STRING =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<MPI_Interface>"
			+ "<PaymentAuthReq>"
			+ "<version/>"
			+ "<merchantID/>"
			+ "<paymentAuthMsg/>"
			+ "</PaymentAuthReq>"
			+ "</MPI_Interface>";

	/**
	 * Constructor
	 */
	public PaymentAuthReqMessage() throws MessagingException {
		//Set message type
		setType(MSG_TYPE);

		//Set message version
		setVersion(MSG_VERSION);
	}

	/**
	 * XMLSerializable interface method.
	 * This method consturcts a DOM document which represents this message.
	 * @return DOM document object
	 */
	public Document toXML() {
		try {
			//Create document template
			Document dom =
				XMLUtil.createDocument(PAYMENTAUTHREQ_XML_STRING, false);

			//Sanity check
			if (dom == null) {
				this.logger.error("Failed to create document template.");
				return null;
			}

			//Shared attributes
			Element element;

			//Add message id attribute (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/PaymentAuthReq");
			element.setAttribute("id", getId());

			//Set version (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/PaymentAuthReq/version");
			element.appendChild(dom.createTextNode(getVersion()));

			//Set merchantID (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/PaymentAuthReq/merchantID");
			element.appendChild(dom.createTextNode(getMerchantID()));

			//Set PaymentAuthMsg (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/MPI_Interface/PaymentAuthReq/paymentAuthMsg");
			element.appendChild(dom.createTextNode(getPaymentAuthMsg()));

			return dom;
		} catch (Exception e) {
			this.logger.error(
				"Failed to convert PaymentAuthReq message to DOM document.",
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
					+ "<PaymentAuthReq id=\"");

			sb.append(XMLUtil.filterSpecialChars(getId()));
			sb.append("\"><version>");
			sb.append(XMLUtil.filterSpecialChars(getVersion()));
			sb.append("</version><merchantID>");
			sb.append(XMLUtil.filterSpecialChars(getMerchantID()));
			sb.append("</merchantID><paymentAuthMsg>");
			sb.append(XMLUtil.filterSpecialChars(getPaymentAuthMsg()));
			sb.append("</paymentAuthMsg></PaymentAuthReq></MPI_Interface>");
			
			return sb.toString();
		} catch (Exception e) {
			logger.error(
				"Failed to convert PaymentAuthReq message to XML string.",
				e);
			return null;
		}

	}

	/**
	 * Overrides super class validate()
	 */
	public boolean validate() throws MessagingException {
		//Mandatory field check
		MessageValidator.validateField(
			"UNKNOWN",
			getId(),
			"[PaymentAuthReq.id]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getMerchantID(),
			"[PaymentAuthReq.Merchant.merchantID]",
			1,
			24,
			true);
		MessageValidator.validateField(
			getId(),
			getPaymentAuthMsg(),
			"[PaymentAuthReq.paymentAuthMsg]",
			1,
			-1,
			true);

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
			"[PaymentAuthReq.id]",
			-1,
			-1,
			true);
		super.setId(in);
	}

	/**
	 * Gets the paymentAuthMsg
	 * @return Returns a String
	 */
	public String getPaymentAuthMsg() {
		return paymentAuthMsg;
	}
	/**
	 * Sets the paymentAuthMsg
	 * @param in The paymentAuthMsg to set
	 */
	public void setPaymentAuthMsg(String in) throws MessagingException {
		//Check Field: paymentAuthMsg
		MessageValidator.validateField(
			getId(),
			in,
			"[PaymentAuthReq.paymentAuthMsg]",
			1,
			-1,
			true);
		this.paymentAuthMsg = in;
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
			"[PaymentAuthReq.merchantID]",
			1,
			24,
			true);
			
		// check format
		if (!MessageValidator.isValidMerchantID(merchantID)) {
			logger.error(MERCHANT_ERRMSG_LONG);
			throw new MessagingException(
				getId(),
				ErrorCodes.ERROR_CODE_5,
				ErrorCodes.ERROR_MESSAGE_5,
				"[PaymentVerifRes.merchantID]",
				MERCHANT_ERRMSG,
				MERCHANT_ERRMSG_LONG);
		}

		this.merchantID = merchantID;
	}

	/**
	 * Returns the merchantData.
	 * @return String
	 */
	public String getMerchantData() {
		return merchantData;
	}

	/**
	 * Sets the merchantData.
	 * @param merchantData The merchantData to set
	 */
	public void setMerchantData(String merchantData) {
		this.merchantData = merchantData;
	}

}