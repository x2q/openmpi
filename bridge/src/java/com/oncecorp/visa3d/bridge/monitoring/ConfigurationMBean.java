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

package com.oncecorp.visa3d.bridge.monitoring;

/**
 * <p>Title: ConfigurationMBean </p>
 * <p>Description: Configuration related attributes and operations published to JMX server</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation </p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */

public interface ConfigurationMBean
{
    /**
     * Load configuration from a given XML string
     * @param xml - configuration XML string
     */
    public void loadFromString( String xml );

    /**
     * Load configuration from a given file
     * @param fname - file name
     */
    public void loadFromFile( String fname );

    /**
     * Default loading configuration file
     */
    public void reload();

    /**
     * Save the configuration data
     */
    public void save();

    /**
     * Set configuration file name
     * @param name - configuration file name
     */
    public void setFileName( String name );

    /**
     * Get configuration file name
     * @return - configuration file name
     */
    public String getFileName();

    /**
     * Get auto save flag
     * @return
     */
    public boolean isAutoSave();

    /**
     * Set auto save value
     * @param flag - auto save flag
     */
    public void setAutoSave( boolean flag );

    /**
     *
     * @return - configuration data from file or not
     */
    public boolean isFromFile();

    /**
     * Set whether configuration data from file or not
     * @param flag - configuration from flag
     */
    public void setFromFile( boolean flag );

    /**
     *
     * @return - the name of data source
     */
    public String getDataSourceName();

    /**
     * Set the name of data source
     * @param name - data source name
     */
    public void setDataSourceName( String name );

    /**
     *
     * @return the String value of tableName.
     */
    public String getTableName();

    /**
     *
     * @param aTableName - the new value for tableName
     */
    public void setTableName(String aTableName);

    /**
     *
     * @return - Configuration XML string
     */
    public String retriveConfigurationXml();

    /**
     *
     * @return - Messages definition XML string
     */
    public String retriveMessageMappingXml();
}