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

package com.oncecorp.visa3d.mpi.controller;

import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.monitoring.ExecutionControlMBean;

import java.util.Calendar;
import java.util.Date;

import javax.management.AttributeChangeNotification;
import javax.management.NotificationBroadcasterSupport;

import org.apache.log4j.Logger;

/**
 * Class description
 * 
 * @author Alan Zhang
 * @version 1.0 4-Dec-02
 */
public class ExecutionControl extends NotificationBroadcasterSupport 
                           implements ExecutionControlMBean {

	public final static String STATUS_NORMAL = "0";
	public final static String STATUS_STOPPED_BY_CORE = "1";
	public final static String STATUS_STOPPED_BY_CONSOLE = "2";
	
	private static ExecutionControl ec; 
	
	protected Logger logger =
		MPILogger.getLogger(ExecutionControl.class.getName());

	private static String executionStatus;
	private static String stopReason;
	private static long startTime;
	private static long stopTime;

	static {
		executionStatus = STATUS_NORMAL;
		startTime = Calendar.getInstance().getTime().getTime();
	}
	
	public ExecutionControl() {
		super();
		if (ec == null) ec = this;
	}
	
	public synchronized static ExecutionControl getInstance() {
		if (ec == null) {
			ec = new ExecutionControl();
		}
		
		return ec;
	}

	/**
	 * Returns server execution status
	 * @return True if running
	 */
	public String getExecutionStatus() {
		return executionStatus;
	}

	/**
	 * Manually start server
	 */
	public void start() {
		setExecutionStatus(STATUS_NORMAL);
		Date current = Calendar.getInstance().getTime();
		setStartTime(current.getTime());
		logger.warn("Server manually started at: " + current.toString());
	}

	/**
	 * Manully stop server
	 */
	public void stop(String status, String reason) {
		// get current Date
		Date current = Calendar.getInstance().getTime();
		
        /*
         * [Martin's Note - Jun 26, 2003 - 17:00]
         * Send status notification for all stop conditions. The NotificationListener will
         * decide if he needs to process it or not.
         */
		if (!status.equalsIgnoreCase(STATUS_NORMAL)) {
		   AttributeChangeNotification att = new AttributeChangeNotification(this,          // The source of this notification
		                                     0,                                             // sequenceNumber
		                                     current.getTime(),                             // timestamp
                                             reason,                                        // Reason for stop
                                             "ExecutionStatus",                             // Attribute Name
                                             "java.lang.String",                            // Attribute Type
                                             executionStatus,                               // Attribute old value
                                             status);                                       // Attribute new value
           sendNotification(att);
		   logger.warn("Notification sent at: "+ current.toString() + "\nReason is: " + reason);
		}
		
		// other settings
		setExecutionStatus(status);
		setStopTime(current.getTime());
		setStopReason(reason);
		logger.warn("\nServer stopped at: " + current.toString());
	}

	/**
	 * Returns the manualStartTime.
	 * @return long
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * Returns the manualStopTime.
	 * @return long
	 */
	public long getStopTime() {
		return stopTime;
	}

	/**
	 * Returns the stopReason.
	 * @return String
	 */
	public String getStopReason() {
		return stopReason;
	}

	/**
	 * Sets the manualStartTime.
	 * @param manualStartTime The manualStartTime to set
	 */
	private void setStartTime(long manualStartTime) {
		ExecutionControl.startTime = manualStartTime;
	}

	/**
	 * Sets the manualStopTime.
	 * @param manualStopTime The manualStopTime to set
	 */
	private void setStopTime(long manualStopTime) {
		ExecutionControl.stopTime = manualStopTime;
	}

	/**
	 * Sets the stopReason.
	 * @param stopReason The stopReason to set
	 */
	public void setStopReason(String stopReason) {
		ExecutionControl.stopReason = stopReason;
	}

	/**
	 * Sets the executionStatus.
	 * @param executionStatus The executionStatus to set
	 */
	public void setExecutionStatus(String executionStatus) {
		ExecutionControl.executionStatus = executionStatus;
	}

}
