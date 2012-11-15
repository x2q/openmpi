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

package com.oncecorp.visa3d.bridge.plugin.messagebased;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;
import com.oncecorp.visa3d.bridge.plugin.PluginException;
import com.oncecorp.visa3d.bridge.plugin.soap.SOAPDeliveryService;
import com.oncecorp.visa3d.bridge.utility.XMLUtils;

/**
 *
 * A SOAP client that can receive individual messages and deliver them
 * to a web service.  The web service is not provided.
 * 
 * The actual SOAP client is handled by the SoapDeliveryService
 */
public class SOAPMessageClient extends MessageBasedPlugin
{

	private String m_url;
	private SOAPDeliveryService m_soapService;

	private static Logger m_logger =
		DataBridgeLoger.getLogger(SOAPMessageClient.class.getName());

	private static String SOAP_ENDPOINT_URL_TAG = "endpointURL";

	private static boolean m_initialized = false;
	/**
	 * @see com.oncecorp.visa3d.bridge.listening.MPIMessageListener#config(String)
	 */
	public void config(String xml)
	{

		try
		{
			Document configData = XMLUtils.getDocumentFromString(xml);

			// assumption is that there is a url tag with a value.
			m_url =
				XMLUtils.getText(
					(Element) configData.getElementsByTagName(
						SOAP_ENDPOINT_URL_TAG).item(
						0));

			m_logger.debug("SOAPPurchaseClient setting SOAP URL to " + m_url);
			m_soapService = new SOAPDeliveryService(m_url);
			m_initialized = true;
		}
		catch (Exception e)
		{
			m_logger.error(
				"SOAP Message Client did not receive web service endpoint URL",
				e);
			m_initialized = false;
		}

	}

	/**
	 * @see com.oncecorp.visa3d.bridge.listening.MPIMessageListener#getDescription()
	 */
	public String getDescription()
	{
		return "SOAP Plugin delivering messages to " + m_url;
	}

	/**
	 * @see com.oncecorp.visa3d.bridge.listening.MPIMessageListener#getName()
	 */
	public String getName()
	{
		return "SOAPMessageClient";
	}

	/**
	 * @see com.oncecorp.visa3d.bridge.plugin.PluginRoot#init()
	 */
	public void init() throws PluginException
	{
	}

	/**
	 * @see com.oncecorp.visa3d.bridge.plugin.PluginRoot#isInitialized()
	 */
	public boolean isInitialized()
	{
		return m_initialized;
	}

	/**
	 * @see com.oncecorp.visa3d.bridge.listening.MPIMessageListener#handleMsg(Properties, Document)
	 */
	public boolean handleMsg(Properties props, Document msg)
	{
		try
		{
			m_logger.debug("In [SOAPMessageClient handleMsg]");

			m_soapService.deliverMessage(msg);
			m_logger.debug("Out [SOAPMessageClient handleMsg]");

			return true;
		}
		catch (Exception e)
		{
			m_logger.error("Unable to handle message", e);
			throw new PluginException();
		}
	}

	/**
	 * @see com.oncecorp.visa3d.bridge.listening.MPIMessageListener#start()
	 */
	public void start()
	{
		// do nothing
	}

	/**
	 * @see com.oncecorp.visa3d.bridge.listening.MPIMessageListener#stop()
	 */
	public void stop()
	{
		// do nothing
	}

}
