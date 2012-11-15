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

import com.oncecorp.visa3d.mpi.domain.CardRange;
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
 * Implementation of Visa 3-D CRRes message (Version 1.0.2).
 * The CRRes (Card Range Response) message is sent from the Visa Directory to the 
 * Merchant Server Plug-in (MPI) in response to a CRReq messages. It is used to provide
 * the list of participationg card ranges in order to update the MPI's internal cache 
 * information.
 * 
 * @version $Revision: 21 $
 * @author	Alan Zhang
 */
public class CRResMessage extends Message {
	/**
	 * Message Type
	 */
	public static final String MSG_TYPE = "CRRes";

	/**
	 * Message Version
	 */
	public static final String MSG_VERSION = "1.0.2";

	/**
	 * XML String used to create CRRes message document
	 */
	public static final String CRRES_XML_STRING =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<ThreeDSecure>"
			+ "<Message>"
			+ "<CRRes>"
			+ "<version/>"
			+ "</CRRes>"
			+ "</Message>"
			+ "</ThreeDSecure>";

	// Define the constant error messages. Will facilitate future i18n tasks 
	private final static String ID_ERRMSG =
		"Null, size is 0 or size exceeds 30.";
	private final static String ID_ERRMSG_LONG =
		"Invalid field: [CRRes.id] - " + ID_ERRMSG;

	private final static String SN_ERRMSG = "Size exceeds 20.";
	private final static String SN_ERRMSG_LONG =
		"Invalid field. [CRRes.serialNumber] - " + SN_ERRMSG;

	private final static String VENDORCODE_ERRMSG = "Size exceeds 256.";
	private final static String VENDORCODE_ERRMSG_LONG =
		"Invalid field. [CRRes.vendorCode] - " + VENDORCODE_ERRMSG;

	private final static String IREQCODE_ERRMSG = "Unrecognized value";
	private final static String IREQCODE_ERRMSG_LONG =
		"Invalid field. [CRRes.IReq.iReqCode] - " + IREQCODE_ERRMSG;

	private final static String CRSERIAL_ERRMSG =
		"CR and serialNumber should be provided at same time.";
	private final static String CRSERIAL_ERRMSG_LONG =
		"Invalid message: [CRRes] - " + CRSERIAL_ERRMSG;

	private final static String NULLCR_ERRMSG =
		"Exclusive attributes [CR] and [iReqCode] are both null.";
	private final static String NULLCR_ERRMSG_LONG =
		"Invalid message: [CRRes] - " + NULLCR_ERRMSG;

	private final static String SERIAL_IREQCODE_ERRMSG =
		"Exclusive attributes [serialNumber] and [iReqCode] are both provided.";
	private final static String SERIAL_IREQCODE_ERRMSG_LONG =
		"Invalid message: [CRRes] - " + SERIAL_IREQCODE_ERRMSG;

	private final static String COEXISTCR_ERRMSG =
		"Exclusive attributes [CR] and [iReqCode] can't co-exist.";
	private final static String COEXISTCR_ERRMSG_LONG =
		"Invalid message: [CRRes] - " + COEXISTCR_ERRMSG;

	private final static String IREQDETAIL_ERRMSG =
		"[iReqDetail] supplied without [iReqCode].";
	private final static String IREQDETAIL_ERRMSG_LONG =
		"Invalid message: [CRRes] - " + IREQDETAIL_ERRMSG;

	private final static String RANGE_ERRMSG = "One of card ranges is invalid.";
	private final static String RANGE_ERRMSG_LONG =
		"Invalid message: [CRRes] - " + RANGE_ERRMSG;

	private final static String CRITICAL_ERRMSG =
		"Critical extension not supported";
	private final static String CRITICAL_ERRMSG_LONG =
		"Invalid message: [CRRes] - " + CRITICAL_ERRMSG;

	/*
	 * Error message for version 1.0.1
	 */
	private final static String SERIAL_IREQCODE_ERRMSG2 =
		"When [iReqCode] provided, [serailNumber] should be 0.";
	private final static String SERIAL_IREQCODE_ERRMSG2_LONG =
		"Invalid message: [CRRes] - " + SERIAL_IREQCODE_ERRMSG2;

	/**
	 * Local Log4J logger
	 */
	private transient Logger logger =
		MPILogger.getLogger(CRResMessage.class.getName());

	/**
	 * Message Structure 
	 */
	private CardRange[] cr;
	private String serialNumber;
	private String ireqCode;
	private String ireqDetail;
	private String vendorCode;
	private Extension[] extension;
	
	/**
	 * Opaque message attributes that SHOULD not be exposed
	 * externally using the <code>toString()</code> or the <code>toXML()</code> method
	 */
	private String protocol;
	
	/**
	 * constructor
	 */
	public CRResMessage() throws MessagingException {
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
			Document dom = XMLUtil.createDocument(CRRES_XML_STRING, false);

			//Sanity check
			if (dom == null) {
				this.logger.error(
					"Failed to create document template for CRRes message.");
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
					"/ThreeDSecure/Message/CRRes/version");
			element.appendChild(dom.createTextNode(getVersion()));

			//Set CR (Optional)
			if ((getCr() != null) && (getCr().length != 0)) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/ThreeDSecure/Message/CRRes");

				CardRange[] ranges = getCr();
				for (int i = 0; i < cr.length; i++) {
					CardRange range = ranges[i];

					//Create CR elements
					Element cre = dom.createElement("CR");
					Element begin = dom.createElement("begin");
					Element end = dom.createElement("end");
					Element action = dom.createElement("action");

					//Set values
					begin.appendChild(
						dom.createTextNode(range.getBegin().toString()));
					end.appendChild(
						dom.createTextNode(range.getEnd().toString()));
					action.appendChild(dom.createTextNode(range.getAction()));

					//Constructs CR element
					cre.appendChild(begin);
					cre.appendChild(end);
					cre.appendChild(action);

					//Add CR element as child of Message element
					element.appendChild(cre);
				}
			}

			//Set serial number (Optional)
			if ((getSerialNumber() != null)
				&& (getSerialNumber().length() != 0)) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/ThreeDSecure/Message/CRRes");
				Element sn = dom.createElement("serialNumber");
				sn.appendChild(dom.createTextNode(getSerialNumber()));
				element.appendChild(sn);
			}

			//Set IReq (Optional)
			if ((getIreqCode() != null) && (getIreqCode().length() != 0)) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/ThreeDSecure/Message/CRRes");

				//Create IReq elements
				Element ireqEle = dom.createElement("IReq");
				Element ireqCodeEle = dom.createElement("iReqCode");
				ireqCodeEle.appendChild(dom.createTextNode(getIreqCode()));
				ireqEle.appendChild(ireqCodeEle);

				if ((getIreqDetail() != null)
					&& (getIreqDetail().length() != 0)) {
					Element ireqDetailEle = dom.createElement("iReqDetail");
					ireqDetailEle.appendChild(
						dom.createTextNode(getIreqDetail()));
					ireqEle.appendChild(ireqDetailEle);
				}

				if ((getVendorCode() != null)
					&& (getVendorCode().length() != 0)) {
					Element vendorCodeEle = dom.createElement("vendorCode");
					vendorCodeEle.appendChild(
						dom.createTextNode(getVendorCode()));
					ireqEle.appendChild(vendorCodeEle);
				}

				//Add IReq element as a child of Message element
				element.appendChild(ireqEle);
			}

			return dom;
		} catch (Exception e) {
			this.logger.error(
				"Failed to convert CRRes message to DOM document.",
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
					+ "<ThreeDSecure>"
					+ "<Message id=\"");

			sb.append(XMLUtil.filterSpecialChars(getId()));
			sb.append("\"><CRRes><version>");
			sb.append(XMLUtil.filterSpecialChars(getVersion()));
			sb.append("</version>");

			//Set CR (Optional)
			if ((getCr() != null) && (getCr().length != 0)) {
				CardRange[] ranges = getCr();
				for (int i = 0; i < cr.length; i++) {
					CardRange range = ranges[i];

					sb.append(range.toString());
				}
			}

			//Set serial number (Optional)
			if ((getSerialNumber() != null)
				&& (getSerialNumber().length() != 0)) {
				sb.append("<serialNumber>");
				sb.append(XMLUtil.filterSpecialChars(getSerialNumber()));
				sb.append("</serialNumber>");
			}

			//Set IReq (Optional)
			if ((getIreqCode() != null) && (getIreqCode().length() != 0)) {
				sb.append("<IReq><iReqCode>");
				sb.append(XMLUtil.filterSpecialChars(getIreqCode()));
				sb.append("</iReqCode>");

				if ((getIreqDetail() != null)
					&& (getIreqDetail().length() != 0)) {
					sb.append("<iReqDetail>");
					sb.append(XMLUtil.filterSpecialChars(getIreqDetail()));
					sb.append("</iReqDetail>");
				}

				if ((getVendorCode() != null)
					&& (getVendorCode().length() != 0)) {
					sb.append("<vendorCode>");
					sb.append(XMLUtil.filterSpecialChars(getVendorCode()));
					sb.append("</vendorCode>");
				}

				sb.append("</IReq>");
			}

			sb.append("</CRRes></Message></ThreeDSecure>");

			return sb.toString();
		} catch (Exception e) {
			logger.error("Failed to convert CRRes message to XML string.", e);
			return null;
		}

	}

	/**
	 * Validate message attributes against Visa 3-D DTD definition.
	 * 
	 * @return True if valid
	 */
	public boolean validate() throws MessagingException {
		//Check exclusiveness of CR and IReqCode, serialNumber
		if ((getCr() != null) && (getCr().length != 0)) {
			if ((getIreqCode() != null) && (getIreqCode().length() != 0)) {
				logger.error(COEXISTCR_ERRMSG_LONG);
				throw new MessagingException(
					getId(),
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"[CRRes.CR, CRRes.iReqCode]",
					COEXISTCR_ERRMSG,
					COEXISTCR_ERRMSG_LONG);
			} else {
				//serialNumber should be provided with CR as well
				if ((getSerialNumber() == null)
					|| (getSerialNumber().length() == 0)) {
					logger.error(CRSERIAL_ERRMSG_LONG);
					throw new MessagingException(
						getId(),
						ErrorCodes.ERROR_CODE_3,
						ErrorCodes.ERROR_MESSAGE_3,
						"serialNumber",
						CRSERIAL_ERRMSG,
						CRSERIAL_ERRMSG_LONG);
				}
			}
		}

		/*
		 * [Alan's note - Feb 18, 2003]
		 * Temp solution for differential validations of iReqCode/serialNumber from 
		 * CRRes version 1.0.1/1.0.2
		 */
		if (getVersion().equals(CRResMessage.MSG_VERSION)) {
			// version 1.0.2 validation
			// check exclusiveness of IReqCode and serialNumber
			if ((getIreqCode() != null) && (getIreqCode().length() != 0)) {
				if ((getSerialNumber() != null)
					&& (getSerialNumber().length() != 0)) {
					logger.error(SERIAL_IREQCODE_ERRMSG_LONG);
					throw new MessagingException(
						getId(),
						ErrorCodes.ERROR_CODE_5,
						ErrorCodes.ERROR_MESSAGE_5,
						"[CRRes.serialNumber, CRRes.iReqCode]",
						SERIAL_IREQCODE_ERRMSG,
						SERIAL_IREQCODE_ERRMSG_LONG);
				}
			}
		} else {
			// version 1.0.1 validation
			// check IReqCode and serialNumber
			if ((getIreqCode() != null) && (getIreqCode().length() != 0)) {
				if ((getSerialNumber() == null)
					|| (!getSerialNumber().equals("0"))) {
					logger.error(SERIAL_IREQCODE_ERRMSG2_LONG);
					throw new MessagingException(
						getId(),
						ErrorCodes.ERROR_CODE_5,
						ErrorCodes.ERROR_MESSAGE_5,
						"[CRRes.serialNumber, CRRes.iReqCode]",
						SERIAL_IREQCODE_ERRMSG2,
						SERIAL_IREQCODE_ERRMSG2_LONG);
				}
			}
		}

		//iReqDetail never supplied without iReqCode (if applicable)
		if ((getIreqDetail() != null) && (getIreqDetail().length() != 0)) {
			if ((getIreqCode() == null) || (getIreqCode().length() == 0)) {
				logger.error(IREQDETAIL_ERRMSG_LONG);
				throw new MessagingException(
					getId(),
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"[CRRes.iReqCode, CRRes.iReqDetail]",
					IREQDETAIL_ERRMSG,
					IREQDETAIL_ERRMSG_LONG);
			}
		}

		//verify CR
		if (getCr() != null) {
			CardRange[] crs = getCr();
			for (int i = 0; i < crs.length; i++) {
				CardRange cr = crs[i];
				if ((cr.getAction() == null)
					|| (cr.getBegin() == null)
					|| (cr.getEnd() == null)) {
					logger.error(
						"Invalid CRRes message. One of Card ranges is invalid. Begin: "
							+ cr.getBegin()
							+ ", End: "
							+ cr.getEnd()
							+ ", Action: "
							+ cr.getAction());
					throw new MessagingException(
						getId(),
						ErrorCodes.ERROR_CODE_5,
						ErrorCodes.ERROR_MESSAGE_5,
						"[CRRes.CR]",
						RANGE_ERRMSG,
						RANGE_ERRMSG_LONG);
				}
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
						"[CRRes.Extension]",
						CRITICAL_ERRMSG,
						CRITICAL_ERRMSG_LONG);
				}
			}
		}

		logger.debug("CRRes message is valid.");
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
	 * Returns the cr.
	 * @return CardRange[]
	 */
	public CardRange[] getCr() {
		return cr;
	}

	/**
	 * Returns the ireqCode.
	 * @return String
	 */
	public String getIreqCode() {
		return ireqCode;
	}

	/**
	 * Returns the ireqDetail.
	 * @return String
	 */
	public String getIreqDetail() {
		return ireqDetail;
	}

	/**
	 * Returns the serialNumber.
	 * @return String
	 */
	public String getSerialNumber() {
		return serialNumber;
	}

	/**
	 * Returns the vendorCode.
	 * @return String
	 */
	public String getVendorCode() {
		return vendorCode;
	}

	/**
	 * Sets the cr.
	 * @param cr The cr to set
	 */
	public void setCr(CardRange[] cr) {
		this.cr = cr;
	}

	/**
	 * Sets the ireqCode.
	 * @param ireqCode The ireqCode to set
	 */
	public void setIreqCode(String ireqCode) throws MessagingException {
		if ((ireqCode != null) && (ireqCode.length() != 0)) {
			if (IReqCodeValidator.isNotValidIReqCode(ireqCode)) {
				this.logger.error(IREQCODE_ERRMSG_LONG);
				throw new MessagingException(
					getId(),
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"[CRRes.IReq.iReqCode]",
					IREQCODE_ERRMSG,
					IREQCODE_ERRMSG_LONG + "Value: " + ireqCode);
			}
		}

		this.ireqCode = ireqCode;
	}

	/**
	 * Sets the ireqDetail.
	 * @param ireqDetail The ireqDetail to set
	 */
	public void setIreqDetail(String ireqDetail) {
		this.ireqDetail = ireqDetail;
	}

	/**
	 * Sets the serialNumber.
	 * @param serialNumber The serialNumber to set
	 */
	public void setSerialNumber(String serialNumber)
		throws MessagingException {
		MessageValidator.validateDigitField(
			getId(),
			serialNumber,
			"[CRRes.serialNumber]",
			1,
			20,
			false);
		this.serialNumber = serialNumber;
	}

	/**
	 * Sets the vendorCode.
	 * @param vendorCode The vendorCode to set
	 */
	public void setVendorCode(String vendorCode) throws MessagingException {
		// Length check 
		// Field is optional 		
		MessageValidator.validateField(
			getId(),
			vendorCode,
			"[CRRes.vendorCode]",
			-1,
			256,
			false);
		this.vendorCode = vendorCode;
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

	/**
	 * @return String
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * Sets the protocol.
	 * @param protocol The protocol to set
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

}
