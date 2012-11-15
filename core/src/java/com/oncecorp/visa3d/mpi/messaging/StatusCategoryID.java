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
 * The StatusCategoryID class is used as key for statistic counter per status.
 * 
 * @author Alan Zhang
 * @version 1.0 Dec 04, 2002
 */
public class StatusCategoryID implements Comparable {

	private String status;
	private String msgType;
	
	/**
	 * Constructor for StatusCategoryID.
	 */
	private StatusCategoryID() {
		super();
	}

	/** 
	 * Constructor
	 */
	public StatusCategoryID(String status, String msgType) {
		super();
		this.status 		= status;
		this.msgType 	= msgType;
	}	
	

	/** 
	 * Implements the hashCode function to be used in hash-type collections.
	 * 
	 * @return Hashcode for StatusCategoryID instance
	 */
	public int hashCode() {
		return (getStatus()+getMsgType()).hashCode();
	}
	
	/**
	 * Civilized version of the toString conversion method.
	 * Output intelligent string describing StatusCategoryID information
	 */
	public String toString() {
		return "[StatusCategoryID:"+ this.status + "/" + this.msgType + "]";
	}

	/**
	 * Implementation of the Comparable interface. This is needed is 
	 * order to use this class in Set-related collections.
	 */
	public int compareTo(Object anObject) {
		StatusCategoryID id2Compare = (StatusCategoryID) anObject;
		int compareResult = this.status.compareTo(id2Compare.getStatus());
		if (compareResult != 0) return compareResult;
		
		// If message name is equal then we we need to compare the version number
		compareResult = this.msgType.compareTo(id2Compare.getMsgType());
		return compareResult;			
	}

	/**
	 * Returns true only if both the name and the version are equal.
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object anObject) {
		if (! (anObject instanceof StatusCategoryID)) return false;
		StatusCategoryID id2Compare = (StatusCategoryID) anObject;

		return (this.status.equals(id2Compare.getStatus()) && 
				this.msgType.equals(id2Compare.getMsgType()));
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

}
