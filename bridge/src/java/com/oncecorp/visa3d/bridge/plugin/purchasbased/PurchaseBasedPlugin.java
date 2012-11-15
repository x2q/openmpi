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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.oncecorp.visa3d.bridge.auditing.PropertiesConstants;
import com.oncecorp.visa3d.bridge.listening.TimerServiceListener;
import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;
import com.oncecorp.visa3d.bridge.plugin.PluginException;
import com.oncecorp.visa3d.bridge.plugin.PluginRoot;
import com.oncecorp.visa3d.bridge.utility.XMLUtils;

/**
 * @author rmaccharles
 *
 *  Caches individual MPI messages until a complete purchase transaction is complete, and then
 *  delivers the purchase data as a unit.
 * 
 *  Duplicate message sub-elements are removed
 */

public abstract class PurchaseBasedPlugin
	extends PluginRoot
	implements TimerServiceListener
{

	// The set of open transactions so far
	// Key:  MessageID -- String
	// Value:  Open Message Transaction -- Hashmap with key: message type, value: NodeList
	private HashMap m_openTransactions = new HashMap();

	// A set of timestamps 
	// Key: MessageID -- String
	// Value:  Last time we received a message for this id -- Date
	private HashMap m_transactionLastUpdateTime = new HashMap();
	private boolean m_registered = false;
	private boolean m_initialized = false;

	/**
	 *  This is hardcoded to 24h but may need to be made configurable in the future
	 */
	private long m_maxTransactionAge = 86400000;

	private static Logger m_logger =
		DataBridgeLoger.getLogger(PurchaseBasedPlugin.class.getName());

	/**********  Begin XML message tags ****************************/

	/**********   Begin Visa3D message data tags ****************************/
	protected final static String MESSAGE_TAG = "Message";

	protected final static String PURCHASE_ID_ATTRIB = "id";

	protected final static String MERCHANT_TAG = "Merchant";
	protected final static String MERCHANT_ACQ_BIN_TAG = "acqBIN";
	protected final static String MERCHANT_ID_TAG = "merID";
	protected final static String MERCHANT_NAME_TAG = "name";
	protected final static String MERCHANT_COUNTRY_TAG = "country";
	protected final static String MERCHANT_URL_TAG = "url";

	protected final static String PURCHASE_XID_TAG = "xid";
	protected final static String PURCHASE_DATE_TAG = "date";
	protected final static String PURCHASE_AMOUNT_TAG = "amount";
	protected final static String PURCHASE_PURCHAMOUNT_TAG = "purchAmount";
	protected final static String PURCHASE_CURRENCY_TAG = "currency";
	protected final static String PURCHASE_DESC_TAG = "desc";
	protected final static String PURCHASE_EXP_TAG = "exponent";
	protected final static String PURCHASE_RECUR_TAG = "Recur";
	protected final static String PURCHASE_RECUR_FREQUENCY_TAG = "frequency";
	protected final static String PURCHASE_RECUR_ENDRECUR_TAG = "endRecur";
	protected final static String PURCHASE_INSTALL_TAG = "install";

	protected final static String EXTENSION_TAG = "Extension";
	protected final static String EXTENSION_ID_ATTRIB = "id";
	protected final static String EXTENSION_CRITICAL_ATTRIB = "critical";

	//	protected final static String SERIAL_NUMBER_TAG = "serialNumber";
	protected final static String PAN_TAG = "pan";

	protected final static String BROWSER_TAG = "Browser";
	protected final static String BROWSER_CAT_TAG = "deviceCategory";
	protected final static String BROWSER_ACCEPT_TAG = "accept";
	protected final static String BROWSER_USERAGENT_TAG = "userAgent";

	protected final static String CH_TAG = "CH";
	protected final static String CH_ENROLLED_TAG = "enrolled";
	protected final static String CH_ACCTID_TAG = "acctID";
	protected final static String CH_EXPIRY_TAG = "expiry";

	protected final static String URL_TAG = "url";
	protected final static String PROTOCOL_TAG = "protocol";

	protected final static String IREQ_TAG = "IReq";
	protected final static String IREQ_CODE_TAG = "ireq_iReqCode";
	protected final static String IREQ_DETAIL_TAG = "ireq_iReqDetail";

	protected final static String TX_TAG = "TX";
	protected final static String TX_TIME_TAG = "time";
	protected final static String TX_STATUS_TAG = "status";
	protected final static String TX_CAVV_TAG = "cavv";
	protected final static String TX_ECI_TAG = "eci";
	protected final static String TX_CAVVALG_TAG = "cavvAlgorithm";

	protected final static String ERROR_TAG = "Error";
	protected final static String ERRORCODE_TAG = "errorCode";
	protected final static String ERRORMESSAGE_TAG = "errorMessage";
	protected final static String ERRORDETAIL_TAG = "errorDetail";

	protected final static String VENDORCODE_TAG = "vendorCode";

	/**********   End Visa3D message data tags ****************************/

	/**********   Begin MPI message data tags  ****************************/

	protected static String MPI_INTERFACE_TAG = "MPI_Interface";
	protected static String MPI_ID_ATTRIBUTE_NAME = "id";
	protected static String PURCHASEINFO_TAG = "purchaseInfo";
	protected static String MPI_MERCHANT_ID_TAG = "merchantID";
	protected static String MPI_ENROLLED_TAG = "enrolled";

	/**********   End MPI message data tags  ****************************/

	/**********  Begin XML message tags ****************************/

	public PurchaseBasedPlugin()
	{
		super();
	}

	/**
	 * @see com.oncecorp.visa3d.bridge.plugin.PluginRoot#isInitialized()
	 */
	public boolean isInitialized()
	{
		return true;
	}

	/**
	 * Utility method that would be nice to have as part of DOM
	 */
	private Element getFirstElement(Node theNode)
	{
		if (theNode == null)
		{
			return null;
		}

		NodeList theChildren = theNode.getChildNodes();
		for (int i = 0; i < theChildren.getLength(); i++)
		{
			if (theChildren.item(i) instanceof Element)
			{
				return (Element) theChildren.item(i);
			}
		}
		return null;
	}

	/**
	 * Determine if the message represents the last possible message for a given purchase
	 */
	private boolean isTransactionalBoundaryMessage(Document msg)
	{

		if (msg
			.getElementsByTagName(PropertiesConstants.PAYMENT_AUTH_RES)
			.getLength()
			> 0)
		{
			// always the end of the transaction
			return true;
		}
		NodeList paymentVerifResElements =
			msg.getElementsByTagName(PropertiesConstants.PAYMENT_VERIF_RES);
		if (paymentVerifResElements.getLength() > 0)
		{

			// If the PaymentVerifRes indicates that the user is not enrolled, then this is the end of the transaction
			// However, if the user is enrolled, then then PARES is the end of a transaction

			// Anything other than an upper case "Y" for the "enrolled" attribute is considered
			// to be the end of the transaction

			// We are assuming that the enrolled tag in unambiguous

			NodeList enrolledElements =
				msg.getElementsByTagName(MPI_ENROLLED_TAG);
			String enrolled;
			if (enrolledElements.getLength() != 0)
			{
				enrolled = XMLUtils.getText((Element) enrolledElements.item(0));
				if (enrolled.equals("Y"))
				{
					return false;
				}
				else
				{
					return true;
				}
			}
			else
			{
				m_logger.warn(
					"Enrolled element is missing from enrolment response message. Assuming that card holder is not enrolled");
				return true;
			}

		}

		if (msg.getElementsByTagName(ERROR_TAG).getLength() > 0)
		{
			// always the end of the transaction
			return true;
		}
		if (msg.getElementsByTagName(PropertiesConstants.MPI_ERROR).getLength()
			> 0)
		{
			// always the end of the transaction
			return true;
		}

		// any other message type is not the end of the transaction
		return false;
	}

	/**
	 * @see com.oncecorp.visa3d.bridge.listening.MPIMessageListener#handleMsg(Properties, Document)
	 * 
	 *  Get to the message body and ask the aggregator to aggregate its elements
	 *  Note that this method needs to be aware of the differences between MPI and 3DSecure messages
	 */
	public boolean handleMsg(Properties props, Document msg)
	{
		try
		{
			m_logger.debug("In [PurchaseBased handleMsg]");

			// find the first element with a messasge id attribute
			// the assumption is that there is exactly 1 such message

			Element messageHolderElement =
				((Element) msg.getElementsByTagName(MESSAGE_TAG).item(0));

			String messageID;
			Element messageBodyElement;
			if (messageHolderElement == null)
			{
				// Message is an MPI message.  ID is on the message body element

				messageBodyElement =
					this.getFirstElement(
						((Element) msg
							.getElementsByTagName(MPI_INTERFACE_TAG)
							.item(0)));
				messageID =
					messageBodyElement.getAttribute(MPI_ID_ATTRIBUTE_NAME);
			}
			else
			{
				// Message is a Visa3D message.  ID is on the message holder element
				messageBodyElement = getFirstElement(messageHolderElement);
				messageID =
					messageHolderElement.getAttribute(PURCHASE_ID_ATTRIB);
			}

			Map transactionSoFar = (Map) m_openTransactions.get(messageID);
			if (transactionSoFar == null)
			{
				// create a timestamp for this transaction
				m_transactionLastUpdateTime.put(messageID, new Date());
				transactionSoFar = new HashMap();
			}
			// the aggregator can handle null arraylists
			transactionSoFar =
				MessageAggregator.aggregateMessage(
					transactionSoFar,
					messageBodyElement);

			// Technically, this put is only necessary the first time we see this
			// message id, but it is best to be explicit rather than play with side effects
			m_openTransactions.put(messageID, transactionSoFar);

			// do we need to flush this transaction?
			if (isTransactionalBoundaryMessage(msg))
			{
				// we want to remove this open transaction no matter what -- even if the child fails. So order
				// is important here
				removeOpenTransaction(messageID);
				m_transactionLastUpdateTime.remove(messageID);
				this.handlePurchaseTransaction(
					messageID,
					MessageAggregator.convertToDocument(transactionSoFar));
			}
			m_logger.debug("Out [PurchaseBased handleMsg]");

			return true;
		}
		catch (Exception e)
		{

			m_logger.error("Could not handle message", e);

			return false;
		}
	}

	/**
	 * @see com.oncecorp.visa3d.bridge.listening.MPIMessageListener#start()
	 */
	public void start()
	{
		// do nothing by default
	}

	/**
	 * @see com.oncecorp.visa3d.bridge.listening.MPIMessageListener#stop()
	 */
	public void stop()
	{
		// do nothing by default
	}

	/**
	 * @see com.oncecorp.visa3d.bridge.listening.MPIMessageListener#acceptDynamicFilterChange()
	 * Children may not overwrite this method.
	 */
	public final boolean acceptDynamicFilterChange()
	{
		return false;
	}

	/**
	 * @see com.oncecorp.visa3d.bridge.listening.MPIMessageListener#acceptFilterChange()
	 * Purchase based plug-ins do not allow filtering because filtering the VERES message
	 * makes it impossible to determine the end of a purchase
	 */
	public final boolean acceptFilterChange()
	{
		return false;
	}

	/**
	 * @see com.oncecorp.visa3d.bridge.listening.TimerServiceListener#timerNotify()
	 * 
	 * Clean up any open transactions older the the maximum allowed time period
	 * 
	 */
	public void timerNotify()
	{
		Date nextTimeStamp;
		Date currentTime = new Date();
		Object nextKey;

		m_logger.debug("In [PurchaseBased timerNotify]");

		for (Iterator i = m_transactionLastUpdateTime.keySet().iterator();
			i.hasNext();
			)
		{
			nextKey = i.next();
			nextTimeStamp = (Date) m_transactionLastUpdateTime.get(nextKey);
			if (currentTime.getTime() - nextTimeStamp.getTime()
				> m_maxTransactionAge)
			{
				Map badMessage = (Map) m_openTransactions.get(nextKey);
				// get rid of this transaction and pass it to the child as a partial transaction
				removeOpenTransaction(nextKey);
				handlePartialPurchaseTransaction(
					nextKey.toString(),
					MessageAggregator.convertToDocument(badMessage));
			}

		}
		m_logger.debug("Out [PurchaseBased timerNotify]");

	}

	private void removeOpenTransaction(Object messageID)
	{
		m_openTransactions.remove(messageID);
		m_transactionLastUpdateTime.remove(messageID);
	}

	/**
	 * Method handlePurchaseTransaction.  Called when a complete purchase message is delivered
	 * @param messageID The message id
	 * @param msg The purchase based messeage
	 * @return boolean Return true if the message was handled cleanly
	 */
	public abstract boolean handlePurchaseTransaction(
		String messageID,
		Document msg);

	/**
	 * Method handlePartialPurchaseTransaction. Called whenever a purchase transaction can not complete,
	 * such as in the case when a transaction times out prior to the completion of a transaction.
	 * The data known so far is delivered as a partial transaction
	 * @param messageID  The message id for the partial message
	 * @param msg  The partial message
	 * @return boolean Return true if the message is handled cleanly
	 */
	public abstract boolean handlePartialPurchaseTransaction(
		String messageID,
		Document msg);

	/**
	 * @see com.oncecorp.visa3d.bridge.listening.MPIMessageListener#register()
	 */
	public void register()
	{
		super.register();
		m_registered = true;
	}

	/**
	 * @see com.oncecorp.visa3d.bridge.listening.MPIMessageListener#unregister()
	 * 
	 * Object of this class should be discarded after being unregistered
	 */
	public void unregister()
	{
		m_initialized = false;
		super.unregister();
		// Call init just in case somebody reuses this object. However, the design intent is that
		// this object is thrown away if unregistered
		init();
	}

	/**
	 * @see com.oncecorp.visa3d.bridge.plugin.PluginRoot#init()
	 */
	public void init() throws PluginException
	{
		m_openTransactions.clear();
		m_transactionLastUpdateTime.clear();

		m_registered = false;
		m_initialized = true;
	}

	/**
	 * @see com.oncecorp.visa3d.bridge.listening.MPIMessageListener#config(String)
	 */
	public void config(String xml)
	{
	}

	/**
	 * Purchase based plugins don't accept filter changes
	 */
	public final boolean acceptFilter()
	{
		return false;
	}

}
