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

package com.oncecorp.visa3d.mpi.messaging;

/**
 * Exception information used to propage error information occuring within the messaging component
 *              
 * @version $Revision: 13 $
 * @author	Jun Shi
 * @author  Martin Dufort (mdufort@oncecorp.com)
 */
public class MessagingException extends Exception {

	// Attribute to enable easy propagation of errorCode to external clients 
	private String id;
	private String errorCode;
	private String errorMsg;
	private String errorDetail;
	private String vendorCode;

	// Default values for use with default constructor
	private static final String DEFAULT_ERROR_CODE 	= MessageEngine.MESSAGING_EXCEPTION_CODE;
	private static final String DEFAULT_ERROR_MSG 	= MessageEngine.GENERIC_ERROR_MESSAGE;
	private static final String DEFAULT_VENDOR_CODE = "";

	/**
	 * Override super class constructor to construct an Exception 
	 * with the specified message.
	 * 
	 * @param message 	Detailed message
	 * @param e         an Exception 
	 * @return datailed Exception message
	 */
	public MessagingException(String message) {
		// Invoke explicit constructor with default values	 
		this(DEFAULT_ERROR_CODE, DEFAULT_ERROR_MSG, DEFAULT_VENDOR_CODE, message);
	}

	public MessagingException(Exception e) {
		this(
			DEFAULT_ERROR_CODE,
			DEFAULT_ERROR_MSG,
			DEFAULT_VENDOR_CODE,
			e.getMessage());
	}

	public String toString() {
		StringBuffer strRep = new StringBuffer();
		strRep.append("Messaging Exception: errorCode=");
		strRep.append(getErrorCode());
		strRep.append(" errorMsg=");
		strRep.append(getErrorMsg());
		strRep.append(" vendorCode=");
		strRep.append(getVendorCode());
		strRep.append(" exceptionMessage=");
		strRep.append(getMessage());

		return strRep.toString();
	}

	/**
	 * Constructor that allows specification of error code and message
	 * 
	 * @param anErrorCode	ErrorCode associated to this exception
	 * @param anErrorMsg	ErrorMessage associated to this exception
	 * @param aVendorCode	Vendor Code associated to this exception
	 * @param aMessage 		Exception related message
	 */
	public MessagingException(
		String anErrorCode,
		String anErrorMsg,
		String aVendorCode,
		String aMessage) {
		super(aMessage);
		this.id = "UNKNOWN";
		this.errorCode = anErrorCode;
		this.errorMsg = anErrorMsg;
		this.vendorCode = aVendorCode;
	}

	/**
	 * Constructor that allows specification of error code and message
	 * 
	 * @param anID 			ErrorID associated to this exception
	 * @param anErrorCode	ErrorCode associated to this exception
	 * @param anErrorMsg	ErrorMessage associated to this exception
	 * @param anErrorDetail ErrorDetail associated to this exception
	 * @param aVendorCode	Vendor Code associated to this exception
	 * @param aMessage 		Exception related message
	 */
	public MessagingException( String anID, String anErrorCode, String anErrorMsg, 
							   String anErrorDetail, String aVendorCode, String aMessage) {
		super(aMessage);
		this.id 			= anID;
		this.errorCode 		= anErrorCode;
		this.errorMsg 		= anErrorMsg;
		this.errorDetail 	= anErrorDetail;
		this.vendorCode 	= aVendorCode;
	}

	/**
	 * Constructor that allows specification of all detail fields when 
	 * creating both the 3-D Secure Error message and the MPIError message
	 * 
	 * @param anID 			ErrorID associated to this exception
	 * @param anErrorCode	ErrorCode associated to this exception
	 * @param anErrorMsg	ErrorMessage associated to this exception
	 * @param aVendorCode	Vendor Code associated to this exception
	 * @param aMessage 		Exception related message
	 * @deprecated
	 */
	public MessagingException(
		String anID,
		String anErrorCode,
		String anErrorMsg,
		String aVendorCode,
		String aMessage) {
		super(aMessage);
		this.id = anID;
		this.errorCode = anErrorCode;
		this.errorMsg = anErrorMsg;
		this.vendorCode = aVendorCode;
	}

	/**
	 * Returns the errorCode.
	 * @return String
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Returns the errorMsg.
	 * @return String
	 */
	public String getErrorMsg() {
		return errorMsg;
	}

	/**
	 * Returns the vendorCode.
	 * @return String
	 */
	public String getVendorCode() {
		return vendorCode;
	}

	/**
	 * Sets the errorCode.
	 * @param errorCode The errorCode to set
	 */
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * Sets the errorMsg.
	 * @param errorMsg The errorMsg to set
	 */
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	/**
	 * Sets the vendorCode.
	 * @param vendorCode The vendorCode to set
	 */
	public void setVendorCode(String vendorCode) {
		this.vendorCode = vendorCode;
	}
	/**
	 * Gets the id
	 * @return Returns a String
	 */
	public String getId() {
		return id;
	}
	/**
	 * Sets the id
	 * @param id The id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the errorDetail.
	 * @return String
	 */
	public String getErrorDetail() {
		return errorDetail;
	}

	/**
	 * Sets the errorDetail.
	 * @param errorDetail The errorDetail to set
	 */
	public void setErrorDetail(String errorDetail) {
		this.errorDetail = errorDetail;
	}

}