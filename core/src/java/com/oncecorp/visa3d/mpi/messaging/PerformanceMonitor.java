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

import com.oncecorp.visa3d.mpi.configuration.Config;
import com.oncecorp.visa3d.mpi.configuration.ConfigurationException;
import com.oncecorp.visa3d.mpi.configuration.MPIConfigDefinition;
import com.oncecorp.visa3d.mpi.controller.AuthenticatorSession;
import com.oncecorp.visa3d.mpi.domain.payment.CRReqMessage;
import com.oncecorp.visa3d.mpi.domain.payment.CRResMessage;
import com.oncecorp.visa3d.mpi.domain.payment.PAResMessage;
import com.oncecorp.visa3d.mpi.domain.payment.VEResMessage;
import com.oncecorp.visa3d.mpi.domain.profile.IPResMessage;
import com.oncecorp.visa3d.mpi.intf.payment.PaymentAuthResMessage;
import com.oncecorp.visa3d.mpi.intf.payment.PaymentVerifResMessage;
import com.oncecorp.visa3d.mpi.intf.profile.ProfileAuthResMessage;
import com.oncecorp.visa3d.mpi.intf.profile.ProfileVerifResMessage;
import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.meta.MessageUID;
import com.oncecorp.visa3d.mpi.monitoring.PerformanceMonitorMBean;
import com.oncecorp.visa3d.mpi.utility.Utils;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

/**
 * Class description
 *
 * @author azhang
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0 3-Dec-02
 */
public class PerformanceMonitor implements PerformanceMonitorMBean {

	/**
	 * Martin's Note - Jan 24 - 2003 @ 16:52
	 * Removing profile message that are not needed for the MPI now
	 */

	/*
		private final String[][] STATUS_CATEGORY =
			new String[][] { { "Authenticated", "PaymentAuthRes" }, {
				"Authenticated", "ProfileAuthRes" }, {
				"Authenticated", "PARes" }, {
				"Authenticated", "IPRes" }, {
				"Not Authenticated", "PaymentAuthRes" }, {
				"Not Authenticated", "ProfileAuthRes" }, {
				"Not Authenticated", "PARes" }, {
				"Not Authenticated", "IPRes" }, {
				"Enrolled", "VERes" }, {
				"Enrolled", "PaymentVerifRes" }, {
				"Enrolled", "ProfileVerifRes" }, {
				"Not Enrolled", "PaymentVerifRes" }, {
				"Not Enrolled", "ProfileVerifRes" }, {
				"Not Enrolled", "VERes" }, {
				"Unknown", "PaymentVerifRes" }, {
				"Unknown", "PaymentAuthRes" }, {
				"Unknown", "ProfileVerifRes" }, {
				"Unknown", "ProfileAuthRes" }, {
				"Unknown", "VERes" }, {
				"Unknown", "IPRes" }, {
				"Unknown", "PARes" }
		};
	*/

	private final String[][] STATUS_CATEGORY =
		new String[][] { { "Authenticated", "PaymentAuthRes" }, {
			"Authenticated", "PARes" }, {
			"Not Authenticated", "PaymentAuthRes" }, {
			"Not Authenticated", "PARes" }, {
			"Enrolled", "VERes" }, {
			"Enrolled", "PaymentVerifRes" }, {
			"Not Enrolled", "PaymentVerifRes" }, {
			"Not Enrolled", "VERes" }, {
			"Unknown", "PaymentVerifRes" }, {
			"Unknown", "PaymentAuthRes" }, {
			"Unknown", "VERes" }, {
			"Unknown", "PARes" }
	};

	private final String AUTHENTICATED = "Authenticated";
	private final String NOT_AUTHENTICATED = "Not Authenticated";
	private final String ENROLLED = "Enrolled";
	private final String NOT_ENROLLED = "Not Enrolled";
	private final String UNKNOWN = "Unknown";

	/**
	 * Local logger
	 */
	protected static Logger logger =
		MPILogger.getLogger(PerformanceMonitor.class.getName());

	/**
	 * Synchronized token
	 */
	protected static byte[] token = new byte[] { 0 };

	/**
	 * singleton instance
	 */
	private static PerformanceMonitor performanceMonitor;

	/**
	 * TreeMap contains MessageUID/Individual message counter
	 */
	private static Map msgTypeCounters;

	/**
	 * TreeMap contains StatusCategoryID/counter
	 */
	private  static Map msgStatusCounters;

	/**
	 * TreeMap contains MerchantID/counter
	 */
	private static Map msgMerchantIDCounters;

	/**
	 * Total processed message counter
	 */
	private static long totalCounter;

	/**
	 * Peak TPS(Transaction per second)
	 */
	private static float peakTPS;

	/**
	 * Average TPS
	 */
	private static float averageTPS;

	/**
	 * Sampling time (in secs)
	 */
	private static long samplingTime;

	/**
	 * Counting startup time
	 */
	private static long statupTime;

	/**
	 * Peak time
	 */
	private static long peakTime;

	/**
	 * Last flush time
	 */
	private static long lastFlushTime;

	/**
	 * Last flush counter
	 */
	private static long lastFlushCounter;

	// zero counter backup
	private static Map zeroTypeCounter;
	private static Map zeroStatusCounter;
	private static Map zeroMerchantCounter;

	/**
	 * Constructor
	 */
	public PerformanceMonitor() {
		if (performanceMonitor == null) {

			performanceMonitor = this;

			// Get all message definition to retrieve message UIDs
			HashMap meta =
				(HashMap) (new MessagingConfigAccessor()).getConfigData();

			// Initialize counters per message type
			msgTypeCounters = Collections.synchronizedMap( new TreeMap() );
            zeroTypeCounter = Collections.synchronizedMap(new TreeMap() );
			Iterator iterator = meta.keySet().iterator();
			int i = 0;
            String key;
			while (iterator.hasNext()) {
				MessageUID uid = (MessageUID) iterator.next();
				logger.debug("Meta UID: " + uid.toString());

				/*
				 * [Alan's Note - Jan 24, 2003]
				 * We are not counting CR messages, so don't include
				 * them in the mapping
				 */
				if (!uid.getName().equals(CRReqMessage.MSG_TYPE)
					&& !uid.getName().equals(CRResMessage.MSG_TYPE)) {
					// Create counter for this UID
                    key = Utils.getMessageMappingKey(
                            uid.getName(), uid.getVersion() );
					msgTypeCounters.put(key,  getZeroProtocolCounter() );
                    zeroTypeCounter.put(key,  getZeroProtocolCounter() );
				}
			}


			// Initialize counters per status
			msgStatusCounters = Collections.synchronizedMap( new TreeMap() );
            zeroStatusCounter = Collections.synchronizedMap( new TreeMap() );
			for (int k = 0; k < STATUS_CATEGORY.length; k++) {
                key = Utils.getMessageMappingKey(
                        STATUS_CATEGORY[k][0],
						STATUS_CATEGORY[k][1]);

				msgStatusCounters.put( key, getZeroProtocolCounter());
                zeroStatusCounter.put( key, getZeroProtocolCounter());
			}


			// Initialize counters per merchant ID
			msgMerchantIDCounters = Collections.synchronizedMap( new TreeMap() );
            zeroMerchantCounter = Collections.synchronizedMap( new TreeMap() );
			Iterator merchantList = null;
			try {
				merchantList =
					((Map) Config
						.getConfigReference()
						.getMerchantConfigData())
						.keySet()
						.iterator();
			} catch (ConfigurationException e) {
				logger.error(
					"Failed to retrieve merchant list from config.",
					e);
			}
			String temp = null;
			while (merchantList.hasNext()) {
				temp = (String) merchantList.next();
				msgMerchantIDCounters.put(temp, getZeroProtocolCounter());
                zeroMerchantCounter.put(temp, getZeroProtocolCounter());
			}

			//Set startup, lastFlush time and peakTime
			long current = Calendar.getInstance().getTime().getTime();
			setStatupTime(current);
			setLastFlushTime(current);
			setPeakTime(current);

			// Get sampling time
			String time = null;
			try {
				time =
					(String) Config.getConfigReference().getConfigData(
						MPIConfigDefinition.PERFOMANCE_MONITOR_SAMPLING_TIME);

				setSamplingTime(Long.parseLong(time));
			} catch (ConfigurationException e) {
				logger.error(
					"Failed to retrieve sampling time from config.",
					e);
			} catch (NumberFormatException nfe) {
				logger.error("Failed to convert sampling time: " + time, nfe);
			}

			logger.debug(
				"PerformanceMonitor status: "
					+ "Total count: "
					+ getTotalCounter()
					+ ", Startup time: "
					+ getStatupTime());
		}

	}

	public synchronized static PerformanceMonitor getInstance() {
		if (performanceMonitor == null) {
			performanceMonitor = new PerformanceMonitor();
			logger.info("Performance Monitor init finished.");
		}

		return performanceMonitor;
	}

	/**
	 * Count message.
	 * Following counters will be increased:
	 * <ul>
	 * <li>counter per message type</li>
	 * <li>counter per status</li>
	 * <li>counter per merchantID</li>
	 * <li>overall total counter</li>
	 * </ul>
	 *
	 * Following performance statistic index will be calculated:
	 * <ul>
	 * <li>average TPS</li>
	 * <li>peak TPS</li>
	 * </ul>
	 *
	 * @param msg The message to be counted
	 * @param merID The merchantID
	 */
	public void count(Message msg, String merID) {
		synchronized (token) {
			Map counter = null;

			// Increase counter per message type first
			// Create UID
            String key = Utils.getMessageMappingKey( msg.getType(), msg.getVersion() );
            String protocol = "" + Utils.toProtocolByte(
                    AuthenticatorSession.instance().getProtocol() );

			// Get counter by UID
			counter = (Map) getMsgTypeCounters().get(key);

            increaseMapItemValue( counter, protocol );
            increaseMapItemValue( counter, ""+Utils.ALL_SUPPORT );

			logger.debug("Counter per type increased: " + key );

			// Increase counter per status
			// Categorize message
			StatusCategoryID scid = categorize(msg);
			if (scid != null) {
                key = Utils.getMessageMappingKey( scid.getStatus(),
                        scid.getMsgType() );
				counter = (Map) getMsgStatusCounters().get(key);
                increaseMapItemValue( counter, protocol );
                increaseMapItemValue( counter, ""+Utils.ALL_SUPPORT );
				logger.debug(
					"Counter per status increased: " + scid.toString());
			} else {
				logger.debug("Message is not belong to status category.");
			}

			// Increase counter per merchantID
			// Check counter existence
			if (getMsgMerchantIDCounters().get(merID) == null) {
				// Counter not exist, create one for this merchant ID
				msgMerchantIDCounters.put(merID, getZeroProtocolCounter());
				zeroMerchantCounter.put(merID, getZeroProtocolCounter());
				logger.debug("New counter created for merchant: " + merID);
			}
			// Counter exist, increase counter
			counter = (Map) getMsgMerchantIDCounters().get(merID);
            increaseMapItemValue( counter, protocol );
            increaseMapItemValue( counter, ""+Utils.ALL_SUPPORT );
			logger.debug("Counter increaed for merchant: " + merID);

			// Increase total counter
			setTotalCounter(getTotalCounter() + 1);
			logger.debug("New total count: " + getTotalCounter());

			long current = Calendar.getInstance().getTime().getTime();

			// Recalculate average TPS
			long period = (current - getLastFlushTime()) / 1000;
			logger.debug("Average period: " + period);
			if (period != 0) {
				setAverageTPS((float) getTotalCounter() / period);
				logger.debug("New average TPS: " + getAverageTPS());
			}

			// Flush peak TPS if necessary
			period = (current - getPeakTime()) / 1000;
			logger.debug("Peak period: " + period);
			if (period >= getSamplingTime()) {
				float newPeakTPS = (float) getTotalCounter() / period;

				if (newPeakTPS > getPeakTPS()) {
					setPeakTPS(newPeakTPS);
					logger.debug("New peak TPS:" + getPeakTPS());
				}

				//setLastFlushCounter(getTotalCounter());
				setPeakTime(current);
			}
		}
	}

	/**
	 * Analyze message to sort out status category
	 * @param msg The message
	 * @return The StatusCategory. Return Null if the message is not belong to any category
	 */
	private StatusCategoryID categorize(Message msg) {
		String status = null;
		if (msg instanceof PaymentAuthResMessage) {
			status = ((PaymentAuthResMessage) msg).getStatus();
			if (status.equalsIgnoreCase("Y") || status.equalsIgnoreCase("A")) {
				return new StatusCategoryID(AUTHENTICATED, msg.getType());
			} else {
				if (status.equalsIgnoreCase("N")) {
					return new StatusCategoryID(
						NOT_AUTHENTICATED,
						msg.getType());
				} else {
					if (status.equalsIgnoreCase("U")) {
						return new StatusCategoryID(UNKNOWN, msg.getType());
					} else {
						return null;
					}
				}
			}
		} else {
			if (msg instanceof PaymentVerifResMessage) {
				status = ((PaymentVerifResMessage) msg).getEnrolled();
				if (status.equalsIgnoreCase("Y")) {
					return new StatusCategoryID(ENROLLED, msg.getType());
				} else {
					if (status.equalsIgnoreCase("N")) {
						return new StatusCategoryID(
							NOT_ENROLLED,
							msg.getType());
					} else {
						if (status.equalsIgnoreCase("U")) {
							return new StatusCategoryID(UNKNOWN, msg.getType());
						} else {
							return null;
						}
					}
				}
			} else {
				if (msg instanceof PAResMessage) {
					status = ((PAResMessage) msg).getTxStatus();
					if (status.equalsIgnoreCase("Y")
						|| status.equalsIgnoreCase("A")) {
						return new StatusCategoryID(
							AUTHENTICATED,
							msg.getType());
					} else {
						if (status.equalsIgnoreCase("N")) {
							return new StatusCategoryID(
								NOT_AUTHENTICATED,
								msg.getType());
						} else {
							if (status.equalsIgnoreCase("U")) {
								return new StatusCategoryID(
									UNKNOWN,
									msg.getType());
							} else {
								return null;
							}
						}
					}
				} else {
					if (msg instanceof VEResMessage) {
						status = ((VEResMessage) msg).getEnrolled();
						if (status.equalsIgnoreCase("Y")) {
							return new StatusCategoryID(
								ENROLLED,
								msg.getType());
						} else {
							if (status.equalsIgnoreCase("N")) {
								return new StatusCategoryID(
									NOT_ENROLLED,
									msg.getType());
							} else {
								if (status.equalsIgnoreCase("U")) {
									return new StatusCategoryID(
										UNKNOWN,
										msg.getType());
								} else {
									return null;
								}
							}
						}
					} else {
						if (msg instanceof ProfileAuthResMessage) {
							status = ((ProfileAuthResMessage) msg).getStatus();
							if (status.equalsIgnoreCase("Y")) {
								return new StatusCategoryID(
									AUTHENTICATED,
									msg.getType());
							} else {
								if (status.equalsIgnoreCase("N")) {
									return new StatusCategoryID(
										NOT_AUTHENTICATED,
										msg.getType());
								} else {
									if (status.equalsIgnoreCase("U")) {
										return new StatusCategoryID(
											UNKNOWN,
											msg.getType());
									} else {
										return null;
									}
								}
							}
						} else {
							if (msg instanceof ProfileVerifResMessage) {
								status =
									((ProfileVerifResMessage) msg)
										.getEnrolled();
								if (status.equalsIgnoreCase("Y")) {
									return new StatusCategoryID(
										ENROLLED,
										msg.getType());
								} else {
									if (status.equalsIgnoreCase("N")) {
										return new StatusCategoryID(
											NOT_ENROLLED,
											msg.getType());
									} else {
										if (status.equalsIgnoreCase("U")) {
											return new StatusCategoryID(
												UNKNOWN,
												msg.getType());
										} else {
											return null;
										}
									}
								}
							} else {
								if (msg instanceof IPResMessage) {
									status = ((IPResMessage) msg).getTxStatus();
									if (status.equalsIgnoreCase("Y")) {
										return new StatusCategoryID(
											AUTHENTICATED,
											msg.getType());
									} else {
										if (status.equalsIgnoreCase("N")) {
											return new StatusCategoryID(
												NOT_AUTHENTICATED,
												msg.getType());
										} else {
											if (status.equalsIgnoreCase("U")) {
												return new StatusCategoryID(
													UNKNOWN,
													msg.getType());
											} else {
												return null;
											}
										}
									}
								} else {
									return null;
								}
							}
						}
					}
				}
			}
		}

	}

    /**
     * Returns statistic result per message type
     * @return The vector contains MsgTypeStatisticResult objects
     */
    public Map optMsgTypeStatistic() {
        return PerformanceMonitor.getMsgTypeCounters();
    }

    /**
     * Returns statistic result per status
     * @return The map contains MsgStatusStatisticResult objects
     */
    public Map optMsgStatusStatistic() {
        return PerformanceMonitor.getMsgStatusCounters();
    }

    /**
     * Returns statistic result per merchantID
     * @return The map contains MsgMerchantIDStatisticResult objects
     */
    public Map optMsgMerchantIDStatistic() {
        return PerformanceMonitor.getMsgMerchantIDCounters();
    }

	/**
	 * Flush total counter
	 */
	public void optFlush() {
		synchronized (token) {
			setLastFlushCounter(getTotalCounter());
			setTotalCounter(0);
			setPeakTPS(0);
			setAverageTPS(0);
			setPeakTime(Calendar.getInstance().getTime().getTime());
			setLastFlushTime(Calendar.getInstance().getTime().getTime());

			/*
			 * [Alan's Note - Jan 24, 2003]
			 * Flush statistic counters as well.
			 */
			setMsgTypeCounters( initZeroCounter(zeroTypeCounter) );
			setMsgStatusCounters( initZeroCounter(zeroStatusCounter) );
			setMsgMerchantIDCounters( initZeroCounter(zeroMerchantCounter) );
		}
	}

    /**
     * Make zero counter
     * @param map
     * @return
     */
    public static Map initZeroCounter( Map map )
    {
        Map newmap = Collections.synchronizedMap( new TreeMap() );

        for ( Iterator lt = map.keySet().iterator(); lt.hasNext(); )
        {
            newmap.put( lt.next(), getZeroProtocolCounter() );
        }

        return newmap;
    }

	/**
	 * Returns the averageTPS.
	 * @return long
	 */
	public float getAverageTPS() {
		return averageTPS;
	}

	/**
	 * Returns the counters.
	 * @return TreeMap
	 */
	public static Map getMsgTypeCounters() {
		return PerformanceMonitor.msgTypeCounters;
	}

	/**
	 * Returns the peakTPS.
	 * @return long
	 */
	public float getPeakTPS() {
		return peakTPS;
	}

	/**
	 * Returns the samplingTime.
	 * @return long
	 */
	public long getSamplingTime() {
		return samplingTime;
	}

	/**
	 * Returns the totalCounter.
	 * @return long
	 */
	public long getTotalCounter() {
		return totalCounter;
	}

	/**
	 * Sets the averageTPS.
	 * @param averageTPS The averageTPS to set
	 */
	public static void setAverageTPS(float averageTPS) {
		PerformanceMonitor.averageTPS = averageTPS;
	}

	/**
	 * Sets the counters.
	 * @param counters The counters to set
	 */
	public static void setMsgTypeCounters( Map counters ) {
		PerformanceMonitor.msgTypeCounters = counters;
	}

	/**
	 * Sets the peakTPS.
	 * @param peakTPS The peakTPS to set
	 */
	public static void setPeakTPS(float peakTPS) {
		PerformanceMonitor.peakTPS = peakTPS;
	}

	/**
	 * Sets the samplingTime.
	 * @param samplingTime The samplingTime to set
	 */
	public void setSamplingTime(long samplingTime) {
		PerformanceMonitor.samplingTime = samplingTime;
	}

	/**
	 * Sets the totalCounter.
	 * @param totalCounter The totalCounter to set
	 */
	public static void setTotalCounter(long totalCounter) {
		PerformanceMonitor.totalCounter = totalCounter;
	}

	/**
	 * Returns the lastFlushTime.
	 * @return long
	 */
	public long getPeakTime() {
		return peakTime;
	}

	/**
	 * Returns the statupTime.
	 * @return long
	 */
	public long getStatupTime() {
		return statupTime;
	}

	/**
	 * Sets the lastFlushTime.
	 * @param lastFlushTime The lastFlushTime to set
	 */
	public static void setPeakTime(long lastFlushTime) {
		PerformanceMonitor.peakTime = lastFlushTime;
	}

	/**
	 * Sets the statupTime.
	 * @param statupTime The statupTime to set
	 */
	public static void setStatupTime(long statupTime) {
		PerformanceMonitor.statupTime = statupTime;
	}

	/**
	 * Returns the lastFlushCounter.
	 * @return long
	 */
	public long getLastFlushCounter() {
		return lastFlushCounter;
	}

	/**
	 * Sets the lastFlushCounter.
	 * @param lastFlushCounter The lastFlushCounter to set
	 */
	public static void setLastFlushCounter(long lastFlushCounter) {
		PerformanceMonitor.lastFlushCounter = lastFlushCounter;
	}

	/**
	 * Returns the msgStatusCounters.
	 * @return TreeMap
	 */
	public static Map getMsgStatusCounters() {
		return PerformanceMonitor.msgStatusCounters;
	}

	/**
	 * Returns the msgMerchantIDCounters.
	 * @return TreeMap
	 */
	public static Map getMsgMerchantIDCounters() {
		return PerformanceMonitor.msgMerchantIDCounters;
	}

	/**
	 * Returns the lastFlushTime.
	 * @return long
	 */
	public long getLastFlushTime() {
		return lastFlushTime;
	}

	/**
	 * Sets the lastFlushTime.
	 * @param lastFlushTime The lastFlushTime to set
	 */
	public static void setLastFlushTime(long lastFlushTime) {
		PerformanceMonitor.lastFlushTime = lastFlushTime;
	}

	/**
	 * Sets the msgStatusCounters.
	 * @param msgStatusCounters The msgStatusCounters to set
	 */
	public static void setMsgStatusCounters(  Map msgStatusCounters ) {
		PerformanceMonitor.msgStatusCounters = msgStatusCounters;
	}

	/**
	 * Sets the msgMerchantIDCounters.
	 * @param msgMerchantIDCounters The msgMerchantIDCounters to set
	 */
	public static void setMsgMerchantIDCounters( Map msgMerchantIDCounters ) {
		PerformanceMonitor.msgMerchantIDCounters = msgMerchantIDCounters;
	}

    /**
     * Increase the mapping item integer value
     */
    public static void increaseMapItemValue( Map props, String key )
    {
        if ( props == null || key == null )
            return;

        Long value = (Long)props.get( key );
        if ( value != null )
            props.put( key, new Long( value.longValue() + 1 ) );
    }

    /**
     *
     * @return - counters mapping divided by protocol
     */
    public static Map getZeroProtocolCounter()
    {
        Map counters = Collections.synchronizedMap( new TreeMap() );

        counters.put( ""+ Utils.VISA_SUPPORT, new Long(0) );
        counters.put( ""+ Utils.MASTER_SUPPORT, new Long(0) );
        counters.put( ""+ Utils.ALL_SUPPORT, new Long(0) );

        return counters;
    }}
