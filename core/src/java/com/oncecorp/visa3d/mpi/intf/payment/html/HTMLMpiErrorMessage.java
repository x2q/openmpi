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

import org.apache.log4j.Logger;

import com.oncecorp.visa3d.mpi.intf.payment.MPIErrorMessage;
import com.oncecorp.visa3d.mpi.logging.MPILogger;

import com.ibm.xml.dsig.util.Base64;

/**
 * Class description
 *
 * @author Gang Wu
 * @version 1.0 3-Jun-03
 */
public class HTMLMpiErrorMessage extends HTMLMessage implements HTMLFieldDefinition
{
	private String tdsVersion;
	private String tdsId;
	private String tdsType;
	private String tdsMerchantID;
	private String tdsErrorCode;
	private String tdsErrorMessage;
	private String tdsErrorDetail;
	private String tdsVendorCode;
	protected Logger logger =
		MPILogger.getLogger(HTMLMpiErrorMessage.class.getName());


	public HTMLMpiErrorMessage()
	{
	}

	public HTMLMpiErrorMessage( Object o )
	{
		populate( o );
	}

	/**
	 * @see com.oncecorp.visa3d.mpi.intf.payment.html.HTMLMessage#isValid()
	 */
	public boolean isValid()
	{
		return true;
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
	* @return the String value of tdsMerchantID.
	*/
	public String getTdsMerchantID(){
		return tdsMerchantID;
	}

	/**
	*
	* @param aTdsMerchantID - the new value for tdsMerchantID
	*/
	public void setTdsMerchantID(String aTdsMerchantID){
		tdsMerchantID = aTdsMerchantID;
	}


	/**
	*
	* @return the String value of tdsErrorCode.
	*/
	public String getTdsErrorCode(){
		return tdsErrorCode;
	}

	/**
	*
	* @param aTdsErrorCode - the new value for tdsErrorCode
	*/
	public void setTdsErrorCode(String aTdsErrorCode){
		tdsErrorCode = aTdsErrorCode;
	}


	/**
	*
	* @return the String value of tdsErrorMessage.
	*/
	public String getTdsErrorMessage(){
		return tdsErrorMessage;
	}

	/**
	*
	* @param aTdsErrorMessage - the new value for tdsErrorMessage
	*/
	public void setTdsErrorMessage(String aTdsErrorMessage){
		tdsErrorMessage = aTdsErrorMessage;
	}


	/**
	*
	* @return the String value of tdsErrorDetail.
	*/
	public String getTdsErrorDetail(){
		return tdsErrorDetail;
	}

	/**
	*
	* @param aTdsErrorDetail - the new value for tdsErrorDetail
	*/
	public void setTdsErrorDetail(String aTdsErrorDetail){
		tdsErrorDetail = aTdsErrorDetail;
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

	/**
	*
	* @return the String value of tdsId.
	*/
	public String getTdsId(){
		return tdsId;
	}

	/**
	*
	* @param aTdsId - the new value for tdsId
	*/
	public void setTdsId(String aTdsId){
		tdsId = aTdsId;
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
	 * @see com.oncecorp.visa3d.mpi.intf.payment.html.HTMLMessage#toHtml()
	 */
	public Object transform( ) {
		logger.debug("Enter HTMLMpiErrorMessage transform.");

		// need to define format of error response html form
		StringBuffer sb = new StringBuffer("<html>\n");
		sb.append("  <head><title>Master Card Secure Code Authentication</title></head>\n");
		sb.append("  <body>\n");
		sb.append("    <form name=\"MPIErrorForm\" action=\"javascript:window.close()");
		sb.append("\" method=\"post\">\n");

		sb.append("    <center>\n");
		sb.append("      <h1>Error Page</h1>\n");
		sb.append("      Your master card maybe non-enrolled \n");

		if ( tdsErrorCode != null )
		{
			sb.append("<p>      Error Code:\n");
			sb.append("      " + tdsErrorCode + "</p>\n");
		}
		if ( tdsErrorMessage != null )
		{
			sb.append("<p>      Error Message:\n");
			sb.append("      " + tdsErrorMessage + "</p>\n");
		}
		if ( tdsErrorDetail != null )
		{
			sb.append("<p>      Error Detail:\n");
			sb.append("      " + tdsErrorDetail + "</p>\n");
		}

		sb.append( generateHiddenFields() );
		sb.append("      </center><input type=\"button\" onClick=\"javascript:window.close()\" value=\"close window\">\n");
		sb.append("  </form>\n");
		sb.append("  </body>\n");
		sb.append("</html>\n");

		return sb.toString();
	}

	/**
	 * Add Master secure code hidden fields to the HTML form which will be finally
	 * delivered to the Merchant application.
	 * @return - the HTML form string which contains all hidden fields definition.
	 */
	public String generateHiddenFields(  )
	{
		StringBuffer sb = new StringBuffer();

		// TDS_version
		printHiddenFields( sb, TDS_VERSION, getTdsVersion() );

		// TDS_type
		printHiddenFields( sb, TDS_TYPE, getTdsType() );

		// TDS_ID
		printHiddenFields( sb, TDS_ID, getTdsId() );

		// TDS_MERCHANT_ID
		printHiddenFields( sb, TDS_MERCHANT_ID, getTdsMerchantID() );

		// TDS_ERROR_CODE
		printHiddenFields( sb, TDS_ERROR_CODE, getTdsErrorCode() );

		// TDS_ERROR_MESSAGE
		printHiddenFields( sb, TDS_ERROR_MESSAGE, getTdsErrorMessage() );

		// TDS_ERROR_DETAIL
		printHiddenFields( sb, TDS_ERROR_DETAIL, getTdsErrorDetail() );

		// TDS_VENDOR_CODE
		printHiddenFields( sb, TDS_VENDOR_CODE, getTdsVendorCode() );

		return sb.toString();
	}

	/**
	 * Populates fields with given MPIErrorMessage
	 * @see com.oncecorp.visa3d.mpi.intf.payment.html.HTMLMessage#populate(Object)
	 */
	public void populate(Object o) {
		// sanity check
		if ((o == null) || !(o instanceof MPIErrorMessage)) {
			logger.error("HTMLMpiErrorMessage could be only populated with a MPIErrorMessage.");
			return;
		}

		logger.debug("Enter HTMLMpiErrorMessage populate.");
		MPIErrorMessage emsg = (MPIErrorMessage) o;

		setTdsId( emsg.getId() );
		setTdsType( emsg.getType() );
		setTdsVersion( emsg.MSG_VERSION );
		setTdsMerchantID( emsg.getMerchantID() );
		setTdsErrorCode( emsg.getErrorCode() );
		setTdsErrorMessage( emsg.getErrorMessage() );
		setTdsErrorDetail( emsg.getErrorDetail() );
		setTdsVendorCode( emsg.getVendorCode() );

	}
}


