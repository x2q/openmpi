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
 * Implement message class for VEReq. The VEReq (Verif 
 * Enrollment Request) message is sent by the Merchant Server Plug-in
 * to the Visa directory to determine whether a particular PAN is
 * enrolled in 3-D Secure.
 *  
 * @version $Revision: 17 $
 * @author	Jun Shi
 */

public class VEReqMessage extends Message {

	// Message type
	public final static String MSG_TYPE = "VEReq";

	// Message version
	// ---------------------------------------------------------
	// This message implementation supports BOTH 1.0.1 and 1.0.2
	// ---------------------------------------------------------
	//	public final static String MSG_VERSION = "1.0.1";
	public final static String MSG_VERSION = "1.0.2";

	/**
	 * XML Message Structure
	 */
	private static final String VEREQ_XML_STRING =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<ThreeDSecure>"
			+ "<Message>"
			+ "<VEReq>"
			+ "<version/>"
			+ "<pan/>"
			+ "<Merchant>"
			+ "<acqBIN/>"
			+ "<merID/>"
			+ "</Merchant>"
			+ "</VEReq>"
			+ "</Message>"
			+ "</ThreeDSecure>";

	/**
	 * VEReq Message Fields
	 */
	private String pan; // Mandatory 
	private String acqBIN; // Mandatory
	private String merID; // Mandatory
	private String password; // Conditional
	private String deviceCategory; // Optional
	private String accept; // Conditional
	private String userAgent; // Conditional
	private Extension[] extension; // Optional

	// Validation error messages 
	private final static String CRITICAL_ERRMSG =
		"Critical extension not supported";
	private final static String CRITICAL_ERRMSG_LONG =
		"Invalid message: [VEReq] - " + CRITICAL_ERRMSG;

	private final static String DEVICE_ERRMSG =
		"[Browser.deviceCategory should be a positive integer.";
	private final static String DEVICE_ERRMSG_LONG =
		"Invalid message: [VEReq] - " + DEVICE_ERRMSG;

	// version 1.0.1 error message
	private final static String DEVICE_ERRMSG2 =
		"[VEReq.Browser.deviceCategory should be '0' or '1'.";
	private final static String DEVICE_ERRMSG2_LONG =
		"Invalid message: [VEReq] - " + DEVICE_ERRMSG2;

	// Local Log4J logger
	private transient Logger logger =
		MPILogger.getLogger(VEReqMessage.class.getName());

	/** Constructor: set type and version for this object */
	public VEReqMessage() throws MessagingException {
		//Set message type
		setType(MSG_TYPE);

		//Set messsage version
		setVersion(MSG_VERSION);
	}

	/** 
	 * [Getter and Setter methods]
	 * [Setters] validates VEReq message fields to make sure all mandatory 
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

	public String getPan() {
		return pan;
	}
	public void setPan(String pan) throws MessagingException {
		//Check Field: pan
		MessageValidator.validateDigitField(
			getId(),
			pan,
			"[VEReq.pan]",
			13,
			19,
			true);
		this.pan = pan;
	}

	public String getAcqBIN() {
		return acqBIN;
	}
	public void setAcqBIN(String acqBIN) throws MessagingException {
		//Check Field: Acquirer BIN
		MessageValidator.validateDigitField(
			getId(),
			acqBIN,
			"[VEReq.Merchant.acqBIN]",
			1,
			11,
			true);
		this.acqBIN = acqBIN;
	}

	public String getMerID() {
		return merID;
	}
	public void setMerID(String merID) throws MessagingException {
		//Check Field: Merchant ID
		MessageValidator.validateField(
			getId(),
			merID,
			"[VEReq.Merchant.merID]",
			1,
			24,
			true);
		this.merID = merID;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) throws MessagingException {
		//Check Field: Password
		MessageValidator.validateField(
			getId(),
			password,
			"[VEReq.Merchant.password]",
			8,
			8,
			false);
		this.password = password;
	}

	public String getDeviceCategory() {
		return deviceCategory;
	}

	public void setDeviceCategory(String in) throws MessagingException {
		/*
		 * [Alan's note - Feb 18, 2003]
		 * Temp solution for differential validations of deviceCategory 
		 * from version 1.0.1/1.0.2
		 */
		if (getVersion().equals(VEReqMessage.MSG_VERSION)) {
			// version 1.0.2 validation
			//Check Field: Browser_deviceCategory
			if ((in != null) && (in.length() != 0)) {
				try {
					int number = Integer.parseInt(in);
					if (number < 0) {
						this.logger.error(
							DEVICE_ERRMSG + " Value: {" + in + "}");
						throw new MessagingException(
							getId(),
							ErrorCodes.ERROR_CODE_5,
							ErrorCodes.ERROR_MESSAGE_5,
							"[VEReq.Browser.deviceCategory]",
							DEVICE_ERRMSG,
							DEVICE_ERRMSG_LONG + " Value: {" + in + "}");

					}
				} catch (NumberFormatException nfe) {
					this.logger.error(DEVICE_ERRMSG + " Value: {" + in + "}");
					throw new MessagingException(
						getId(),
						ErrorCodes.ERROR_CODE_5,
						ErrorCodes.ERROR_MESSAGE_5,
						"[VEReq.Browser.deviceCategory",
						DEVICE_ERRMSG,
						DEVICE_ERRMSG_LONG + " Value: {" + in + "}");
				}
			}
		} else {
			// version 1.0.1 validation
			//Check Field: Browser_deviceCategory
			if ((in != null) && (in.length() != 0)) {
				if (!in.equals("0") && !in.equals("1")) {
					logger.error(DEVICE_ERRMSG + " Value: {" + in + "}");
					throw new MessagingException(
						getId(),
						ErrorCodes.ERROR_CODE_5,
						ErrorCodes.ERROR_MESSAGE_5,
						"[VEReq.Browser.deviceCategory]",
						DEVICE_ERRMSG,
						DEVICE_ERRMSG_LONG + " Value: {" + in + "}");

				}
			}

		}

		this.deviceCategory = in;
	}

	public String getAccept() {
		return accept;
	}

	public void setAccept(String accept) throws MessagingException {
		this.accept = accept;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) throws MessagingException {
		this.userAgent = userAgent;
	}

	public Extension[] getExtension() {
		return extension;
	}

	public void setExtension(Extension[] extension) {
		this.extension = extension;
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
			getPan(),
			"[VEReq.pan]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getAcqBIN(),
			"[VEReq.Merchant.acqBIN]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getMerID(),
			"[VEReq.Merchant.merchantID]",
			1,
			-1,
			true);

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
						"[VEReq.Extension]",
						CRITICAL_ERRMSG,
						CRITICAL_ERRMSG_LONG);
				}
			}
		}
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
			Document dom = XMLUtil.createDocument(VEREQ_XML_STRING, false);

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
					"/ThreeDSecure/Message/VEReq/version");
			element.appendChild(dom.createTextNode(getVersion()));

			//Set pan (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/ThreeDSecure/Message/VEReq/pan");
			element.appendChild(dom.createTextNode(getPan()));

			//Set Merchant acquirer BIN (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/ThreeDSecure/Message/VEReq/Merchant/acqBIN");
			element.appendChild(dom.createTextNode(getAcqBIN()));

			//Set Merchant merID (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/ThreeDSecure/Message/VEReq/Merchant/merID");
			element.appendChild(dom.createTextNode(getMerID()));

			//Set Merchant Password (Optional)
			if (getPassword() != null && getPassword().length() != 0) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/ThreeDSecure/Message/VEReq/Merchant");
				Element child = dom.createElement("password");
				child.appendChild(dom.createTextNode(getPassword()));
				element.appendChild(child);
			}

			//Set Browser deviceCategory (Optional)
			if (getDeviceCategory() != null
				&& getDeviceCategory().length() != 0) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/ThreeDSecure/Message/VEReq");

				Element child = dom.createElement("Browser");
				Element categoryElement = dom.createElement("deviceCategory");
				categoryElement.appendChild(
					dom.createTextNode(getDeviceCategory()));
				child.appendChild(categoryElement);
				element.appendChild(child);
			}

			//Set Browser accept (Optional)
			if (getAccept() != null && getAccept().length() != 0) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/ThreeDSecure/Message/VEReq");
				Element child = dom.createElement("Browser");
				Element acceptElement = dom.createElement("accept");
				acceptElement.appendChild(dom.createTextNode(getAccept()));
				child.appendChild(acceptElement);
				element.appendChild(child);
			}

			//Set Browser userAgent (Optional)
			if (getUserAgent() != null && getUserAgent().length() != 0) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/ThreeDSecure/Message/VEReq");
				Element child = dom.createElement("Browser");
				Element userAgentElement = dom.createElement("userAgent");
				userAgentElement.appendChild(
					dom.createTextNode(getUserAgent()));
				child.appendChild(userAgentElement);
				element.appendChild(child);
			}

			// -------- Added by Alan Zhang (Aug 09, 2002) ----------------------
			// Set Extensions (Optional)
			if ((getExtension() != null) && (getExtension().length != 0)) {
				//Get extensions
				Extension[] extensions = getExtension();

				//Get VEReq element
				Element vereqElement =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/ThreeDSecure/Message/VEReq");

				//Append all extension elements
				for (int i = 0; i < extensions.length; i++) {
					//Get individual extension
					Extension extension = extensions[i];

					//Append Extension element to VEReq element
					vereqElement.appendChild(
						dom.importNode(extension.toXML(), true));
				}

			}
			//--------- Adding end ---------------------------------------------

			logger.debug("[VEReqMessage.toXML] completed!");
			return dom;
		} catch (Exception e) {
			logger.error("Failed to convert VEReq message to DOM document.", e);
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
			sb.append("\"><VEReq><version>");
			sb.append(XMLUtil.filterSpecialChars(getVersion()));
			sb.append("</version><pan>");
			sb.append(XMLUtil.filterSpecialChars(getPan()));
			sb.append("</pan><Merchant><acqBIN>");
			sb.append(XMLUtil.filterSpecialChars(getAcqBIN()));
			sb.append("</acqBIN><merID>");
			sb.append(XMLUtil.filterSpecialChars(getMerID()));
			sb.append("</merID>");

			// add Merchant Password (Optional)
			if (isNotNull(getPassword())) {
				sb.append("<password>");
				sb.append(XMLUtil.filterSpecialChars(getPassword()));
				sb.append("</password>");
			}

			sb.append("</Merchant>");

			// add Browser (Optional)
			if (isNotNull(getDeviceCategory())
				|| isNotNull(getAccept())
				|| isNotNull(getUserAgent())) {
				sb.append("<Browser>");
				if (isNotNull(getDeviceCategory())) {
					sb.append("<deviceCategory>");
					sb.append(XMLUtil.filterSpecialChars(getDeviceCategory()));
					sb.append("</deviceCategory>");
				}

				if (isNotNull(getAccept())) {
					sb.append("<accept>");
					sb.append(XMLUtil.filterSpecialChars(getAccept()));
					sb.append("</accept>");
				}

				if (isNotNull(getUserAgent())) {
					sb.append("<userAgent>");
					sb.append(XMLUtil.filterSpecialChars(getUserAgent()));
					sb.append("</userAgent>");
				}

				sb.append("</Browser>");
			}

			// add Extensions (Optional)
			if ((getExtension() != null) && (getExtension().length != 0)) {
				//Get extensions
				Extension[] extensions = getExtension();

				//Append all extension elements
				for (int i = 0; i < extensions.length; i++) {
					//Get individual extension
					Extension extension = extensions[i];

					//Append Extension element to VEReq element
					sb.append(extension.toString());
				}
			}

			sb.append("</VEReq></Message></ThreeDSecure>");

			return sb.toString();
		} catch (Exception e) {
			logger.error("Failed to convert VEReq message to XML string.", e);
			return null;
		}

	}

	private boolean isNotNull(String in) {
		if ((in == null) || (in.length() == 0))
			return false;
		else
			return true;
	}
}
