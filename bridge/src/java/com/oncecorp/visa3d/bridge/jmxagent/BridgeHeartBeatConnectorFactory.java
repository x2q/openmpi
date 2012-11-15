/*
 * Copyright (C) MX4J.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package com.oncecorp.visa3d.bridge.jmxagent;
import java.util.Properties;

import mx4j.connector.RemoteMBeanServer;
import mx4j.connector.rmi.RMIConnector;
import mx4j.connector.rmi.jrmp.JRMPConnector;
import mx4j.log.Log;
import mx4j.log.Logger;
import mx4j.tools.heartbeat.ConnectorException;
import mx4j.tools.heartbeat.HeartBeatMBean;

/**
 *
 */
public class BridgeHeartBeatConnectorFactory {
	private static BridgeHeartBeatConnectorFactory m_singleton = null;

	public static BridgeHeartBeatConnectorFactory getFactory()
	{
		if (m_singleton == null)
		{
			m_singleton = new BridgeHeartBeatConnectorFactory();
		}
		return m_singleton;
	}

	private BridgeHeartBeatConnectorFactory()
	{
	}



	public RemoteMBeanServer getConnector(Object connType, Object address, Properties props) throws ConnectorException
	{
		if (connType.equals(HeartBeatMBean.RMI_TYPE))
		{
			RMIConnector conn;
			try
			{
				Logger logger = getLogger();
				if (logger.isEnabledFor(Logger.TRACE)) logger.trace(getClass().getName() + ".getConnector: connType=" + connType.toString() + " addr=" + address.toString());
				conn = new JRMPConnector();
				conn.connect((String)address, props);
			}
			catch (Exception ex)
			{
				// FIXME: crude, very crude
				throw new ConnectorException(ex.getClass().getName());
			}
			return conn.getRemoteMBeanServer();
		}
		throw new ConnectorException("Unknown connector type");
	}

	private Logger getLogger()
	{
		return Log.getLogger(getClass().getName());
	}
}

