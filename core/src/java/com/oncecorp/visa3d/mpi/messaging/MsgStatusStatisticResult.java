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
public class MsgStatusStatisticResult implements Serializable, Comparable {
	private String status;
	private String msgType;
	private String counter;

	public int compareTo(Object o) {
		MsgStatusStatisticResult result = (MsgStatusStatisticResult) o;

		int compRes = this.status.compareTo(result.getStatus());
		// if they belong to same message type, then we compare status
		if (compRes == 0) {
			compRes = this.msgType.compareTo(result.getMsgType());
		}
		return compRes;
	}

	public boolean equals(Object anObject) {
		if (!(anObject instanceof MsgStatusStatisticResult))
			return false;
		MsgStatusStatisticResult result =
			(MsgStatusStatisticResult) anObject;

		return (
			this.status.equals(result.getStatus())
				&& this.msgType.equals(result.getMsgType())
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
	 * Returns the msgType.
	 * @return String
	 */
	public String getMsgType() {
		return msgType;
	}

	/**
	 * Returns the status.
	 * @return String
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Sets the counter.
	 * @param counter The counter to set
	 */
	public void setCounter(String counter) {
		this.counter = counter;
	}

	/**
	 * Sets the msgType.
	 * @param msgType The msgType to set
	 */
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	/**
	 * Sets the status.
	 * @param status The status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

}
