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

package com.oncecorp.visa3d.bridge.listening;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.apache.log4j.Logger;
import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;

import com.oncecorp.visa3d.bridge.beans.BeansHelper;
import com.oncecorp.visa3d.bridge.beans.MessageFieldBean;
import com.oncecorp.visa3d.bridge.beans.MessageDefinitionBean;
import com.oncecorp.visa3d.bridge.utility.XMLUtils;
import com.oncecorp.visa3d.bridge.security.ShaMessageDigest;

/**
 * <p>Title: MessageColumnDataExtract</p>
 * <p>Description: Extract message data needed by auditing from the original messages </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */

public class MessageColumnDataExtract
{
    public final static String MESSAGE_ID =          "message_id";
    public final static String MESSAGE_STATUS =      "message_status";
    public final static String CARD_NUMBER =         "card_number";
    public final static String CARD_NUMBER_FLAG =    "card_number_flag";
    public final static String TRANSACTION_ID =      "transaction_id";

    public final static String ALL_MESSAGE   =       "ALL";

    private static Hashtable m_message_status = new Hashtable();
    private static Hashtable m_card_number = new Hashtable();
    private static Hashtable m_transaction_id = new Hashtable();

    private static Hashtable m_allColumnDefinitions = new Hashtable();

    static {

        m_message_status.put(
                BeansHelper.getMessageMappingKey("PARes", "1.0.1" ), "TX/status");
        m_message_status.put(
                BeansHelper.getMessageMappingKey("PARes", "1.0.2" ), "TX/status");
        m_message_status.put(
                BeansHelper.getMessageMappingKey("PaymentAuthRes", "1.0" ), "status");
        m_message_status.put(
                BeansHelper.getMessageMappingKey("PaymentAuthRes", "1.1" ), "status");
        m_message_status.put(
                BeansHelper.getMessageMappingKey("VERes", "1.0.1" ), "CH/enrolled");
        m_message_status.put(
                BeansHelper.getMessageMappingKey("VERes", "1.0.2" ), "CH/enrolled");
        m_message_status.put(
                BeansHelper.getMessageMappingKey("PaymentVerifRes", "1.0" ), "enrolled");
        m_allColumnDefinitions.put( MESSAGE_STATUS, m_message_status );

        /**
         * Card number should handle different
         */
        m_card_number.put(
                BeansHelper.getMessageMappingKey("VEReq", "1.0.1" ), "pan");
        m_card_number.put(
                BeansHelper.getMessageMappingKey("VEReq", "1.0.2" ), "pan");
        m_card_number.put(
                BeansHelper.getMessageMappingKey("PARes", "1.0.1" ), "pan");
        m_card_number.put(
                BeansHelper.getMessageMappingKey("PARes", "1.0.2" ), "pan");
        m_card_number.put(
                BeansHelper.getMessageMappingKey("PaymentVerifReq", "1.0" ), "pan");
        m_card_number.put(
                BeansHelper.getMessageMappingKey("PaymentVerifReq", "1.1" ), "pan");
        m_allColumnDefinitions.put( CARD_NUMBER, m_card_number );


        m_transaction_id.put(
                BeansHelper.getMessageMappingKey("PaymentVerifReq", "1.1" ), "transactionID");
        m_transaction_id.put(
                BeansHelper.getMessageMappingKey("PaymentAuthRes", "1.0" ), "transactionID");
        m_transaction_id.put(
                BeansHelper.getMessageMappingKey("PaymentAuthRes", "1.1" ), "transactionID");
        m_allColumnDefinitions.put( TRANSACTION_ID, m_transaction_id );
    }

    private static Logger m_logger = DataBridgeLoger.getLogger(
            MessageColumnDataExtract.class.getName() );

    /**
     * Default constructor
     */
    public MessageColumnDataExtract()
    {
    }

    /**
     * Get given column's XPath for the given message type and version
     * @param tbl - the column XPath defintion table
     * @param type - message type
     * @param version - message version
     * @return - the XPath of a given column field under the given type and version
     */
    public static String getColumnDefinitionXpath( Hashtable tbl,
            String type, String version )
    {
        if ( tbl.containsKey( ALL_MESSAGE ) )
            return (String) tbl.get( ALL_MESSAGE );
        else
            return (String) tbl.get(
                    BeansHelper.getMessageMappingKey( type, version) );
    }

    /**
     * Extract the message column needed data before fields operation
     * @param tbl - the properties tbale contain the setting value
     * @param type - message type
     * @param version - message version
     * @param doc - message document
     */
    public static void getMessageColumnData( Properties tbl, String type,
            String version, Document doc )
    {
       m_logger.debug("Enter getMessageColumnData for ["
                      + type + "] [" + version + "]" );
       boolean idFlag = false;
       NodeList nodes = doc.getElementsByTagName("Message");
       if ( nodes != null && nodes.getLength() > 0 ) {
           Element message = (Element)nodes.item(0);
           tbl.put( MESSAGE_ID, message.getAttribute("id") );
           idFlag = true;
       }

       Element root = MessageFieldsFilter.getMessageTypeRoot( doc, type, version );

        if ( root != null )
        {
            if ( !idFlag )
                tbl.put( MESSAGE_ID, root.getAttribute("id") );

            String name = "";
            String key;
            Hashtable dtbl;
            boolean flag;
            for ( Iterator lt = m_allColumnDefinitions.keySet().iterator();
                  lt.hasNext(); )
            {
                key = (String) lt.next();
                dtbl = (Hashtable) m_allColumnDefinitions.get( key );
                name = getColumnDefinitionXpath( dtbl, type, version );
                flag = false;
                if ( name != null )
                {
                    Element node = XMLUtils.findXPathElement( root, name );
                    if ( node != null )
                    {
                        tbl.put( key,  XMLUtils.getText( node ) );
                        flag = true;
                    }
                }

                if ( !flag )
                    tbl.put( key, "" );
            }
        }

        m_logger.debug("Leave getMessageColumnData for ["
                       + type + "] [" + version + "]" );
    }

    /**
     * Set up card number and its flag from the message document
     * @param tbl - the properties table to set attribute
     * @param type - message type
     * @param version - message version
     * @param doc - message body document after message fields operation
     * @param fields - message fields definition list
     */
    public static void checkCardNumberField( Properties tbl, String type, String version,
            Document doc, List fields )
    {

        m_logger.debug("Enter checkCardNumberField for ["
                       + type + "] [" + version + "]" );

        String card_number = tbl.getProperty( CARD_NUMBER );

        /**
         * Set default value
         */
        tbl.put( CARD_NUMBER, "" );
        tbl.put( CARD_NUMBER_FLAG, ""+BeansHelper.NONE_FLAG );

        Element root = MessageFieldsFilter.getMessageTypeRoot( doc, type, version );

        if ( root != null )
        {
            String name = getColumnDefinitionXpath( m_card_number, type, version );
            String cardValue = null;

            if ( name != null )
            {
                Element node = XMLUtils.findXPathElement( root, name );
                if ( node != null )
                {
                    cardValue = XMLUtils.getText( node ) ;
                }
            }

            if ( cardValue != null )
            {

                MessageFieldBean mfb;
                MessageDefinitionBean mdb = MessageFieldsFilter
                        .getMessageMappingBean()
                        .getMessageDefinition( type, version );
                List mlist = mdb.getMandatoryList();
                boolean isEncrypted = false;

                if ( mlist.contains( name ) )
                {
                    tbl.put( CARD_NUMBER_FLAG, ""+BeansHelper.ENCRYPTION_FLAG );
                    isEncrypted = true;
                }
                else
                    tbl.put( CARD_NUMBER_FLAG, ""+BeansHelper.PLAIN_FLAG );
                if ( fields != null )
                {
                    boolean foundFlag = false;

                    for ( Iterator lt = fields.iterator(); !foundFlag && lt.hasNext() ; )
                    {
                        mfb = (MessageFieldBean) lt.next();
                        if ( mfb.getName().trim().equalsIgnoreCase( name.trim() ) )
                        {
                           if ( mfb.getFormat() != null &&
                                      !mfb.getFormat().trim().equals("") )
                           {
                                tbl.put( CARD_NUMBER_FLAG, ""+BeansHelper.MASK_FLAG );
                                isEncrypted = false;
                           }
                            else if ( mfb.isEncryption() )
                            {
                                tbl.put( CARD_NUMBER_FLAG, ""+BeansHelper.ENCRYPTION_FLAG);
                                isEncrypted = true;
                            }

                            foundFlag = true;
                        }
                    }
                }

                if ( !isEncrypted )
                    tbl.put( CARD_NUMBER, cardValue );
                else
                {
                    try {
                        tbl.put( CARD_NUMBER, ShaMessageDigest.digestString( card_number ) );
                    } catch ( Exception e )
                    {
                        m_logger.debug("Error during message digest.", e);
                        tbl.put( CARD_NUMBER, "" );
                    }
                }
            }

            m_logger.debug("Leave checkCardNumberField for ["
                          + type + "] [" + version + "]" );
       }
    }

}