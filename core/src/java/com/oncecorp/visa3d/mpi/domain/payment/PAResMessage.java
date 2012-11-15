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

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Description: Implement message class for PARes. The PARes (Payer
 * Authentication Response) message is sent by the ACS in response to
 * the PAReq regardless of whether authentication is successful.
 *
 * @version $Revision: 27 $
 * @author	Alan Zhang
 */
public class PAResMessage extends Message {
    /**
     * Message Type
     */
    public final static String MSG_TYPE = "PARes";

    // Message Version
    // ---------------------------------------------------------
    // This message implementation supports BOTH 1.0.1 and 1.0.2
    // ---------------------------------------------------------
    //	public final static String MSG_VERSION = "1.0.1";
    public final static String MSG_VERSION = "1.0.2";

    /**
     * XML Message Structure
     */
    private static final String PARES_XML_STRING =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<ThreeDSecure>"
            + "<Message>"
            + "<PARes>"
            + "<version/>"
            + "<Merchant>"
            + "<acqBIN/>"
            + "<merID/>"
            + "</Merchant>"
            + "<Purchase>"
            + "<xid/>"
            + "<date/>"
            + "<purchAmount/>"
            + "<currency/>"
            + "<exponent/>"
            + "</Purchase>"
            + "<pan/>"
            + "<TX>"
            + "<time/>"
            + "<status/>"
            + "</TX>"
            + "</PARes>"
            + "</Message>"
            + "</ThreeDSecure>";

    /**
     * Local Log4J logger
     */
    private transient Logger logger =
            MPILogger.getLogger(PAResMessage.class.getName());

    // Message attribute structure definition
    private String refId;
    private String merAcqBIN;
    private String merID;
    private String purXid;
    private String purDate;
    private String purAmount;
    private String purCurrency;
    private String purExponent;
    private String pan;
    private String txTime;
    private String txStatus;
    private String txCavv;
    private String txEci;
    private String txCavvAlgorithm;
    private String ireqCode;
    private String ireqDetail;
    private String ireqVendorCode;
    private Extension[] extension;
    private String signature;

    // Constant validation error string
    private static final String IREQCODE_ERRMSG = "Size exceeds 2.";
    private static final String IREQCODE_ERRMSG_LONG =
            "Invalid field: [PARes.IReq.iReqCode] - " + IREQCODE_ERRMSG;

    private static final String PDATE_ERRMSG =
            "Invalid date format; Must be YYYYMMDD HH:MM:SS";
    private static final String PDATE_ERRMSG_LONG =
            "Invalid field: [PARes.Purchase.date] - " + PDATE_ERRMSG;

    private static final String PURCURR_ERRMSG =
            "Purchase currency code is not a ISO 4217 currency code.";
    private static final String PURCURR_ERRMSG_LONG =
            "Invalid field: [PARes.Purchase.currency] - " + PURCURR_ERRMSG;

    private static final String EXPMATCH_ERRMSG =
            "Exponent for [PARes.Purchase.currency] does not match ISO 4217 exponent value.";
    private static final String EXPMATCH_ERRMSG_LONG =
            "Invalid message: [PARes] - " + EXPMATCH_ERRMSG;

    private static final String TXTIME_ERRMSG =
            "Invalid date format; Must be YYYYMMDD HH:MM:SS";
    private static final String TXTIME_ERRMSG_LONG =
            "Invalid field: [PARes.TX.time] - " + TXTIME_ERRMSG;

    private static final String ZEROPAN_ERRMSG =
            "Value of [PARes.pan] should be ALL zeros when [PARes.TX.Status] NOT equal to 'Y'.";
    private static final String ZEROPAN_ERRMSG_LONG =
            "Invalid field: [PARes.pan] - " + ZEROPAN_ERRMSG;

    private final static String CRITICAL_ERRMSG =
            "Critical extension not supported";
    private final static String CRITICAL_ERRMSG_LONG =
            "Invalid message: [PARes] - " + CRITICAL_ERRMSG;

    private final static String IDETAIL_ERRMSG =
            "[IReq.iReqCode] is empty but [IReq.iReqDetail] contains a value.";
    private final static String IDETAIL_ERRMSG_LONG =
            "Invalid message: [VERes] - " + IDETAIL_ERRMSG;

    private final static String IREQ_ERRMSG =
            "[status] is equal to 'Y' or 'A' and [IReq.iReqCode] is specified.";
    private final static String IREQ_ERRMSG_LONG =
            "Invalid message: [PARes] - " + IREQ_ERRMSG;

    private final static String VCODE_ERRMSG =
            "[IReq.iReqCode] is empty but [IReq.vendorCode] contains a value.";
    private final static String VCODE_ERRMSG_LONG =
            "Invalid message: [VERes] - " + VCODE_ERRMSG;

    private final static String STATUS_ERRMSG = "Unknown TxStatus.";
    private final static String STATUS_ERRMSG_LONG =
            "Invalid message: [PARes] - " + STATUS_ERRMSG;

    private final static String ALGORITHM_ERRMSG =
            "[PARes.TX.cavvAlgorithm should be a positive integer.";
    private final static String ALGORITHM_ERRMSG_LONG =
            "Invalid message: [PARes] - " + ALGORITHM_ERRMSG;

    /*
     * version 1.0.1 error message
     */
    private final static String ALGORITHM_ERRMSG2 =
            "[PARes.TX.cavvAlgorithm should be '0' or '1'.";
    private final static String ALGORITHM_ERRMSG2_LONG =
            "Invalid message: [PARes] - " + ALGORITHM_ERRMSG2;

    /**
     * Constructor
     */
    public PAResMessage() throws MessagingException {
        //Set messsage type
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
            Document dom = toXMLWithoutSignature();

            //Sanity check
            if (dom == null) {
                this.logger.error(
                        "toXMLWithoutSignature() returns NULL result.");
                return null;
            }

            //Set XML Signature
            Document signatureDoc =
                    XMLUtil.createDocument(getSignature(), false);
            Element root = signatureDoc.getDocumentElement();

            Element element =
                    (Element) XMLUtil.getFirstNodeByXPath(
                            dom,
                            "/ThreeDSecure/Message");
            element.appendChild(dom.importNode(root, true));

            return dom;
        }
        catch (Exception e) {
            this.logger.error(
                    "Failed to convert PARes message to DOM document.",
                    e);
            return null;
        }
    }

    /**
     * Convenient method to create XML document representing this message without signature element
     */
    public Document toXMLWithoutSignature() {
        try {
            //Create document template
            Document dom = XMLUtil.createDocument(PARES_XML_STRING, false);

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

            //Set Reference id (Mandatory)
            element =
                    (Element) XMLUtil.getFirstNodeByXPath(
                            dom,
                            "/ThreeDSecure/Message/PARes");
            element.setAttribute("id", getRefId());

            //Set version (Mandatory)
            element =
                    (Element) XMLUtil.getFirstNodeByXPath(
                            dom,
                            "/ThreeDSecure/Message/PARes/version");
            element.appendChild(dom.createTextNode(getVersion()));

            //Set Merchant acquirer BIN (Mandatory)
            element =
                    (Element) XMLUtil.getFirstNodeByXPath(
                            dom,
                            "/ThreeDSecure/Message/PARes/Merchant/acqBIN");
            element.appendChild(dom.createTextNode(getMerAcqBIN()));

            //Set Merchant ID (Mandatory)
            element =
                    (Element) XMLUtil.getFirstNodeByXPath(
                            dom,
                            "/ThreeDSecure/Message/PARes/Merchant/merID");
            element.appendChild(dom.createTextNode(getMerID()));

            //Set Purchase Transaction ID (Mandatory)
            element =
                    (Element) XMLUtil.getFirstNodeByXPath(
                            dom,
                            "/ThreeDSecure/Message/PARes/Purchase/xid");
            element.appendChild(dom.createTextNode(getPurXid()));

            //Set Purchase date & time (Mandatory)
            element =
                    (Element) XMLUtil.getFirstNodeByXPath(
                            dom,
                            "/ThreeDSecure/Message/PARes/Purchase/date");
            element.appendChild(dom.createTextNode(getPurDate()));

            //Set Purchase amount (Mandatory)
            element =
                    (Element) XMLUtil.getFirstNodeByXPath(
                            dom,
                            "/ThreeDSecure/Message/PARes/Purchase/purchAmount");
            element.appendChild(dom.createTextNode(getPurAmount()));

            //Set Purchase currency (Mandatory)
            element =
                    (Element) XMLUtil.getFirstNodeByXPath(
                            dom,
                            "/ThreeDSecure/Message/PARes/Purchase/currency");
            element.appendChild(dom.createTextNode(getPurCurrency()));

            //Set Purchase exponent (Mandatory)
            element =
                    (Element) XMLUtil.getFirstNodeByXPath(
                            dom,
                            "/ThreeDSecure/Message/PARes/Purchase/exponent");
            element.appendChild(dom.createTextNode(getPurExponent()));

            //Set Cardholder PAN (Mandatory)
            element =
                    (Element) XMLUtil.getFirstNodeByXPath(
                            dom,
                            "/ThreeDSecure/Message/PARes/pan");
            element.appendChild(dom.createTextNode(getPan()));

            //Set Transaction signature Date & Time (Mandatory)
            element =
                    (Element) XMLUtil.getFirstNodeByXPath(
                            dom,
                            "/ThreeDSecure/Message/PARes/TX/time");
            element.appendChild(dom.createTextNode(getTxTime()));

            //Set Transaction Status (Mandatory)
            element =
                    (Element) XMLUtil.getFirstNodeByXPath(
                            dom,
                            "/ThreeDSecure/Message/PARes/TX/status");
            element.appendChild(dom.createTextNode(getTxStatus()));

            //Set Cardholder Authentication Verification  Value (Optional)
            if (isNotNull(getTxCavv())) {
                element =
                        (Element) XMLUtil.getFirstNodeByXPath(
                                dom,
                                "/ThreeDSecure/Message/PARes/TX");
                Element child = dom.createElement("cavv");
                child.appendChild(dom.createTextNode(getTxCavv()));
                element.appendChild(child);
            }

            //Set Eletronic Commerce Indicator (Optional)
            if (isNotNull(getTxEci())) {
                element =
                        (Element) XMLUtil.getFirstNodeByXPath(
                                dom,
                                "/ThreeDSecure/Message/PARes/TX");
                Element child = dom.createElement("eci");
                child.appendChild(dom.createTextNode(getTxEci()));
                element.appendChild(child);
            }

            //Set CAVV Algorithm (Optional)
            if (isNotNull(getTxCavvAlgorithm())) {
                element =
                        (Element) XMLUtil.getFirstNodeByXPath(
                                dom,
                                "/ThreeDSecure/Message/PARes/TX");
                Element child = dom.createElement("cavvAlgorithm");
                child.appendChild(dom.createTextNode(getTxCavvAlgorithm()));
                element.appendChild(child);
            }

            //Set Invalid Request Code (Optional)
            if (isNotNull(getIreqCode())) {
                element =
                        (Element) XMLUtil.getFirstNodeByXPath(
                                dom,
                                "/ThreeDSecure/Message/PARes");
                Element parent = dom.createElement("IReq");
                Element child = dom.createElement("iReqCode");
                child.appendChild(dom.createTextNode(getIreqCode()));
                parent.appendChild(child);

                //Set Invalid Request Detail (Optional)
                if (isNotNull(getIreqDetail())) {
                    child = dom.createElement("iReqDetail");
                    child.appendChild(dom.createTextNode(getIreqDetail()));
                    parent.appendChild(child);
                }

                //Set Vendor Code (Optional)
                if (isNotNull(getIreqVendorCode())) {
                    child = dom.createElement("vendorCode");
                    child.appendChild(dom.createTextNode(getIreqVendorCode()));
                    parent.appendChild(child);
                }

                element.appendChild(parent);
            }

            //Set Extensions (Optional)
            if ((getExtension() != null) && (getExtension().length != 0)) {
                //Get extensions
                Extension[] extensions = getExtension();

                //Get PARes element
                Element paresElement =
                        (Element) XMLUtil.getFirstNodeByXPath(
                                dom,
                                "/ThreeDSecure/Message/PARes");

                //Append all extension elements
                for (int i = 0; i < extensions.length; i++) {
                    //Get individual extension
                    Extension extension = extensions[i];

                    //Append Extension element to VEReq element
                    paresElement.appendChild(
                            dom.importNode(extension.toXML(), true));
                }

            }

            return dom;
        }
        catch (Exception e) {
            this.logger.error(
                    "Failed to convert PARes message to DOM document.",
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
            sb.append("\"><PARes id=\"");
            sb.append(XMLUtil.filterSpecialChars(getRefId()));
            sb.append("\"><version>");
            sb.append(XMLUtil.filterSpecialChars(getVersion()));
            sb.append("</version><Merchant><acqBIN>");
            sb.append(XMLUtil.filterSpecialChars(getMerAcqBIN()));
            sb.append("</acqBIN><merID>");
            sb.append(XMLUtil.filterSpecialChars(getMerID()));
            sb.append("</merID></Merchant><Purchase><xid>");
            sb.append(XMLUtil.filterSpecialChars(getPurXid()));
            sb.append("</xid><date>");
            sb.append(XMLUtil.filterSpecialChars(getPurDate()));
            sb.append("</date><purchAmount>");
            sb.append(XMLUtil.filterSpecialChars(getPurAmount()));
            sb.append("</purchAmount><currency>");
            sb.append(XMLUtil.filterSpecialChars(getPurCurrency()));
            sb.append("</currency><exponent>");
            sb.append(XMLUtil.filterSpecialChars(getPurExponent()));
            sb.append("</exponent></Purchase><pan>");
            sb.append(XMLUtil.filterSpecialChars(getPan()));
            sb.append("</pan><TX><time>");
            sb.append(XMLUtil.filterSpecialChars(getTxTime()));
            sb.append("</time><status>");
            sb.append(XMLUtil.filterSpecialChars(getTxStatus()));
            sb.append("</status>");

            // add Cardholder Authentication Verification  Value (Optional)
            if (isNotNull(getTxCavv())) {
                sb.append("<cavv>");
                sb.append(XMLUtil.filterSpecialChars(getTxCavv()));
                sb.append("</cavv>");
            }

            // add Eletronic Commerce Indicator (Optional)
            if (isNotNull(getTxEci())) {
                sb.append("<eci>");
                sb.append(XMLUtil.filterSpecialChars(getTxEci()));
                sb.append("</eci>");
            }

            // add CAVV Algorithm (Optional)
            if (isNotNull(getTxCavvAlgorithm())) {
                sb.append("<cavvAlgorithm>");
                sb.append(XMLUtil.filterSpecialChars(getTxCavvAlgorithm()));
                sb.append("</cavvAlgorithm>");
            }

            sb.append("</TX>");

            // add Invalid Request Code (Optional)
            if (isNotNull(getIreqCode())) {
                sb.append("<IReq><iReqCode>");
                sb.append(XMLUtil.filterSpecialChars(getIreqCode()));
                sb.append("</iReqCode>");

                // add Invalid Request Detail (Optional)
                if (isNotNull(getIreqDetail())) {
                    sb.append("<iReqDetail>");
                    sb.append(XMLUtil.filterSpecialChars(getIreqDetail()));
                    sb.append("</iReqDetail>");
                }

                // add Vendor Code (Optional)
                if (isNotNull(getIreqVendorCode())) {
                    sb.append("<vendorCode>");
                    sb.append(XMLUtil.filterSpecialChars(getIreqVendorCode()));
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

                    //Append Extension element to VEReq element
                    sb.append(extension.toString());
                }

            }

            sb.append("</PARes>");

            // add XML Signature
            String sigStr = getSignature();
            int idx = sigStr.indexOf("<Signature");
            sb.append(sigStr.substring(idx));

            sb.append("</Message></ThreeDSecure>");

            return sb.toString();
        }
        catch (Exception e) {
            logger.error("Failed to convert PARes message to XML string.", e);
            return null;
        }

    }

    /**
     * Overrides super class validate()
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
                getRefId(),
                "[PARes.refid]",
                1,
                -1,
                true);
        MessageValidator.validateField(
                getId(),
                getMerAcqBIN(),
                "[PARes.Merchant.acqBIN]",
                1,
                -1,
                true);
        MessageValidator.validateField(
                getId(),
                getMerID(),
                "[PARes.Merchant.merID]",
                1,
                -1,
                true);
        MessageValidator.validateField(
                getId(),
                getPurXid(),
                "[PARes.Purchase.xid]",
                1,
                -1,
                true);
        MessageValidator.validateField(
                getId(),
                getPurDate(),
                "[PARes.Purchase.date]",
                1,
                -1,
                true);
        MessageValidator.validateField(
                getId(),
                getPurAmount(),
                "[PARes.Purchase.purchAmount]",
                1,
                -1,
                true);
        MessageValidator.validateField(
                getId(),
                getPurCurrency(),
                "[PARes.Purchase.currency]",
                1,
                -1,
                true);
        MessageValidator.validateField(
                getId(),
                getPurExponent(),
                "[PARes.Purchase.exponent]",
                1,
                -1,
                true);
        MessageValidator.validateField(
                getId(),
                getTxTime(),
                "[PARes.TX.time]",
                1,
                -1,
                true);
        MessageValidator.validateField(
                getId(),
                getTxStatus(),
                "[PARes.TX.status]",
                1,
                -1,
                true);
        MessageValidator.validateField(
                getId(),
                getSignature(),
                "[Signature]",
                -1,
                -1,
                true);

        // Check match between currency and exponent
        String isoExponent =
                CurrencyCode.getExponentForCurrency(getPurCurrency());
        if ((isoExponent == null)
                || (!isoExponent.equalsIgnoreCase(getPurExponent()))) {
            this.logger.error(EXPMATCH_ERRMSG_LONG);
            throw new MessagingException(
                    getId(),
                    ErrorCodes.ERROR_CODE_5,
                    ErrorCodes.ERROR_MESSAGE_5,
                    "[PARes.Purchase.currency, PARes.Purchase.exponent]",
                    EXPMATCH_ERRMSG,
                    EXPMATCH_ERRMSG_LONG);
        }

        //Check Field: Transaction Status
        /*
         * [Alan's note - Feb 18, 2003]
         * Temp solution for differential validations of Trasnscation status &
         * cavvAlgorithm from version 1.0.1/1.0.2
         */
        if (getVersion().equals(PAResMessage.MSG_VERSION)) {
            // version 1.0.2 validation
            /*
             * [Martin's Note: Nov 1, 2002 12:17:30 PM]
             * In 1.0.2 of the protocol, TX.status = "A" is now
             * a valid status. Processing is similar to TX.status = "Y"
             */
            validate102txStatus();
        }
        else {
            // version 1.0.1 validation
            //Check Field: Transaction Status
            validate101txStatus();
        }

        // Validate IReqCode
        validateIReqCode();

        //Check Field: iReqDetail
        if (getIreqDetail() != null) {
            if (getIreqCode() == null) {
                this.logger.error(IDETAIL_ERRMSG_LONG);
                throw new MessagingException(
                        getId(),
                        ErrorCodes.ERROR_CODE_3,
                        ErrorCodes.ERROR_MESSAGE_3,
                        "[VERes.IReq.iReqCode, VERes.IReq.iReqDetail]",
                        IDETAIL_ERRMSG,
                        IDETAIL_ERRMSG_LONG);
            }
            else {
                if (getIreqCode().length() == 0) {
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
        if (getIreqVendorCode() != null) {
            if (getIreqCode() == null) {
                this.logger.error(VCODE_ERRMSG_LONG);
                throw new MessagingException(
                        getId(),
                        ErrorCodes.ERROR_CODE_3,
                        ErrorCodes.ERROR_MESSAGE_3,
                        "[VERes.IReq.iReqCode, VERes.IReq.vendorCode]",
                        VCODE_ERRMSG,
                        VCODE_ERRMSG_LONG);
            }
            else {
                if (getIreqCode().length() == 0) {
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

        // Validate optional extension part
        if ((extension != null) && (extension.length > 0)) {
            for (int i = 0; i < extension.length; i++) {
                // Iterate over all extension and throw an exception for any
                // critical extension that we do not support
                if (!Extension.isSupported(getType(), getVersion(), extension[i])) {
                    throw new MessagingException(
                            getId(),
                            ErrorCodes.ERROR_CODE_4,
                            ErrorCodes.ERROR_MESSAGE_4,
                            "[PARes.Extension]",
                            CRITICAL_ERRMSG,
                            CRITICAL_ERRMSG_LONG);
                }
            }
        }

        return true;
    }

    private void validateIReqCode() throws MessagingException {
        // Correction by mdufort@oncecorp.com for bug Id: #649
        // Check Field: iReqCode can be specified when status is = "Y" or "A")
        if ((getTxStatus().equalsIgnoreCase("y") || getTxStatus().equalsIgnoreCase("a"))
                && (getIreqCode() != null)
                && (getIreqCode().length() != 0)) {
            this.logger.error(IREQ_ERRMSG_LONG);
            throw new MessagingException(
                    getId(),
                    ErrorCodes.ERROR_CODE_5,
                    ErrorCodes.ERROR_MESSAGE_5,
                    "[PARes.IReq.iReqCode, PARes.status]",
                    IREQ_ERRMSG,
                    IREQ_ERRMSG_LONG);
        }
    }

    private void validate101txStatus() throws MessagingException {
        if (getTxStatus().equalsIgnoreCase("y")) {
            //Status is 'Y', then TX.Cavv, TX.eci, TX.CavvAlgorithm become mandatory
            //Check Field: Cardholder Authentication Verification  Value
            MessageValidator.validateField(
                    getId(),
                    getTxCavv(),
                    "[PARes.TX.cavv]",
                    28,
                    28,
                    true);

            //Check Field: Electronic Commerce Indicator
            MessageValidator.validateDigitField(
                    getId(),
                    getTxEci(),
                    "[PARes.TX.eci]",
                    2,
                    2,
                    true);

            //Check Field: Cavv Algorithm
            MessageValidator.validateDigitField(
                    getId(),
                    getTxCavvAlgorithm(),
                    "[PARes.TX.cavvAlgorithm]",
                    1,
                    1,
                    true);
        }
        else if (getTxStatus().equalsIgnoreCase("u") || getTxStatus().equalsIgnoreCase("n")) {
            //Status is "U" or "N", then Cardholder PAN should be all zeros.
            char[] pan = getPan().toCharArray();
            for (int i = 0; i < pan.length; i++) {
                if (pan[i] != '0') {
                    this.logger.error(ZEROPAN_ERRMSG_LONG);
                    throw new MessagingException(
                            getId(),
                            ErrorCodes.ERROR_CODE_5,
                            ErrorCodes.ERROR_MESSAGE_5,
                            "[PARes.pan]",
                            ZEROPAN_ERRMSG,
                            ZEROPAN_ERRMSG_LONG);
                }
            }
        }
        else {
            //Unknown status
            this.logger.error(STATUS_ERRMSG_LONG);
            throw new MessagingException(
                    getId(),
                    ErrorCodes.ERROR_CODE_5,
                    ErrorCodes.ERROR_MESSAGE_5,
                    "[PARes.TX.status]",
                    STATUS_ERRMSG,
                    STATUS_ERRMSG_LONG);
        }
    }

    private void validate102txStatus() throws MessagingException {
        if ((getTxStatus().equalsIgnoreCase("y")) || (getTxStatus().equalsIgnoreCase("a"))) {
            // Status is 'Y' or 'A', then TX.Cavv, TX.eci, TX.CavvAlgorithm become mandatory
            // Check Field: Cardholder Authentication Verification  Value
            MessageValidator.validateField(
                    getId(),
                    getTxCavv(),
                    "[PARes.TX.cavv]",
                    28,
                    28,
                    true);

            // Check Field: Electronic Commerce Indicator
            MessageValidator.validateDigitField(
                    getId(),
                    getTxEci(),
                    "[PARes.TX.eci]",
                    2,
                    2,
                    true);

            // Check Field: Cavv Algorithm
            MessageValidator.validateDigitField(
                    getId(),
                    getTxCavvAlgorithm(),
                    "[PARes.TX.cavvAlgorithm]",
                    1,
                    -1,
                    true);
        }
        else if (getTxStatus().equalsIgnoreCase("u") || getTxStatus().equalsIgnoreCase("n")) {
            // Status is "U" or "N", then Cardholder PAN should be all zeros.
            char[] pan = getPan().toCharArray();
            for (int i = 0; i < pan.length; i++) {
                if (pan[i] != '0') {
                    this.logger.error(ZEROPAN_ERRMSG_LONG);
                    throw new MessagingException(
                            getId(),
                            ErrorCodes.ERROR_CODE_5,
                            ErrorCodes.ERROR_MESSAGE_5,
                            "[PARes.pan]",
                            ZEROPAN_ERRMSG,
                            ZEROPAN_ERRMSG_LONG);
                }
            }
        }
        else {
            // Unknown status
            this.logger.error(STATUS_ERRMSG_LONG);
            throw new MessagingException(
                    getId(),
                    ErrorCodes.ERROR_CODE_5,
                    ErrorCodes.ERROR_MESSAGE_5,
                    "[PARes.TX.status]",
                    STATUS_ERRMSG,
                    STATUS_ERRMSG_LONG);
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
        //Check Field: Acquirer BIN
        MessageValidator.validateDigitField(
                getId(),
                in,
                "[PARes.Merchant.acqBIN]",
                1,
                11,
                true);
        this.merAcqBIN = in;
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
                "[PARes.Merchant.merID]",
                1,
                24,
                true);
        this.merID = in;
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
        //Check Field: Transaction ID
        MessageValidator.validateField(
                getId(),
                in,
                "[PARes.Purchase.xid]",
                28,
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
                "[PARes.Purchase.date]",
                1,
                17,
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
        }
        catch (ParseException pe) {
            this.logger.error("Invalid date format of Purchase Date.");
            throw new MessagingException(
                    getId(),
                    ErrorCodes.ERROR_CODE_5,
                    ErrorCodes.ERROR_MESSAGE_5,
                    "[PARes.Purchase.date]",
                    PDATE_ERRMSG,
                    PDATE_ERRMSG_LONG + " Value: {" + in + "}");
        }
        this.purDate = in;
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
        //Check Field: Purchase Amount
        MessageValidator.validateDigitField(
                getId(),
                in,
                "[PARes.Purchase.purchAmount]",
                1,
                12,
                true);
        this.purAmount = in;
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
        //Check Field: Purchase Exponent
        MessageValidator.validateField(
                getId(),
                in,
                "[PARes.Purchase.exponent]",
                1,
                1,
                true);
        this.purExponent = in;
    }

    /**
     * Gets the pan
     * @return Returns a String
     */
    public String getPan() {
        return pan;
    }

    /**
     * Sets the pan
     * @param in The pan to set
     */
    public void setPan(String in) throws MessagingException {
        //Check Field: Cardholder PAN
        MessageValidator.validateDigitField(
                getId(),
                in,
                "[PARes.pan]",
                13,
                19,
                true);
        this.pan = in;
    }

    /**
     * Gets the txTime
     * @return Returns a String
     */
    public String getTxTime() {
        return txTime;
    }

    /**
     * Sets the txTime
     * @param in The txTime to set
     */
    public void setTxTime(String in) throws MessagingException {
        //Check Field: Transaction signature date & time
        MessageValidator.validateField(
                getId(),
                in,
                "[PARes.TX.time]",
                1,
                17,
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
        }
        catch (ParseException pe) {
            this.logger.error("Invalid date format of Signature Date.");
            throw new MessagingException(
                    getId(),
                    ErrorCodes.ERROR_CODE_5,
                    ErrorCodes.ERROR_MESSAGE_5,
                    "[PARes.TX.time]",
                    TXTIME_ERRMSG,
                    TXTIME_ERRMSG_LONG + " Value: {" + in + "}");
        }
        this.txTime = in;
    }

    /**
     * Gets the txStatus
     * @return Returns a String
     */
    public String getTxStatus() {
        return txStatus;
    }

    /**
     * Sets the txStatus
     * @param in The txStatus to set
     */
    public void setTxStatus(String in) throws MessagingException {
        //Check Field: Transaction Status
        MessageValidator.validateField(
                getId(),
                in,
                "[PARes.TX.status]",
                1,
                1,
                true);
        this.txStatus = in;
    }

    /**
     * Gets the txCavv
     * @return Returns a String
     */
    public String getTxCavv() {
        return txCavv;
    }

    /**
     * Sets the txCavv
     * @param in The txCavv to set
     */
    public void setTxCavv(String in) throws MessagingException {
        //Check Field: Cardholder Authentication Verification  Value
        MessageValidator.validateField(
                getId(),
                in,
                "[PARes.TX.cavv]",
                28,
                28,
                false);
        this.txCavv = in;
    }

    /**
     * Gets the txEci
     * @return Returns a String
     */
    public String getTxEci() {
        return txEci;
    }

    /**
     * Sets the txEci
     * @param in The txEci to set
     */
    public void setTxEci(String in) throws MessagingException {
        //Check Field: Electronic Commerce Indicator
        MessageValidator.validateField(
                getId(),
                in,
                "[PARes.TX.eci]",
                2,
                2,
                false);
        this.txEci = in;
    }

    /**
     * Gets the txCavvAlgorithm
     * @return Returns a String
     */
    public String getTxCavvAlgorithm() {
        return txCavvAlgorithm;
    }

    /**
     * Sets the txCavvAlgorithm
     * @param in The txCavvAlgorithm to set
     */
    public void setTxCavvAlgorithm(String in) throws MessagingException {
        /*
         * [Alan's note - Feb 18, 2003]
         * Temp solution for differential validations of cavvAlgorithm from
         * version 1.0.1/1.0.2
         */
        if (getVersion().equals(PAResMessage.MSG_VERSION)) {
            // version 1.0.2 validation
            //Check Field: Cavv Algorithm (any positive integer)
            if ((in != null) && (in.length() != 0)) {
                try {
                    int number = Integer.parseInt(in);
                    if (number < 0) {
                        this.logger.error(
                                ALGORITHM_ERRMSG + " Value: {" + in + "}");
                        throw new MessagingException(
                                getId(),
                                ErrorCodes.ERROR_CODE_5,
                                ErrorCodes.ERROR_MESSAGE_5,
                                "[PARes.TX.cavvAlgorithm]",
                                ALGORITHM_ERRMSG,
                                ALGORITHM_ERRMSG_LONG + " Value: {" + in + "}");

                    }
                }
                catch (NumberFormatException nfe) {
                    this.logger.error(
                            ALGORITHM_ERRMSG + " Value: {" + in + "}");
                    throw new MessagingException(
                            getId(),
                            ErrorCodes.ERROR_CODE_5,
                            ErrorCodes.ERROR_MESSAGE_5,
                            "[PARes.TX.cavvAlgorithm]",
                            ALGORITHM_ERRMSG,
                            ALGORITHM_ERRMSG_LONG + " Value: {" + in + "}");
                }
            }
        }
        else {
            // version 1.0.1 validation
            //Check Field: Cavv Algorithm (Supported value for version 1.0.1: '0' or '1')
            if ((in != null) && (in.length() != 0)) {
                if (!in.equals("0") && !in.equals("1")) {
                    this.logger.error(
                            ALGORITHM_ERRMSG2 + " Value: {" + in + "}");
                    throw new MessagingException(
                            getId(),
                            ErrorCodes.ERROR_CODE_5,
                            ErrorCodes.ERROR_MESSAGE_5,
                            "[PARes.TX.cavvAlgorithm]",
                            ALGORITHM_ERRMSG2,
                            ALGORITHM_ERRMSG2_LONG + " Value: {" + in + "}");
                }
            }

        }

        this.txCavvAlgorithm = in;
    }

    /**
     * Gets the ireqCode
     * @return Returns a String
     */
    public String getIreqCode() {
        return ireqCode;
    }

    /**
     * Sets the ireqCode
     * @param in The ireqCode to set
     */
    public void setIreqCode(String in) throws MessagingException {
        //Check Field: Invalid Request Code
        if ((in != null) && (in.length() != 0)) {
            if (IReqCodeValidator.isNotValidIReqCode(in)) {
                this.logger.error(
                        IREQCODE_ERRMSG_LONG + " Value: {" + in + "}");
                throw new MessagingException(
                        getId(),
                        ErrorCodes.ERROR_CODE_5,
                        ErrorCodes.ERROR_MESSAGE_5,
                        "[PARes.IReq.iReqCode]",
                        IREQCODE_ERRMSG,
                        IREQCODE_ERRMSG_LONG + " Value: {" + in + "}");
            }
        }

        this.ireqCode = in;
    }

    /**
     * Gets the ireqDetail
     * @return Returns a String
     */
    public String getIreqDetail() {
        return ireqDetail;
    }

    /**
     * Sets the ireqDetail
     * @param in The ireqDetail to set
     */
    public void setIreqDetail(String in) throws MessagingException {
        //Check Filed: Invalid Request Detail (Only exist when ireqCode provided)
        MessageValidator.validateField(
                getId(),
                in,
                "[PARes.IReq.iReqDetail]",
                1,
                256,
                false);
        this.ireqDetail = in;
    }

    /**
     * Gets the ireqVendorCode
     * @return Returns a String
     */
    public String getIreqVendorCode() {
        return ireqVendorCode;
    }

    /**
     * Sets the ireqVendorCode
     * @param in The ireqVendorCode to set
     */
    public void setIreqVendorCode(String in) throws MessagingException {
        //Check Field: Invalid Request Code
        MessageValidator.validateField(
                getId(),
                in,
                "[PARes.IReq.vendorCode]",
                1,
                256,
                false);
        this.ireqVendorCode = in;
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
        //Check Field: Purchase Currency
        MessageValidator.validateDigitField(
                getId(),
                in,
                "[PARes.Purchase.currency]",
                1,
                3,
                true);

        // Validate currency using currency code table
        if (!CurrencyCode.isCodeValid(in)) {
            this.logger.error(PURCURR_ERRMSG_LONG + " Value: {" + in + "}");
            throw new MessagingException(
                    getId(),
                    ErrorCodes.ERROR_CODE_5,
                    ErrorCodes.ERROR_MESSAGE_5,
                    "[PARes.Purchase.currency]",
                    PURCURR_ERRMSG,
                    PURCURR_ERRMSG_LONG + " Value: {" + in + "}");
        }
        this.purCurrency = in;
    }

    /**
     * Gets the signature
     * @return Returns a Serializable
     */
    public String getSignature() {
        return signature;
    }

    /**
     * Sets the signature
     * @param in The signature to set
     */
    public void setSignature(String in) throws MessagingException {
        //Check Field: Signature
        MessageValidator.validateField(getId(), in, "[Signature]", 1, -1, true);
        this.signature = in;
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

    /**
     * Gets the refId
     * @return Returns a String
     */
    public String getRefId() {
        return refId;
    }

    /**
     * Sets the refId
     * @param refId The refId to set
     */
    public void setRefId(String refId) throws MessagingException {
        this.refId = refId;
    }

    private final boolean isNotNull(String in) {
        if ((in == null) || (in.length() == 0))
            return false;
        else
            return true;
    }

}