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

package com.oncecorp.visa3d.bridge.plugin.purchasbased;

import org.apache.log4j.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;
import com.oncecorp.visa3d.bridge.plugin.PluginException;
import com.oncecorp.visa3d.bridge.plugin.soap.SOAPDeliveryService;
import com.oncecorp.visa3d.bridge.utility.XMLUtils;


/**
 * A plug in that delivers purchase based messages to a web service
 *
 */
public class SOAPPurchaseClient extends PurchaseBasedPlugin
{

	private String m_url;
	private SOAPDeliveryService m_soapService;
	private static String SOAP_ENDPOINT_URL_TAG = "endpointURL";
	private static Logger m_logger =
		DataBridgeLoger.getLogger(SOAPPurchaseClient.class.getName());

	public SOAPPurchaseClient(String serverURL)
	{
		m_url = serverURL;
		m_soapService = new SOAPDeliveryService(m_url);
	}

	private boolean m_initialized = false;
	/**
	 * @see com.oncecorp.visa3d.bridge.plugin.purchasbased.PurchaseBasedPlugin#handlePartialPurchaseTransaction(Document)
	 */
	public boolean handlePartialPurchaseTransaction(
		String messageID,
		Document msg)
	{
		try
		{
			m_soapService.deliverMessage(messageID, msg, true);
			return true;
		}
		catch (Exception e)
		{
			/**
			 * todo capture the details of the original exception in the plugin exception
			 */
			throw new PluginException();
		}
	}

	/**
	 * @see com.oncecorp.visa3d.bridge.plugin.purchasbased.PurchaseBasedPlugin#handlePurchaseTransaction(Document)
	 */
	public boolean handlePurchaseTransaction(String messageID, Document msg)
	{
		try
		{
			m_soapService.deliverMessage(messageID, msg, false);
			return true;
		}
		catch (Exception e)
		{
			/**
			 * todo capture the details of the original exception in the plugin exception
			 */
			throw new PluginException();
		}
	}

	/**
	 * @see com.oncecorp.visa3d.bridge.listening.MPIMessageListener#getDescription()
	 */
	public String getDescription()
	{
		return "SOAP Purchase Based Client delivering messages to " + m_url;
	}

	/**
	 * @see com.oncecorp.visa3d.bridge.listening.MPIMessageListener#getName()
	 */
	public String getName()
	{
		return "SOAPPurchaseClient";
	}

	/**
	 * @see com.oncecorp.visa3d.bridge.listening.MPIMessageListener#config(String)
	 */
	public void config(String xml)
	{
		try
		{
			super.config(xml);
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
				"SOAPPurchaseClient could not determine SOAP URL"
					+ this.getName());
			m_initialized = false;
		}
	}

	/**
	 * @see com.oncecorp.visa3d.bridge.plugin.PluginRoot#isInitialized()
	 */
	public boolean isInitialized()
	{
		return m_initialized;
	}

}
