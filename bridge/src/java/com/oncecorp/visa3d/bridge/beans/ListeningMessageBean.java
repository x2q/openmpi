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
 * <p>Title: ListeningMessageBean</p>
 * <p>Description: Hold the listening message issues</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */

public class ListeningMessageBean implements  Cloneable, Serializable
{

    private String type = null;
    private String version = null;

    private List   fields = Collections.synchronizedList( new ArrayList() );

    /**
     * Default constructor
     */
    public ListeningMessageBean()
    {
    }

    /**
     * Construct a ListeningMessage with the type and version
     * @param type - message type
     * @param version - message version
     */
    public ListeningMessageBean( String type,  String version )
    {
        setType( type );
        setVersion( version );
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
    public void addField( MessageFieldBean field )
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
    public void removeField( MessageFieldBean field )
    {
        if ( fields.contains( field ) )
            fields.remove( field );
    }

    /**
     * Deep clone this bean
     * @return - the cloned listening message bean
     */
    public Object clone()
    {
        ListeningMessageBean lmb = new ListeningMessageBean(
                this.type, this.version );

        List lst = Collections.synchronizedList( new ArrayList() );

        MessageFieldBean mfb;

        for ( Iterator lt = fields.iterator(); lt.hasNext(); )
        {
            mfb = (MessageFieldBean) lt.next();
            lst.add( mfb.clone() );
        }

        lmb.setFields( lst );
        return lmb;
    }

}