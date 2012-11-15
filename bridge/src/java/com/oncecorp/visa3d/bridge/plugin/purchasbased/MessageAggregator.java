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

import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;

/**
 * A utility class used by Purchase Based Plugins for aggregating messages.
 *
 */
public class MessageAggregator
{

	private static Logger m_logger =
		DataBridgeLoger.getLogger(MessageAggregator.class.getName());

	/**
	 * The intent is to call the static utility methods directly, so constructor is not needed.
	 * Could do this as a singleton too
	 * Since all services are stateless, it it best if all plugins share access to these methods rather
	 * than instantiate multiple copies
	 */
	private MessageAggregator()
	{
	}

	/**
	 * Method aggregateMessage. Adds child elements of a root element to a map.  The list contains unique
	 * NODELISTS of elements  (relative to their element names).  Duplicate element lists replace existing ones.
	 * 
	 * 
	 * An important note here is that this method will continue to work if message "Extensions" are introduced, but
	 * only for extension added to a single message type.  If multiple messages contain Extensions, then this
	 * logic will throw away duplicates which is incorrect behavior.
	 * 
	 * There is one of several issues to resolve when messages extensions are introduced.
	 * 
	 * 
	 * @param aggregateSoFar
	 * @param messageBodyElement:  An MPI message body whose root tag start with the message type node (for example
	 * VERes)
	 * @return Map:  Key: Element node name, Value: NodeList
	 */
	protected static Map aggregateMessage(
		Map aggregateSoFar,
		Element messageBodyElement)
	{

		m_logger.debug("In MessageAggregator.aggregateMessage");

		NodeList messageNodes = messageBodyElement.getChildNodes();
		Node nextMessageChild;
		String nextMessageChildNodeName;

		for (int j = 0; j < messageNodes.getLength(); j++)
		{
			nextMessageChild = messageNodes.item(j);

			if (nextMessageChild instanceof Element)
			{
				nextMessageChildNodeName =
					((Element) nextMessageChild).getNodeName();
				aggregateSoFar.put(
					nextMessageChildNodeName,
					messageBodyElement.getElementsByTagName(
						nextMessageChildNodeName));
			}
		}
		m_logger.debug("Out MessageAggregator.aggregateMessage");

		return aggregateSoFar;
	}

	/**
	 * Method convertToDocument.
	 * @param aggregateMessage:  Map returned from aggregateMessage method
	 * @return Document.  See design docs for document DTD.  
	 */
	protected static Document convertToDocument(Map aggregateMessage)
	{
		m_logger.debug("In MessageAggregator.convertToDocument");

		try
		{
			Document theDocument =
				DocumentBuilderFactory
					.newInstance()
					.newDocumentBuilder()
					.newDocument();

			Element root =
				(Element) theDocument.createElement(
					PurchaseBasedPlugin.MESSAGE_TAG);
			theDocument.appendChild(root);

			NodeList nextNodeList;
			for (Iterator i = aggregateMessage.keySet().iterator();
				i.hasNext();
				)
			{
				nextNodeList = (NodeList) aggregateMessage.get(i.next());

				for (int j = 0; j < nextNodeList.getLength(); j++)
				{
					Node orphan =
						theDocument.importNode(nextNodeList.item(j), true);
					root.appendChild(orphan);
				}

			}
			return theDocument;
		}
		catch (Exception e)
		{
			m_logger.error("Could not convert message to Document", e);
		}
		m_logger.debug("Out MessageAggregator.convertToDocument");

		return null;

	}

}
