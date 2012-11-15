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

import com.oncecorp.visa3d.mpi.configuration.MPIConfigDefinition;
import com.oncecorp.visa3d.mpi.logging.MPILogger;

import org.apache.log4j.Logger;
import java.util.TreeSet;

/**
 * Manages cached card range. The cache is synchronized but only blocked during refreshing.
 * This class provides card number verification as well. 
 * 
 * @version 0.1 Oct 17, 2002
 * @author	Alan Zhang
 */
public class CardRangeManager {
	/**
	 * Local log4j Logger
	 */
	private static Logger logger = MPILogger.getLogger(CardRangeManager.class.getName());

	/**
	 * The card range for each authentication protocol
	 */
	private static CardRangeList visaList = new CardRangeList();
	private static CardRangeList secureCodeList = new CardRangeList();

	/**
	 * Cache Lock
	 */
	private static boolean available = true; // Indicate if we can refresh the master copy of card ranges

	/**
	 * Refresh cached card range. This will be called by CRRes message processor whenever new card
	 * range updates received. The processor updates card range in buffered cache and calls this method 
	 * to refresh main cache.
	 */
	public synchronized void refresh(String aProtocolType) {

		CardRangeList protocolRangeList = getWorkingList(aProtocolType);

		while (!available) {
			try {
				logger.debug("Acquire lock to refresh Card Range Cache...");
				wait();
			}
			catch (InterruptedException ie) {
				logger.debug("refresh() thread interrupted by  other thread.");
			}
		}

		available = false;
		logger.debug("Lock obtained. Start refreshing Card Range Cache...");

		if (protocolRangeList.getBufferedBegins() != null) {
			protocolRangeList.setBegins(castToLongArray(protocolRangeList.getBufferedBegins().toArray()));
			protocolRangeList.setEnds(castToLongArray(protocolRangeList.getBufferedEnds().toArray()));
		}
		else {
			protocolRangeList.setBegins(null);
			protocolRangeList.setEnds(null);
		}
		logger.debug("Refreshing finished.");

		available = true;
		logger.debug("Lock released by refresher.");
		notifyAll();

	}

	private static CardRangeList getWorkingList(String aProtocolType) {
		// Get the proper list to use 
		CardRangeList protocolRangeList = null;
		if (aProtocolType.equalsIgnoreCase(MPIConfigDefinition.PROTOCOL_MASTERCARD_SECURE_CODE)) {
			protocolRangeList = secureCodeList;
		}
		else if (aProtocolType.equalsIgnoreCase(MPIConfigDefinition.PROTOCOL_VISA_3D_SECURE)) {
			protocolRangeList = visaList;
		}
		return protocolRangeList;
	}

	/**
	 * Verify given card see if it's in one of cached card range.
	 * 
	 * @param card The given card number
	 * @return True if the card is in one of cached card range.
	 */
	public boolean verifyCard(String card) {
		// Retrieve the proper list for the type of card we are dealing with	
		CardRangeList protocolList = null;
		if (card.startsWith("4")) {
			protocolList = getWorkingList(MPIConfigDefinition.PROTOCOL_VISA_3D_SECURE);
		}
		else if (card.startsWith("5")) {
			protocolList = getWorkingList(MPIConfigDefinition.PROTOCOL_MASTERCARD_SECURE_CODE);
		}
		else {
			// Unknown card, verification fails automatically
			return false;
		}

		logger.debug("Starting verifying card " + card + "...");
		//Check CR load status
		if (protocolList.getBegins() == null) {
			if (protocolList.isCardRangeLoaded()) {
				logger.debug("Card Range loaded with NULL value. Skip card range verification.");
				return true;
			}
			else {
				logger.debug(
					"Card Range cache has not been initialized. Skip card range verification.");
				return true;
			}
		}

		Long[] begins = protocolList.getBegins();
		Long[] ends   = protocolList.getEnds();
		Long cardNumber = Long.valueOf(card);
		int size = begins.length;
		int index = size / 2;
		int factor = index / 2;

		while (true) {
			if ((cardNumber.compareTo(begins[index]) >= 0)
				&& ((index == (size - 1)) || (cardNumber.compareTo(begins[index + 1]) < 0))) {
				logger.debug("Narrow to card range [" + index + "] :" + begins[index] + " to " + ends[index]);

				if (cardNumber.compareTo(ends[index]) <= 0) {
					logger.debug("Card [" + card + "] is in range.");
					return true;
				}
				else {
					logger.debug("Card [" + card + "] is not in any cached range.");
					return false;
				}
			}
			else {
				if (cardNumber.compareTo(begins[index]) < 0) {
					if (factor != 0)
						index -= factor;
					else
						index -= 1;
				}
				else {
					if (factor != 0)
						index += factor;
					else
						index += 1;
				}

				if (factor != 0)
					factor /= 2;

				if ((index < 0) || (index == size)) {
					logger.debug("Card [" + card + "] is not in any cached range.");
					return false;
				}
			}
		}
	}

	/**
	 * Returns the bufferedBegins.
	 * @return TreeSet
	 */
	public static TreeSet getBufferedBegins(String aProtocolType) {
		return getWorkingList(aProtocolType).getBufferedBegins();
	}

	/**
	 * Returns the bufferedEnds.
	 * @return TreeSet
	 */
	public static TreeSet getBufferedEnds(String aProtocolType) {
		return getWorkingList(aProtocolType).getBufferedEnds();
	}

	/**
	 * Sets the bufferedBegins.
	 * @param bufferedBegins The bufferedBegins to set
	 */
	public static void setBufferedBegins(String aProtocolType, TreeSet bufferedBegins) {
		getWorkingList(aProtocolType).setBufferedBegins(bufferedBegins);
	}

	/**
	 * Sets the bufferedEnds.
	 * @param bufferedEnds The bufferedEnds to set
	 */
	public static void setBufferedEnds(String aProtocolType, TreeSet bufferedEnds) {
		getWorkingList(aProtocolType).setBufferedEnds(bufferedEnds);
	}

	/**
	 * Returns the cardRangeLoaded.
	 * @return boolean
	 */
	public static boolean isCardRangeLoaded(String aProtocolType) {
		return getWorkingList(aProtocolType).isCardRangeLoaded();
	}

	/**
	 * Returns the serialNumber.
	 * @return String
	 */
	public static String getSerialNumber(String aProtocolType) {
		return getWorkingList(aProtocolType).getSerialNumber();
	}

	/**
	 * Sets the cardRangeLoaded.
	 * @param cardRangeLoaded The cardRangeLoaded to set
	 */
	public static void setCardRangeLoaded(String aProtocolType, boolean cardRangeLoaded) {
		getWorkingList(aProtocolType).setCardRangeLoaded(cardRangeLoaded);
	}

	/**
	 * Sets the serialNumber.
	 * @param serialNumber The serialNumber to set
	 */
	public static void setSerialNumber(String aProtocolType, String serialNumber) {
		getWorkingList(aProtocolType).setSerialNumber(serialNumber);
	}

	private Long[] castToLongArray(Object[] array) {
		if (array == null)
			return null;
		Long[] newArray = new Long[array.length];
		for (int i = 0; i < array.length; i++) {
			newArray[i] = (Long) array[i];
		}

		return newArray;
	}

}
