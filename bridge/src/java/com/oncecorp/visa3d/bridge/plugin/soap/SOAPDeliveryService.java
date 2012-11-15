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

package com.oncecorp.visa3d.bridge.plugin.soap;

import java.io.ByteArrayOutputStream;

import javax.xml.messaging.URLEndpoint;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;

/**
 * A class for making SOAP calls to a configured web service
 *
 */
public class SOAPDeliveryService
{

	private String m_urlEndpoint;

	/** The target web service is required to use these parameters so they
	 * are non configurable
	 */
	private String m_targetObjectURI = "urn:handleMessage";
	private String m_methodName = "handleMessage";
	private String m_messageParam = "message";

	private static Logger m_logger =
		DataBridgeLoger.getLogger(SOAPDeliveryService.class.getName());

	public SOAPDeliveryService(String serverURL)
	{
		m_urlEndpoint = serverURL;
	}

	/**
	 * Method deliverMessage.
	 * @param theMessage  The Message to deliver
	 * @return SOAPMessage The response
	 * @throws Exception
	 */
	public SOAPMessage deliverMessage(Document theMessage) throws Exception
	{
		try
		{

	
			// convert the document to a string
			Transformer transformer =
				TransformerFactory.newInstance().newTransformer();

			DOMSource source = new DOMSource(theMessage);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(os);
			transformer.transform(source, result);

			URLEndpoint endpoint = new URLEndpoint(m_urlEndpoint);

			SOAPConnectionFactory scf = SOAPConnectionFactory.newInstance();
			SOAPConnection connection = scf.createConnection();

			MessageFactory mf = MessageFactory.newInstance();

			// Create a message from the message factory.
			SOAPMessage msg = mf.createMessage();

			//			MimeHeaders hdrs = msg.getMimeHeaders();
			//			hdrs.addHeader(
			//				"SOAPAction",
			//				"\"http://www.webserviceX.NET/GetQuote\"");

			SOAPPart soapPart = msg.getSOAPPart();
			SOAPEnvelope envelope = soapPart.getEnvelope();
			envelope.addNamespaceDeclaration(
				"xsi",
				"http://www.w3.org/2001/XMLSchema-instance");
			envelope.addNamespaceDeclaration(
				"xsd",
				"http://www.w3.org/2001/XMLSchema");
			SOAPBody body = envelope.getBody();

			SOAPElement belement =
				body.addChildElement(
					envelope.createName(m_methodName, "", m_targetObjectURI));
			belement.addChildElement("msg").addTextNode(os.toString());
			msg.saveChanges();
			SOAPMessage reply = connection.call(msg, endpoint);

			connection.close();
			m_logger.debug("Out [SOAPDeliveryService deliverMessage]");

			return reply;

		}
		catch (Throwable e)
		{
			m_logger.error("Unable to deliver SOAP Message", e);
			return null;
		}
	}

	/**
	 * Method deliverMessage.
	 * @param messageID The message id
	 * @param theMessage The message body
	 * @param partialTransaction Is this a partial or complete transaction?
	 * @return SOAPMessage The response message
	 * @throws Exception
	 */
	public SOAPMessage deliverMessage(
		String messageID,
		Document theMessage,
		boolean partialTransaction)
		throws Exception
	{
		try
		{


			// convert the document to a string
			Transformer transformer =
				TransformerFactory.newInstance().newTransformer();

			DOMSource source = new DOMSource(theMessage);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(os);
			transformer.transform(source, result);

			URLEndpoint endpoint = new URLEndpoint(m_urlEndpoint);

			SOAPConnectionFactory scf = SOAPConnectionFactory.newInstance();
			SOAPConnection connection = scf.createConnection();

			MessageFactory mf = MessageFactory.newInstance();

			// Create a message from the message factory.
			SOAPMessage msg = mf.createMessage();

			//			MimeHeaders hdrs = msg.getMimeHeaders();
			//			hdrs.addHeader(
			//				"SOAPAction",
			//				"\"http://www.webserviceX.NET/GetQuote\"");

			SOAPPart soapPart = msg.getSOAPPart();
			SOAPEnvelope envelope = soapPart.getEnvelope();
			envelope.addNamespaceDeclaration(
				"xsi",
				"http://www.w3.org/2001/XMLSchema-instance");
			envelope.addNamespaceDeclaration(
				"xsd",
				"http://www.w3.org/2001/XMLSchema");
			SOAPBody body = envelope.getBody();

			SOAPElement belement =
				body.addChildElement(
					envelope.createName(m_methodName, "", m_targetObjectURI));
			belement.addChildElement("msgID").addTextNode(messageID);

			belement.addChildElement("msg").addTextNode(os.toString());
			belement.addChildElement("partialMsg").addTextNode(
				new Boolean(partialTransaction).toString());

			msg.saveChanges();
			SOAPMessage reply = connection.call(msg, endpoint);

			connection.close();
			m_logger.debug("In [SOAPDeliveryService deliverMessage2]");

			return reply;

		}
		catch (Throwable e)
		{
			m_logger.error("Unable to deliver SOAP Message", e);
			return null;
		}

	}

}
