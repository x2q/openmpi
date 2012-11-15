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

/**
 * Error code / message interface
 *  
 * @version 0.1 Sep 05, 2002
 * @author	Alan Zhang
 */

public interface ErrorCodes {
	/**
	 * Error Code: 1
	 */
	public final static String ERROR_CODE_1 = "1";

	/**
	 * Error Code: 2
	 */
	public final static String ERROR_CODE_2 = "2";

	/**
	 * Error Code: 3
	 */
	public final static String ERROR_CODE_3 = "3";

	/**
	 * Error Code: 4
	 */
	public final static String ERROR_CODE_4 = "4";

	/**
	 * Error Code: 5
	 */
	public final static String ERROR_CODE_5 = "5";

	/**
	 * Error Code: 6
	 */
	public final static String ERROR_CODE_6 = "6";
	
	/**
	 * Error Code: 60
	 */
	public final static String ERROR_CODE_60 = "60";
	
	/**
	 * Error Code: 70
	 */
	public final static String ERROR_CODE_70 = "70";
	
	/**
	 * Error Code: 80
	 */
	public final static String ERROR_CODE_80 = "80";
	
	/**
	 * Error Code: 98
	 */
	public final static String ERROR_CODE_98 = "98";
	
	/**
	 * Error Code: 99
	 */
	public final static String ERROR_CODE_99 = "99";
	

	/**
	 * Error Message: 1
	 */
	public final static String ERROR_MESSAGE_1 = "Root element invalid.";

	/**
	 * Error Message: 2
	 */
	public final static String ERROR_MESSAGE_2 =  "Message element not a defined message.";

	/**
	 * Error Message: 3
	 */
	public final static String ERROR_MESSAGE_3 = "Required element missing.";

	/**
	 * Error Message: 4
	 */
	public final static String ERROR_MESSAGE_4 = "Critical element not recognized.";

	/**
	 * Error Message: 5
	 */
	public final static String ERROR_MESSAGE_5 = "Format of one or more elements is invalid according to the specification.";

	/**
	 * Error Message: 6
	 */
	public final static String ERROR_MESSAGE_6 = "Protocol version too old.";
	
	/**
	 * Error Message: 60
	 */
	public final static String ERROR_MESSAGE_60 = "Unsupported category for mobile device.";
	
	/**
	 * Error Message: 70
	 */
	public final static String ERROR_MESSAGE_70 = "Incomplete transaction.";
	
	/**
	 * Error Message: 80
	 */
	public final static String ERROR_MESSAGE_80 = "Unsupported merchant.";
	
	/**
	 * Error Message: 98
	 */
	public final static String ERROR_MESSAGE_98 = "Transient system failure.";

	/**
	 * Error Message: 99
	 */
	public final static String ERROR_MESSAGE_99 = "Permanent system failure.";


}