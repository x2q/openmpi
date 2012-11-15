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
 * @author azhang
 * @version 1.0 4-Dec-02
 */
public class MsgTypeStatisticResult implements Serializable, Comparable {
	private String type;
	private String version;
	private String counter;
		
	public int compareTo(Object o) {
		MsgTypeStatisticResult result = (MsgTypeStatisticResult) o;

		int compRes = this.type.compareTo(result.getType());
		// if they belong to same message type, then we compare version
		if (compRes == 0) {
			compRes = this.version.compareTo(result.getVersion());
		}
		return compRes;
	}

	public boolean equals(Object anObject) {
		if (!(anObject instanceof MsgTypeStatisticResult))
			return false;
		MsgTypeStatisticResult result =
			(MsgTypeStatisticResult) anObject;

		return (
			this.type.equals(result.getType())
				&& this.version.equals(result.getVersion())
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
	 * Returns the type.
	 * @return String
	 */
	public String getType() {
		return type;
	}

	/**
	 * Returns the version.
	 * @return String
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the counter.
	 * @param counter The counter to set
	 */
	public void setCounter(String counter) {
		this.counter = counter;
	}

	/**
	 * Sets the type.
	 * @param type The type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Sets the version.
	 * @param version The version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

}
