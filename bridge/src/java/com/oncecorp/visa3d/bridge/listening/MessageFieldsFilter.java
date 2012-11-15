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

import java.io.File;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.text.Format;
import java.text.SimpleDateFormat;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import org.apache.log4j.Logger;
import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;
import com.oncecorp.visa3d.bridge.beans.MessageFieldBean;
import com.oncecorp.visa3d.bridge.utility.XMLUtils;
import com.oncecorp.visa3d.bridge.security.TripleDESEncrypter;
import com.oncecorp.visa3d.bridge.beans.MessageDefinitionBean;
import com.oncecorp.visa3d.bridge.beans.FieldDefinitionBean;
import com.oncecorp.visa3d.bridge.beans.MessageMappingBean;
import com.oncecorp.visa3d.bridge.beans.BeansHelper;
import com.oncecorp.visa3d.bridge.configure.FileHandler;

/**
 * <p>Title: MessageFieldsFilter</p>
 * <p>Description: Provide fields related filter functionalities to a given
 * XML message document</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */


public class MessageFieldsFilter
{

    private static Logger m_logger = DataBridgeLoger.getLogger(
            MessageFieldsFilter.class.getName() );

    private static String DEFINITION_FILE_NAME = "mpi-messages.xml";
    private static MessageMappingBean m_messages = null;

    private static SimpleDateFormat m_sdf = new SimpleDateFormat("yyyyMMdd hh:mm:ss");

    private static Map m_parsedMasks = Collections.synchronizedMap( new HashMap() );

    /**
     * Default constructor
     */
    public MessageFieldsFilter()
    {

    }

    /**
     * Initialize message path table and mandatory message fields table
     */
    public static void initMessageTable( String filePath )
    {
        try {
            m_logger.debug("Load from property file");
            String fname;
            if ( filePath != null )
                fname = filePath + File.separator + DEFINITION_FILE_NAME;
            else
                fname = DEFINITION_FILE_NAME;
            Document doc = new FileHandler().load( fname );
            MessageMappingBean bean = BeansHelper.messageMappingFromXml( null, doc );
            if ( bean != null )
                m_messages = bean;
            else
                m_logger.warn("Can't extract message definition.");

            m_logger.debug("Finish loading from property file");
        } catch ( Exception e )
        {
            m_logger.warn("Loading property file exception", e);
        }

    }

    /**
     * Extract not defined fields from the message document
     * @param type - message type
     * @param version - message version
     * @param items - message field items
     * @param doc - message document
     */
    public static void extractFields( String type, String version, List items, Document doc )
    {
       if ( doc == null || m_messages == null )
       {
           m_logger.warn("Not message document get or no message mapping definition");
           return;
       }
       m_logger.debug("Enter extractFields.");

       if ( items != null && items.size() > 0 )
       {
           MessageDefinitionBean mdb = m_messages.getMessageDefinition( type, version );
           if ( mdb == null )
           {
               m_logger.debug("No definition for the given message type ["
                              + type + "], version [" + version + "]" );
               return;
           }

           String snode = mdb.getXpath();
           if ( snode == null || snode.trim().equals("") )
           {
               m_logger.warn("Message type [" + type  + "], version [" + version + "] not define messages path ");
               return;
           }

           Element root = XMLUtils.findXPathElement(
                   doc.getDocumentElement(), snode );
           if ( root == null )
           {
               m_logger.warn("Message type [" + type + "][" + snode + "] element not found in the message document");
               return;
           }

           List mlist = mdb.getMandatoryList();
           ArrayList nlist;
           if ( mlist == null || mlist.size() < 1 )
           {
               m_logger.debug("Not mandatory field define for Message type ["
                              + type  + "], version [" + version + "]");
               nlist = new ArrayList();
           }
           else
               nlist = new ArrayList( mlist );

           MessageFieldBean bean;
           for ( Iterator lt = items.iterator(); lt.hasNext(); )
           {
               bean = (MessageFieldBean) lt.next();
               nlist.add( bean.getName() );
           }
           checkNode( "", root, nlist );
       }

       m_logger.debug("Exit extractFields.");
    }

    /**
     * Check the given root's child whether is the ancestor of the given items or
     * is the given items itself
     * @param sroot - root element's xpath from the message type
     * @param root - checked element parent
     * @param items - item list
     */
    private static void checkNode( String sroot, Element root, List items )
    {
        NodeList nlist = root.getChildNodes();
        Node node;
        Element element;
        ArrayList dlist = new ArrayList();
        String snode, spath;
        boolean flag;

        for ( int i = 0; i < nlist.getLength(); i++ )
        {
            node = nlist.item(i);
            if ( node instanceof Element )
            {
                element = (Element) node;
                spath = sroot + "/" + element.getNodeName();
                flag = false;
                for ( Iterator lt = items.iterator(); !flag && lt.hasNext(); )
                {
                    snode = (String)lt.next();
                    if ( snode.charAt(0) != '/' )
                        snode = '/' + snode ;
                    if ( snode.startsWith( spath ) )
                        flag = true;
                }
                if ( !flag )
                    dlist.add( node );
                else
                    checkNode( spath, element, items );
            }
        }

        for ( Iterator lt = dlist.iterator(); lt.hasNext(); )
        {
            node = (Node) lt.next();
            root.removeChild( node );
        }
    }

    /**
     * Encrypt message fileds defined by configuration data
     * @param type - message type
     * @param version - message version
     * @param items - message field items
     * @param doc - XML document
     */
    public static void encryptFields( String type, String version, List items, Document doc )
    {
        if ( doc == null || m_messages == null )
        {
            m_logger.warn("Not message document get or no message mapping definition");
            return ;
        }

        m_logger.debug("Enter encryptFields.");

        if ( items != null && items.size() > 0 )
        {
            MessageDefinitionBean mdb = m_messages.getMessageDefinition( type, version );
            String snode = "";
            if ( mdb == null )
            {
                m_logger.debug("No definition for the given message type ["
                               + type + "], version [" + version + "]" );
            }
            else
                snode = mdb.getXpath();
            if ( snode == null || snode.trim().equals("") )
            {
                m_logger.warn("Message type [" + type + "] not defined in messages path table");
                return ;
            }

            Element root = XMLUtils.findXPathElement(
                    doc.getDocumentElement(), snode );
            if ( root == null )
            {
                m_logger.warn("Message type [" + type + "][" + snode + "] element not found in the message document");
                return ;
            }

            MessageFieldBean bean;
            String value, nvalue;
            String [] eresult;
            Element node;
            String name;
            for ( Iterator lt = items.iterator(); lt.hasNext(); )
            {
                bean = (MessageFieldBean) lt.next();
                if ( bean.isEncryption()
                     && ( bean.getFormat() == null
                     || bean.getFormat().equals("")) )
                {
                    m_logger.debug("Encrypt the field:" +  bean.getName() );
                    name = bean.getName();
                    node = XMLUtils.findXPathElement( root, name );
                    if ( node != null )
                    {
                        value = XMLUtils.getText( node );
                        try {
                            eresult = TripleDESEncrypter.getInstance().encrypt( value );
                            nvalue = eresult[0]
                                   +  TripleDESEncrypter.CIPHER_TEXT_IV_DELIMITER
                                   + eresult[1];
                            XMLUtils.setNodeText( node, nvalue );
                        } catch ( Exception e )
                        {
                            m_logger.warn("Encryption field [" + name + "] failed",
                                    e);
                        }
                    }
                }
            }
        }

        m_logger.debug("Exit encryptFields.");

        return ;
    }

    /**
     * Mask message fileds defined by configuration data.
     * This support the following rules:
     * <ul>
     * <li> Standard Java message format which is enclosed by { and };
     * <li> Mask format, which can be in the format X{1}#{3}XXX where # means
     * original letter, {3} means occurence number. {0} means any, it only can occurence once
     * in the format. If {0} occure before #, which means count from end, otherwise from front.
     * </ul>
     * @param type - message type
     * @param version - message version
     * @param items - message field items
     * @param doc - XML document
     */
    public static void maskFields( String type, String version, List items, Document doc )
    {
        if ( doc == null || m_messages == null )
        {
            m_logger.warn("Not message document get or no message mapping definition");
            return ;
        }
        m_logger.debug("Enter maskFields.");

        if ( items != null  && items.size() > 0 )
        {
            MessageDefinitionBean mdb = m_messages.getMessageDefinition( type, version );
            String snode = "";
            if ( mdb == null )
            {
                m_logger.debug("No definition for the given message type ["
                               + type + "], version [" + version + "]" );
            }
            else
                snode = mdb.getXpath();
            if ( snode == null || snode.trim().equals("") )
            {
                m_logger.warn("Message type [" + type + "] not defined in messages path table");
                return ;
            }

            Element root = XMLUtils.findXPathElement(
                    doc.getDocumentElement(), snode );
            if ( root == null )
            {
                m_logger.warn("Message type [" + type + "][" + snode + "] element not found in the message document");
                return ;
            }

            MessageFieldBean bean;
            String value, nvalue, fmt, name;
            Element node;
            for ( Iterator lt = items.iterator(); lt.hasNext(); )
            {
                bean = (MessageFieldBean) lt.next();
                fmt = bean.getFormat();
                if ( fmt != null && !fmt.trim().equals("") )
                {
                    m_logger.debug("do formatting:" +  bean.getName());
                    name = bean.getName();
                    node = XMLUtils.findXPathElement( root, name );
                    if ( node == null )
                        continue;

                    value = XMLUtils.getText( node );
                    nvalue = value;
                    fmt = fmt.trim();

                    MaskToken pmask = (MaskToken) m_parsedMasks.get( fmt );
                    if ( pmask == null && ( fmt.startsWith("{") && fmt.endsWith("}") ) )
                    {
                        pmask = parseMessageFormat( fmt );
                        if ( pmask != null )
                            m_parsedMasks.put( fmt, pmask );
                    }

                    MessageFormat mf = null;

                    if ( pmask != null && !pmask.isMaskFlag() )
                        mf = pmask.getJavaMessageFormat();

                    if ( mf != null )
                    {
                        try {
                            Object objs[] = new Object[1];
                            objs[0] = value;
                            switch ( pmask.getMessageObjectType() )
                            {
                                case MaskToken.NUMBER_FMT:
                                    objs[0] = new Double( value );
                                    break;
                                case MaskToken.DATE_FMT:
                                    objs[0] = m_sdf.parse( value );
                                    break;
                            }
                            nvalue = mf.format(  objs );
                        } catch ( Exception e )
                        {
                            e.printStackTrace();
                            m_logger.error("Format error with " + fmt, e);
                        }
                    }
                    else
                    {
                        if ( pmask == null )
                        {
                            pmask = parseMaskFormat( fmt );
                            m_parsedMasks.put( fmt , pmask );
                        }
                        StringBuffer sb = pmask.getParsedArray();
                        int wildPlace = pmask.getWildCharPlace();

                        char lastcc = 0;
                        char cc;
                        char nstr[] = new char[ value.length() ];

                        if ( wildPlace != -1 )
                        {
                            cc = sb.charAt( wildPlace );
                            m_logger.debug("wildchar is " + cc + " place=" +  wildPlace );
                            for ( int i = 0; i < value.length(); i++ )
                                nstr[i] = cc;

                            for ( int i = 0; i < wildPlace && i < value.length(); i++ )
                            {
                                lastcc = sb.charAt( i );
                                if ( lastcc != '#' )
                                    cc = lastcc;
                                else
                                    cc = value.charAt(i);
                                nstr[i] = cc;
                            }

                            for ( int i = sb.length() - 1, k = value.length() - 1;
                                  k > wildPlace && i > wildPlace; i--, k-- )
                            {
                                lastcc = sb.charAt( i );
                                if ( lastcc != '#' )
                                    cc = lastcc;
                                else
                                    cc = value.charAt(k);
                                nstr[k] = cc;
                            }
                        }
                        else
                        {
                            for ( int i = 0; i < value.length(); i++ )
                            {
                                if ( sb.length() <= i )
                                    cc = value.charAt(i);
                                else
                                {
                                    lastcc = sb.charAt( i );
                                    if ( lastcc != '#' )
                                        cc = lastcc;
                                    else
                                        cc = value.charAt(i);
                                }
                                nstr[i] = cc;
                            }
                        }

                        nvalue = new String( nstr );
                    }

                    m_logger.debug("field value from [" +  value + "]  to [" + nvalue + "]" );
                    XMLUtils.setNodeText( node, nvalue );
                }
            }
        }

        m_logger.debug("Exit maskFields.");

        return ;
    }

    /**
     * Encrypt must encrypt fields such as PAN which is not encrypted or masked before
     * by custom defined fields'methods.
     * @param items - fields that have been defined
     * @param doc - XML document
     * @param type - message type
     * @param version - message version
     */
    public static void doMustEncrypt( List items, Document doc,
                                      String type, String version )
    {
        m_logger.debug( "Enter doMustEncrypt.");

        if ( doc == null || m_messages == null )
        {
            m_logger.warn("Not message document get or no message mapping definition");
            return ;
        }

        MessageDefinitionBean mdb = m_messages.getMessageDefinition( type, version );
        String snode = "";
        if ( mdb == null )
        {
            m_logger.debug("No definition for the given message type ["
                           + type + "], version [" + version + "]" );
            return;
        }
        else
            snode = mdb.getXpath();
        if ( snode == null || snode.trim().equals("") )
        {
            m_logger.warn("Message type [" + type + "] not defined in messages path table");
            return ;
        }

        Element root = XMLUtils.findXPathElement(
                doc.getDocumentElement(), snode );

        if ( root == null )
        {
            m_logger.warn("Message type [" + type + "][" + snode + "] element not found in the message document");
            return ;
        }

        List mlist = mdb.getMustEncryptionList();
        if ( mlist == null )
        {
            m_logger.debug("No must encryption fields defined for the given message type ["
                           + type + "], version [" + version + "]" );
            return ;
        }

        String path, value,  nvalue;
        String[] eresult;
        Element node;

        MessageFieldBean bean;
        ArrayList fields = new ArrayList();
        if ( items != null )
        {
            for ( Iterator lt = items.iterator(); lt.hasNext(); )
            {
                bean = (MessageFieldBean) lt.next();
                if ( ( bean.getFormat() != null && !bean.getFormat().trim().equals("") )
                    || bean.isEncryption() )
                    fields.add( bean.getName() );
            }
        }

        for ( Iterator lt = mlist.iterator(); lt.hasNext(); )
        {
            path = (String) lt.next();
            if ( !fields.contains( path ) )
            {
                m_logger.debug("Do must encrypt:" +  path );
                node = XMLUtils.findXPathElement( doc.getDocumentElement(), path );
                if ( node != null )
                {
                    value = XMLUtils.getText( node );

                    try {
                        eresult = TripleDESEncrypter.getInstance().encrypt( value );
                        nvalue = eresult[0]
                               + TripleDESEncrypter.CIPHER_TEXT_IV_DELIMITER
                               + eresult[1];
                        XMLUtils.setNodeText( node, nvalue );
                    } catch ( Exception e )
                    {
                        m_logger.warn("Encryption field [" + path + "] failed",
                                e);
                    }
                }

            }
        }
        m_logger.debug( "Exit doMustEncrypt.");
    }


    /**
     * Parse message field format and construct Java message format instance
     * @param fmt - message field format definition
     * @return - parsed mask token
     */
    public static MaskToken parseMessageFormat( String fmt )
    {

        try {
           fmt = fmt.substring( 1, fmt.length() - 1);
           int sb = fmt.indexOf(",");
           String javaFmt;
           int type = MaskToken.STRING_FMT;
           if ( sb != -1 )
           {
               String ftype = fmt.substring(0, sb );
               String lstr = fmt.substring( sb + 1);
               if ( ftype.trim().equalsIgnoreCase("number") )
               {
                   javaFmt = "{0,number," + lstr + "}";
                   type = MaskToken.NUMBER_FMT;
               }
               else if ( ftype.trim().equalsIgnoreCase("date") )
               {
                   SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd hh:mm:ss");
                   javaFmt = "{0,date," + lstr  + "}";
                   type = MaskToken.DATE_FMT;
               }
               else
               {
                   javaFmt = lstr ;
               }
           }
           else
           {
               javaFmt = fmt ;
           }
           return new MaskToken( new MessageFormat( javaFmt ), type );
       } catch ( Exception e )
       {
           e.printStackTrace();
           m_logger.error("Format error with " + fmt, e);
       }

       return null;
   }

   /**
    * Parse message field format and construct mask string array
    * @param fmt - message field format definition
    * @return - parsed mask token
    */
   public static MaskToken parseMaskFormat( String fmt )
   {
       StringBuffer sb = new StringBuffer();
       char lastcc = 0;
       char cc;
       int ce;
       int num = 1;
       String snum;
       int wildPlace = -1;

       for ( int i = 0; i < fmt.length() ; i++ )
       {
           cc = fmt.charAt( i );
           if ( cc != '{' )
           {
               sb.append( cc );
               lastcc = cc;
           }
           else
           {
               ce = fmt.indexOf( '}', i );
               if ( ce != -1 )
               {
                   try {
                       snum = fmt.substring( i + 1, ce );
                       num = Integer.valueOf( snum ).intValue();
                       } catch ( Exception e )
                       {
                           m_logger.warn( "Invalid format [" + fmt + "]", e );
                           num = 1;
                       }
                       if ( num == 0 && wildPlace == -1 )
                       {
                           if ( i > 0 )
                               wildPlace = sb.length() - 1;
                           else
                               wildPlace = 0;
                       }
                       else
                       {
                           if ( num == 0 )
                           {
                               m_logger.warn("More than one {0} is defined in the format ["
                                       + fmt + "]" );
                               num = 1;
                           }
                           for ( int j = 1; j < num; j++ )
                               sb.append( lastcc );
                       }
                       i = ce;
               }
               else
               {
                   sb.append( cc );
                   lastcc = cc;
               }
           }
       }

       m_logger.debug("field mask string is [" + sb.toString() + "].");
       return new MaskToken( sb, wildPlace );

   }

    /**
     *
     * @return - message mapping bean
     */
    public static MessageMappingBean getMessageMappingBean()
    {
        return m_messages;
    }

    /**
     * Get the message type root element in the message XML DOM tree
     * @param doc - XML dom document
     * @param type - message type
     * @param version - message version
     * @return - root element
     */
    public static Element getMessageTypeRoot( Document doc,
            String type, String version )
    {

        if ( doc == null || m_messages == null )
        {
            m_logger.warn("Not message document get or no message mapping definition");
            return null;
        }

        MessageDefinitionBean mdb = m_messages.getMessageDefinition( type, version );
        String snode = "";
        if ( mdb == null )
        {
            m_logger.debug("No definition for the given message type ["
                           + type + "], version [" + version + "]" );
            return null;
        }
        else
            snode = mdb.getXpath();
        if ( snode == null || snode.trim().equals("") )
        {
            m_logger.warn("Message type [" + type + "] not defined in messages path table");
            return null;
        }

        Element root = XMLUtils.findXPathElement(
                doc.getDocumentElement(), snode );
        if ( root == null )
            m_logger.warn("Message type [" + type + "][" + snode + "] element not found in the message document");

        return root;
    }
}