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

package com.oncecorp.visa3d.bridge.logging;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.oncecorp.visa3d.bridge.configure.ConfigurationManager;
/**
 * <p>Title: ONCE MPI Data Bridge</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Once Corporation</p>
 * @author yge@oncecorp.com
 * @version 1.0
 */

public class DataBridgeLoger {
  private static final PropertyConfigurator  configurator     = new PropertyConfigurator();
  static final   Properties           props            = new Properties();
  private static Properties           backupProps      = null;;
  private static final String         CONFIG_FILE_NAME = "log4j.properties";
  private static boolean              isConfigured     = false;
  private static String               OK = "OK";
  private static Logger               log4j;

  static {
    initConfig();
  }

  /**
   * This method returns the logger with the particular name.
   * @param name The logger name.
   * @return The logger instance.
   */
  public static Logger getLogger(String name) {
    if ( !isConfigured ) {
      //config();
      initConfig();
    }
    return Logger.getLogger(name);
  }

  /**
   * This method returns the logger whose name is the full name of the given class.
   * @param clazz The given class.
   * @return The logger instance.
   */
  public static Logger getLogger(Class clazz) {
    return DataBridgeLoger.getLogger(clazz.getName());
  }

  /**
   * This method returns the logger whose name is the full name of the class
   * name of the given object instance.
   * @param obj The given object instance.
   * @return The logger instance.
   */
  public static Logger getLogger(Object obj) {
    return DataBridgeLoger.getLogger(obj.getClass().getName());
  }

  /**
   * This method initializes the log4j.
   */
  private static void initConfig() {
    isConfigured = true;
    props.clear();
    props.setProperty("log4j.rootCategory", "DEBUG, dest0");
    props.setProperty("log4j.appender.dest0", "org.apache.log4j.ConsoleAppender");
    props.setProperty("log4j.appender.dest0.layout", "org.apache.log4j.PatternLayout");
    props.setProperty("log4j.appender.dest0.layout.ConversionPattern", "%d{dd MMM yyyy HH:mm:ss,SSS} %-5p [%t] (%F:%L) - %m%n");
    //configure(false);
    PropertyConfigurator.configure(props);
    log4j = Logger.getLogger(DataBridgeLoger.class);
  }

  /**
   * This method initializes the log4j from the config properties faile.
   */
  public static void initConfigFromFile() {
    backupValue();
    props.clear();
    try {
      //props.load(DataBridgeLoger.class.getResourceAsStream(CONFIG_FILE_NAME));
      log4j.debug("Start initConfigFromFile() call.");
      load();
      configure(false, false);
      Logger.getRootLogger().removeAppender("dest0");
    }
    catch (Exception e) {
      //throw new LoggingException("Loggign configuring error.", e);
      e.printStackTrace();
      log4j.error("Loggign configuring error.", e);
    }
  }

  /**
   * This method use properties data in the momery to config the log4j.  It does
   * no save config data into the config file.
   * @return The operation result message.
   */
   /*
  public static String doConfigure(Properties addProps) {
    Logger rootLogget = Logger.getRootLogger();
    try {
      log4j.debug("call configurator.doConfigure()");
      configurator.doConfigure(addProps, rootLogget.getLoggerRepository());
      log4j.debug("call props.putAll()");
      props.putAll(addProps);
      log4j.debug("call save()");
      save();
      log4j.debug("return OK");
      return OK;
      //return configure(true);
    }
    catch (Exception e) {
      log4j.error("Config data is not valid.", e);
      return "Config data is not valid.";
    }
  }*/

  /*
  public static void resetConfiguration() {
    BasicConfigurator.resetConfiguration();
    backupValue();
    props.clear();
  }*/

  public static String configure() {
    return configure(true);
  }

  public static String configure(boolean changeAppend) {
    return configure(true, changeAppend);
  }

  /**
   * This method use properties data in the momery to config the log4j.
   * @param saveFlag <tt>ture</tt> save the config data into the file, otherwise
   * not save.
   * @return The operation result message.
   */
  private static String configure(boolean saveFlag, boolean changeAppend) {
    try {
      PropertyConfigurator.configure(modifyFileProperty(props, changeAppend));
      if ( saveFlag ) {
        save();
      }
      return OK;
    }
    catch (Exception e) {
      PropertyConfigurator.configure(modifyFileProperty(backupProps, changeAppend));
      return "Config data is not valid.";
    }
  }

  /*
  private static Properties modifyFileProperty(Properties p) {
    return modifyFileProperty(p, false);
  }*/
  /**
   * This method converts the config properties object.  It checks the "File"
   * property value to see if it is absolute, if
   * not, put the log file into the default working directory.
   * @param p the Properties object to be converted.
   * @param changeAppend If <tt>true</tt>, set Append property to <tt>false</tt>.
   * @return The result properties object.
   */
  private static Properties modifyFileProperty(Properties p, boolean changeAppend) {
    Properties newProps = new Properties();
    newProps.putAll(p);
    for (Iterator i = newProps.keySet().iterator(); i.hasNext(); ) {
      String name = (String)i.next();
      if ( name.endsWith(".File") ) {
        String value = newProps.getProperty(name);
        File file = new File(value);
        if ( !file.isAbsolute() ) {
          String path = ConfigurationManager.getInstance().getDatabridgeWorkingDirectory() + "/logs";
          if ( value.startsWith("/") ) {
            path = path + value;
          }
          else {
            path = path + "/" + value;
          }
          File newPath = new File(path);
          File parent = newPath.getParentFile();
          if ( parent != null && !parent.exists() ) {
            parent.mkdirs();
          }
          newProps.setProperty(name, path);
        }
        else {
        }
      }
      else if ( changeAppend && name.endsWith(".Append") ) {
        newProps.setProperty(name, "true");
      }
    }
    return newProps;
  }

  /**
   * This method backup the config properties object.
   */
  public static void backupValue() {
    backupProps = new Properties();
    backupProps.putAll(props);
  }

  /**
   * This method set new config properties object to the log4j object.
   * @param p The new config properties object.
   */
  public static void setConfigProperties(Properties p) {
    props.clear();
    props.putAll(p);
  }

  /**
   * This method load properties data form the file.
   * @throws IOException If file operating failed.
   */
  private static void load() throws IOException {
    log4j.debug("Enter load log4j properties file.");
    String configFileFullName =
      ConfigurationManager.getInstance().getDatabridgeWorkingDirectory() + File.separator + CONFIG_FILE_NAME;
    InputStream is = null;
    try {
      is = new FileInputStream( configFileFullName );
    }
    catch ( Exception e ) {
      log4j.warn(configFileFullName + " doesn't exist. ");
      if ( CONFIG_FILE_NAME.indexOf( File.separator ) == -1 ) {
        log4j.debug(CONFIG_FILE_NAME + " is a relative path. Try class path");
        is =  ConfigurationManager.class.getResourceAsStream( CONFIG_FILE_NAME );
      }
    }

    if ( is != null ) {
      log4j.debug("Get the xml string");
      props.clear();
      props.load(is);
    }
    else {
      log4j.error("Can't load log4j properties from file.");
      return;
    }
  }

  /**
   * This method saves the log4j config data into the file.
   */
  private static void save() {
    log4j.debug("Enter save log4j properties file.");
    String configFileFullName =
      ConfigurationManager.getInstance().getDatabridgeWorkingDirectory() + "/" + CONFIG_FILE_NAME;
    File file = null;
    File saveFile = null;
    try {
      file = new File( configFileFullName );
      if ( !file.exists() || !file.canRead() || !file.canWrite() ) {
        log4j.debug(configFileFullName + " doesn't exist. ");
        if ( CONFIG_FILE_NAME.indexOf( File.separator ) == -1 ) {
          log4j.debug(CONFIG_FILE_NAME + " is a relative path. Try class path");
          URL url = ConfigurationManager.class.getResource( CONFIG_FILE_NAME );
          if ( url != null ) {
            String path = url.getFile();
            if ( path != null ) {
              saveFile = new File(path);
              if ( !file.exists() || !file.canRead() || !file.canWrite() ) {
                saveFile = null;
              }
            }
          }
        }
      }
      else {
        saveFile = file;
      }
      if ( saveFile != null ) {
        file = saveFile;
      }

    }
    catch ( Exception e ) {
      log4j.error("Exception during geting configuration file name.");
      file = null;
    }

    if ( file != null ) {

      log4j.debug("Save log4j properties name is:"+ file.getName());

      String apath = file.getAbsolutePath();
      File tmpDir = new File( apath );
      String ppath = tmpDir.getParent();
      String oname = file.getName();

      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

      String tmpFile = ppath + File.separator + sdf.format( new Date() ) + "_" + oname;

      log4j.debug( "Save the old file to: [" +  tmpFile + "]" );
      file.renameTo( new File ( tmpFile ) );

      try {
        OutputStream out = new FileOutputStream(apath, false);
        props.store(out, "");
      }
      catch (IOException e) {
        log4j.error("Write log4j properties file exception.", e);
      }
      log4j.debug("Saved log4j properties");
    }
    else {
      log4j.error("Can't save log4j properties file.");
    }

    log4j.debug("Exit save log4j properties.");
  }

  /**
   * This method is used for testing purpose.
   * @param args
   */
  public static void main(String[] args) {
    initConfig();
  }
}