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

 package com.oncecorp.visa3d.mpi.domain;

import com.oncecorp.visa3d.mpi.utility.XMLUtil;

/**
 * This class represents a single card range. It includes:
 * <ul>
 * <li>Begin: the first number in card range</li>
 * <li>End: the last number in card range</li>
 * <li>action: either 'A' (Add) or 'D' (Delete)</li>
 * </ul>  
 * 
 * @version 0.1 Oct 16, 2002
 * @author	Alan Zhang
 */
public class CardRange {
	/**
	 * Begin of card range
	 */
	private Long begin;
	
	/**
	 * End of card range
	 */
	private Long end;
	
	/**
	 * action
	 */
	private String action;
	
	/**
	 * Constructor
	 */
	public CardRange() {
		super();
	}
	
	/**
	 * Consturctor
	 */
	public CardRange(Long begin, Long end, String action) {
		super();
		this.begin = begin;
		this.end = end;
		this.action = action;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<CR><begin>");
		sb.append(getBegin().toString());
		sb.append("</begin><end>");
		sb.append(getEnd().toString());
		sb.append("</end><action>");
		sb.append(XMLUtil.filterSpecialChars(getAction()));
		sb.append("</action></CR>");
		
		return sb.toString();
	}
	
	
	/**
	 * Returns the action.
	 * @return String
	 */
	public String getAction() {
		return action;
	}

	/**
	 * Returns the begin.
	 * @return Long
	 */
	public Long getBegin() {
		return begin;
	}

	/**
	 * Returns the end.
	 * @return Long
	 */
	public Long getEnd() {
		return end;
	}

	/**
	 * Sets the action.
	 * @param action The action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * Sets the begin.
	 * @param begin The begin to set
	 */
	public void setBegin(Long begin) {
		this.begin = begin;
	}

	/**
	 * Sets the end.
	 * @param end The end to set
	 */
	public void setEnd(Long end) {
		this.end = end;
	}

}
