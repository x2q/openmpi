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
* Validate IReqCode
* 
* @version 0.1 Oct 29, 2002
* @author Alan Zhang
*/
public class IReqCodeValidator {
	private static final String[] IREQ_CODE_LIST =
		new String[] {
			"50",
			"51",
			"52",
			"53",
			"54",
			"55",
			"56",
			"57",
			"98",
			"99" };
		
		
	/**
	 * Check received IReqCode is in Visa3d code list.
	 * 
	 * @param code The IReqCode received
	 * @return False if code is in list.
	 */		
	public static boolean isNotValidIReqCode(String code) {
		/*
		 * [Alan's Note: Nov 5, 2002]
		 * Ref to Visa 3-D Secure Core Function doc, we should be able to accept 
		 * any numeric IReqCode sent by VISA Dir or ACS. and choose a 'close match'. 
		 */
		for (int i=0; i<IREQ_CODE_LIST.length; i++) {
			if (code.equals(IREQ_CODE_LIST[i])) return false;
		}
		
		return true;
	}

}
