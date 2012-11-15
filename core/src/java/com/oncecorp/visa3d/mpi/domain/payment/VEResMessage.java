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
import com.oncecorp.visa3d.mpi.utility.URLValidator;
import com.oncecorp.visa3d.mpi.utility.XMLUtil;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implement message class for VERes. The VERes (Verify 
 * Enrollment Response) message is sent by the ACS (or the Visa 
 * Directory) to the Merchant Server Plug-in (via the Visa Directory)
 * to advise the merchant whether a particular PAN is enrolled in 
 * 3-D Secure.
 *  
 * @version $Revision: 26 $
 * @author	Jun Shi
 */

public class VEResMessage extends Message {

	// Message type
	public final static String MSG_TYPE = "VERes";

	// Message version
	// ---------------------------------------------------------
	// This message implementation supports BOTH 1.0.1 and 1.0.2
	// ---------------------------------------------------------
	//	public final static String MSG_VERSION = "1.0.1";
	public final static String MSG_VERSION = "1.0.2";

	// Valid payment protocol
	private static final String PAYMENT_PROTOCOL_1 = "ThreeDSecure";
	private static final String PAYMENT_PROTOCOL_2 = "SET";

	// Constant error validation messages
	private final static String ENRL_ERRMSG = " Value is not one of: Y/N/U";
	private final static String ENRL_ERRMSG_LONG =
		"Invalid field: [VERes.CH.enrolled] - " + ENRL_ERRMSG;

	private final static String IREQCODE_ERRMSG = "Invalid value.";
	private final static String IREQCODE_ERRMSG_LONG =
		"Invalid field. [VERes.IReq.iReqCode] - " + IREQCODE_ERRMSG;

	private final static String PROTOCOL_ERRMSG = "Invalid value.";
	private final static String PROTOCOL_ERRMSG_LONG =
		"Invalid field. [VERes.protocol] - " + PROTOCOL_ERRMSG;

	private final static String ACCTID_ERRMSG =
		"[CH.acctID] must be set when [CH.enrolled] is equal to 'Y'";
	private final static String ACCTID_ERRMSG_LONG =
		"Invalid message: [VERes] - " + ACCTID_ERRMSG;

	private final static String IREQ_ERRMSG =
		"[CH.enrolled] is equal to 'Y' and [IReq.iReqCode] is specified.";
	private final static String IREQ_ERRMSG_LONG =
		"Invalid message: [VERes] - " + IREQ_ERRMSG;

	private final static String IREQ2_ERRMSG =
		"Invalid [IReq.iReqCode] specified with [CH.enrolled] is equal to 'N'.";
	private final static String IREQ2_ERRMSG_LONG =
		"Invalid message: [VERes] - " + IREQ2_ERRMSG;

	private final static String IDETAIL_ERRMSG =
		"[IReq.iReqCode] is empty but [IReq.iReqDetail] contains a value.";
	private final static String IDETAIL_ERRMSG_LONG =
		"Invalid message: [VERes] - " + IDETAIL_ERRMSG;

	private final static String VCODE_ERRMSG =
		"[IReq.iReqCode] is empty but [IReq.vendorCode] contains a value.";
	private final static String VCODE_ERRMSG_LONG =
		"Invalid message: [VERes] - " + VCODE_ERRMSG;

	////////////////////////////////add protocol error message - [Jun, Oct 31]
	private final static String PROTOCOL1_ERRMSG =
		"[protocol] is empty or more than 2 entries";
	private final static String PROTOCOL1_ERRMSG_LONG =
		"Invalid message: [VERes] - " + PROTOCOL1_ERRMSG;

	private final static String PROTOCOL2_ERRMSG = "[protocol] is unrecognized";
	private final static String PROTOCOL2_ERRMSG_LONG =
		"Invalid message: [VERes] - " + PROTOCOL2_ERRMSG;

	private final static String PROTOCOL3_ERRMSG =
		"[protocol] is SET, we currently don't support it";
	private final static String PROTOCOL3_ERRMSG_LONG =
		"Invalid message: [VERes] - " + PROTOCOL3_ERRMSG;
	///////////////////////////////

	private static final String TERMURL2_ERRMSG =
		"URL Specification is not valid";
	private static final String TERMURL2_ERRMSG_LONG =
		"Invalid field: [VERes.url] - " + TERMURL2_ERRMSG;

	/**
	 * VERes Message Fields
	 */
	private String enrolled; // Mandatory
	private String acctID; // Conditional
	private String url; // Conditional 
	private String[] protocol; // Conditional
	private String iReqCode; // Conditional
	private String iReqDetail; // Optional
	private String vendorCode; // Optional
	private Extension[] extension; // Optional

	// Validation error messages 
	private final static String CRITICAL_ERRMSG =
		"Critical extension not supported";
	private final static String CRITICAL_ERRMSG_LONG =
		"Invalid message: [VERes] - " + CRITICAL_ERRMSG;

	// Local Log4J logger
	private Logger logger = MPILogger.getLogger(VEResMessage.class.getName());

	/** Constructor: set type and version for this object */
	public VEResMessage() throws MessagingException {
		//Set message type
		setType(MSG_TYPE);

		//Set messsage version
		setVersion(MSG_VERSION);
	}

	/** 
	 * [Getter and Setter methods]
	 * [Setters] validates VERes message fields to make sure all mandatory 
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

	public String getEnrolled() {
		return enrolled;
	}
	public void setEnrolled(String enrolled) throws MessagingException {
		// Check Field: enrolled 
		MessageValidator.validateField(
			getId(),
			enrolled,
			"[VERes.CH.enrolled]",
			1,
			-1,
			true);

		if ((!enrolled.equalsIgnoreCase("Y")
			&& !enrolled.equalsIgnoreCase("N")
			&& !enrolled.equalsIgnoreCase("U"))) {
			this.logger.error(ENRL_ERRMSG_LONG + " Value: {" + enrolled + "}");
			throw new MessagingException(
				getId(),
				ErrorCodes.ERROR_CODE_5,
				ErrorCodes.ERROR_MESSAGE_5,
				"[VERes.CH.enrolled]",
				ENRL_ERRMSG,
				ENRL_ERRMSG_LONG + " Value: {" + enrolled + "}");
		}
		this.enrolled = enrolled;
	}

	public String getAcctID() {
		return acctID;
	}
	public void setAcctID(String acctID) throws MessagingException {
		MessageValidator.validateField(
			getId(),
			acctID,
			"[VERes.CH.acctID]",
			1,
			28,
			false);
		this.acctID = acctID;
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) throws MessagingException {
		/*
		 * [Martin's Note - Sept 11, 2003 @ 13:15
		 * Seems the specification does not mandate a maximum URL size. So I'm changing
		 * this to accept URL size up to 1536 characters....
		 */
		MessageValidator.validateField(
			getId(),
			url,
			"[VERes.url]",
			1,
			//255,
			1536,
			false);

		// Verify URL
		if (url != null) {
			if (!URLValidator.isValid(url)) {
				this.logger.error(
					TERMURL2_ERRMSG_LONG + " Value: {" + url + "}");
				throw new MessagingException(
					"UNKNOWN",
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"[VERes.url]",
					TERMURL2_ERRMSG,
					TERMURL2_ERRMSG_LONG + " Value: {" + url + "}");
			}
		}

		this.url = url; // checked in validate()
	}
	public String[] getProtocol() {
		return protocol;
	}
	public void setProtocol(String[] protocol) throws MessagingException {
		if (protocol != null) {
			for (int i = 0; i < protocol.length; i++) {
				/*
				 * [Alan's Note: Oct 31, 2002]
				 * Only supported protocol is: ThreeDSecure 
				 */
				if ((protocol[i] != null)
					&& !protocol[i].equalsIgnoreCase(PAYMENT_PROTOCOL_1)) {
					this.logger.error(PROTOCOL_ERRMSG_LONG);
					throw new MessagingException(
						getId(),
						ErrorCodes.ERROR_CODE_5,
						ErrorCodes.ERROR_MESSAGE_5,
						"[VERes.protocol]",
						PROTOCOL_ERRMSG,
						PROTOCOL_ERRMSG_LONG + "Value: {" + protocol[i] + "}");
				}
			}
		}
		this.protocol = protocol;
	}
	public String getIReqCode() {
		return iReqCode;
	}
	public void setIReqCode(String ireqCode) throws MessagingException {
		if ((ireqCode != null) && (ireqCode.length() != 0)) {
			if (IReqCodeValidator.isNotValidIReqCode(ireqCode)) {
				this.logger.error(IREQCODE_ERRMSG_LONG);
				throw new MessagingException(
					getId(),
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"[VERes.IReq.iReqCode]",
					IREQCODE_ERRMSG,
					IREQCODE_ERRMSG_LONG + "Value: " + ireqCode);
			}
		}

		this.iReqCode = ireqCode; // checked in validate()
	}
	public String getIReqDetail() {
		return iReqDetail;
	}
	public void setIReqDetail(String iReqDetail) throws MessagingException {
		this.iReqDetail = iReqDetail; // checked in validate()
	}
	public String getVendorCode() {
		return vendorCode;
	}
	public void setVendorCode(String vendorCode) throws MessagingException {
		this.vendorCode = vendorCode; // checked in validate()
	}
	public Extension[] getExtension() {
		return extension;
	}
	public void setExtension(Extension[] extension) {
		this.extension = extension;
	}

	// validate depedent fields for this msg
	public boolean validate() throws MessagingException {
		// Mandatory field check
		MessageValidator.validateField(
			"UNKNOWN",
			getId(),
			"[Message.id]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getEnrolled(),
			"[VERes.CH.enrolled]",
			1,
			-1,
			true);

		//Check Fields if enrolled = "Y"
		if (getEnrolled().equalsIgnoreCase("Y")) {
			MessageValidator.validateField(
				getId(),
				getAcctID(),
				"[VERes.CH.acctID]",
				1,
				-1,
				true);
			MessageValidator.validateField(
				getId(),
				getUrl(),
				"[VEReq.url]",
				1,
				-1,
				true);
			MessageValidator.validateField(
				getId(),
				getProtocol(),
				"[VEReq.protocol]",
				1,
				-1,
				true);
		}

		/*
		 * [Alan's Note: Oct 31, 2002]
		 * The only protocol dependency we need check is to make sure that
		 * there is at least one protocol existing while enrolled is 'Y'
		 */
		if (getEnrolled().equalsIgnoreCase("Y")) {
			if ((getProtocol() == null) || (getProtocol().length == 0)) {
				this.logger.error(PROTOCOL1_ERRMSG_LONG);
				throw new MessagingException(
					getId(),
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"[VERes.protocol, VERes.CH.enrolled]",
					PROTOCOL1_ERRMSG,
					PROTOCOL1_ERRMSG_LONG);
			}
		}

		////////////////////////////////add protocol validation - [Jun, Oct 31] ///////////////
		/*
		//Check Field <protocol> if enrolled = "Y"
		if (getEnrolled().equalsIgnoreCase("Y")) {
			 String protocols [] = getProtocol();
			 
			  // IF enrolled is "Y": AT LEAST, protocol should be one of [ThreeDSecure] and [SET], OR BOTH
			 if (protocols.length==0 || protocols.length>2 ) {
			     this.logger.error(PROTOCOL1_ERRMSG_LONG);
			     throw new MessagingException(
			        getId(),
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"[VERes.protocol, VERes.CH.enrolled]",
					PROTOCOL1_ERRMSG,
					PROTOCOL1_ERRMSG_LONG);
			 }
			 
		     // find out if protocol "ThreeDSecure" is soppourted
		     String supportProtocol = null;
		     for (int i = 0; i < protocols.length; i++) {
			      //  support ThreeDSecure 
			      if (protocols[i].equalsIgnoreCase("ThreeDSecure")){
		              supportProtocol = protocols[i];
			          this.logger.debug("The supported protocol is [ThreeDSecure]");
			          break;
		          }
			      //  support SET 
			      if (protocols[i].equalsIgnoreCase("SET")){
		              supportProtocol = protocols[i];
			          this.logger.debug("The supported protocol is [SET]");
		           }
			 }
			 
			 // handling supportProtocol in case it is non "ThreeDSecure" 
			 if (supportProtocol==null) {
			    this.logger.error(PROTOCOL2_ERRMSG_LONG);
			    throw new MessagingException(
			       getId(),
					ErrorCodes.ERROR_CODE_4,
					ErrorCodes.ERROR_MESSAGE_4,
					"[VERes.protocol, VERes.CH.enrolled]",
					PROTOCOL2_ERRMSG,
					PROTOCOL2_ERRMSG_LONG);
			 }
			 else {
			     if(supportProtocol.equalsIgnoreCase("SET")){
			        this.logger.error(PROTOCOL3_ERRMSG_LONG);
			        throw new MessagingException(
			           getId(),
					   ErrorCodes.ERROR_CODE_5,
					   ErrorCodes.ERROR_MESSAGE_5,
					   "[VERes.protocol, VERes.CH.enrolled]",
					   PROTOCOL3_ERRMSG,
					   PROTOCOL3_ERRMSG_LONG); 
			     }
			 }
		}
		*/
		////////////////////////////////////////// add end ///////////////////////////////////

		//Check Field: iReqCode (if iReq is not null, enrolled must be "N")
		if (!getEnrolled().equalsIgnoreCase("N")
			&& !getEnrolled().equalsIgnoreCase("U")
			&& (getIReqCode() != null)
			&& (getIReqCode().length() != 0)) {
			this.logger.error(IREQ_ERRMSG_LONG);
			throw new MessagingException(
				getId(),
				ErrorCodes.ERROR_CODE_5,
				ErrorCodes.ERROR_MESSAGE_5,
				"[VERes.IReq.iReqCode, VERes.CH.enrolled]",
				IREQ_ERRMSG,
				IREQ_ERRMSG_LONG);
		}

		if (getEnrolled().equalsIgnoreCase("N")) {
			// if enrolled = "N", IReqCode still can be null unless IReq is not null !! 
			if ((getIReqCode() != null) && (getIReqCode().length() != 0)) {
				if (getIReqCode().length() > 2) {
					this.logger.error(IREQ2_ERRMSG_LONG);
					throw new MessagingException(
						getId(),
						ErrorCodes.ERROR_CODE_5,
						ErrorCodes.ERROR_MESSAGE_5,
						"[VERes.IReq.iReqCode, VERes.CH.enrolled]",
						IREQ2_ERRMSG,
						IREQ2_ERRMSG_LONG);
				}
			}
		}

		//Check Field: iReqDetail
		if (getIReqDetail() != null) {
			if (getIReqCode() == null) {
				this.logger.error(IDETAIL_ERRMSG_LONG);
				throw new MessagingException(
					getId(),
					ErrorCodes.ERROR_CODE_3,
					ErrorCodes.ERROR_MESSAGE_3,
					"[VERes.IReq.iReqCode, VERes.IReq.iReqDetail]",
					IDETAIL_ERRMSG,
					IDETAIL_ERRMSG_LONG);
			} else {
				if (getIReqCode().length() == 0) {
					this.logger.error(IDETAIL_ERRMSG_LONG);
					throw new MessagingException(
						getId(),
						ErrorCodes.ERROR_CODE_5,
						ErrorCodes.ERROR_MESSAGE_5,
						"[VERes.IReq.iReqCode, VERes.IReq.iReqDetail]",
						IDETAIL_ERRMSG,
						IDETAIL_ERRMSG_LONG);
				}
			}
		}

		//Check Field: vendorCode
		if (getVendorCode() != null) {
			if (getIReqCode() == null) {
				this.logger.error(VCODE_ERRMSG_LONG);
				throw new MessagingException(
					getId(),
					ErrorCodes.ERROR_CODE_3,
					ErrorCodes.ERROR_MESSAGE_3,
					"[VERes.IReq.iReqCode, VERes.IReq.vendorCode]",
					VCODE_ERRMSG,
					VCODE_ERRMSG_LONG);
			} else {
				if (getIReqCode().length() == 0) {
					this.logger.error(VCODE_ERRMSG_LONG);
					throw new MessagingException(
						getId(),
						ErrorCodes.ERROR_CODE_5,
						ErrorCodes.ERROR_MESSAGE_5,
						"[VERes.IReq.iReqCode, VERes.IReq.vendorCode]",
						VCODE_ERRMSG,
						VCODE_ERRMSG_LONG);
				}
			}
		}

		if (getEnrolled().equalsIgnoreCase("N")
			&& (getIReqCode() != null && getIReqCode().length() != 0)) {
			// if enrolled = "N" and IReqCode is not, vendorCode is still Optional 
			if ((vendorCode != null) && (vendorCode.length() != 0)) {
				MessageValidator.validateField(
					getId(),
					vendorCode,
					"[VERes.IReq.vendorCode]",
					-1,
					256,
					true);
			}
		}

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
						"[VERes.Extension]",
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
	 * NOTE:
	 * Due to the business logic, there is no requirement to create this DOM.
	 * HOWEVER:
	 * It can be added if we have some special requiremet.  
	 * 
	 * @return DOM document object
	 */
	public Document toXML() {
		try {
			// XML String used to create VERes message document
			String VERES_XML_STRING =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ "<ThreeDSecure>"
					+ "<Message>"
					+ "<VERes>"
					+ "<version/>"
					+ "<CH>"
					+ "<enrolled/>"
					+ "</CH>"
					+ "</VERes>"
					+ "</Message>"
					+ "</ThreeDSecure>";

			//Create document template
			Document dom = XMLUtil.createDocument(VERES_XML_STRING, false);

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
					"/ThreeDSecure/Message/VERes/version");
			element.appendChild(dom.createTextNode(getVersion()));

			//Set enrolled (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/ThreeDSecure/Message/VERes/CH/enrolled");
			element.appendChild(dom.createTextNode(getEnrolled()));

			//Set acctID (Conditional)
			if (getEnrolled().equalsIgnoreCase("Y")) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/ThreeDSecure/Message/VERes/CH");
				Element child = dom.createElement("acctID");
				child.appendChild(dom.createTextNode(getAcctID()));
				element.appendChild(child);
			}

			//Set url (Conditional)
			if (getEnrolled().equalsIgnoreCase("Y")) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/ThreeDSecure/Message/VERes");
				Element child = dom.createElement("url");
				child.appendChild(dom.createTextNode(getUrl()));
				element.appendChild(child);
			}

			//Set protocol (Conditional)
			if (getEnrolled().equalsIgnoreCase("Y")) {
				//Get protocols
				String[] protocols = getProtocol();

				//Get VERes element
				Element elememt =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/ThreeDSecure/Message/VERes");
				//Append all protocol elements
				for (int i = 0; i < protocols.length; i++) {
					Element child = dom.createElement("protocol");
					child.appendChild(dom.createTextNode(protocols[i]));
					element.appendChild(child);
				}
			}

			//Set iReqCode (Optional)
			if (getIReqCode() != null && getIReqCode().length() != 0) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/ThreeDSecure/Message/VERes");
				Element parent = dom.createElement("IReq");
				Element child = dom.createElement("iReqCode");
				child.appendChild(dom.createTextNode(getIReqCode()));
				parent.appendChild(child);

				//Set Invalid Request Detail (Optional)
				if (getIReqDetail() != null && getIReqDetail().length() != 0) {
					child = dom.createElement("iReqDetail");
					child.appendChild(dom.createTextNode(getIReqDetail()));
					parent.appendChild(child);
				}

				//Set Vendor Code (Optional)
				if (getVendorCode() != null && getVendorCode().length() != 0) {
					child = dom.createElement("vendorCode");
					child.appendChild(dom.createTextNode(getVendorCode()));
					parent.appendChild(child);
				}

				element.appendChild(parent);
			}

			// -------- Added by Jun Shi (Aug 14, 2002) ----------------------
			// Set Extensions (Optional)
			if ((getExtension() != null) && (getExtension().length != 0)) {
				//Get extensions
				Extension[] extensions = getExtension();

				//Get VERes element
				Element veresElement =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/ThreeDSecure/Message/VERes");

				//Append all extension elements
				for (int i = 0; i < extensions.length; i++) {
					//Get individual extension
					Extension extension = extensions[i];

					//Append Extension element to VERes element
					veresElement.appendChild(
						dom.importNode(extension.toXML(), true));
				}

			}
			//--------- Adding end ---------------------------------------------

			logger.debug("[VEResMessage.toXML] completed!");
			return dom;
		} catch (Exception e) {
			logger.error("Failed to convert VERes message to DOM document.", e);
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
			sb.append("\"><VERes><version>");
			sb.append(XMLUtil.filterSpecialChars(getVersion()));
			sb.append("</version><CH><enrolled>");
			sb.append(XMLUtil.filterSpecialChars(getEnrolled()));
			sb.append("</enrolled>");

			// add acctID (Conditional)
			if (getEnrolled().equalsIgnoreCase("Y")) {
				sb.append("<acctID>");
				sb.append(XMLUtil.filterSpecialChars(getAcctID()));
				sb.append("</acctID>");
			}

			sb.append("</CH>");

			// add url (Conditional)
			if (getEnrolled().equalsIgnoreCase("Y")) {
				sb.append("<url>");
				sb.append(XMLUtil.filterSpecialChars(getUrl()));
				sb.append("</url>");
			}

			// add protocol (Conditional)
			if (getEnrolled().equalsIgnoreCase("Y")) {
				//Get protocols
				String[] protocols = getProtocol();

				//Append all protocol elements
				for (int i = 0; i < protocols.length; i++) {
					sb.append("<protocol>");
					sb.append(XMLUtil.filterSpecialChars(protocols[i]));
					sb.append("</protocol>");
				}
			}

			//Set iReqCode (Optional)
			if (getIReqCode() != null && getIReqCode().length() != 0) {
				sb.append("<IReq><iReqCode>");
				sb.append(XMLUtil.filterSpecialChars(getIReqCode())); 
				sb.append("</iReqCode>");

				//Set Invalid Request Detail (Optional)
				if (getIReqDetail() != null && getIReqDetail().length() != 0) {
					sb.append("<iReqDetail>");
					sb.append(XMLUtil.filterSpecialChars(getIReqDetail()));
					sb.append("</iReqDetail>");
				}

				//Set Vendor Code (Optional)
				if (getVendorCode() != null && getVendorCode().length() != 0) {
					sb.append("<vendorCode>");
					sb.append(XMLUtil.filterSpecialChars(getVendorCode()));
					sb.append("</vendorCode>");
				}

				sb.append("</IReq>");
			}

			// add Extensions (Optional)
			if ((getExtension() != null) && (getExtension().length != 0)) {
				//Get extensions
				Extension[] extensions = getExtension();

				//Append all extension elements
				for (int i = 0; i < extensions.length; i++) {
					//Get individual extension
					Extension extension = extensions[i];

					//Append Extension element to VERes element
					sb.append(extension.toString());
				}

			}

			sb.append("</VERes></Message></ThreeDSecure>");

			return sb.toString();
		} catch (Exception e) {
			logger.error("Failed to convert VERes message to XML string.", e);
			return null;
		}

	}
}