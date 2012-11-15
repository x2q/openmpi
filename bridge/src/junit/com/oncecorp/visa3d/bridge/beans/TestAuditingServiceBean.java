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

import junit.framework.TestCase;
import junit.framework.Test;
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

public class TestAuditingServiceBean extends TestCase {

  public TestAuditingServiceBean(String name) {
    super(name);
  }

  private AuditingServiceBean createInitBean() {
    Document doc = JUnitHelper.getDocument(getClass(), "mpi-databridge.xml");
    String xml = XMLUtils.toXmlString(doc);
    AuditingServiceBean bean = new AuditingServiceBean();
    bean.fromXml(xml);
    return bean;
  }

  public void testFromXml() {
    AuditingServiceBean bean = createInitBean();
    assertEquals(bean.getSamplingTime(), 300);
    assertTrue(bean.getMerchantSchema().equals(""));
    assertEquals(bean.isMailAuth(), false);
    assertTrue(bean.getMailUsername().equals("info"));
    assertTrue(bean.getMailPasswd().equals("XXX"));
    assertTrue(bean.getMailInet().equals("mail"));
    assertEquals(bean.getListeners().size(), 2);
    assertNotNull(bean.getListener("handler1"));

    // test the first auditing listener bean.
    AuditingListenerBean albean = bean.getListener("handler1");
    assertTrue(JUnitHelper.isEqual(albean.getStatus(), "stop"));
    assertEquals(albean.getMaxRows(), 10);
    assertEquals(albean.getThreshold(), 7);
    assertEquals(albean.isMailNotify(), true);
    assertTrue(JUnitHelper.isEqual(albean.getPeriod(), "24:00:00"));
    assertTrue(JUnitHelper.isEqual(albean.getEmailTemplate(), "TemplateHandler1.xml"));
    assertTrue(JUnitHelper.isEqual(albean.getSender(), "info@oncecorp.com"));
    assertTrue(JUnitHelper.isEqual(albean.getTo(), "info@oncecorp.com,web@oncecorp.com"));
    assertTrue(JUnitHelper.isEqual(albean.getCc(), "info@oncecorp.com,web@oncecorp.com"));
    assertEquals(albean.getMerchantIds().size(), 1);
    assertTrue(albean.getMerchantIds().get(0).equals("Merchant1"));
  }

  public void testToXml() {
    AuditingServiceBean origienBean = createInitBean();
    String xml = origienBean.toXml();

    // set xml data into a new bean to test its value.
    AuditingServiceBean bean = new AuditingServiceBean();
    bean.fromXml(xml);
    assertEquals(bean.getSamplingTime(), 300);
    assertTrue(bean.getMerchantSchema().equals(""));
    assertEquals(bean.isMailAuth(), false);
    assertTrue(bean.getMailUsername().equals("info"));
    assertTrue(bean.getMailPasswd().equals("XXX"));
    assertTrue(bean.getMailInet().equals("mail"));
    assertEquals(bean.getListeners().size(), 2);
    assertNotNull(bean.getListener("handler1"));

    // test the first auditing listener bean.
    AuditingListenerBean albean = bean.getListener("handler1");
    assertTrue(JUnitHelper.isEqual(albean.getStatus(), "stop"));
    assertEquals(albean.getMaxRows(), 10);
    assertEquals(albean.getThreshold(), 7);
    assertEquals(albean.isMailNotify(), true);
    assertTrue(JUnitHelper.isEqual(albean.getPeriod(), "24:00:00"));
    assertTrue(JUnitHelper.isEqual(albean.getEmailTemplate(), "TemplateHandler1.xml"));
    assertTrue(JUnitHelper.isEqual(albean.getSender(), "info@oncecorp.com"));
    assertTrue(JUnitHelper.isEqual(albean.getTo(), "info@oncecorp.com,web@oncecorp.com"));
    assertTrue(JUnitHelper.isEqual(albean.getCc(), "info@oncecorp.com,web@oncecorp.com"));
    assertEquals(albean.getMerchantIds().size(), 1);
    assertTrue(albean.getMerchantIds().get(0).equals("Merchant1"));
  }

  public static Test suite() {
    //return new TestSuite(TestAuditingServiceBean.class);
    TestSuite suite = new TestSuite("Auditing Service Bean Unit Test Suite");
    suite.addTest(new TestAuditingServiceBean("testFromXml"));
    suite.addTest(new TestAuditingServiceBean("testToXml"));

    return suite;
  }

  public static void main(String[] args) {
    DataBridgeLoger.initConfigFromFile();
    junit.textui.TestRunner.run(suite());
  }
}