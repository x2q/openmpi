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

import java.net.URLEncoder;
import org.apache.log4j.Logger;

import com.oncecorp.visa3d.mpi.intf.payment.PaymentVerifResMessage;
import com.oncecorp.visa3d.mpi.intf.payment.UniqueWindowIDGenerator;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.MessagingException;
import com.oncecorp.visa3d.mpi.utility.XMLUtil;

/**
 * Class description
 *
 * @author Alan Zhang
 * @version 1.0 20-Feb-03
 */
public class HTMLVerifResMessage extends HTMLMessage implements HTMLFieldDefinition {

	private String tdsId;
	private String tdsEnrolled;
	private String tdsHtmlForm;
	private String tdsInvalidReq;

	private String tdsType;
	private String tdsVersion;
	private String tdsMerchantId;
	private String tdsInvalidDetails;
	private String tdsVendorCode;

	protected Logger logger =
		MPILogger.getLogger(HTMLVerifResMessage.class.getName());

	/**
	 * @see com.oncecorp.visa3d.mpi.intf.payment.html.HTMLMessage#isValid()
	 */
	public boolean isValid() {
		// mandatory fields check
		if (XMLUtil.isNull(getTdsId())) {
			logger.error(
				"Field TDS_id should be provided. Current value: "
					+ getTdsId());
			return false;
		}

		if (XMLUtil.isNull(getTdsEnrolled())) {
			logger.error(
				"Field TDS_enrolled should be provided. Current value: "
					+ getTdsEnrolled());
			return false;
		}

		return true;
	}

	/**
	 * @see com.oncecorp.visa3d.mpi.intf.payment.html.HTMLMessage#transform()
	 */
	public Object transform( ) throws MessagingException {
        logger.debug("Enter HTMLVerifResMessage transform.");
        String flag = getTdsEnrolled();
//		String winId = UniqueWindowIDGenerator.getWindowID( getTdsId() );
        StringBuffer sb = new StringBuffer("<html>\n");
        sb.append("  <head><title>3-D Secure Authentication</title></head>\n");
		/**
		 * [Gang Wu's Note: June 10, 2003] These Java script are uselesss as these results
		 * directly send back to the caller.
		 */
		/*
        sb.append("  <script language=\"JavaScript\">\n");
        sb.append("    function OnLoadEvent()\n");
        sb.append("    { if (window.name == null || window.name == \"\") window.name = \"mainWindow" + winId + "\";\n");
        sb.append("      childwin = window.open (\"about:blank\", \"popupName" + winId + "\",\"height=400, width=390, status=yes, dependent=no, scrollbars=yes, resizable=no\");\n");
        sb.append("      document.PaymentVerifResForm.target = \"popupName" + winId + "\";\n");
        sb.append("      document.PaymentVerifResForm.submit();\n");
        sb.append("    }\n");
        sb.append("  </script>");
		*/
        sb.append("  <body>\n");
        sb.append("    <form name=\"PaymentVerifResForm\" action=\"\" method=\"POST\">\n");
		printHiddenFields( sb, TDS_ID, getTdsId() );
		printHiddenFields( sb, TDS_ENROLLED, flag );
		printHiddenFields( sb, TDS_TYPE, getTdsType() );
		printHiddenFields( sb, TDS_VERSION, getTdsVersion() );
		printHiddenFields( sb, TDS_MERCHANT_ID, getTdsMerchantId() );
		printHiddenFields( sb, TDS_VENDOR_CODE, getTdsVendorCode() );
        if ( flag == null || !flag.equals("Y") )
        {
			if ( getTdsInvalidReq() != null )
		    {
				printHiddenFields( sb, TDS_INVALID_REQ, getTdsInvalidReq() );
				printHiddenFields( sb, TDS_INVALID_DETAILS, getTdsInvalidDetails() );
		    }

          logger.debug("Exit HTMLVerifResMessage transform with no-enrolled.");
        }
        else
        {
			logger.debug("Exit HTMLVerifResMessage transform with enrolled.");
			printHiddenFields( sb, TDS_HTML_FORM, URLEncoder.encode(getTdsHtmlForm()) );
        }

        sb.append("    </form>\n");
        sb.append("  </body>\n");
        sb.append("</html>\n");
        return sb.toString();
	}

	/**
	 * Populates fields with given PaymentVerifResMessage
	 * @see com.oncecorp.visa3d.mpi.intf.payment.html.HTMLMessage#populate(Object)
	 */
	public void populate(Object o) {
		// sanity check
		if ((o == null) || !(o instanceof PaymentVerifResMessage)) {
			logger.error("HTMLVerifResMessage could be only populated with a PaymentVerifResMessage.");
			return;
		}

        logger.debug("Enter HTMLVerifResMessage populate.");
		PaymentVerifResMessage pvres = (PaymentVerifResMessage) o;
		setTdsId(pvres.getId());
		setTdsEnrolled(pvres.getEnrolled());
		setTdsHtmlForm(pvres.getHtmlForm());
		setTdsInvalidReq(pvres.getInvalidReq());
		setTdsType( pvres.getType() );
		setTdsVersion( pvres.getVersion() );
		setTdsMerchantId( pvres.getMerchantID() );
		setTdsInvalidDetails( pvres.getInvalidDetails() );
		setTdsVendorCode( pvres.getVendorCode() );
        logger.debug("Exit HTMLVerifResMessage populate.");
	}

	/**
	 * Returns the tdsEnrolled.
	 * @return String
	 */
	public String getTdsEnrolled() {
		return tdsEnrolled;
	}

	/**
	 * Returns the tdsInvalidReq.
	 * @return String
	 */
	public String getTdsInvalidReq() {
		return tdsInvalidReq;
	}

	/**
	 * Returns the tdsHtmlForm.
	 * @return String
	 */
	public String getTdsHtmlForm() {
		return tdsHtmlForm;
	}

	/**
	 * Returns the tdsId.
	 * @return String
	 */
	public String getTdsId() {
		return tdsId;
	}

	/**
	 * Sets the tdsEnrolled.
	 * @param tdsEnrolled The tdsEnrolled to set
	 */
	public void setTdsEnrolled(String tdsEnrolled) {
		this.tdsEnrolled = tdsEnrolled;
	}

	/**
	 * Sets the tdsInvalidReq.
	 * @param tdsInvalidReq The tdsInvalidReq to set
	 */
	public void setTdsInvalidReq(String tdsInvalidReq) {
		this.tdsInvalidReq = tdsInvalidReq;
	}

	/**
	 * Sets the tdsHtmlForm.
	 * @param tdsHtmlForm The tdsHtmlForm to set
	 */
	public void setTdsHtmlForm(String tdsHtmlForm) {
		this.tdsHtmlForm = tdsHtmlForm;
	}

	/**
	 * Sets the tdsId.
	 * @param tdsId The tdsId to set
	 */
	public void setTdsId(String tdsId) {
		this.tdsId = tdsId;
	}



	/**
	*
	* @return the String value of tdsType.
	*/
	public String getTdsType(){
		return tdsType;
	}

	/**
	*
	* @param aTdsType - the new value for tdsType
	*/
	public void setTdsType(String aTdsType){
		tdsType = aTdsType;
	}


	/**
	*
	* @return the String value of tdsVersion.
	*/
	public String getTdsVersion(){
		return tdsVersion;
	}

	/**
	*
	* @param aTdsVersion - the new value for tdsVersion
	*/
	public void setTdsVersion(String aTdsVersion){
		tdsVersion = aTdsVersion;
	}


	/**
	*
	* @return the String value of tdsMerchantId.
	*/
	public String getTdsMerchantId(){
		return tdsMerchantId;
	}

	/**
	*
	* @param aTdsMerchantId - the new value for tdsMerchantId
	*/
	public void setTdsMerchantId(String aTdsMerchantId){
		tdsMerchantId = aTdsMerchantId;
	}


	/**
	*
	* @return the String value of tdsInvalidDetails.
	*/
	public String getTdsInvalidDetails(){
		return tdsInvalidDetails;
	}

	/**
	*
	* @param aTdsInvalidDetails - the new value for tdsInvalidDetails
	*/
	public void setTdsInvalidDetails(String aTdsInvalidDetails){
		tdsInvalidDetails = aTdsInvalidDetails;
	}


	/**
	*
	* @return the String value of tdsVendorCode.
	*/
	public String getTdsVendorCode(){
		return tdsVendorCode;
	}

	/**
	*
	* @param aTdsVendorCode - the new value for tdsVendorCode
	*/
	public void setTdsVendorCode(String aTdsVendorCode){
		tdsVendorCode = aTdsVendorCode;
	}


}
