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

package com.oncecorp.visa3d.mpi.intf.payment;

import com.oncecorp.visa3d.mpi.configuration.Config;
import com.oncecorp.visa3d.mpi.configuration.ConfigurationException;
import com.oncecorp.visa3d.mpi.configuration.MPIConfigDefinition;
import com.oncecorp.visa3d.mpi.domain.payment.ErrorCodes;
import com.oncecorp.visa3d.mpi.domain.payment.ErrorMessage;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.MessageCacheManager;
import com.oncecorp.visa3d.mpi.messaging.MessageEngine;
import com.oncecorp.visa3d.mpi.messaging.MessageGenerator;
import com.oncecorp.visa3d.mpi.messaging.MessagingException;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * Message cache manager for Payment domain. The cache is used to store VEReq & 
 * PAReq messages for later to be matched with PARes message. The two messages are 
 * put in a vector object and stored in HashMap with their common id as key. 
 * 
 * @version 0.1 Oct 15, 2002
 * @author	Alan Zhang
 */

public class PaymentMessageCacheManager implements MessageCacheManager {
	/**
	 * Local log4j Logger
	 */
	Logger logger =
		MPILogger.getLogger(PaymentMessageCacheManager.class.getName());

	/**
	 * Message cache
	 * <ul>
	 * <li>key: id</li>
	 * <li>value: message item</li>
	 * </ul>
	 */
	//private static HashMap messageCache = (HashMap) Collections.synchronizedMap(new HashMap());
	private static HashMap messageCache = new HashMap();

	/**
	 * Item update trace. 
	 * <ul>
	 * <li>key: id</li>
	 * <li>value: timestamp</li>
	 * </ul>
	 */
	private static HashMap trace = new HashMap();

	/**
	 * Synchronization lock
	 */
	private static boolean ready = true;

	/**
	 * Item expiry interval
	 */
	private static long interval;

	static {
		try {
			interval =
				Long.parseLong(
					(String) Config.getConfigReference().getConfigData(
						MPIConfigDefinition.CACHE_EXPIRY_INTERVAL))
					* 60000;
			System.out.println(
				"Cache Expiry interval set to (in millis second): " + interval);
		} catch (NumberFormatException ne) {
			System.err.println(
				"Failed to parse CacheExpiryInterval. Interval set to default as 5 mins.");
			ne.printStackTrace();
			interval = 300000;
		} catch (ConfigurationException ce) {
			System.err.println(
				"Failed to retrieve value of CacheExpiryInterval. Interval set to default as 5 mins.");
			ce.printStackTrace();
			interval = 300000;
		} catch (Exception e) {
			System.err.println(
				"Unexpected error occurred. Interval set to default as 5 mins.");
			e.printStackTrace();
			interval = 300000;
		}
	}

	/**
	 * Get cached item by its identity.
	 * 
	 * @param id The item identity
	 */
	public synchronized Object getCachedItem(String id) {
		//get the item we need
		return messageCache.get(id);
	}

	/**
	 * Get cached item by its identity.
	 * 
	 * @param id The item identity
	 */
	public synchronized void removeItem(String id) {
		while (!ready) {
			try {
				logger.debug("Acquiring lock to remove item: " + id);
				wait();
			} catch (InterruptedException ie) {
				logger.debug(
					"InterruptedException caught. [get item:" + id + "]");
			}
		}

		logger.debug("Lock obtained to remove item " + id);
		ready = false;

		//remove the item from cache and trace
		messageCache.remove(id);
		trace.remove(id);
		logger.debug("Item removed from cache: " + id);

		//Release lock
		ready = true;
		notifyAll();
		logger.debug("Lock released by item " + id);
	}

	/**
	 * Add new item and clean up expiried items. This method is synchronized. 
	 * 
	 * @param id The item identity
	 * @param item The item to be added
	 */
	public synchronized void addItem(String id, Object item)
		throws MessagingException {
		try {
			while (!ready) {
				try {
					logger.debug("Acquiring lock to add item: " + id);
					wait();
				} catch (InterruptedException ie) {
					logger.debug(
						"InterruptedException caught. [add item:" + id + "]");
				}
			}

			logger.debug("Lock obtained to add item " + id);
			ready = false;
			Date timestamp = Calendar.getInstance().getTime();

			//Add item to cache
			messageCache.put(id, item);
			trace.put(id, timestamp);
			logger.debug("Item " + id + " added.");

			//Clean up expiried items
			long rightnow = timestamp.getTime();
			Iterator keys = trace.keySet().iterator();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				long before = ((Date) trace.get(key)).getTime();
				if ((rightnow - before) > interval) {
					//Remove this expiried item from cache
					Object obj = messageCache.remove(key);

					//Release object
					if (obj != null)
						obj = null;

					//Remove this item from trace. To avoid ConcurrentModificationException
					//We call Iterator.remove() to underlyingly remove this item from trace.
					keys.remove();

					logger.debug("Expired item " + key + " removed.");

					// call MessageGenerator to create an empty ErrorMessage
					ErrorMessage errMsg =
						(ErrorMessage) MessageGenerator.create(
							ErrorMessage.MSG_TYPE,
							ErrorMessage.MSG_VERSION);

					// set value for this err msg			
					errMsg.setId(key);
					errMsg.setErrorCode(ErrorCodes.ERROR_CODE_70);
					errMsg.setErrorMessage(ErrorCodes.ERROR_MESSAGE_70);
					errMsg.setErrorDetail("Expired VEReq & PAReq removed without corresponding PARes message.");
					errMsg.setVendorCode("Incomplete transaction [id:" + key + "]");
					errMsg = (ErrorMessage) MessageEngine.process(errMsg);
					logger.debug("Expired item " + key + " published.");
				}
			}

			//Release lock
			ready = true;
			notifyAll();
			logger.debug("Lock released by item " + id);
		} catch (Exception e) {
			logger.error(
				"Error occurred while adding item to message cache: ",
				e);
			throw new MessagingException(e);
		}
	}

	/**
	 * Returns the messageCache.
	 * @return HashMap
	 */
	public static HashMap getMessageCache() {
		return messageCache;
	}

	/**
	 * Returns the trace.
	 * @return HashMap
	 */
	public static HashMap getTrace() {
		return trace;
	}

	/**
	 * Sets the messageCache.
	 * @param messageCache The messageCache to set
	 */
	public static void setMessageCache(HashMap messageCache) {
		PaymentMessageCacheManager.messageCache = messageCache;
	}

	/**
	 * Sets the trace.
	 * @param trace The trace to set
	 */
	public static void setTrace(HashMap trace) {
		PaymentMessageCacheManager.trace = trace;
	}

}
