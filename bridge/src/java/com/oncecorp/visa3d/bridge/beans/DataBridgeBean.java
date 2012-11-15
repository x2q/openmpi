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

import com.oncecorp.visa3d.bridge.utility.ConfigureConstants;

/**
 * <p>Title: DataBridgeBean</p>
 * <p>Description: Hold the Data Bridge related configuration data </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation </p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */


public class DataBridgeBean implements Serializable, ConfigureConstants
{

    private boolean autoSave = true;

    /**
     * This for internal use only.
     */
    private boolean attributeSet = true;

    private ListeningServiceBean listeningService = null;
    private AuditingServiceBean  auditingService = null;
    private MonitoringServiceBean monitoringService = null;
    private PluginServiceBean     pluginService = null;

    /**
     * Default constructor
     */
    public DataBridgeBean()
    {
    }

    /**
     * Get auto save flag
     * @return
     */
    public boolean isAutoSave()
    {
        return autoSave;
    }

    /**
     * Set auto save value
     * @param flag - auto save flag
     */
    public void setAutoSave( boolean flag )
    {
        autoSave = flag;
    }

    /**
     * Configure from XML string
     * @param xml - Configuration XML string
     */
    public void fromXml( String xml )
    {
        BeansHelper.databridgeFromXml( this, xml );
    }

    /**
     * Generate XML string
     * @return - XML definition
     */
    public String toXml()
    {
        String sauto = "false";
        if ( autoSave )
            sauto = "true";

        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?>\r\n");
        sb.append("<!DOCTYPE " + DATA_BRIDGE_TAG +" >\r\n");
        sb.append("<" + DATA_BRIDGE_TAG +" xmlns:mpidb='http://www.oncecorp.com/ONCEmpi/databridge-configuration' "
                  + "autoSave=\"" + sauto + "\">\r\n");

        if ( listeningService != null )
        {
            sb.append( listeningService.toXml() );
        }
        else
        {
            sb.append( "<" + LISTENING_SERVICE_TAG + " />\r\n\r\n");
        }

        if ( auditingService != null )
        {
            sb.append( auditingService.toXml() );
        }
        else
        {
            sb.append( "<" + AUDITING_SERVICE_TAG + " />\r\n\r\n");
        }

        if ( monitoringService != null )
        {
            sb.append( monitoringService.toXml() );
        }
        else
        {
            sb.append( "<" + MONITORING_SERVICE_TAG + " />\r\n\r\n");
        }

        if ( pluginService != null )
        {
            sb.append( pluginService.toXml() );
        }
        else
        {
            sb.append( "<" + PLUGIN_SERVICE_TAG + " />\r\n\r\n");
        }

        sb.append("</" + DATA_BRIDGE_TAG + ">\r\n");

        return sb.toString();
    }

    /**
    *
    * @return the ListeningServiceBean value of listeningService.
    */
    public ListeningServiceBean getListeningService(){
        return listeningService;
    }

    /**
    *
    * @param aListeningService - the new value for listeningService
    */
    public void setListeningService(ListeningServiceBean aListeningService){
        listeningService = aListeningService;
    }


    /**
    *
    * @return the AuditingServiceBean value of auditingService.
    */
    public AuditingServiceBean getAuditingService(){
        return auditingService;
    }

    /**
    *
    * @param aAuditingService - the new value for auditingService
    */
    public void setAuditingService(AuditingServiceBean aAuditingService){
        auditingService = aAuditingService;
    }


    /**
    *
    * @return the MonitoringServiceBean value of monitoringService.
    */
    public MonitoringServiceBean getMonitoringService(){
        return monitoringService;
    }

    /**
    *
    * @param aMonitoringService - the new value for monitoringService
    */
    public void setMonitoringService(MonitoringServiceBean aMonitoringService){
        monitoringService = aMonitoringService;
    }


    /**
    *
    * @return the PluginServiceBean value of pluginService.
    */
    public PluginServiceBean getPluginService(){
        return pluginService;
    }

    /**
    *
    * @param aPluginService - the new value for pluginService
    */
    public void setPluginService(PluginServiceBean aPluginService){
        pluginService = aPluginService;
    }

    /**
    *
    * @return true if attributeSet is set to true.
    */
    public boolean isAttributeSet(){
        return attributeSet;
    }

    /**
    *
    * @param aAttributeSet - the new value for attributeSet
    */
    public void setAttributeSet(boolean aAttributeSet){
        attributeSet = aAttributeSet;
    }


}