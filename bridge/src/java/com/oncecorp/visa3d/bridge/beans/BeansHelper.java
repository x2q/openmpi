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

package com.oncecorp.visa3d.bridge.beans;

import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.Collections;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.StringTokenizer;

import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.apache.log4j.Logger;

import com.oncecorp.visa3d.bridge.utility.ConfigureConstants;
import com.oncecorp.visa3d.bridge.utility.XMLUtils;

/**
 * <p>Title: ListeningHelper</p>
 * <p>Description: Provide bean related configue functions </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */

public class BeansHelper implements ConfigureConstants
{

    /**
     * protocol support
     */
    public final static byte   NONE_SUPPORT          =    0x00;
    public final static byte   VISA_SUPPORT          =    0x01;
    public final static byte   MASTER_SUPPORT        =    0x02;
    public final static byte   ALL_SUPPORT           =    0x7f;

    private static Logger m_logger = null;
    private static Map m_messageDefinition  = null;

    /**
     * The following definition are used to identify the card number
     * column stored flag:
     * 1 --- encryption
     * 2 --- mask
     * 3 --- plain
     * -1 --- none exist
     */
    public final static int ENCRYPTION_FLAG = 1;
    public final static int MASK_FLAG = 2;
    public final static int PLAIN_FLAG = 3;
    public final static int NONE_FLAG = -1;

    public static final String DEFAULT_STATUS_RUNNING = "start";

    /**
     * Default constructor
     */
    public BeansHelper()
    {
    }

    /**
     * Parse the XML element and get the merchants list
     * @param item - the XML element
     * @return - merchants list
     */
    public static List merchantsFromXml ( Element item  )
    {

//        getLogger().debug( "Enter merchantsFromXml" );

        List merchantIds = Collections.synchronizedList( new ArrayList() );

        Element merchantItem;
        NodeList merchantNodes = item.getElementsByTagName(MERCHANT_TAG);
        int j;
        for ( j = 0; merchantNodes != null && j < merchantNodes.getLength();
              j++ )
        {
            merchantItem = (Element) merchantNodes.item(j);
            merchantIds.add( XMLUtils.getText(merchantItem) );
        }

//        getLogger().debug( "Exit merchantsFromXml with " + j + " merchants." );

        return merchantIds;
    }

    /**
     * Parse the XML element and get the messages map
     * @param item - the XML element
     * @return - messages map
     */
    public static Map messagesFromXml ( Element item  )
    {
//        getLogger().debug( "Enter messagesFromXml" );

        Map messages = Collections.synchronizedMap( new TreeMap() );

        Element messageItem, fieldItem;
        NodeList messageNodes = item.getElementsByTagName(MSG_TAG);
        NodeList fieldNodes;
        String type, version;
        String id, classname;
        ListeningMessageBean lmsg;
        MessageFieldBean     msgField;

        for ( int j = 0; messageNodes != null && j < messageNodes.getLength();
              j++ )
        {
            messageItem = (Element) messageNodes.item(j);
            type = messageItem.getAttribute("type").trim();
            version = messageItem.getAttribute("version").trim();
            if ( m_messageDefinition != null
                 && !m_messageDefinition.containsKey( getMessageMappingKey( type, version )) )
            {
                getLogger().debug("Message type [" +  type + "] version ["
                                  + version + "] not defined, ignore it.");
                continue;
            }
            lmsg = new ListeningMessageBean(type, version);
            fieldNodes = messageItem.getElementsByTagName(PLUGIN_FIELD_TAG);
            int k;
            for ( k = 0; fieldNodes != null && k < fieldNodes.getLength();
                  k++ )
            {
                fieldItem = (Element)fieldNodes.item(k);
                msgField = new MessageFieldBean(XMLUtils.getText(fieldItem),
                        fieldItem.getAttribute("encryption"),
                        fieldItem.getAttribute("fmt") );
                lmsg.addField( msgField );
            }
            addMessage( messages, lmsg );
            getLogger().debug( "message [" + type + "] [" + version + "] with " + k + " fields." );

        }

//        getLogger().debug( "Exit messageFromXml" );

        return messages;
    }

    /**
     * Configure data bridge bean with the given XML content
     * @param bean - databridge bean
     * @param xml - XML string
     * @return - the updated data bridge bean or a new bean.
     */
    public static DataBridgeBean databridgeFromXml( DataBridgeBean bean, String xml )
    {
        getLogger().debug( "Enter databridgeFromXml String" );

        xml = wrapHeader(xml);
        DataBridgeBean newbean = databridgeFromXml( bean, XMLUtils.getDocumentFromString( xml, false ) );

        getLogger().debug( "Exit databridgeFromXml String" );

        return newbean;
    }

    /**
     * Configure data bridge bean with the given XML content
     * @param bean - databridge bean
     * @param document - DOM root document
     * @return - the updated data bridge bean or a new bean.
     */
    public static DataBridgeBean databridgeFromXml( DataBridgeBean bean, Document document )
    {
        if ( document == null )
        {
            getLogger().warn("databridgeFromXml bean or document is null ");
            return null;
        }
        if ( bean == null )
            bean = new DataBridgeBean();

        NodeList nodes = document.getElementsByTagName( DATA_BRIDGE_TAG );
        if ( nodes == null || nodes.getLength() != 1 )
        {
            getLogger().warn("Configuration file contains 0 or more than 1 "
                          + DATA_BRIDGE_TAG + " element.");
            return null;
        }

        getLogger().debug( "Enter databridgeFromXml document" );

        boolean setFlag = false;
        Element element = (Element)nodes.item(0);
        String str = (String)element.getAttribute("autoSave");
        if ( str != null && !str.trim().equals("") )
        {
            boolean autoSave = Boolean.valueOf(str).booleanValue();
            bean.setAutoSave( autoSave );
            setFlag = true;
        }

        bean.setAttributeSet( setFlag );

        ListeningServiceBean lsb = listeningServiceFromXml( null,
                getServiceElement( element, LISTENING_SERVICE_TAG ) );
        bean.setListeningService( lsb );

        AuditingServiceBean asb = auditingServiceFromXml( null,
                getServiceElement( element, AUDITING_SERVICE_TAG ) );
        bean.setAuditingService( asb );

        MonitoringServiceBean msb = monitoringServiceFromXml( null,
                 getServiceElement( element, MONITORING_SERVICE_TAG ) );
        bean.setMonitoringService( msb );

        PluginServiceBean psb = pluginServiceFromXml( null,
                 getServiceElement( element, PLUGIN_SERVICE_TAG ) );
        bean.setPluginService( psb );

        getLogger().debug( "Exit databridgeFromXml document" );

        return bean;
    }

    /**
     * Configure listening service bean with the given XML content
     * @param bean - listening service bean
     * @param xml - XML string
     * @return - the updated listening service bean or new bean
     */
    public static ListeningServiceBean listeningServiceFromXml( ListeningServiceBean bean, String xml )
    {
        getLogger().debug( "Enter listeningServiceFromXml String" );

        xml = wrapHeader( xml );
        Document document = XMLUtils.getDocumentFromString( xml );
        ListeningServiceBean newbean = listeningServiceFromXml( bean, document );

        getLogger().debug( "Exit listeningServiceFromXml String" );

        return newbean;
    }

    /**
     * Configure listening service bean with the given XML content
     * @param bean - listening service bean
     * @param document - XML document
     * @return - the updated listening service bean or new bean
     */
    public static ListeningServiceBean listeningServiceFromXml( ListeningServiceBean bean, Document document )
    {
        if ( document == null )
            return null;

        return listeningServiceFromXml( bean,
                  getServiceElement( document, LISTENING_SERVICE_TAG) );

    }

    /**
     * Configure listening service bean with the given XML content
     * @param bean - listening service bean
     * @param element - XML element
     * @return - the updated listening service bean or new bean
     */
    public static ListeningServiceBean listeningServiceFromXml( ListeningServiceBean bean,
            Element item )
    {
        if ( item == null )
            return null;

        if ( bean == null )
            bean = new ListeningServiceBean();

        getLogger().debug( "Enter listeningServiceFromXml Element" );

        String tstr = item.getAttribute("timeServiceInterval");
        long interval = 0;
        if ( tstr != null )
        {
            try {
                interval = Long.valueOf(tstr).longValue();
            } catch ( Exception e )
            {
                interval = 0;
            }
            bean.setTimeServiceInterval( interval );
        }
        NodeList pitem = item.getElementsByTagName( LISTENING_ATTRIBUTE_TAG );
        TreeMap props = new TreeMap();
        String name, value;
        for ( int i = 0; pitem != null && i < pitem.getLength(); i++ )
        {
            item = (Element)pitem.item(i);
            name = item.getAttribute("name");
            value = item.getAttribute("value");
            props.put(name, value);
        }
        bean.setProperties( props );
        getLogger().debug( "Exit listeningServiceFromXml Element" );

        return bean;
    }

    /**
     * Configure monitoring service bean with the given XML content
     * @param bean - monitoring service bean
     * @param xml - XML string
     * @return - the updated monitoring service bean or new bean
     */
    public static MonitoringServiceBean monitoringServiceFromXml( MonitoringServiceBean bean, String xml )
    {
        getLogger().debug( "Enter monitoringServiceFromXml String" );

        xml = wrapHeader( xml );
        Document document = XMLUtils.getDocumentFromString( xml );
        bean = monitoringServiceFromXml( bean, document );

        getLogger().debug( "Exit monitoringServiceFromXml String" );

        return bean;
    }

    /**
     * Configure monitoring service bean with the given XML content
     * @param bean - monitoring service bean
     * @param document - XML document
     * @return - the updated monitoring service bean or new bean
     */
    public static MonitoringServiceBean monitoringServiceFromXml( MonitoringServiceBean bean, Document document )
    {
        if ( document == null )
            return null;

        return monitoringServiceFromXml( bean,
               getServiceElement( document, MONITORING_SERVICE_TAG) );

    }

    /**
     * Get ServiceElement from parent
     * @param element - parent element
     * @return - service element
     */
    public static Element getServiceElement( Element element, String name )
    {
        getLogger().debug( "Enter getServiceElement" );

        NodeList pitem = element.getElementsByTagName( name );
        if ( pitem == null || pitem.getLength() != 1 )
        {
            getLogger().warn("In getServiceElement 0 or more than 1 service [" + name + "] is defined. This configuration data is from a sub bean.");
            return null;
        }
        else
            return (Element) pitem.item(0) ;
    }

    /**
     * Get ServiceElement from document
     * @param element - document
     * @return - service element
     */
    public static Element getServiceElement( Document doc, String name )
    {
        NodeList pitem = doc.getElementsByTagName( name );
        if ( pitem == null || pitem.getLength() != 1 )
        {
            getLogger().warn("In getServiceElement 0 or more than 1 service [" + name + "] is defined.This configuration data is from a sub bean.");
            return null;
        }
        else
            return (Element) pitem.item(0) ;
    }

    /**
     * Configure monitoring service bean with the given XML content
     * @param bean - monitoring service bean
     * @param element - XML element
     * @return - the updated monitoring service bean or new bean
     */
    public static MonitoringServiceBean monitoringServiceFromXml( MonitoringServiceBean bean,
            Element element )
    {

        getLogger().debug( "Enter monitoringServiceFromXml Element" );

        if ( element == null )
            return null;

        if ( bean == null )
            bean = new MonitoringServiceBean();

        Element item;
        NodeList pitem = element.getElementsByTagName( JMX_CONFIG_TAG );
        TreeMap props = new TreeMap();
        String name, value;
        for ( int i = 0; pitem != null && i < pitem.getLength(); i++ )
        {
            item = (Element)pitem.item(i);
            name = item.getAttribute("name");
            value = item.getAttribute("value");
            props.put( name, value );
        }

        bean.setProperties( props );

        getLogger().debug( "Exit monitoringServiceFromXml String" );

        return bean;
    }

    /**
     * Configure plugin channel bean with the given XML content
     * @param bean - plugin channel bean
     * @param xml - XML string
     * @return - the updated plugin channel bean or new bean
     */
    public static PluginChannelBean pluginChannelFromXml( PluginChannelBean bean, String xml )
    {
        getLogger().debug( "Enter pluginChannelFromXml String" );

        xml = wrapHeader( xml );

        Document document = XMLUtils.getDocumentFromString( xml );
        if ( document != null )
        {
            NodeList pitem = document.getElementsByTagName( PLUGIN_CHANNEL_TAG );
            if ( pitem == null || pitem.getLength() != 1 )
            {
                getLogger().warn("In pluginChannelFromXml 0 or more than 1 plugin channel is defined.");
            }
            else
                bean = pluginChannelFromXml( bean, (Element) pitem.item(0) );
        }

        getLogger().debug( "Exit pluginChannelFromXml String" );
        return bean;
    }

    /**
     * Configure plugin channel bean with the given XML content
     * @param bean - plugin channel bean
     * @param item - XML element
     * @return - the updated plugin channel bean or new bean
     */
    public static PluginChannelBean pluginChannelFromXml( PluginChannelBean bean, Element item )
    {

        getLogger().debug( "Enter pluginChannelFromXml Element" );

        if ( bean == null )
            bean = new PluginChannelBean();

        Element citem;
        String id, status;
        NodeList clist;

        id = item.getAttribute("id");
        status = item.getAttribute("status");
        String acceptFilter = item.getAttribute("acceptFilter");
        String acceptDynamicFilter = item.getAttribute("acceptDynamicFilter");
        bean.setId( id );
        bean.setStatus( status );
        bean.setAcceptDynamicFilter( Boolean.valueOf(acceptDynamicFilter).booleanValue());
        bean.setAcceptFilter( Boolean.valueOf( acceptFilter).booleanValue() );
        clist = item.getElementsByTagName( CUSTOM_DATA_TAG );
        if ( clist == null || clist.getLength() != 1 )
        {
            getLogger().warn("In pluginChannelFromXml 0 or more than 1 custom data is defined.");
        }
        else
        {
            citem = (Element)(clist.item(0));
            bean.setCustomData( XMLUtils.getText( citem ) );
            bean.setMerchantIds( merchantsFromXml(item) );
        }

        getLogger().debug( "Exit pluginChannelFromXml Element" );

        return bean;
    }

    /**
     * Configure plugin listener bean with the given XML content
     * @param bean - plugin listener bean
     * @param xml - XML string
     * @return - the updated plugin listener bean or new bean
     */
    public static PluginListenerBean pluginListenerFromXml( PluginListenerBean bean, String xml )
    {
        getLogger().debug( "Enter pluginListenerFromXml String" );

        xml = wrapHeader( xml );
        Document document = XMLUtils.getDocumentFromString( xml );
        if ( document != null )
        {
            NodeList pitem = document.getElementsByTagName( PLUGIN_TAG );
            if ( pitem == null || pitem.getLength() != 1 )
            {
                getLogger().warn("In pluginListenerFromXml 0 or more than 1 plugin listener is defined.");
            }
            else
                bean = pluginListenerFromXml( bean, (Element) pitem.item(0) );
        }

        getLogger().debug( "Exit pluginListenerFromXml String" );

        return bean;
    }

    /**
     * Configure plugin listener bean with the given XML content
     * @param bean - plugin listener bean
     * @param item - XML element
     * @return - the updated plugin listener bean or new bean
     */
    public static PluginListenerBean pluginListenerFromXml( PluginListenerBean bean, Element item )
    {
        getLogger().debug( "Enter pluginListenerFromXml Element" );

        if ( bean == null )
            bean = new PluginListenerBean();

        String id, classname;
        PluginChannelBean pl;
        NodeList pnodes;

        id = item.getAttribute("id");
        classname = item.getAttribute("classname");
        bean.setId( id );
        bean.setClassName( classname );
        bean.setMessages( messagesFromXml(item) );

        Map channels = Collections.synchronizedMap( new TreeMap() );

        pnodes = item.getElementsByTagName( PLUGIN_CHANNEL_TAG );

        int i;
        for ( i = 0; pnodes != null && i < pnodes.getLength(); i++ )
        {
            item = (Element)pnodes.item(i);
            pl = new PluginChannelBean();
            pluginChannelFromXml( pl, item );
            channels.put( pl.getId(), pl );
        }

        bean.setChannels( channels );

        getLogger().debug( "Exit pluginListenerFromXml Element [" + id + "] with " + i + " channels. ");
        return bean;
    }

    /**
     * Configure auditing listener bean with the given XML content
     * @param bean - auditing listener bean
     * @param xml - XML string
     * @return - the updated auditing listener bean or new bean
     */
    public static AuditingListenerBean auditingListenerFromXml( AuditingListenerBean bean, String xml )
    {
        getLogger().debug( "Enter auditingListenerFromXml String" );

        xml = wrapHeader( xml );
        Document document = XMLUtils.getDocumentFromString( xml );
        if ( document != null )
        {
            NodeList pitem = document.getElementsByTagName( LOG_HANDLER_TAG );
            if ( pitem == null || pitem.getLength() != 1 )
            {
                getLogger().warn("In auditingListenerFromXml 0 or more than 1 auditing listener is defined.");
            }
            else
                bean = auditingListenerFromXml( bean, (Element) pitem.item(0) );
        }

        getLogger().debug( "Exit auditingListenerFromXml String" );

        return bean;
    }

    /**
     * Configure auditing listener bean with the given XML content
     * @param bean - auditing listener bean
     * @param item - XML element
     * @return - the updated auditing listener bean or new bean
     */
    public static AuditingListenerBean auditingListenerFromXml( AuditingListenerBean bean, Element item )
    {
        String id, status;

        getLogger().debug( "Enter auditingListenerFromXml Element" );

        if ( bean == null )
            bean = new AuditingListenerBean();

        id = item.getAttribute("id");
        status = item.getAttribute("status");
        bean.setId( id );
        bean.setStatus( status );
        bean.setMaxRows(Integer.parseInt(item.getAttribute("maxRows")));
        bean.setThreshold(Integer.parseInt(item.getAttribute("threshold")));
        bean.setMailNotify("true".equals(item.getAttribute("mailNotify")));
        bean.setPeriod(item.getAttribute("period"));

        bean.setMerchantIds( merchantsFromXml(item) );
        bean.setMessages( messagesFromXml(item) );

        //set mail data.
        Element email = XMLUtils.getChildElement(item, "mpidb:email");
        bean.setEmailTemplate(XMLUtils.getChildElementText(email, "mpidb:emailTemplate"));
        bean.setSender(XMLUtils.getChildElementText(email, "mpidb:sender"));
        bean.setTo(XMLUtils.getChildElementText(email, "mpidb:to"));
        bean.setCc(XMLUtils.getChildElementText(email, "mpidb:cc"));

        // set database data.
        /*
        Element database = XMLUtils.getChildElement(item, "mpidb:database");
        if ( database == null ) {
          return;
        }
        bean.setMessageIdColumnType(getChildElementAttribute(database, "mpidb:message_id", "type"));
        bean.setMerchantIdColumnType(getChildElementAttribute(database, "mpidb:merchant_id", "type"));
        bean.setTimeOfPublishingColumnType(getChildElementAttribute(database, "mpidb:time_of_publishing", "type"));
        bean.setMessageColumnType(getChildElementAttribute(database, "mpidb:message", "type"));
        */

        getLogger().debug( "Exit auditingListenerFromXml Element with ["
                           + id + "] [" + status + "]");

        return bean;
    }

    /**
     * Configure plugin service bean with the given XML content
     * @param bean - plugin service bean
     * @param xml - XML string
     * @return - the updated plugin service bean or new bean
     */
    public static PluginServiceBean pluginServiceFromXml( PluginServiceBean bean, String xml )
    {
        getLogger().debug( "Enter pluginServiceFromXml String" );

        xml = wrapHeader( xml );
        Document document = XMLUtils.getDocumentFromString( xml );
        bean = pluginServiceFromXml( bean, document );

        getLogger().debug( "Exit pluginServiceFromXml String" );

        return bean;
    }

    /**
     * Configure plugin service bean with the given XML content
     * @param bean - plugin service bean
     * @param document - XML document
     * @return - the updated plugin service bean or new bean
     */
    public static PluginServiceBean pluginServiceFromXml( PluginServiceBean bean, Document document )
    {
        if ( document == null )
            return null;

         return pluginServiceFromXml( bean,
              getServiceElement( document, PLUGIN_SERVICE_TAG) );

    }

    /**
     * Configure plugin service bean with the given XML content
     * @param bean - plugin service bean
     * @param item - XML element
     * @return - the updated plugin service bean or new bean
     */
    public static PluginServiceBean pluginServiceFromXml( PluginServiceBean bean, Element item )
    {
        getLogger().debug( "Enter pluginServiceFromXml Element" );

        if ( item == null )
            return null;

        if ( bean == null )
            bean = new PluginServiceBean();

        NodeList pnodes = item.getElementsByTagName(PLUGIN_TAG);
        Map plugins = Collections.synchronizedMap( new TreeMap() );
        PluginListenerBean pl;

        int i;
        for ( i = 0; pnodes != null && i < pnodes.getLength(); i++ )
        {
            item = (Element) pnodes.item(i);
            pl = pluginListenerFromXml( null, item );
            if ( pl != null )
                plugins.put( pl.getId(), pl );
        }

        bean.setPlugins( plugins );

        getLogger().debug( "Exit pluginServiceFromXml Element with " + i + " listeners." );

        return bean;
    }

    /**
     * Configure auditing service bean with the given XML content
     * @param bean - auditing service bean
     * @param xml - XML string
     * @return - the updated auditing service bean or new bean
     */
    public static AuditingServiceBean auditingServiceFromXml( AuditingServiceBean bean, String xml )
    {
        getLogger().debug( "Enter auditingServiceFromXml String" );

        xml = wrapHeader( xml );
        Document document = XMLUtils.getDocumentFromString( xml );
        bean = auditingServiceFromXml( bean, document );

        getLogger().debug( "Exit auditingServiceFromXml String" );

        return bean;
    }

    /**
     * Configure auditing service bean with the given XML content
     * @param bean - auditing service bean
     * @param document - XML document
     * @return - the updated auditing service bean or new bean
     */
    public static AuditingServiceBean auditingServiceFromXml( AuditingServiceBean bean, Document document )
    {
        if ( document == null )
            return null;

        return auditingServiceFromXml( bean,
             getServiceElement( document, AUDITING_SERVICE_TAG ) );

    }

    /**
     * Configure auditing service bean with the given XML content
     * @param bean - auditing service bean
     * @param item - XML element
     * @return - the updated auditing service bean or new bean
     */
    public static AuditingServiceBean auditingServiceFromXml( AuditingServiceBean bean, Element item )
    {
        getLogger().debug( "Enter auditingServiceFromXml Elemnt" );

        if ( item == null )
            return null;

        if ( bean == null )
            bean = new AuditingServiceBean();

        bean.setSamplingTime(Integer.parseInt(item.getAttribute("samplingTime")));
        bean.setMerchantJNDI(item.getAttribute("merchantJNDI"));
        bean.setMerchantSchema(item.getAttribute("merchantSchema"));
        Element mailServer = XMLUtils.getChildElement(item, "mpidb:mailServer");
        /**
         *[Gang Wu's Note: March 19, 2003] Use boolean value to parse the string
         */
        try {
            bean.setMailAuth( Boolean.valueOf(
                    mailServer.getAttribute("auth") ).booleanValue());
        } catch ( Exception e )
        {
            getLogger().debug("set Mail Auth boolean value is invalid.", e);
        }
//        bean.setMailAuth("yes".equals(mailServer.getAttribute("auth")));
        bean.setMailUsername(mailServer.getAttribute("username"));
        bean.setMailPasswd(mailServer.getAttribute("passwd"));
        bean.setMailInet(mailServer.getAttribute("inet"));

        Map listeners = Collections.synchronizedMap( new TreeMap() );
        AuditingListenerBean apl;

        NodeList pnodes = item.getElementsByTagName( LOG_HANDLER_TAG );

        int i;
        for ( i = 0; pnodes != null && i < pnodes.getLength(); i++ )
        {
            item = (Element) pnodes.item(i);
            apl = new AuditingListenerBean();
            auditingListenerFromXml( apl, item );
            listeners.put( apl.getId(), apl );
        }

        bean.setListeners( listeners );

        getLogger().debug( "Exit auditingServiceFromXml Element with " + i + " loggers.");

        return bean;
    }

    /**
     * Xml string for merchants list definition
     *
     * @param merchants - merchants list
     * @param indent - number of indent
     * @return - Xml string for merchants definition
     */
    public static String merchants2Xml( List merchants, String indent )
    {
//        getLogger().debug( "Enter merchant2Xml" );

        if ( merchants == null || merchants.size() == 0 )
            return null;

        String id;
        StringBuffer sb = new StringBuffer();
        for (Iterator lt = merchants.iterator(); lt.hasNext(); )
        {
            id = (String) lt.next();
            sb.append( indent + "<" + MERCHANT_TAG + ">");
            sb.append(id);
            sb.append("</"+ MERCHANT_TAG +">\r\n");
        }

//        getLogger().debug( "Exit merchant2Xml" );

        return sb.toString();
    }

    /**
     * Export Xml string for given messages
     * @param msgs - messages defined in plugin or auditing service
     * @param indent - number of indent
     * @return - xml configuration string
     */
    public static String message2Xml( Map msgs, String indent )
    {
//        getLogger().debug( "Enter message2Xml" );

        if ( msgs == null || msgs.size() == 0 )
            return null;

        StringBuffer sb = new StringBuffer();
        String type, version;
        ListeningMessageBean lmsg;
        MessageFieldBean mf;
        List fields;

        for ( Iterator lt = msgs.values().iterator(); lt.hasNext(); )
        {
            sb.append(indent + "<" + MSG_TAG );
            lmsg = (ListeningMessageBean) lt.next();
            type = lmsg.getType();
            if ( type != null && !type.trim().equals("") )
            {
                sb.append(" type=\"");
                sb.append( type );
                sb.append("\"");
            }
            version = lmsg.getVersion();
            if ( version != null && !version.trim().equals("") )
            {
                sb.append(" version=\"");
                sb.append( version );
                sb.append("\"");
            }
            sb.append(">\r\n");
            fields = lmsg.getFields();

            if ( fields != null && fields.size() > 0 )
            {
                for ( Iterator lt2 = fields.iterator(); lt2.hasNext(); )
                {
                    mf = (MessageFieldBean)lt2.next();
                    if (mf.isSelected()) {
                        sb.append( field2Xml(mf, indent + "\t") );
                    }

                }
            }

            sb.append( indent + "</" + MSG_TAG + ">\r\n");

        }

//        getLogger().debug( "Exit message2Xml" );

        return sb.toString();
    }

    /**
     * Export Xml string for given message field
     * @param field - a message field
     * @param indent - number of indent
     * @return - xml configuration string
     */
    public static String field2Xml( MessageFieldBean field, String indent )
    {
//        getLogger().debug( "Enter field2Xml" );

        if ( !field.isSelected() )
        {
            getLogger().debug( "Not selected field [" +  field.getName() + "]" );
            return "";
        }

        StringBuffer sb = new StringBuffer();
        sb.append( indent + "<" + PLUGIN_FIELD_TAG );
        if ( field.isEncryption() )
            sb.append( " encryption=\"true\"");

        String fmt = field.getFormat();
        if ( fmt != null && !fmt.trim().equals("") )
            sb.append(" fmt=\"" + fmt + "\"");

        sb.append(">");
        sb.append( field.getName() );
        sb.append("</" + PLUGIN_FIELD_TAG + ">\r\n");

//        getLogger().debug( "Exit field2Xml" );

        return sb.toString();
    }

    /**
     * Generate a message mapping key from type and version
     * @param type - messages type
     * @param version - message version
     * @return - combined message key
     */
    public static String getMessageMappingKey( String type, String version )
    {
        if ( type == null )
            return "";

        if ( version == null )
            version = "";

        return type.trim() + MESSAGE_KEY_DELIMITER + version.trim();
    }

    /**
     * Add a message type from listening message bean to the messages map
     * @param messages - messages map
     * @param msg - a listening message bean
     */
    public static void addMessage(  Map messages, ListeningMessageBean msg )
    {
        if ( msg == null )
            return;

        String type = msg.getType();
        String version = msg.getVersion();

        String key = getMessageMappingKey( type, version );
        if ( !messages.containsKey( key ) )
            messages.put( key, msg );
    }

    /**
     * Retrive a given message type and version from messages map
     * @param messages - messages map
     * @param type - message type
     * @param version - message version
     * @return - Listening message bean
     */
    public static  ListeningMessageBean retriveMessage( Map messages, String type, String version )
    {
        return (ListeningMessageBean)messages.get( getMessageMappingKey( type, version ) );
    }

    /**
     * Remove a message type defined by listening message bean from messages map
     * @param messages - messages map
     * @param msg - a listening message bean
     */
    public static void removeMessage( Map messages, ListeningMessageBean msg )
    {
        if ( msg == null )
            return;

        String type = msg.getType();
        String version = msg.getVersion();

        String key = getMessageMappingKey( type, version );
        if ( messages.containsKey( key ) )
            messages.remove( key );
    }

    /**
     * Remove a message type from messages map
     * @param messages - messages map
     * @param type - the message type
     */
    public static void removeMessage( Map messages, String type )
    {
        String key;
        ArrayList rlist = new ArrayList();

        for ( Iterator lt = messages.keySet().iterator(); lt.hasNext(); )
        {
            key = (String)lt.next();
            if ( key.startsWith( type + MESSAGE_KEY_DELIMITER ) )
                rlist.add( key );
        }

        for ( Iterator lt = rlist.iterator(); lt.hasNext(); )
        {
            key = (String)lt.next();
            messages.remove(key);
        }

    }

    /**
     * Remove a given message type and version from messages map
     * @param messages - messages map
     * @param type - message type
     * @param version - message version
     */
    public static void removeMessage( Map messages, String type, String version )
    {
        if ( version == null )
            removeMessage( messages, type );

        String key = getMessageMappingKey( type, version );
        if ( messages.containsKey( key ) )
            messages.remove( key );
    }

    /**
     * Wrap data bridge header to the given XML string, any XML generate by bean
     * should call this method
     * @param xml - XML string
     * @return - a ready XML string for parser
     */
    public static String  wrapHeader( String xml )
    {
         if ( xml == null )
             return "";
         if ( xml.indexOf("<mpidb:databridge xmlns:mpidb=") == -1 )
             return "<mpidb:databridge xmlns:mpidb=\"http://www.oncecorp.com/ONCEmpi/databridge-configuration\">\r\n"
                 + xml
                 + "</mpidb:databridge>\r\n";
         else
             return xml;

    }

    /*
    public static String getChildElementAttribute(Element element, String elementName, String attributeName) {
      Element child = XMLUtils.getChildElement(element, elementName);
      if ( child == null ) {
        return null;
      }
      return child.getAttribute(attributeName);
    }*/

  /**
   * This method returns the bean property getter methods.
   * @return The getter methods array.
   */
   /*
  public static Method[] getBeanGetterMethods(Class clazz) {
    Method[] methods = clazz.getDeclaredMethods();
    ArrayList list = new ArrayList();
    for (int i = 0; i < methods.length; i++) {
      String name = methods[i].getName();
      if ( name.startsWith("get") ) {
        String type = methods[i].getReturnType().getName();
        if ( type.equals("boolean") ||
             type.equals("byte") ||
             type.equals("char") ||
             type.equals("int") ||
             type.equals("long") ||
             type.equals("float") ||
             type.equals("double") ||
             type.equals("java.lang.String")
        ) {
          list.add(methods[i]);
        }
      }
    }
    Method[] getters = new Method[list.size()];
    list.toArray(getters);
    return getters;
  }*/

 /**
  * Set logger handler for this helper class
  * @param logger - Log4J handler
  */
  public static void setLogger( Logger logger )
  {
      m_logger = logger;
  }

  /**
   *
   * @return - return the set logger handler or create a new one with default properties
   */
  public static Logger getLogger()
  {
      if ( m_logger == null )
          m_logger = Logger.getLogger( BeansHelper.class );

      return m_logger;
  }

  /**
   * Extract messageMappingBean from the given xml string
   * @param bean - messageMappingBean
   * @param xml - xml definition string
   * @return - the populated messageMappingBean
   */
  public static MessageMappingBean messageMappingFromXml(
          MessageMappingBean bean, String xml )
  {
      getLogger().debug( "Enter messageMappingFromXml string" );

      Document document = XMLUtils.getDocumentFromString( xml );
      bean = messageMappingFromXml( bean, document );
      getLogger().debug( "Exit messageMappingFromXml string" );

      return bean;
  }

  /**
   * Extract messageMappingBean from the given DOM Document
   * @param bean - messageMappingBean
   * @param xml - xml definition string
   * @return - the populated messageMappingBean
   */
  public static MessageMappingBean messageMappingFromXml(
          MessageMappingBean bean, Document document )
  {
      if ( bean == null )
          bean = new MessageMappingBean();

      getLogger().debug( "Enter messageMappingFromXml document" );

      if ( document != null )
      {
          NodeList pitem = document.getElementsByTagName( "messageDefiniton" );
          if ( pitem == null || pitem.getLength() != 1 )
          {
              getLogger().warn("In messageMappingFromXml 0 or more than 1 root element is defined.");
          }
          else
          {
              NodeList messages = ((Element)pitem.item(0)).getElementsByTagName("message");
              MessageDefinitionBean mdb;
              List mdbList, fdbList;
              FieldDefinitionBean fdb;
              String version, type, xpath, str;
              Element item, field;
              boolean mandatory = false, mustEncryption = false;
              StringTokenizer st;

              for ( int i = 0; messages != null && i < messages.getLength(); i++ )
              {
                  item = (Element)messages.item(i);
                  type = item.getAttribute("type");
                  version = item.getAttribute("version");
                  xpath = item.getAttribute("xpath");
                  mdbList = new ArrayList();
                  st = new StringTokenizer( version, ",", false );

                  mdb = null;
                  while ( st.hasMoreTokens() )
                  {
                      mdb = new MessageDefinitionBean( type.trim(), st.nextToken().trim(), xpath );
                      mdbList.add( mdb );
                  }

                  if ( mdb == null )
                  {
                      mdb = new MessageDefinitionBean( type.trim(), version.trim(), xpath );
                      mdbList.add( mdb );
                  }

                  pitem = item.getElementsByTagName("field");

                  for ( int j = 0; pitem != null && j < pitem.getLength(); j++)
                  {
                      field = (Element) pitem.item(j);
                      xpath = XMLUtils.getText( field );
                      str = (String)field.getAttribute("mandatory");
                      if ( str != null && !str.trim().equals("") )
                      {
                          try {
                              mandatory = Boolean.valueOf( str ).booleanValue();
                          } catch ( Exception e )
                          {
                              mandatory = false;
                          }
                      }
                      str = (String)field.getAttribute("mustEncryption");
                      if ( str != null && !str.trim().equals("") )
                      {
                          try {
                              mustEncryption = Boolean.valueOf( str ).booleanValue();
                          } catch ( Exception e )
                          {
                              mustEncryption = false;
                          }
                      }
                      fdb = new FieldDefinitionBean( xpath, mandatory, mustEncryption );
                      mdb.addField( fdb );
                  }

                  fdbList = mdb.getFields();
                  for ( Iterator lt = mdbList.iterator(); lt.hasNext(); )
                  {
                      mdb = (MessageDefinitionBean) lt.next();
                      mdb.setFields( fdbList );
                      bean.addMessageItem( mdb );
                  }
              }
          }
      }

      getLogger().debug( "Exit messageMappingFromXml document" );


      return bean;
  }

  /**
   * Set the message definition mapping table
   * @param messageMap - message definition mapping
   */
  public static void setMessageDefinitionMap( Map messageMap )
  {
      m_messageDefinition = messageMap;
  }

  /**
   * Clone the merchant ID list
   * @param merchantIDs - the original merchant id list
   * @return - cloned merchant id list
   */
  public static List cloneMerchantList( List merchantIDs )
  {
      if ( merchantIDs == null )
          return null;

      List lst = Collections.synchronizedList( new ArrayList() );
      String id;

      for ( Iterator lt = merchantIDs.iterator(); lt.hasNext(); )
      {
          id = (String) lt.next();
          lst.add( new String ( id ) );
      }

      return lst;
  }

  /**
   * Clone the given messages mapping
   * @param msgs - a given messages mapping
   * @return - cloned messages mapping
   */
  public static Map cloneListeningMessageMap( Map msgs )
  {
      if ( msgs == null )
          return null;

      Map messages = Collections.synchronizedMap( new TreeMap() );

      Object key;
      ListeningMessageBean lmb;

      for ( Iterator lt = msgs.keySet().iterator(); lt.hasNext(); )
      {
          key = lt.next();
          lmb = (ListeningMessageBean) msgs.get( key );
          messages.put( key, lmb.clone() );
      }

      return messages;
  }

  /**
   * Extract type part from type and version binder key
   * @param key - join key
   * @return - type part of the key
   */
  public static String extractTypeFromKey( String key )
  {
      if ( key == null || key.indexOf( MESSAGE_KEY_DELIMITER ) == -1 )
          return key;

      return key.substring(0, key.indexOf( MESSAGE_KEY_DELIMITER ) );
  }

  /**
   * Extract version part from type and version binder key
   * @param key - join key
   * @return - version part of the key
   */
  public static String extractVersionFromKey( String key )
  {
      if ( key == null || key.indexOf( MESSAGE_KEY_DELIMITER ) == -1 )
          return "";

      return key.substring(key.indexOf( MESSAGE_KEY_DELIMITER )
                           + MESSAGE_KEY_DELIMITER.length() );
  }

}