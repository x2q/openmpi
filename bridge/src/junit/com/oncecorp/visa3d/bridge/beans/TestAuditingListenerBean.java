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
import org.xml.sax.InputSource;

import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;
import com.oncecorp.visa3d.bridge.utility.XMLUtils;
import com.oncecorp.visa3d.bridge.utility.JUnitHelper;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Once Corporation</p>
 * @author yge@oncecorp.com
 * @version 1.0
 */

public class TestAuditingListenerBean extends TestCase {

  public TestAuditingListenerBean(String name) {
    super(name);
  }

  private AuditingListenerBean createInitBean() {
    Document doc = JUnitHelper.getDocument(getClass(), "mpi-databridge.xml");
    String xml = XMLUtils.toXmlString(doc);
    AuditingServiceBean serverBean = new AuditingServiceBean();
    serverBean.fromXml(xml);
    return serverBean.getListener("handler1");
  }

  public void testFromXml() {
    AuditingListenerBean bean = createInitBean();
    assertTrue(JUnitHelper.isEqual(bean.getStatus(), "stop"));
    assertEquals(bean.getMaxRows(), 10);
    assertEquals(bean.getThreshold(), 7);
    assertEquals(bean.isMailNotify(), true);
    assertTrue(JUnitHelper.isEqual(bean.getPeriod(), "24:00:00"));
    assertTrue(JUnitHelper.isEqual(bean.getEmailTemplate(), "TemplateHandler1.xml"));
    assertTrue(JUnitHelper.isEqual(bean.getSender(), "info@oncecorp.com"));
    assertTrue(JUnitHelper.isEqual(bean.getTo(), "info@oncecorp.com,web@oncecorp.com"));
    assertTrue(JUnitHelper.isEqual(bean.getCc(), "info@oncecorp.com,web@oncecorp.com"));
    assertEquals(bean.getMerchantIds().size(), 1);
    assertTrue(bean.getMerchantIds().get(0).equals("Merchant1"));
  }

  public void testToXml() {
    AuditingListenerBean bean_0 = createInitBean();
    String xml = bean_0.toXml();

    AuditingListenerBean bean = new AuditingListenerBean();
    bean.fromXml(xml);
    assertTrue(JUnitHelper.isEqual(bean.getStatus(), "stop"));
    assertEquals(bean.getMaxRows(), 10);
    assertEquals(bean.getThreshold(), 7);
    assertEquals(bean.isMailNotify(), true);
    assertTrue(JUnitHelper.isEqual(bean.getPeriod(), "24:00:00"));
    assertTrue(JUnitHelper.isEqual(bean.getEmailTemplate(), "TemplateHandler1.xml"));
    assertTrue(JUnitHelper.isEqual(bean.getSender(), "info@oncecorp.com"));
    assertTrue(JUnitHelper.isEqual(bean.getTo(), "info@oncecorp.com,web@oncecorp.com"));
    assertTrue(JUnitHelper.isEqual(bean.getCc(), "info@oncecorp.com,web@oncecorp.com"));
    assertEquals(bean.getMerchantIds().size(), 1);
    assertTrue(bean.getMerchantIds().get(0).equals("Merchant1"));
  }

  public void testAddMerchant() {
    AuditingListenerBean bean = createInitBean();
    bean.addMerchant("Merchant2");
    assertEquals(bean.getMerchantIds().size(), 2);
    assertTrue(bean.getMerchantIds().contains("Merchant2"));
  }

  public static Test suite() {
    TestSuite suite = new TestSuite("Auditing Listener Bean Unit Test Suite");
    suite.addTest(new TestAuditingListenerBean("testFromXml"));
    suite.addTest(new TestAuditingListenerBean("testToXml"));
    suite.addTest(new TestAuditingListenerBean("testAddMerchant"));

    return suite;
    //return new TestSuite(TestAuditingListenerBean.class);
  }

  public static void main(String[] args) {
    DataBridgeLoger.initConfigFromFile();
    junit.textui.TestRunner.run(suite());
  }
}