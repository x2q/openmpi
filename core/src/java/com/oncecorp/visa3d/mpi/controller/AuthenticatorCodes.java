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

package com.oncecorp.visa3d.mpi.controller;


/**
 * The class holds all Authenticator error codes
 * and messages
 * 
 * @version $Revision: 17 $
 * @author	Alan Zhang
 */
public class AuthenticatorCodes {
	/**
	 * Code of FATAL_ERROR
	 */
	public final static String FATAL_ERROR = "999";

	/**
	 * Message of FATAL_ERROR
	 */
	public final static String FATAL_ERROR_MSG =
		"Fatal MPI processing error. Consult administrator to details.";

	/**
	 * Code of IO_ERROR
	 */
	public final static String IO_ERROR = "998";

	/**
	 * Message of IO_ERROR
	 */
	public final static String IO_ERROR_MSG =
		"IO problem occurred between AuthenticatorServlet and Merchant Application.";

	/**
	 * Code of SERVER_STOPPED
	 */
	public final static String SERVER_STOPPED = "997";

	/**
	 * Message of SERVER_STOPPED
	 */
	public final static String SERVER_STOPPED_MSG = "Authenticator Server is stopped.";

	/**
	 * Code of UNEXPECTED_ERROR
	 */
	public final static String UNEXPECTED_ERROR = "995";

	/**
	 * Message of UNEXPECTED_ERROR
	 */
	public final static String UNEXPECTED_ERROR_MSG = "Unexpected error. Consult administrator for details.";

	/**
	 * Code of INVALID_REQUEST_MESSAGE
	 */
	public final static String INVALID_REQUEST_MESSAGE = "994";

	/**
	 * Message of INVALID_REQUEST_MESSAGE
	 */
	public final static String INVALID_REQUEST_MESSAGE_MSG =
		"Request message is not well-formed/valid XML string. Please refer to MPIInterface DTD for details.";

	/**
	 * Code of INVALID_HTML_FIELDS
	 */
	public final static String INVALID_HTML_FIELDS = "800";

	/**
	 * Message of FATAL_ERROR
	 */
	public final static String INVALID_HTML_FIELDS_MSG =
		"Required hidden fields with NULl or wrong value.";

	/**
	 * Convenience method to create error response
	 * @param code Error code
	 * @param msg Error message
	 * @return MPIError message XML String
	 */
	public static String getMPIErrorMsg(String code, String msg) {
		return getMPIErrorMsg("UNKNOWN", code, msg);
	}

	/**
	 * Convenience method to create error response
	 * @param id Error id
	 * @param code Error code
	 * @param msg Error message
	 * @return MPIError message XML String
	 */
	public static String getMPIErrorMsg(String id, String code, String msg) {
		//Construct MPI error message XML string
		String errorMsg =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<MPI_Interface>"
				+ "<MPIError id=\""
				+ id
				+ "\">"
				+ "<version>1.1</version>"
				+ "<errorCode>"
				+ code
				+ "</errorCode>"
				+ "<errorMessage>"
				+ msg
				+ "</errorMessage>"
				+ "</MPIError>"
				+ "</MPI_Interface>";

		return errorMsg;
	}
}