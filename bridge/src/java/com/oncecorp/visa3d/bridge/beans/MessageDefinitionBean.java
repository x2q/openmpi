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
import java.util.Collections;
import java.util.ArrayList;
import java.util.Iterator;

import java.io.Serializable;

/**
 * <p>Title: MessageDefinitionBean</p>
 * <p>Description: Hold the message definition issues: type, version and xpath</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */

public class MessageDefinitionBean implements Serializable
{

    private String type = null;
    private String version = null;
    private String xpath = null;

    private List   fields = Collections.synchronizedList( new ArrayList() );

    private List   m_mandatoryList = null;

    private List   m_mustEncryptionList = null;

    /**
     * Default constructor
     */
    public MessageDefinitionBean()
    {
    }

    /**
     * Construct a ListeningMessage with the type and version
     * @param type - message type
     * @param version - message version
     * @param xpath - message xpath
     */
    public MessageDefinitionBean( String type,  String version, String xpath )
    {
        setType( type );
        setVersion( version );
        setXpath( xpath );
    }

    /**
    *
    * @return the String value of type.
    */
    public String getType(){
        return type;
    }

    /**
    *
    * @param aType - the new value for type
    */
    public void setType(String aType){
        type = aType;
    }


    /**
    *
    * @return the String value of version.
    */
    public String getVersion(){
        return version;
    }

    /**
    *
    * @param aVersion - the new value for version
    */
    public void setVersion(String aVersion){
        version = aVersion;
    }

    /**
    *
    * @return the List value of fields.
    */
    public List getFields(){
        return fields;
    }

    /**
    *
    * @param aFields - the new value for fields
    */
    public void setFields(List aFields){
        fields = aFields;
    }

    /**
     * Add one message field
     * @param field - the message field
     */
    public void addField( FieldDefinitionBean field )
    {
        if ( fields.contains( field ) )
            return;
        else
            fields.add( field );
    }

    /**
     * Remove a given message field
     * @param field - the message field
     */
    public void removeField( FieldDefinitionBean field )
    {
        if ( fields.contains( field ) )
            fields.remove( field );
    }

    /**
    *
    * @return the String value of xpath.
    */
    public String getXpath(){
        return xpath;
    }

    /**
    *
    * @param aXpath - the new value for xpath
    */
    public void setXpath(String aXpath){
        xpath = aXpath;
    }

    /**
     *
     * @return - mandatory field list
     */
    public List getMandatoryList()
    {
        if ( m_mandatoryList != null )
            return m_mandatoryList;
        else
        {
            m_mandatoryList = new ArrayList();
            FieldDefinitionBean fdb;
            for ( Iterator lt = fields.iterator(); lt.hasNext(); )
            {
                fdb = (FieldDefinitionBean) lt.next();
                if ( fdb.isMandatory() )
                    m_mandatoryList.add( fdb.getXpath() );
            }

            return m_mandatoryList;
        }
    }

    /**
     *
     * @return - must encryption field list
     */
    public List getMustEncryptionList()
    {
        if ( m_mustEncryptionList != null )
            return m_mustEncryptionList;
        else
        {
            m_mustEncryptionList = new ArrayList();
            FieldDefinitionBean fdb;
            for ( Iterator lt = fields.iterator(); lt.hasNext(); )
            {
                fdb = (FieldDefinitionBean) lt.next();
                if ( fdb.isMustEncryption() )
                    m_mustEncryptionList.add( fdb.getXpath() );
            }

            return m_mustEncryptionList;
        }

    }

    /**
     * Extract a field definition bean from the fields list
     * @param xpath - the given field xpath
     * @return - found field definition bean.
     */
    public FieldDefinitionBean getFieldFromXpath( String xpath )
    {
        if ( fields == null || fields.size() < 1 || xpath == null )
            return null;

        FieldDefinitionBean fdb;
        for ( Iterator lt = fields.iterator(); lt.hasNext(); )
        {
            fdb = (FieldDefinitionBean) lt.next();
            if ( fdb.getXpath().equals( xpath.trim() ) )
                return fdb;
        }

        return null;
    }

}