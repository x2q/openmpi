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

package com.oncecorp.visa3d.mpi.intf.payment.html;

/**
 * Hidden field definitions for MasterCard(TM) SecureCode protocol
 *
 * @author Alan Zhang
 * @version 1.0 20-Feb-03
 */
public interface HTMLFieldDefinition {

	public final static String TDS_ID = "TDS_id";
	public final static String TDS_TYPE = "TDS_type";
	public final static String TDS_VERSION = "TDS_version";
	public final static String TDS_MERCHANT_ID = "TDS_merchantID";
	public final static String TDS_MERCHANT_TERM_URL = "TDS_merchantTermURL";
	public final static String TDS_MERCHANT_DATA = "TDS_merchantData";
	public final static String TDS_TRANSACTION_ID = "TDS_transactionID";
	public final static String TDS_CARD_NUMBER = "TDS_cardNumber";
	public final static String TDS_CARD_EXPIRY = "TDS_cardExpiry";
	public final static String TDS_PURCHASE_DATE = "TDS_purchaseDate";
	public final static String TDS_DISPLAY_AMOUNT = "TDS_displayAmount";
	public final static String TDS_PURCHASE_AMOUNT = "TDS_purchaseAmount";
	public final static String TDS_CURRENCY_CODE = "TDS_currencyCode";
	public final static String TDS_PURCHASE_DETDS = "TDS_purchaseDesc";
	public final static String TDS_PURCHASE_INFO = "TDS_purchaseInfo";
	public final static String TDS_PURCHASE_END_RECUR = "TDS_purchaseEndRecur";
	public final static String TDS_PURCHASE_FREQUENCY = "TDS_purchaseFrequency";
	public final static String TDS_PURCHASE_INSTALL = "TDS_purchaseInstall";
	public final static String TDS_BROWSER_CAT = "TDS_browserCat";
	public final static String TDS_BROWSER_ACCEPT = "TDS_browserAccept";
	public final static String TDS_BROWSER_AGENT = "TDS_browserAgent";

	public final static String TDS_ENROLLED = "TDS_enrolled";
	public final static String TDS_HTML_FORM = "TDS_htmlForm";
	public final static String TDS_ERROR_CODE = "TDS_errorCode";
	public final static String TDS_ERROR_MESSAGE = "TDS_errorMessage";
	public final static String TDS_ERROR_DETAIL = "TDS_errorDetail";
	public final static String TDS_VENDOR_CODE = "TDS_vendorCode";
	public final static String TDS_INVALID_DETAILS = "TDS_invalidDetails";
	public final static String TDS_INVALID_REQ = "TDS_invalidReq";

	public final static String PARES = "PaRes";
	public final static String MD = "MD";

	public final static String TDS_AUTH_DATE = "TDS_authDate";
	public final static String TDS_CURRENCY = "TDS_currency";
	public final static String TDS_EXPONENT = "TDS_exponent";
	public final static String TDS_STATUS = "TDS_status";
	public final static String TDS_CAVV = "TDS_cavv";
	public final static String TDS_ECI = "TDS_eci";
	public final static String TDS_ALGORITHM = "TDS_algorithm";

	public final static String UCAF_ENABLED = "Ucaf_Enabled";
	//public final static String UCAF_SALE_AMOUNT = "Ucaf_Sale_Amount";
	public final static String UCAF_PAYMENT_CARD_NUMBER = "Ucaf_Payment_Card_Number";
	public final static String UCAF_AUTHENTICATION_DATA = "Ucaf_Authentication_Data";

}
