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

import com.oncecorp.visa3d.bridge.monitoring.LoggingMBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * <p>Title: ONCE MPI Data Bridge</p>
 * <p>Description: This is the MBean class that is used for dynamically
 * updating the data bridge logging configuration.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Once Corporation</p>
 * @author yge@oncecorp.com
 * @version 1.0
 */

public class Logging implements LoggingMBean {
  private static final Logger log4j = DataBridgeLoger.getLogger(Logging.class);

  /**
   * This method adds the new logging config value.
   * @param name The config name.
   * @param value The config value.
   * @return The perform result.
   */
  public String addConfigValue(String name, String value) {
    if ( DataBridgeLoger.props.containsKey(name) ) {
      log4j.warn("Duplicate property name -- \"" + name + "\".");
      return "Duplicate property name -- \"" + name + "\".";
    }
    return putConfigValue(name, value);
  }
  /**
   * This mehtod updates the config value.
   * @param name The config name.
   * @param value The config value.
   * @return The perform result.
   */
  public String updateConfigValue(String name, String value) {
    if ( !DataBridgeLoger.props.containsKey(name) ) {
      log4j.warn("The property \"" + name + "\" does not exist.");
      return "The property \"" + name + "\" does not exist.";
    }
    return putConfigValue(name, value);
  }
  /**
   * This method sets the config value.
   * @param name The config name.
   * @param value The config value.
   * @return The perform result.
   */
  public String putConfigValue(String name, String value) {
    DataBridgeLoger.backupValue();
    DataBridgeLoger.props.setProperty(name, value);
    return DataBridgeLoger.configure();
    //Properties props = new Properties();
    //props.setProperty(name, value);
    //return DataBridgeLoger.doConfigure(props);
  }
  /**
   * This method remove a config value with particular name.
   * @param name The config name.
   * @return The perform result.
   */
  public String removeConfigValue(String name) {
    if ( !DataBridgeLoger.props.containsKey(name) ) {
      log4j.warn("The property \"" + name + "\" does not exist.");
      return "The property \"" + name + "\" does not exist.";
    }
    DataBridgeLoger.backupValue();
    DataBridgeLoger.props.remove(name);
    return DataBridgeLoger.configure();
  }

  /**
   * This method returns the config value with particular name.
   * @param name The config name.
   * @return The cofig value.
   */
  public String obtainConfigValue(String name) {
    return DataBridgeLoger.props.getProperty(name);
  }
  /**
   * This method returns the all config names.
   * @return The config name array.
   */
  public String[] obtainConfigNames() {
    ArrayList list = new ArrayList(DataBridgeLoger.props.keySet());
    Collections.sort(list);
    String[] result = new String[list.size()];
    list.toArray(result);
    return result;
  }
  /**
   * This method returns the config name/value mapping.
   * @return The config name/value mapping.
   */
  public Properties obtainConfigProperties() {
    return DataBridgeLoger.props;
  }
  /**
   * This method add config into the logging using the given data in the java
   * properties object.
   * @param props The java properties object contains the logging config data.
   * @return The perform result.
   */
  //public String appendConfigProperties(Properties props) {
  //  return DataBridgeLoger.doConfigure(props);
  //}
  /**
   * This method re-config the logging using the given data in the java
   * properties object.
   * @param props The java properties object contains the logging config data.
   * @return The perform result.
   */
  public String resetConfigProperties(Properties props) {
    DataBridgeLoger.backupValue();
    DataBridgeLoger.setConfigProperties(props);
    return DataBridgeLoger.configure(false);
  }

  /**
   * This method sets re-config the logging using the given data in the java
   * properties object.
   * @param props The java properties object contains the logging config data.
   * @return The perform result.
   */

  public String putConfigProterties(Properties props) {
    DataBridgeLoger.backupValue();
    DataBridgeLoger.setConfigProperties(props);
    return DataBridgeLoger.configure();
  }
}