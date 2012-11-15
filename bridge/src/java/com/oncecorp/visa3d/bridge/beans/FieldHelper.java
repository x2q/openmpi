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

import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

/**
 * <p>Title: FieldHelper</p>
 * <p>Description: Provide helper function to transform MessageFieldBean to GenericFieldBean
 * and GetAllGenericFields for a given message type and version </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */


public class FieldHelper
{

    /**
     * Default constructor
     */
    public FieldHelper()
    {
    }

    /**
     * Get a generic message fields mapping for a given message type and version
     * @param bean - MessageMappingBean which holds all message definition
     * @param type - message type
     * @param version - message version
     * @return - a GenericFieldBean mapping for a given message type and version
     */
    public static HashMap getAllGenericFields( MessageMappingBean bean,
            String type, String version )
    {
        String key = BeansHelper.getMessageMappingKey( type, version );

        if ( bean == null )
            return null;

        List list = bean.getFieldsList( type, version );
        if ( list == null )
            return null;

        FieldDefinitionBean fdb;
        GenericFieldBean gfb;
        HashMap rmap = new HashMap();
        for ( Iterator lt = list.iterator(); lt.hasNext(); )
        {
            fdb = (FieldDefinitionBean) lt.next();
            gfb = new GenericFieldBean();
            gfb.setName( fdb.getXpath() );
            gfb.setMandatory( fdb.isMandatory() );
            gfb.setMustEncryption( fdb.isMustEncryption() );
            rmap.put( gfb.getName(), gfb );
        }

        return rmap;

    }

    /**
     * Transform a given MessageFieldBean list to a GenericFieldBean List
     * @param bean - MessageMappingBean which holds all message definition
     * @param type - message type
     * @param version - message version
     * @param fields - a given MessageFieldBean list
     * @return - a GenericFieldBean mapping from given MessageFieldBean list
     */
    public static HashMap transformToGeneric( MessageMappingBean bean,
            String type, String version , List fields )
    {
        if ( fields == null || fields.size() < 1 )
            return null;
        HashMap gfields = getAllGenericFields( bean, type, version );

        MessageFieldBean mfb;
        GenericFieldBean gfb;

        /**
         * Change to false for all as we use the value from the input list
         */
        for ( Iterator lt = gfields.values().iterator(); lt.hasNext(); )
        {
            gfb = (GenericFieldBean) lt.next();
            gfb.setSelected( false );
        }


        for ( Iterator lt = fields.iterator(); lt.hasNext(); )
        {
            mfb = (MessageFieldBean) lt.next();
            gfb = (GenericFieldBean)gfields.get( mfb.getName() );
            if ( gfb != null ) {
                gfb.setSelected(mfb.isSelected());
                gfb.setEncryption(mfb.isEncryption());
                gfb.setFormat(mfb.getFormat());
            }
        }

        return gfields;
    }

}