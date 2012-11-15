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

package com.oncecorp.visa3d.mpi.configuration;

import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.oncecorp.visa3d.mpi.controller.CardRangeMonitor;
import com.oncecorp.visa3d.mpi.controller.ErrorHandlerThread;
import com.oncecorp.visa3d.mpi.controller.ErrorHandlerThreadManager;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.monitoring.ConfiguratorMBean;
import com.oncecorp.visa3d.mpi.publishing.PublishingManager;

/**
 * Class description
 * 
 * @author azhang
 * @version 1.0 6-Dec-02
 */
public class Configurator implements ConfiguratorMBean {

	protected Logger logger = MPILogger.getLogger(Configurator.class.getName());

	/**
	 * Load core config data
	 */
	public HashMap loadCoreConfigData() {
			try {
				return Config.getConfigReference().getCoreConfigData();
			}
			catch (ConfigurationException e) {
				e.printStackTrace();
			}
			return null;

	}

	/**
	 * Save core config data
	 */
	public String saveCoreConfigData(HashMap data) {
		
		try {
			// Reset core config data in memory
			Config.getConfigReference().setCoreConfigData(data);
			logger.debug("Core config data saved in runtime.");
		}
		catch (ConfigurationException e) {
		}

		// Reset core config data through accessor
		ConfigAccessor ca = ConfigAccessorGenerator.getAccessor(ConfigAccessorType.ACCESSOR_TYPE_MPI);

		ca.setConfigData(data);
		logger.debug("Core config data saved in persistence.");

		/*
		 * [Martin's Note: Jan 21, 2003 5:29:05 PM] Need to calculate actual value in ms 
		 * because configuration contains value specified
		 * in minutes
		 */
		CoreConfigInfo cInfo = (CoreConfigInfo) data.get(MPIConfigDefinition.CR_CACHE_EXPIRED);
		long cacheExpired  = (long) Long.parseLong(cInfo.getValue());
		
		cInfo = (CoreConfigInfo) data.get(MPIConfigDefinition.CR_MONITOR_SLEEP_INTERVAL);
		long sleepInterval = (long) Long.parseLong(cInfo.getValue());
		logger.debug("Setting the cache expiry interval to " + cacheExpired);
		logger.debug("Setting the sleep interval to " + sleepInterval);

		CardRangeMonitor.setCacheExpiryInterval(cacheExpired * 60 * 1000);
		CardRangeMonitor.setSleepInterval(sleepInterval * 60 * 1000);

		/* 
		 * [Martin's Note: Jan 21, 2003 5:33:38 PM We interrupt the CR threads so it can 
		 * refresh itself with the newly set sleeping
		 * values.
		 */
		// Reset CRMonitor config data
		try {
			/* 
			 * [Martin's Note: Mar 10, 2003 3:17:17 PM]
			 * Need a new strategy to ensure the thread is interrupted properly.
			 * Under weblogic, the CardRangeMonitor thread is not classified within 
			 * the same thread group so we cannot access it. We
			 * should modify this to go all the way up to the root thread and search 
			 * for the needed thread from there.
			 */
//			Thread[] all = new Thread[Thread.activeCount()];
//			Thread.enumerate(all);
//
//			for (int i = 0; i < all.length; i++) {
//				logger.debug("Filtering thread: " + all[i].getName());
//				if (all[i].getName().equals(CardRangeMonitor.THREAD_NAME)) {
//					CardRangeMonitor aThread = (CardRangeMonitor) all[i];
//					aThread.interrupt();
//					logger.debug("CR monitor config data updated.");
//					break;
//				}
//			}

			// Reset PublishManager
			PublishingManager.reset();

			// update ErrorHandlerThreadManger & ErrorHandlerThread
			cInfo = (CoreConfigInfo) data.get(MPIConfigDefinition.ERROR_DISPATCH_MODE);
			ErrorHandlerThreadManager.setDispatchMode(cInfo.getValue());
			int interval;
			try {
				cInfo = (CoreConfigInfo) data.get(MPIConfigDefinition.ErrorThreadSleepInterval);
				interval = Integer.parseInt(cInfo.getValue());
				ErrorHandlerThreadManager.setErrorThreadSleepInterval(interval);
				ErrorHandlerThread.setInterval(interval);
				logger.debug("Error Handler Thread updated.");
			}
			catch (NumberFormatException nfe) {
				logger.error("Invalid value for ErrorThreadSleepInteval. Skip update.", nfe);
			}
		}
		catch (SecurityException se) {
			logger.error("CR monitor update failed", se);
			return "CR monitor update failed: " + se.getMessage();
		}

		return "Successfully saved Core config data.";
	}

	/**
	 * Refresh merchant config data to synchronize with changes
	 * done by Data Bridge
	 */
	public HashMap refreshMerchantConfigData() {
		try {
			Config.reload();
			//return "Successfully refreshed merchant config data.";
			return Config.getConfigReference().getMerchantConfigData();
		}
		catch (ConfigurationException e) {
			logger.error("Failed to refresh merchant config data.", e);
			//return "Failed to refresh merchant config data." + e.getMessage();
			return null;
		}
	}

	/**
	 * Load log4j config data
	 */
	public Properties loadLog4jConfigData() {
		//Get config accessor
		ConfigAccessor accessor =
			ConfigAccessorGenerator.getAccessor(ConfigAccessorType.ACCESSOR_TYPE_LOGGING);

		Properties ps = (Properties) accessor.getConfigData();

		//Reconfig runtime
		MPILogger.reconfig(ps);

		//Get config data
		return ps;
	}

	/**
	 * Save log4j config data
	 * @param props The new properties
	 */
	public String saveLog4jConfigData(Properties props) {
		//Save throught the Config object
		try {
			Config.getConfigReference().setLog4jConfigData(props);
		}
		catch (ConfigurationException e) {
			logger.error("Unable to save the Log4J configuration in the repository");
		}
		
		//Reconfig runtime
		MPILogger.reconfig(props);

		return "Successfully saved Log4J config data.";
	}

}
