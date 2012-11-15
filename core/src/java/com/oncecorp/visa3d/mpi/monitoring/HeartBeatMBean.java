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

package com.oncecorp.visa3d.mpi.monitoring;

import java.util.Properties;

/**
 * Interface to be implemented by all modules that want to support
 * heartbeat mechanism. Modified to be allow to receive properties information
 */
public interface HeartBeatMBean {
	// default HeartBeatMBean object name
	public static final String HEARTBEAT_OBJECT_NAME = "type=heartBeat";

	// connector types
	public static final String RMI_TYPE = "rmi";
	public static final String IIOP_TYPE = "iiop";

	// properties
	public static final String TRIES_PROP = "mx4j.heartbeat.tries";
	public static final String PERIOD_PROP = "mx4j.heartbeat.period";

	// default heartbeat period in seconds
	public static final int DEFAULT_PERIOD = 10;

	/**
	 * HeartBeat source will retry calling listener a few times and then will
	 * declare listener down
	 */
	public static final int DEFAULT_RETRIES = 3;

	/**
	 * Adds the specified heartbeat listener to receive heartbeat notifications from
	 * this HeartBeatMBean.
	 */
	public void addHeartBeatListener(String heartBeatListenerName, Object connectorType, Object listenerAddress, Properties props);

	/**
	 * Removes the specified heartbeat listener so that it no longer receives
	 * heartbeat notifications from this HeartBeatMBean.
	 */
	public void removeHeartBeatListener(String heartBeatListenerName);
}
