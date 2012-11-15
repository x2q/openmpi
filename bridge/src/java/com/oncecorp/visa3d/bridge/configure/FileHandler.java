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

/**
 * <p>Title: FileHandler </p>
 * <p>Description: Provides file related functions for configuration manager</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */
import java.io.InputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.oncecorp.visa3d.bridge.utility.XMLUtils;


import org.apache.log4j.Logger;
import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;

public class FileHandler
{

    private static Logger m_logger = DataBridgeLoger.getLogger(
           FileHandler.class.getName() );

    /**
     * Default constructor
     */
    public FileHandler()
    {
    }

    /**
     * Load configuration file
     * @param fname - configuration XML file
     * @return - XML document
     */
    public Document load( String fname )
    {
        m_logger.debug("Enter load");
        InputStream is = null;
        try {
            is = new FileInputStream( fname );
        } catch ( Exception e )
        {
            m_logger.debug(fname + " doesn't exist. ");
            if ( fname.indexOf( File.separator ) == -1 )
            {
                m_logger.debug(fname + " is a relative path. Try class path");
                is =  ConfigurationManager.class.getResourceAsStream( fname );
            }
        }
        if ( is != null )
        {
            m_logger.debug("Get the xml string");
            return XMLUtils.getDocument( new InputSource( is ), false );
        }
        else
        {
            m_logger.error("Can't load xml string from the file.");
            return null;
        }
    }

    /**
     * Get a file's parent directory, first search from the filename,
     * if not found, search the configurationManager class resource path
     * @param fname -  configuration file name
     * @return - configuration file parent path
     */
    public static String  getFileParentPath( String fname )
    {
        return getFileParentPath( fname, ConfigurationManager.class );
    }

    /**
     * Get a file's parent directory, first search from the filename,
     * if not found, search the class resource path
     *
     * @param fname -  configuration file name
     * @param cls - called class
     * @return - configuration file parent path
     */
    public static String  getFileParentPath( String fname, Class cls )
    {
        m_logger.debug("Enter getFileParentPath");
        File file = null;
        try {
            file = new File( fname );
            if ( !file.exists() || !file.canRead() )
            {
                m_logger.debug(fname + " doesn't exist. ");
                if ( fname.indexOf( File.separator ) == -1 )
                {
                    m_logger.debug(fname + " is a relative path. Try class path");
                    file = null;
                    URL url = cls.getResource( fname );
                    if ( url != null )
                    {
                        String path = url.getFile();

                        m_logger.debug("Find file in class path:"+ path);

                        if ( path != null )
                        {
                            file = new File(path);
                            if ( !file.exists() || !file.canRead() )
                                file = null;
                        }
                    }
                }
            }
        } catch ( Exception e )
        {
            m_logger.error("Exception during geting configuration file name.");
            file = null;
        }

        if ( file != null )
        {
            File tmpDir = new File ( file.getAbsolutePath() );
            m_logger.debug("configuration file parent path is:" + tmpDir.getParent());
            return tmpDir.getParent();
        }

        m_logger.debug("Exit getFileParentPath with not found.");

        return null;
    }

    /**
     * Save the configuration data to the file, the old file is saved.
     *
     * @param fname - configuration file name
     * @param xml - XML configuration string
     */
    public void save( String fname, String xml )
    {
        m_logger.debug("Enter save");
        File file = null;
        File saveFile = null;
        try {
            file = new File( fname );
            if ( !file.exists() || !file.canRead() || !file.canWrite() )
            {
                m_logger.debug(fname + " doesn't exist. ");
                if ( fname.indexOf( File.separator ) == -1 )
                {
                    m_logger.debug(fname + " is a relative path. Try class path");
                    URL url = ConfigurationManager.class.getResource( fname );
                    if ( url != null )
                    {
                        String path = url.getFile();
                        if ( path != null )
                        {
                            saveFile = new File(path);
                            if ( !file.exists() || !file.canRead() || !file.canWrite() )
                                saveFile = null;
                        }
                    }
                }
            }
            else
                saveFile = file;

            if ( saveFile != null )
                file = saveFile;

        } catch ( Exception e )
        {
            m_logger.error("Exception during geting configuration file name.");
            file = null;
        }

        if ( file != null )
        {

            m_logger.debug("Save configuration file name is:"+ file.getName());

            String apath = file.getAbsolutePath();
            File tmpDir = new File( apath );
            String ppath = tmpDir.getParent();
            String oname = file.getName();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

            String tmpFile = ppath + File.separator + sdf.format( new Date() ) + "_" + oname;

            m_logger.debug( "Save the old file to: [" +  tmpFile + "]" );
            file.renameTo( new File ( tmpFile ) );

            if ( xml != null )
            {
                try {
                    FileWriter fw = new FileWriter( apath );
                    fw.write( xml );
                    fw.flush();
                    fw.close();
                    } catch ( Exception e )
                    {
                        m_logger.error("Write configure file exception.", e);
                    }
            }

            m_logger.debug("Saved the xml string");
        }
        else
        {
            m_logger.error("Can't save configuration data to file.");
        }

        m_logger.debug("Exit save.");
    }

}