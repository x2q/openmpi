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

package com.oncecorp.visa3d.bridge.configure;

import java.io.Serializable;
import com.oncecorp.visa3d.bridge.monitoring.ConfigurationMBean;
import com.oncecorp.visa3d.bridge.listening.MessageFieldsFilter;
import com.oncecorp.visa3d.bridge.beans.MessageMappingBean;

/**
 * <p>Title: Configuration </p>
 * <p>Description: Implement ConfigurationMBean related attributes and operations published to JMX server</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation </p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */


public class Configuration implements Serializable, ConfigurationMBean
{
    private String fileName = null;
    private boolean fromFile = true;
    private String dataSourceName = null;
    private boolean autoSave = false;
    private String tableName = null;

    public Configuration()
    {
    }

    /**
     * Load configuration from a given XML string
     * @param xml - configuration XML string
     */
    public void loadFromString( String xml )
    {
        ConfigurationManager.getInstance().loadFromString( xml );
    }

    /**
     * Load configuration from a given file
     * @param fname - file name
     */
    public void loadFromFile( String fname )
    {
        ConfigurationManager.getInstance().loadFromFile( fname );
    }

    /**
     * Default loading configuration file
     */
    public void reload()
    {
        ConfigurationManager.getInstance().reload();
    }

    /**
     * Save the configuration data
     */
    public void save()
    {
        ConfigurationManager.getInstance().save();
    }

    /**
     * Set configuration file name
     * @param name - configuration file name
     */
    public void setFileName( String name )
    {
        fileName = name;
        ConfigurationManager.getInstance().setFileName( name );
    }

    /**
     * Get configuration file name
     * @return - configuration file name
     */
    public String getFileName()
    {
        fileName = ConfigurationManager.getInstance().getFileName();
        return fileName;
    }

    /**
     * Get auto save flag
     * @return
     */
    public boolean isAutoSave()
    {
        autoSave = ConfigurationManager.getInstance().isAutoSave();
        return autoSave;
    }

    /**
     * Set auto save value
     * @param flag - auto save flag
     */
    public void setAutoSave( boolean flag )
    {
        autoSave = flag;
        ConfigurationManager.getInstance().setAutoSave( flag );
    }

    /**
     *
     * @return - configuration data from file or not
     */
    public boolean isFromFile()
    {
        fromFile = ConfigurationManager.getInstance().isFromFile();
        return fromFile;
    }

    /**
     * Set whether configuration data from file or not
     * @param flag - configuration from flag
     */
    public void setFromFile( boolean flag )
    {
        fromFile = flag;
        ConfigurationManager.getInstance().setFromFile( flag );
    }

    /**
     *
     * @return - the name of data source
     */
    public String getDataSourceName()
    {
        dataSourceName = ConfigurationManager.getInstance().getDataSourceName();
        return dataSourceName;
    }

    /**
     * Set the name of data source
     * @param name - data source name
     */
    public void setDataSourceName( String name )
    {
        dataSourceName = name;
        ConfigurationManager.getInstance().setDataSourceName( name );
    }

    /**
     *
     * @return the String value of tableName.
     */
    public String getTableName(){
        tableName = ConfigurationManager.getInstance().getTableName();
        return tableName;
    }

    /**
     *
     * @param aTableName - the new value for tableName
     */
    public void setTableName(String aTableName){
        tableName = aTableName;
        ConfigurationManager.getInstance().setTableName( tableName );
    }

    /**
     *
     * @return - Configuration XML string
     */
    public String retriveConfigurationXml()
    {
        return ConfigurationManager.getInstance().toXml();
    }

    /**
     *
     * @return - Messages definition XML string
     */
    public String retriveMessageMappingXml()
    {
        MessageMappingBean bean = MessageFieldsFilter.getMessageMappingBean();
        if ( bean == null )
            return "";
        else
            return bean.toXml();
    }

}
