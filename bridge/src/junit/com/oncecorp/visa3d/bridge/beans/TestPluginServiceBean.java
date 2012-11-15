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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Document;
import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;
import com.oncecorp.visa3d.bridge.utility.JUnitHelper;
import com.oncecorp.visa3d.bridge.utility.XMLUtils;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Once Corporation</p>
 * @author yge@oncecorp.com
 * @version 1.0
 */

public class TestPluginServiceBean extends TestCase {

  /**
   * The constructor of the class.
   * @param name The instance name.
   */
  public TestPluginServiceBean(String name) {
    super(name);
  }

  /**
   * This method returns the <tt>ListeningServiceBean</tt> object from the
   * sample xml file.
   * @return The <tt>ListeningServiceBean</tt> object.
   */
  private PluginServiceBean createInitBean() {
    Document doc = JUnitHelper.getDocument(getClass(), "mpi-databridge.xml");
    String xml = XMLUtils.toXmlString(doc);
    PluginServiceBean bean = new PluginServiceBean();
    bean.fromXml(xml);
    return bean;
  }

  /**
   * This method test hte <tt>fromXml()</tt> method.
   */
  public void testFromXml() {
    PluginServiceBean bean = createInitBean();
    assertEquals(bean.getPlugins().size(), 2);
    PluginListenerBean plbean = bean.getPluginListener("plugin1");
    assertNotNull(plbean);

    // test PluginListenerBean
    // 1. test message
    assertEquals(plbean.getMessages().size(), 2);
    ListeningMessageBean lmbean = (ListeningMessageBean)plbean.getMessages().get(BeansHelper.getMessageMappingKey("PARes", "1.0.1"));
    assertNotNull(lmbean);
    assertEquals(lmbean.getFields().size(), 1);
    MessageFieldBean mfbean = (MessageFieldBean)lmbean.getFields().get(0);
    assertEquals(mfbean.isEncryption(), true);
    assertTrue(mfbean.getName().equals("CH/acctID"));
    // 2. test channel
    assertEquals(plbean.getChannels().size(), 2);
    PluginChannelBean pcbean = (PluginChannelBean)plbean.getChannels().get("channel1");
    assertNotNull(pcbean);
    assertTrue(pcbean.getStatus().equals("start"));
    assertEquals(pcbean.isAcceptFilter(), true);
    assertEquals(pcbean.isAcceptDynamicFilter(), true);
    assertEquals(pcbean.getMerchantIds().size(), 1);
    assertTrue(pcbean.getMerchantIds().get(0).equals("Merchant1"));
    assertTrue(pcbean.getCustomData().equals("XXXXXX"));
  }

  /**
   * This method test hte <tt>toXml()</tt> method.
   */
  public void testToXml() {
    PluginServiceBean originBean = createInitBean();
    String xml = originBean.toXml();
    // set fata in the xml string to the bean to do the test.
    PluginServiceBean bean = new PluginServiceBean();
    bean.fromXml(xml);
    assertEquals(bean.getPlugins().size(), 2);
    PluginListenerBean plbean = bean.getPluginListener("plugin1");
    assertNotNull(plbean);

    // test PluginListenerBean
    // 1. test message
    assertEquals(plbean.getMessages().size(), 2);
    ListeningMessageBean lmbean = (ListeningMessageBean)plbean.getMessages().get(BeansHelper.getMessageMappingKey("PARes", "1.0.1"));
    assertNotNull(lmbean);
    assertEquals(lmbean.getFields().size(), 1);
    MessageFieldBean mfbean = (MessageFieldBean)lmbean.getFields().get(0);
    assertEquals(mfbean.isEncryption(), true);
    assertTrue(mfbean.getName().equals("CH/acctID"));
    // 2. test channel
    assertEquals(plbean.getChannels().size(), 2);
    PluginChannelBean pcbean = (PluginChannelBean)plbean.getChannels().get("channel1");
    assertNotNull(pcbean);
    assertTrue(pcbean.getStatus().equals("start"));
    assertEquals(pcbean.isAcceptFilter(), true);
    assertEquals(pcbean.isAcceptDynamicFilter(), true);
    assertEquals(pcbean.getMerchantIds().size(), 1);
    assertTrue(pcbean.getMerchantIds().get(0).equals("Merchant1"));
    assertTrue(pcbean.getCustomData().equals("XXXXXX"));
  }

  public static Test suite() {
    //return new TestSuite(TestPluginServiceBean.class);
    TestSuite suite = new TestSuite("Plugin Service Bean Unit Test Suite");
    suite.addTest(new TestPluginServiceBean("testFromXml"));
    suite.addTest(new TestPluginServiceBean("testToXml"));
    return suite;
  }

  public static void main(String[] args) {
    DataBridgeLoger.initConfigFromFile();
    junit.textui.TestRunner.run(suite());
  }
}