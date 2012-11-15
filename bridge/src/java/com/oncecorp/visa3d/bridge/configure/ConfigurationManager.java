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

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.Map;

import org.w3c.dom.Document;

import com.oncecorp.visa3d.bridge.utility.XMLUtils;
import com.oncecorp.visa3d.bridge.listening.ListeningManager;
import com.oncecorp.visa3d.bridge.listening.TimerService;
import com.oncecorp.visa3d.bridge.beans.DataBridgeBean;
import com.oncecorp.visa3d.bridge.beans.BeansHelper;
import com.oncecorp.visa3d.bridge.listening.MessageFieldsFilter;
import com.oncecorp.visa3d.bridge.auditing.AuditingUtils;

import org.apache.log4j.Logger;
import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;

/**
 * <p>Title: ConfigurationManager </p>
 * <p>Description: This class is responsible for configuration related functionalities.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation </p>
 * @author Gang Wu ( gwu@oncecorp.com)
 * @version 1.0
 */
public class ConfigurationManager
{

    private final static String  CONFIG_FILE_NAME = "mpi-databridge.xml";

    private final static String  PROPERTY_FILE_NAME = "config.properties";

    private final static String  FILE_NAME_PROPERTY = "fileName";
    private final static String  FROM_FILE_PROPERTY = "fromFile";
    private final static String  DATA_SOURCE_PROPERTY = "dbsource";
    private final static String  TABLE_NAME_PROPERTY = "tableName";

    private String fileName = null;
    private boolean fromFile = true;
    private String dataSourceName = null;
    private String tableName = null;

    private FileHandler m_fileHandler = new FileHandler();
    private DatabaseHandler m_dbHandler = new DatabaseHandler();

    private DataBridgeBean m_databridge = null;

    private static  final List   m_configurables =
            Collections.synchronizedList( new ArrayList() );

    private static Logger m_logger = DataBridgeLoger.getLogger(
            ConfigurationManager.class.getName() );

    private final static ConfigurationManager m_instance = new ConfigurationManager();

    private ConfigurationManager()
    {
        m_logger.debug("Enter constructor ConfigurationManager");
        BeansHelper.setLogger( DataBridgeLoger.getLogger( BeansHelper.class ) );
        m_databridge = new DataBridgeBean();
        registerConfigurable( new ListeningConfig() );
        registerConfigurable( new AuditingConfig() );
        registerConfigurable( new MonitoringServiceConfig() );
        registerConfigurable( new PluginConfigure() );
        m_logger.debug("Exit constructor ConfigurationManager");
    }

    /**
     * Configuration Manager implement Singleton pattern, this method returns
     * the only instance.
     * @return - Configuration Manager instance
     */
    public static ConfigurationManager getInstance()
    {
        return m_instance;
    }

    /**
     * Initialize the configuration manager.
     */
    public void initialize()
    {
        this.initialize( null );
    }

    /**
     * Initialize the configuration manager.
     */
    public void initialize( Map preDefines )
    {
        m_logger.debug("Enter initialize()");
        Map configProps = null;
        if ( preDefines != null && preDefines.size() > 0 )
        {
            configProps = preDefines;
        }
        else
        {
            try {
                m_logger.debug("Load from property file");
                Properties properties = new Properties();
                properties.load(
                        ConfigurationManager.class.getResourceAsStream(PROPERTY_FILE_NAME) );
                configProps = properties;
                m_logger.debug("Finish loading from property file");
            } catch ( Exception e )
            {
                m_logger.warn("Loading property file exception", e);
            }
        }
        if ( configProps != null )
        {
            try {
                boolean fflag = false;
                boolean dbflag = false;
                String str = (String)configProps.get(FILE_NAME_PROPERTY);
                if ( str != null && !str.trim().equals("") )
                {
                    fileName = str;
                    m_logger.debug("fileName is:" + fileName);
                    fflag = true;
                }
                str = (String)configProps.get(DATA_SOURCE_PROPERTY);
                if ( str != null && !str.trim().equals("") )
                {
                    dataSourceName = str;
                    m_dbHandler.setDataSourceName( str );
                    dbflag = true;
                    m_logger.debug("datasourceName is:" + dataSourceName );
                }
                str = (String)configProps.get(TABLE_NAME_PROPERTY);
                if ( str != null && !str.trim().equals("") )
                {
                    tableName = str;
                    m_logger.debug("tableName is:" + tableName );
                }
                str = (String)configProps.get(FROM_FILE_PROPERTY);
                if ( str != null && !str.trim().equals("") )
                {
                    fromFile = Boolean.valueOf(str).booleanValue();
                    m_logger.debug("fromFile is:" + fromFile );
                }
                else
                {
                    if ( fflag )
                        fromFile = true;
                    else if ( dbflag )
                        fromFile = false;
                }
            }
            catch (Exception e) {
                m_logger.error( "initialize the configuration manager error.", e);
            }
        }
        m_dbHandler.setTableName( tableName );
        DataBridgeLoger.initConfigFromFile();
        MessageFieldsFilter.initMessageTable( getDatabridgeWorkingDirectory() );
        AuditingUtils.initMessageTable( MessageFieldsFilter.getMessageMappingBean() );
        BeansHelper.setMessageDefinitionMap(
                MessageFieldsFilter.getMessageMappingBean().getMessages() );
        this.reload();
        TimerService.getInstance().startService();
        m_logger.debug("Exit initialize()");
    }

    /**
     *
     * @return - current databridge working directory
     */
    public static String getDatabridgeWorkingDirectory()
    {
        return FileHandler.getFileParentPath( m_instance.getFileName() );
    }

    /**
     * Load configuration from a given file
     * @param fname - file name
     */
    public void loadFromFile( String fname )
    {
        String filename = getFileName();
        setFileName( fname );
        config ( m_fileHandler.load( getFileName() ), false );
        checkAndDoAutoSave();
        setFileName( filename );
    }

    /**
     * Load configuration from a given XML string
     * @param xml - configuration XML string
     */
    public void loadFromString( String xml )
    {
        if ( xml != null && !xml.trim().equals("") )
        {
            xml = BeansHelper.wrapHeader( xml );
            config ( XMLUtils.getDocumentFromString( xml, false ), true  );
            checkAndDoAutoSave();
        }
    }

    /**
     * Auto save function for MBean
     */
    public void checkAndDoAutoSave()
    {
        if ( isAutoSave() )
            save();
    }

    /**
     * Default loading configuration file
     */
    public void reload()
    {
        m_logger.debug("Enter load()");
        if ( fromFile )
            config ( m_fileHandler.load( getFileName() ), false );
        else
            config ( m_dbHandler.load(  ), false );

        m_logger.debug("Exit load()");
    }

    private void config( Document document, boolean fromString )
    {
        m_logger.debug("Enter config");
        DataBridgeBean bean = BeansHelper.databridgeFromXml( null, document );
        if ( bean == null )
        {
            m_logger.warn(" Can't extract the definition for databridgebean.");
            return;
        }
        Configurable conf;
        for (Iterator lt = m_configurables.iterator(); lt.hasNext(); )
        {
            conf = (Configurable)lt.next();
            if ( !fromString )
                conf.config( bean );
            else
            {
                if ( ( conf instanceof AuditingConfig )
                     && bean.getAuditingService() != null )
                {
                    conf.config( bean );
                    m_databridge.setAuditingService( bean.getAuditingService() );
                }
                else if ( ( conf instanceof MonitoringServiceConfig )
                          && bean.getMonitoringService() != null )
                {
                    conf.config( bean );
                    m_databridge.setMonitoringService( bean.getMonitoringService() );
                }
                else if ( ( conf instanceof ListeningConfig )
                          && bean.getListeningService() != null )
                {
                    conf.config( bean );
                    m_databridge.setListeningService( bean.getListeningService() );
                }
                else if ( ( conf instanceof PluginConfigure )
                          && bean.getPluginService() != null )
                {
                    conf.config( bean );
                    m_databridge.setPluginService( bean.getPluginService() );
                }
            }
        }
        if ( !fromString )
            m_databridge = bean;
        else
        {
            if ( bean.isAttributeSet() )
                m_databridge.setAutoSave( bean.isAutoSave() );
        }
        m_logger.debug("Exit config");
    }

    /**
     * Set configuration file name
     * @param name - configuration file name
     */
    public void setFileName( String name )
    {
        fileName = name;
    }

    /**
     * Get configuration file name
     * @return - configuration file name
     */
    public String getFileName()
    {
        if ( fileName == null || fileName.trim().equals("") )
            return CONFIG_FILE_NAME;
        else
            return fileName;
    }

    /**
     * Save the configuration data
     */
    public void save()
    {
        if ( fromFile )
            m_fileHandler.save( getFileName(), m_databridge.toXml() );
        else
            m_dbHandler.save(  m_databridge.toXml() );
    }

    /**
     * Get auto save flag
     * @return -  the auto save flag that defined by configuration data
     */
    public boolean isAutoSave()
    {
        return m_databridge.isAutoSave();
    }

    /**
     * Set auto save value
     * @param flag - auto save flag
     */
    public void setAutoSave( boolean flag )
    {
        m_databridge.setAutoSave( flag );
        if ( flag )
            save();
    }

    /**
     * Set whether configuration data from file or not
     * @param flag - configuration from flag
     */
    public void setFromFile( boolean flag )
    {
        fromFile = flag;
    }

    /**
     *
     * @return - configuration data from file or not
     */
    public boolean isFromFile()
    {
        return fromFile;
    }

    /**
     *
     * @return - the name of data source
     */
    public String getDataSourceName()
    {
        return dataSourceName;
    }

    /**
     * Set the name of data source
     * @param name - data source name
     */
    public void setDataSourceName( String name )
    {
        dataSourceName = name;
    }

    /**
     *
     * @return the String value of tableName.
     */
    public String getTableName(){
        return tableName;
    }

    /**
     *
     * @param aTableName - the new value for tableName
     */
    public void setTableName(String aTableName){
        tableName = aTableName;
    }

    /**
     * Register a configurable bean
     * @param conf - a configurable bean
     */
    public void registerConfigurable( Configurable conf )
    {
        if ( !m_configurables.contains( conf ) )
            m_configurables.add( conf );
    }

    /**
     * Unregister a configurable bean
     * @param conf - a configurable bean
     */
    public void unregisterConfigurable( Configurable conf )
    {
        if ( m_configurables.contains( conf ) )
            m_configurables.remove( conf );
    }

    /**
     *
     * @return - XML string of the configuration data
     */
    public String toXml()
    {
        if ( m_databridge != null )
            return m_databridge.toXml();
        else
            return "";
    }


}