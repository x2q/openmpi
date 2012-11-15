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

import java.util.Map;
import java.util.Iterator;
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

public class TestListeningServiceBean extends TestCase {

  /**
   * The constructor of the class.
   * @param name The instance name.
   */
  public TestListeningServiceBean(String name) {
    super(name);
  }

  /**
   * This method returns the <tt>ListeningServiceBean</tt> object from the
   * sample xml file.
   * @return The <tt>ListeningServiceBean</tt> object.
   */
  private ListeningServiceBean createInitBean() {
    Document doc = JUnitHelper.getDocument(getClass(), "mpi-databridge.xml");
    String xml = XMLUtils.toXmlString(doc);
//System.out.println(xml);
    ListeningServiceBean bean = new ListeningServiceBean();
    bean.fromXml(xml);
    return bean;
  }

  /**
   * This method test hte <tt>fromXml()</tt> method.
   */
  public void testFromXml() {
    ListeningServiceBean bean = createInitBean();
    assertEquals(bean.getTimeServiceInterval(), 1440L);
    Map props = bean.getProperties();
    assertNotNull(props);
    assertTrue(JUnitHelper.isEqual(props.get("TripleDESKeystoreLocation"), "C:\\work\\oncempi\\new\\src\\java\\com\\oncecorp\\visa3d\\bridge\\security\\local.keystore"));
    assertTrue(JUnitHelper.isEqual(props.get("TripleDESKeystorePwd"), "changeit"));
    assertTrue(JUnitHelper.isEqual(props.get("TripleDESRawkeyAlias"), "testkey"));
    assertTrue(JUnitHelper.isEqual(props.get("TripleDESRawkeyPwd"), "changeit"));
    assertTrue(JUnitHelper.isEqual(props.get("JCEProvider"), "com.sun.crypto.provider.SunJCE"));
  }

  /**
   * This method test hte <tt>toXml()</tt> method.
   */
  public void testToXml() {
    ListeningServiceBean originBean = createInitBean();
    String xml = originBean.toXml();

    // set fata in the xml string to the bean to do the test.
    ListeningServiceBean bean = new ListeningServiceBean();
    bean.fromXml(xml);
    assertEquals(bean.getTimeServiceInterval(), 1440L);
    Map props = bean.getProperties();
    assertNotNull(props);
    assertTrue(JUnitHelper.isEqual(props.get("TripleDESKeystoreLocation"), "C:\\work\\oncempi\\new\\src\\java\\com\\oncecorp\\visa3d\\bridge\\security\\local.keystore"));
    assertTrue(JUnitHelper.isEqual(props.get("TripleDESKeystorePwd"), "changeit"));
    assertTrue(JUnitHelper.isEqual(props.get("TripleDESRawkeyAlias"), "testkey"));
    assertTrue(JUnitHelper.isEqual(props.get("TripleDESRawkeyPwd"), "changeit"));
    assertTrue(JUnitHelper.isEqual(props.get("JCEProvider"), "com.sun.crypto.provider.SunJCE"));
  }

  public static Test suite() {
    //return new TestSuite(TestListeningServiceBean.class);
    TestSuite suite = new TestSuite("Listening Service Bean Unit Test Suite");
    suite.addTest(new TestListeningServiceBean("testFromXml"));
    suite.addTest(new TestListeningServiceBean("testToXml"));

    return suite;
  }

  public static void main(String[] args) {
    DataBridgeLoger.initConfigFromFile();
    junit.textui.TestRunner.run(suite());
  }
}