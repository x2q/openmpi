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

import java.io.Serializable;

/**
 * Class description
 * 
 * @author Alan Zhang
 * @version 1.0 4-Dec-02
 */
public class MsgMerchantIDStatisticResult implements Serializable, Comparable {
	private String merchantID;
	private String counter;

	public int compareTo(Object o) {
		MsgMerchantIDStatisticResult result = (MsgMerchantIDStatisticResult) o;

		return (this.merchantID.compareTo(result.getMerchantID()));
	}

	public boolean equals(Object anObject) {
		if (!(anObject instanceof MsgMerchantIDStatisticResult))
			return false;
		MsgMerchantIDStatisticResult result =
			(MsgMerchantIDStatisticResult) anObject;

		return (
			this.merchantID.equals(result.getMerchantID())
				&& this.counter.equals(result.getCounter()));
	}

	/**
	 * Returns the counter.
	 * @return String
	 */
	public String getCounter() {
		return counter;
	}

	/**
	 * Returns the merchantID.
	 * @return String
	 */
	public String getMerchantID() {
		return merchantID;
	}

	/**
	 * Sets the counter.
	 * @param counter The counter to set
	 */
	public void setCounter(String counter) {
		this.counter = counter;
	}

	/**
	 * Sets the merchantID.
	 * @param merchantID The merchantID to set
	 */
	public void setMerchantID(String merchantID) {
		this.merchantID = merchantID;
	}

}
