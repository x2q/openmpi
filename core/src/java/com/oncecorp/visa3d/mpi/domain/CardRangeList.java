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

import java.util.TreeSet;

/**
 * This class contains all the credit card ranges needed for a specific authentication
 * protocol. Currently this is either (VbV or SecureCode). 
 * 
 * The configuration of the Core Server determines which of the two protocols are supported
 * and will update the cache appropriately using both the <code>CardRangeMonitor</code> and the 
 * <code>CardRangeManager</code>
 * 
 * @see com.oncecorp.visa3d.mpi.domain.CardRangeManager
 * @see com.oncecorp.visa3d.mpi.controller.CardRangeMonitor
 * @author Martin Dufort (mdufort@oncecorp.com)
 */
public class CardRangeList {

	/**
	 * Card range cache
	 */
	private Long[] begins;
	private Long[] ends;

	/**
	 * Buffered card range. Used only when receiving a new set of ranges. Will be synchronized
	 * with the master copy kept in the <code>begins</code> and <code>ends</code> member
	 * variables
	 */
	private TreeSet bufferedBegins;
	private TreeSet bufferedEnds;

	/**
	 * Cache load flag
	 */
	private boolean cardRangeLoaded;

	/**
	 * Cache serialNumber
	 */
	private String serialNumber;

	/**
	 * @return TreeSet
	 */
	public TreeSet getBufferedBegins() {
		return bufferedBegins;
	}

	/**
	 * @return TreeSet
	 */
	public TreeSet getBufferedEnds() {
		return bufferedEnds;
	}

	/**
	 * @return boolean
	 */
	public boolean isCardRangeLoaded() {
		return cardRangeLoaded;
	}

	/**
	 * @return Long[]
	 */
	public Long[] getEnds() {
		return ends;
	}

	/**
	 * @return String
	 */
	public String getSerialNumber() {
		return serialNumber;
	}

	/**
	 * Sets the bufferedBegins.
	 * @param bufferedBegins The bufferedBegins to set
	 */
	public void setBufferedBegins(TreeSet bufferedBegins) {
		this.bufferedBegins = bufferedBegins;
	}

	/**
	 * Sets the bufferedEnds.
	 * @param bufferedEnds The bufferedEnds to set
	 */
	public void setBufferedEnds(TreeSet bufferedEnds) {
		this.bufferedEnds = bufferedEnds;
	}

	/**
	 * Sets the cardRangeLoaded.
	 * @param cardRangeLoaded The cardRangeLoaded to set
	 */
	public void setCardRangeLoaded(boolean cardRangeLoaded) {
		this.cardRangeLoaded = cardRangeLoaded;
	}

	/**
	 * Sets the ends.
	 * @param ends The ends to set
	 */
	public void setEnds(Long[] ends) {
		this.ends = ends;
	}

	/**
	 * Sets the serialNumber.
	 * @param serialNumber The serialNumber to set
	 */
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	/**
	 * @return Long[]
	 */
	public Long[] getBegins() {
		return begins;
	}

	/**
	 * Sets the begins.
	 * @param begins The begins to set
	 */
	public void setBegins(Long[] begins) {
		this.begins = begins;
	}

}
