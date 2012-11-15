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

package com.oncecorp.visa3d.mpi.jmxagent;

import com.oncecorp.visa3d.mpi.monitoring.HeartBeatMBean;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.management.ObjectName;

import mx4j.connector.RemoteMBeanServer;
import mx4j.log.Log;
import mx4j.log.Logger;
import mx4j.tools.heartbeat.ConnectorException;


/**
 * Adapted from the HeartBeat class in the MX4J class library....
 */
public class HeartBeat implements HeartBeatMBean {
	private HashMap m_listeners = new HashMap();
	private String m_name = HeartBeatMBean.HEARTBEAT_OBJECT_NAME;
	private int m_period = HeartBeatMBean.DEFAULT_PERIOD;
	private int m_retries = HeartBeatMBean.DEFAULT_RETRIES;

	private ObjectName m_objectName;
	private HeartBeatThread m_hbThread = new HeartBeatThread();

	private Thread m_t;
	private boolean m_started[] = new boolean[1];

	public HeartBeat(String name) {
		m_name = name;
		try {
			m_objectName = new ObjectName(m_name);
		}
		catch (Exception ex) {
		}

		Properties env = System.getProperties();
		String p = env.getProperty(TRIES_PROP);
		if (p != null)
			m_retries = (new Integer(p)).intValue();

		p = env.getProperty(PERIOD_PROP);
		if (p != null)
			m_period = (new Integer(p)).intValue();

		m_started[0] = false;
	}

	// When this method returns it guarantees that heartbeat thread has started
	public void start() {
		// start heartbeat thread
		m_t = new Thread(m_hbThread, m_name);
		m_t.start();

		// synchronize with the thread
		synchronized (m_started) {
			while (!m_started[0]) {
				try {
					m_started.wait();
				}
				catch (Exception e) {
				}
			}
		}
	}

	public void stop() {
		Logger logger = getLogger();

		if (logger.isEnabledFor(Logger.TRACE))
			logger.trace(getClass().getName() + ".stop");

		// prepare to stop
		m_hbThread.stop();

		// interrupt HeartBeat thread
		m_t.interrupt();

		Thread.yield();
	}
	

	/**
	 * Adds the specified heartbeat listener to receive heartbeat notifications from
	 * this HeartBeatMBean.
	 */
	public void addHeartBeatListener(String listenerName, Object connectorType, Object listenerAddress, Properties props) {
		Logger logger = getLogger();
		if (logger.isEnabledFor(Logger.DEBUG))
			logger.debug("HeartBeat.addHeartBeatListener: connType=" + connectorType);

		try {
			RemoteMBeanServer conn = CoreHeartBeatConnectorFactory.getFactory().getConnector(connectorType, listenerAddress, props);

			HeartBeatSession session = new HeartBeatSession(conn);

			synchronized (m_listeners) {
				m_listeners.put(listenerName, session);
			}
		}
		catch (ConnectorException ex) {
			// FIXME:
			if (logger.isEnabledFor(Logger.DEBUG))
				logger.debug("HeartBeat.addHeartBeatListener exception", ex);
		}
	}

	/**
	 * Removes the specified heartbeat listener so that it no longer receives
	 * heartbeat notifications from this HeartBeatMBean.
	 */
	public void removeHeartBeatListener(String heartBeatListenerName) {
		synchronized (m_listeners) {
			m_listeners.remove(heartBeatListenerName);
		}
	}

	// Getter/Setter for the heartbeat period in milliseconds.
	public int getPeriod() {
		return m_period;
	}

	public void setPeriod(int period) {
		m_period = period;
	}

	// Getter/Setter for the number of retries.
	public int getRetries() {
		return m_retries;
	}

	public void setRetries(int nretries) {
		this.m_retries = nretries;
	}

	private Logger getLogger() {
		return Log.getLogger(getClass().getName());
	}

	private class HeartBeatThread implements Runnable {
		private boolean m_stop = false;

		public void run() {
			// notify creator that this thread is running
			synchronized (m_started) {
				m_started[0] = true;
				m_started.notifyAll();
			}

			Logger logger = getLogger();

			while (true) {
				HeartBeatSession sess = null;
				String listener = null;
				RemoteMBeanServer conn;
				Object[] params = new Object[1];
				String[] signature = new String[1];
				params[0] = m_objectName.getCanonicalName();
				signature[0] = "java.lang.String";

				try {
					HashMap listeners;
					synchronized (m_listeners) {
						listeners = (HashMap)m_listeners.clone();
					}

					// loop thru currently registered listeners
					Set keys = listeners.keySet();
					for (Iterator it = keys.iterator(); it.hasNext();) {
						listener = (String)it.next();
						sess = (HeartBeatSession)listeners.get(listener);
						conn = sess.getConnection();
						if (logger.isEnabledFor(Logger.DEBUG))
							logger.debug("HeartBeatThread.run: calling " + listener);
						try {
							conn.invoke(new ObjectName(listener), "processHeartBeat", params, signature);
							sess.reset();
						}
						catch (RemoteException ex) {
							// remote invocation has failed. Should we continue?
							if (logger.isEnabledFor(Logger.DEBUG))
								logger.debug("HeartBeatThread.run: ConnectorException listnr=" + listener);

							if (!sess.shouldContinue()) {
								// remove this listener
								synchronized (m_listeners) {
									m_listeners.remove(listener);
									if (logger.isEnabledFor(Logger.DEBUG))
										logger.debug("HeartBeatThread.run: removed listenr=" + listener);
								}

								listeners.remove(listener);
							}
						} // catch
					} // for (Iterator it = keys.iterator(); it.hasNext();)
				}
				catch (Exception ex) {
					if (logger.isEnabledFor(Logger.DEBUG))
						logger.debug("HeartBeatThread.run exception", ex);
				}

				// Sleep for this amount of time before sending the heart beat informatio
				// to all our listeners
				try {
					Thread.sleep(1000 * m_period);
				}
				catch (Exception ex) {
				}

				if (m_stop) {
					return;
				}
			} // while(true)
		} // run

		public void stop() {
			m_stop = true;
		}
	} // HeartBeatThread

	// utility class to hold info for heartbeat session
	private class HeartBeatSession {
		private RemoteMBeanServer m_conn;
		private int m_failCount = 0;

		public HeartBeatSession(RemoteMBeanServer conn) {
			m_conn = conn;
		}

		public void reset() {
			m_failCount = 0;
		}

		public boolean shouldContinue() {
			m_failCount++;

			if (m_failCount > m_retries) {
				return false;
			}
			return true;
		}

		public RemoteMBeanServer getConnection() {
			return m_conn;
		}
	} // private class HeartBeatSession
}
