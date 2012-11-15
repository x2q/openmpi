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

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implement message class for PAReq. The PAReq (Payer 
 * Authentication Request) message is sent by the Merchant Server Plug-in
 * to the ACS through the cardholder system, providing the data required 
 * to attempt cardholder authentication.
 * 
 * @version $Revision: 20 $
 * @author	Alan Zhang
 */
public class PAReqMessage extends Message {

	/**
	 * Message Type
	 */
	public static final String MSG_TYPE = "PAReq";

	// ---------------------------------------------------------
	// This message implementation supports BOTH 1.0.1 and 1.0.2
	// ---------------------------------------------------------
	//	public final static String MSG_VERSION = "1.0.1";
	public final static String MSG_VERSION = "1.0.2";

	/**
	 * XML Message Structure
	 */
	private static final String PAREQ_XML_STRING =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<ThreeDSecure>"
			+ "<Message>"
			+ "<PAReq>"
			+ "<version/>"
			+ "<Merchant>"
			+ "<acqBIN/>"
			+ "<merID/>"
			+ "<name/>"
			+ "<country/>"
			+ "<url/>"
			+ "</Merchant>"
			+ "<Purchase>"
			+ "<xid/>"
			+ "<date/>"
			+ "<amount/>"
			+ "<purchAmount/>"
			+ "<currency/>"
			+ "<exponent/>"
			+ "</Purchase>"
			+ "<CH>"
			+ "<acctID/>"
			+ "<expiry/>"
			+ "</CH>"
			+ "</PAReq>"
			+ "</Message>"
			+ "</ThreeDSecure>";

	/**
	 * Local Log4J logger
	 */
	private transient Logger logger =
		MPILogger.getLogger(PAReqMessage.class.getName());

	// Message structure
	private String merAcqBIN;
	private String merID;
	private String merName;
	private String merCountry;
	private String merURL;
	private String purXid;
	private String purDate;
	private String purDispAmount;
	private String purAmount;
	private String purCurrency;
	private String purExponent;
	private String purDesc;
	private String purRecurFrequency;
	private String purRecurExpiry;
	private String purInstall;
	private String chAcctID;
	private String chExpiry;
	private Extension[] extension;

	// Constant validation error string
	private static final String CHEXP_ERRMSG =
		"Null, size is 0 or size exceeds 4.";
	private static final String CHEXP_ERRMSG_LONG =
		"Invalid field: [PAReq.CH.expiry]. - " + CHEXP_ERRMSG;

	private static final String PDATE_ERRMSG =
		"Null, size is 0 or size exceeds 17.";
	private static final String PDATE_ERRMSG_LONG =
		"Invalid field: [PAReq.Purchase.date] - " + PDATE_ERRMSG;

	private static final String RECURCOEXIST_ERRMSG =
		"[Recur.frequency] & [Recur.endRecur] should both be specified.";
	private static final String RECURCOEXIST_ERRMSG_LONG =
		"Invalid message: [PAReq.id] - " + RECURCOEXIST_ERRMSG;

	private final static String CRITICAL_ERRMSG =
		"Critical extension not supported";
	private final static String CRITICAL_ERRMSG_LONG =
		"Invalid message: [PAReq] - " + CRITICAL_ERRMSG;

	private static final String TERMURL2_ERRMSG =
		"URL Specification is not valid";
	private static final String TERMURL2_ERRMSG_LONG =
		"Invalid field: [PAReq.Merchant.url] - " + TERMURL2_ERRMSG;

	/**
	 * Constructor
	 */
	public PAReqMessage() throws MessagingException {
		//Set messsage version
		setVersion(MSG_VERSION);

		//Set message type
		setType(MSG_TYPE);
	}

	/**
	 * XMLSerializable interface method.
	 * This method consturcts a DOM document which represents this message.
	 * @return DOM document object
	 */
	public Document toXML() {
		try {

			//Create document template
			Document dom = XMLUtil.createDocument(PAREQ_XML_STRING, false);

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
					"/ThreeDSecure/Message");
			element.setAttribute("id", getId());

			//Set version (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/ThreeDSecure/Message/PAReq/version");
			element.appendChild(dom.createTextNode(getVersion()));

			//Set Merchant acquirer BIN (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/ThreeDSecure/Message/PAReq/Merchant/acqBIN");
			element.appendChild(dom.createTextNode(getMerAcqBIN()));

			//Set Merchant ID (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/ThreeDSecure/Message/PAReq/Merchant/merID");
			element.appendChild(dom.createTextNode(getMerID()));

			//Set Merchant name (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/ThreeDSecure/Message/PAReq/Merchant/name");
			element.appendChild(dom.createTextNode(getMerName()));

			//Set Merchant country code (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/ThreeDSecure/Message/PAReq/Merchant/country");
			element.appendChild(dom.createTextNode(getMerCountry()));

			//Set Merchant URL (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/ThreeDSecure/Message/PAReq/Merchant/url");
			element.appendChild(dom.createTextNode(getMerURL()));

			//Set Purchase Transaction ID (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/ThreeDSecure/Message/PAReq/Purchase/xid");
			element.appendChild(dom.createTextNode(getPurXid()));

			//Set Purchase date & time (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/ThreeDSecure/Message/PAReq/Purchase/date");
			element.appendChild(dom.createTextNode(getPurDate()));

			//Set Purchase display amount (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/ThreeDSecure/Message/PAReq/Purchase/amount");
			element.appendChild(dom.createTextNode(getPurDispAmount()));

			//Set Purchase amount (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/ThreeDSecure/Message/PAReq/Purchase/purchAmount");
			element.appendChild(dom.createTextNode(getPurAmount()));

			//Set Purchase currency (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/ThreeDSecure/Message/PAReq/Purchase/currency");
			element.appendChild(dom.createTextNode(getPurCurrency()));

			//Set Purchase exponent (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/ThreeDSecure/Message/PAReq/Purchase/exponent");
			element.appendChild(dom.createTextNode(getPurExponent()));

			//Set Purchase description (Optional)
			if (getPurDesc() != null) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/ThreeDSecure/Message/PAReq/Purchase");
				Element child = dom.createElement("desc");
				child.appendChild(dom.createTextNode(getPurDesc()));
				element.appendChild(child);
			}

			//Set Purchase Recurring (Optional)
			if (isNotNull(getPurRecurFrequency())
				|| isNotNull(getPurRecurExpiry())) {
				if (isNotNull(getPurRecurFrequency())
					&& isNotNull(getPurRecurExpiry())) {
					element =
						(Element) XMLUtil.getFirstNodeByXPath(
							dom,
							"/ThreeDSecure/Message/PAReq/Purchase");
					Element child = dom.createElement("Recur");
					Element frequencyElement = dom.createElement("frequency");
					Element endRecurElement = dom.createElement("endRecur");
					frequencyElement.appendChild(
						dom.createTextNode(getPurRecurFrequency()));
					endRecurElement.appendChild(
						dom.createTextNode(getPurRecurExpiry()));
					child.appendChild(frequencyElement);
					child.appendChild(endRecurElement);
					element.appendChild(child);
				} else {
					this.logger.error(
						"Failed to create PAReq message document. [Recurring frequency & expiry should be co-existed.]");
					return null;
				}
			}

			//Set Purchase installment (Optional)
			if (isNotNull(getPurInstall())) {
				element =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/ThreeDSecure/Message/PAReq/Purchase");
				Element child = dom.createElement("install");
				child.appendChild(dom.createTextNode(getPurInstall()));
				element.appendChild(child);
			}

			//Set Account ID (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/ThreeDSecure/Message/PAReq/CH/acctID");
			element.appendChild(dom.createTextNode(getChAcctID()));

			//Set Card Expiry Date (Mandatory)
			element =
				(Element) XMLUtil.getFirstNodeByXPath(
					dom,
					"/ThreeDSecure/Message/PAReq/CH/expiry");
			element.appendChild(dom.createTextNode(getChExpiry()));

			//Set Extensions (Optional)
			if ((getExtension() != null) && (getExtension().length != 0)) {
				//Get extensions
				Extension[] extensions = getExtension();

				//Get PAReq element
				Element pareqElement =
					(Element) XMLUtil.getFirstNodeByXPath(
						dom,
						"/ThreeDSecure/Message/PAReq");

				//Append all extension elements
				for (int i = 0; i < extensions.length; i++) {
					//Get individual extension
					Extension extension = extensions[i];

					//Append Extension element to VEReq element
					pareqElement.appendChild(
						dom.importNode(extension.toXML(), true));
				}

			}

			return dom;
		} catch (Exception e) {
			this.logger.error(
				"Failed to convert PAReq message to DOM document.",
				e);
			return null;
		}
	}

	/**
	 * Validation method for the PAReq message
	 */
	public boolean validate() throws MessagingException {
		//Check mandatory fields
		MessageValidator.validateField(
			"UNKNOWN",
			getId(),
			"[Message.id]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getMerAcqBIN(),
			"[PAReq.acqBIN]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getMerID(),
			"[PAReq.Merchant.merID]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getMerName(),
			"[PAReq.Merchant.name]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getMerCountry(),
			"[PAReq.Merchant.country]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getMerURL(),
			"[PAReq.Merchant.url]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getPurXid(),
			"[PAReq.Purchase.xid]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getPurDate(),
			"[PAReq.Purchase.date]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getPurAmount(),
			"[PAReq.Purchase.purchAmount]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getPurDispAmount(),
			"[PAReq.Purchase.amount]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getPurCurrency(),
			"[PAReq.Purchase.currency]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getPurExponent(),
			"[PAReq.Purchase.exponent]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getChAcctID(),
			"[PAReq.CH.acctID]",
			1,
			-1,
			true);
		MessageValidator.validateField(
			getId(),
			getChExpiry(),
			"[PAReq.CH.expiry]",
			1,
			-1,
			true);

		//Check Fields: Purchase Recurring Frequence & Expiry
		if ((getPurRecurFrequency() != null)
			&& (getPurRecurFrequency().length() > 0)) {
			if ((getPurRecurExpiry() == null)
				|| (getPurRecurExpiry().length() == 0)) {
				this.logger.error(RECURCOEXIST_ERRMSG_LONG);
				throw new MessagingException(
					getId(),
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"[PAReq.Recur.frequency, PAReq.Recur.endRecur]",
					RECURCOEXIST_ERRMSG,
					RECURCOEXIST_ERRMSG_LONG);
			}
		}

		if ((getPurRecurExpiry() != null)
			&& (getPurRecurExpiry().length() != 0)) {
			if ((getPurRecurFrequency() == null)
				|| (getPurRecurFrequency().length() == 0)) {
				this.logger.error(RECURCOEXIST_ERRMSG_LONG);
				throw new MessagingException(
					getId(),
					ErrorCodes.ERROR_CODE_5,
					ErrorCodes.ERROR_MESSAGE_5,
					"[PAReq.Recur.frequency, PAReq.Recur.endRecur]",
					RECURCOEXIST_ERRMSG,
					RECURCOEXIST_ERRMSG_LONG);
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
						"[PAReq.Extension]",
						CRITICAL_ERRMSG,
						CRITICAL_ERRMSG_LONG);
				}
			}
		}

		return true;
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
			sb.append("\"><PAReq><version>");
			sb.append(XMLUtil.filterSpecialChars(getVersion()));
			sb.append("</version><Merchant><acqBIN>");
			sb.append(XMLUtil.filterSpecialChars(getMerAcqBIN()));
			sb.append("</acqBIN><merID>");
			sb.append(XMLUtil.filterSpecialChars(getMerID()));
			sb.append("</merID><name>");
			sb.append(XMLUtil.filterSpecialChars(getMerName()));
			sb.append("</name><country>");
			sb.append(XMLUtil.filterSpecialChars(getMerCountry()));
			sb.append("</country><url>");
			sb.append(XMLUtil.filterSpecialChars(getMerURL()));
			sb.append("</url></Merchant><Purchase><xid>");
			sb.append(XMLUtil.filterSpecialChars(getPurXid()));
			sb.append("</xid><date>");
			sb.append(XMLUtil.filterSpecialChars(getPurDate()));
			sb.append("</date><amount>");
			sb.append(XMLUtil.filterSpecialChars(getPurDispAmount()));
			sb.append("</amount><purchAmount>");
			sb.append(XMLUtil.filterSpecialChars(getPurAmount()));
			sb.append("</purchAmount><currency>");
			sb.append(XMLUtil.filterSpecialChars(getPurCurrency()));
			sb.append("</currency><exponent>");
			sb.append(XMLUtil.filterSpecialChars(getPurExponent()));
			sb.append("</exponent>");

			// add purchase description (Optional)
			if (isNotNull(getPurDesc())) {
				sb.append("<desc>");
				sb.append(XMLUtil.filterSpecialChars(getPurDesc()));
				sb.append("</desc>");
			}

			// add Purchase Recurring (Optional)
			if (isNotNull(getPurRecurFrequency())
				|| isNotNull(getPurRecurExpiry())) {
				if (isNotNull(getPurRecurFrequency())
					&& isNotNull(getPurRecurExpiry())) {
					sb.append("<Recur><frequency>");
					sb.append(XMLUtil.filterSpecialChars(getPurRecurFrequency()));
					sb.append("</frequency><endRecur>");
					sb.append(XMLUtil.filterSpecialChars(getPurRecurExpiry()));
					sb.append("</endRecur></Recur>");
				} else {
					this.logger.error(
						"Failed to create PAReq message document. [Recurring frequency & endRecur should be co-existed.]");
					return null;
				}
			}

			// add Purchase installment (Optional)
			if (isNotNull(getPurInstall())) {
				sb.append("<install>");
				sb.append(XMLUtil.filterSpecialChars(getPurInstall()));
				sb.append("</install>");
			}

			sb.append("</Purchase><CH><acctID>");
			sb.append(XMLUtil.filterSpecialChars(getChAcctID()));
			sb.append("</acctID><expiry>");
			sb.append(XMLUtil.filterSpecialChars(getChExpiry()));
			sb.append("</expiry></CH>");

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

			sb.append("</PAReq></Message></ThreeDSecure>");

			return sb.toString();
		} catch (Exception e) {
			logger.error("Failed to convert PAReq message to XML string.", e);
			return null;
		}

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
	 * Gets the merAcqBIN
	 * @return Returns a String
	 */
	public String getMerAcqBIN() {
		return merAcqBIN;
	}
	/**
	 * Sets the merAcqBIN
	 * @param in The merAcqBIN to set
	 */
	public void setMerAcqBIN(String in) throws MessagingException {
		MessageValidator.validateDigitField(
			getId(),
			in,
			"[PAReq.Merchant.acqBIN]",
			1,
			11,
			true);
		this.merAcqBIN = in;
	}

	/**
	 * Gets the merCountry
	 * @return Returns a String
	 */
	public String getMerCountry() {
		return merCountry;
	}
	/**
	 * Sets the merCountry
	 * @param in The merCountry to set
	 */
	public void setMerCountry(String in) throws MessagingException {
		MessageValidator.validateField(
			getId(),
			in,
			"[PAReq.Merchant.country]",
			1,
			3,
			true);
		this.merCountry = in;
	}

	/**
	 * Gets the merURL
	 * @return Returns a String
	 */
	public String getMerURL() {
		return merURL;
	}
	/**
	 * Sets the merURL
	 * @param in The merURL to set
	 */
	public void setMerURL(String in) throws MessagingException {
		MessageValidator.validateField(
			getId(),
			in,
			"[PAReq.Merchant.url]",
			1,
			255,
			true);

		// Verify URL
		if (!URLValidator.isValid(in)) {
			this.logger.error(TERMURL2_ERRMSG_LONG + " Value: {" + in + "}");
			throw new MessagingException(
				"UNKNOWN",
				ErrorCodes.ERROR_CODE_5,
				ErrorCodes.ERROR_MESSAGE_5,
				"[PAReq.Merchant.url]",
				TERMURL2_ERRMSG,
				TERMURL2_ERRMSG_LONG + " Value: {" + in + "}");
		}

		this.merURL = in;
	}

	/**
	 * Gets the purXid
	 * @return Returns a String
	 */
	public String getPurXid() {
		return purXid;
	}
	/**
	 * Sets the purXid
	 * @param in The purXid to set
	 */
	public void setPurXid(String in) throws MessagingException {
		MessageValidator.validateField(
			getId(),
			in,
			"[PAReq.Purchase.xid]",
			1,
			28,
			true);
		this.purXid = in;
	}

	/**
	 * Gets the purDate
	 * @return Returns a String
	 */
	public String getPurDate() {
		return purDate;
	}

	/**
	 * Sets the purDate
	 * @param in The purDate to set
	 */
	public void setPurDate(String in) throws MessagingException {
		//Check Field: Purchase Date & Time
		MessageValidator.validateField(
			getId(),
			in,
			"[PAReq.Purchase.date]",
			1,
			28,
			true);
		//Check date format
		try {
			//Create formatter
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
			sdf.setLenient(false);

			//Verify data format
			sdf.parse(in);

			//Release formatter
			sdf = null;
		} catch (ParseException pe) {
			this.logger.error("Invalid date format of Purchase Date.");
			throw new MessagingException(
				getId(),
				ErrorCodes.ERROR_CODE_5,
				ErrorCodes.ERROR_MESSAGE_5,
				"[PAReq.Purchase.date]",
				PDATE_ERRMSG,
				PDATE_ERRMSG_LONG + " Value: {" + in + "}");
		}

		this.purDate = in;
	}

	/**
	 * Gets the purDispAmount
	 * @return Returns a String
	 */
	public String getPurDispAmount() {
		return purDispAmount;
	}
	/**
	 * Sets the purDispAmount
	 * @param in The purDispAmount to set
	 */
	public void setPurDispAmount(String in) throws MessagingException {
		MessageValidator.validateField(
			getId(),
			in,
			"[PAReq.Purchase.amount]",
			1,
			20,
			true);
		this.purDispAmount = in;
	}

	/**
	 * Gets the purAmount
	 * @return Returns a String
	 */
	public String getPurAmount() {
		return purAmount;
	}
	/**
	 * Sets the purAmount
	 * @param in The purAmount to set
	 */
	public void setPurAmount(String in) throws MessagingException {
		MessageValidator.validateField(
			getId(),
			in,
			"[PAReq.Purchase.purchAmount]",
			1,
			12,
			true);
		this.purAmount = in;
	}

	/**
	 * Gets the purCurrency
	 * @return Returns a String
	 */
	public String getPurCurrency() {
		return purCurrency;
	}
	/**
	 * Sets the purCurrency
	 * @param in The purCurrency to set
	 */
	public void setPurCurrency(String in) throws MessagingException {
		MessageValidator.validateDigitField(
			getId(),
			in,
			"[PAReq.Purchase.currency]",
			1,
			3,
			true);

		// Validate currency using currency code table
		if (!CurrencyCode.isCodeValid(in)) {
			this.logger.error(
				"Invalid field: [PAReq.Purchase.currency] - Currency used is unknown. Value: {"
					+ in
					+ "}");
			throw new MessagingException(
				getId(),
				ErrorCodes.ERROR_CODE_5,
				ErrorCodes.ERROR_MESSAGE_5,
				"[PAReq.Purchase.currency]",
				"Currency used is unknown.",
				"Invalid field: [PAReq.Purchase.currency] - Currency used is unknown. Value: {"
					+ in
					+ "}");
		}
		this.purCurrency = in;
	}

	/**
	 * Gets the purExponent
	 * @return Returns a String
	 */
	public String getPurExponent() {
		return purExponent;
	}
	/**
	 * Sets the purExponent
	 * @param in The purExponent to set
	 */
	public void setPurExponent(String in) throws MessagingException {
		MessageValidator.validateField(
			getId(),
			in,
			"[PAReq.Purchase.exponent]",
			1,
			1,
			true);
		this.purExponent = in;
	}

	/**
	 * Gets the purDesc
	 * @return Returns a String
	 */
	public String getPurDesc() {
		return purDesc;
	}
	/**
	 * Sets the purDesc
	 * @param in The purDesc to set
	 */
	public void setPurDesc(String in) throws MessagingException {
		MessageValidator.validateField(
			getId(),
			in,
			"[PAReq.Purchase.desc]",
			1,
			125,
			false);
		this.purDesc = in;
	}

	/**
	 * Gets the purRecurFrequency
	 * @return Returns a String
	 */
	public String getPurRecurFrequency() {
		return purRecurFrequency;
	}
	/**
	 * Sets the purRecurFrequency
	 * @param in The purRecurFrequency to set
	 */
	public void setPurRecurFrequency(String in) throws MessagingException {
		//Check Field: Purchase Recurring
		MessageValidator.validateField(
			getId(),
			in,
			"[PAReq.Purchase.Recur.frequency]",
			1,
			3,
			false);
		this.purRecurFrequency = in;
	}

	/**
	 * Gets the purRecurExpiry
	 * @return Returns a String
	 */
	public String getPurRecurExpiry() {
		return purRecurExpiry;
	}
	/**
	 * Sets the purRecurExpiry
	 * @param in The purRecurExpiry to set
	 */
	public void setPurRecurExpiry(String in) throws MessagingException {
		//Check Field: Purchase Recurring
		MessageValidator.validateField(
			getId(),
			in,
			"[PAReq.Purchase.Recur.endRecur]",
			1,
			8,
			false);
		this.purRecurExpiry = in;
	}

	/**
	 * Gets the merName
	 * @return Returns a String
	 */
	public String getMerName() {
		return merName;
	}
	/**
	 * Sets the merName
	 * @param in The merName to set
	 */
	public void setMerName(String in) throws MessagingException {
		//Check Field: Merchant Name
		MessageValidator.validateField(
			getId(),
			in,
			"[PAReq.Merchant.name]",
			1,
			25,
			true);
		this.merName = in;
	}

	/**
	 * Gets the purInstall
	 * @return Returns a String
	 */
	public String getPurInstall() {
		return purInstall;
	}
	/**
	 * Sets the purInstall
	 * @param in The purInstall to set
	 */
	public void setPurInstall(String in) throws MessagingException {
		//Check Field: Purchase Install
		MessageValidator.validateField(
			getId(),
			in,
			"[PAReq.Purchase.install]",
			1,
			12,
			false);
		this.purInstall = in;
	}

	/**
	 * Gets the chAcctID
	 * @return Returns a String
	 */
	public String getChAcctID() {
		return chAcctID;
	}
	/**
	 * Sets the chAcctID
	 * @param in The chAcctID to set
	 */
	public void setChAcctID(String in) throws MessagingException {
		//Check Field: Cardholder Account ID
		MessageValidator.validateField(
			getId(),
			in,
			"[PAReq.CH.acctID]",
			1,
			28,
			true);
		this.chAcctID = in;
	}

	/**
	 * Gets the chExpiry
	 * @return Returns a String
	 */
	public String getChExpiry() {
		return chExpiry;
	}
	/**
	 * Sets the chExpiry
	 * @param in The chExpiry to set
	 */
	public void setChExpiry(String in) throws MessagingException {
		//Check Field: Card expiry date
		MessageValidator.validateField(
			getId(),
			in,
			"[PAReq.CH.expiry]",
			1,
			4,
			true);

		//Check date format
		try {
			//Create formatter
			SimpleDateFormat sdf = new SimpleDateFormat("yyMM");
			sdf.setLenient(false);

			//Verify data format
			sdf.parse(in);

			//Release formatter	
			sdf = null;
		} catch (ParseException pe) {
			this.logger.error("Invalid date format of Card Expiry Date.");
			throw new MessagingException(
				getId(),
				ErrorCodes.ERROR_CODE_5,
				ErrorCodes.ERROR_MESSAGE_5,
				"[PAReq.CH.expiry]",
				CHEXP_ERRMSG,
				CHEXP_ERRMSG_LONG + " Value: {" + in + "}");
		}
		this.chExpiry = in;
	}

	/**
	 * Gets the merID
	 * @return Returns a String
	 */
	public String getMerID() {
		return merID;
	}
	/**
	 * Sets the merID
	 * @param in The merID to set
	 */
	public void setMerID(String in) throws MessagingException {
		//Check Field: Merchant ID
		MessageValidator.validateField(
			getId(),
			in,
			"[PAReq.Merchant.merID]",
			1,
			24,
			true);
		this.merID = in;
	}

	/**
	 * Gets the extension
	 * @return Returns a Extension[]
	 */
	public Extension[] getExtension() {
		return extension;
	}
	/**
	 * Sets the extension
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