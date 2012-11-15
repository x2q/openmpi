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

import java.io.Serializable;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

import com.oncecorp.visa3d.bridge.utility.ConfigureConstants;

/**
 * <p>Title: PluginChannelBean</p>
 * <p>Description: Holds plugin channel related issues </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */


public class PluginChannelBean implements Serializable, ConfigureConstants
{
    private String id = null;
    private String status = BeansHelper.DEFAULT_STATUS_RUNNING;
    private String customData = null;
    private boolean acceptFilter = true;
    private boolean acceptDynamicFilter = true;

    private List merchantIds = Collections.synchronizedList( new ArrayList() );

    /**
     * Default constructor
     */
    public PluginChannelBean()
    {
    }

    /**
     * Constructor with identify and status
     * @param id - channel identify
     * @param status - channel status
     */
    public PluginChannelBean( String id, String status )
    {
        setId( id );
        setStatus( status );
    }

    /**
    *
    * @return the String value of id.
    */
    public String getId(){
        return id;
    }

    /**
    *
    * @param aId - the new value for id
    */
    public void setId(String aId){
        id = aId;
    }


    /**
    *
    * @return the String value of status.
    */
    public String getStatus(){
        return status;
    }

    /**
    *
    * @param aStatus - the new value for status
    */
    public void setStatus(String aStatus){
        status = aStatus;
    }


    /**
    *
    * @return the String value of customData.
    */
    public String getCustomData(){
        return customData;
    }

    /**
    *
    * @param aCustomData - the new value for customData
    */
    public void setCustomData(String aCustomData){
        customData = aCustomData;
    }


    /**
    *
    * @return the List value of merchantIds.
    */
    public List getMerchantIds(){
        return merchantIds;
    }

    /**
    *
    * @param aMerchantIds - the new value for merchantIds
    */
    public void setMerchantIds(List aMerchantIds){
        merchantIds = aMerchantIds;
    }

    /**
     * Add a merchant to the listening list
     * @param id - the merchant identify
     */
    public void addMerchant( String id )
    {
        if ( !merchantIds.contains( id ) )
            merchantIds.add( id );
    }

    /**
     * Remove a mechant from the listening list
     * @param id - a merchant identify
     */
    public void removeMerchant( String id )
    {
        if ( merchantIds.contains( id ) )
            merchantIds.remove( id );
    }

    /**
     * Configure from XML string
     * @param xml - Configuration XML string
     */
    public void fromXml( String xml )
    {
        BeansHelper.pluginChannelFromXml( this, xml );
    }

    /**
    *
    * @return true if acceptFilter is set to true.
    */
    public boolean isAcceptFilter(){
        return acceptFilter;
    }

    /**
    *
    * @param aAcceptFilter - the new value for acceptFilter
    */
    public void setAcceptFilter(boolean aAcceptFilter){
        acceptFilter = aAcceptFilter;
    }


    /**
    *
    * @return true if acceptDynamicFilter is set to true.
    */
    public boolean isAcceptDynamicFilter(){
        return acceptDynamicFilter;
    }

    /**
    *
    * @param aAcceptDynamicFilter - the new value for acceptDynamicFilter
    */
    public void setAcceptDynamicFilter(boolean aAcceptDynamicFilter){
        acceptDynamicFilter = aAcceptDynamicFilter;
    }

    /**
     * Generate XML string
     * @return - XML definition
     */
    public String toXml()
    {
        String str = "start";
        if ( status != null && status.equals("stop") )
            str = "stop";
        String str0 = "\r\n\t\t\t<" + PLUGIN_CHANNEL_TAG   + " id=\""
                      + id +"\" status=\""
                      + str  + "\" acceptFilter=\""
                      + acceptFilter + "\" acceptDynamicFilter=\""
                      + acceptDynamicFilter + "\">\r\n";
        String str1 = BeansHelper.merchants2Xml( merchantIds, "\t\t\t\t" );
        if ( str1 != null )
            str0 += str1;
        if ( customData != null )
        {
            str0 += "\t\t\t\t<" + CUSTOM_DATA_TAG
                  + " ><![CDATA[" + customData +"]]></"
                  + CUSTOM_DATA_TAG + ">\r\n";
        }
        String str2 = "\t\t\t</" + PLUGIN_CHANNEL_TAG + ">\r\n";
        str0 += str2;

        return str0;
    }

}