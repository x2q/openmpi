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

package com.oncecorp.visa3d.bridge.auditing;

import com.oncecorp.visa3d.bridge.beans.AuditingListenerBean;
import com.oncecorp.visa3d.bridge.beans.AuditingServiceBean;
import com.oncecorp.visa3d.bridge.beans.BeansHelper;
import com.oncecorp.visa3d.bridge.beans.MerchantInfoBean;
import com.oncecorp.visa3d.bridge.configure.ConfigurationManager;
import com.oncecorp.visa3d.bridge.monitoring.AuditingMBean;
import com.oncecorp.visa3d.bridge.listening.ListeningManager;
import com.oncecorp.visa3d.bridge.listening.ListeningUtils;
import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;
import org.apache.log4j.Logger;

/**
 * <p>Title: ONCE MPI Data Bridge</p>
 * <p>Description: This is the MBean class for updating the auditing config.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Once Corporation</p>
 * @author yge@oncecorp.com
 * @version 1.0
 */

public class Auditing implements AuditingMBean {

  private static final Logger  log4j = DataBridgeLoger.getLogger(Auditing.class);

  /**
   * The default constructor.
   */
  public Auditing() {
  }

  /**
   * This method returns the auditing server xml string.
   * @return The auditing server xml string.
   */
  public String obtainAuditingServerXml() {
    return AuditingManager.getInstance().getBean().toXml();
  }
  /**
   * This method sets the auditing server xml string.
   * @param xml The auditing server xml string.
   */
  public void putAuditingServerXml(String xml) {
    AuditingServiceBean updateBean = BeansHelper.auditingServiceFromXml(null, xml);
    if ( updateBean != null && AuditingManager.getInstance().update(updateBean) ) {
      ListeningManager.getInstance().resetAuditingListeners( updateBean.getListeners() );
      if ( ConfigurationManager.getInstance().isAutoSave() ) {
        ConfigurationManager.getInstance().save();
      }
    }
  }

  /**
   * This method add new auditing logger object to the auditing service.
   * @param xml The xml string that contains the new logger data.
   */
  public void addAuditingLogger(String xml) {
    AuditingListenerBean bean = BeansHelper.auditingListenerFromXml(null, xml);
    if ( bean != null ) {
      log4j.debug("bean != null");
      addAuditingLogger(bean);
    }
    else {
      log4j.debug("bean == null");
    }
  }

  void addAuditingLogger(AuditingListenerBean bean) {
    if ( AuditingManager.getInstance().addAuditingLogger(bean) ) {
      log4j.debug("logger added -- " + bean.getId());
      /**
       * The following Added by Gang Wu:
       * bean is the AuditingListenerBean
       * It should be called after Auditing manager add
       */
      ListeningManager.getInstance().addAuditingListener( bean );
      /**
       * Add finish
       */
      if ( ConfigurationManager.getInstance().isAutoSave() ) {
        ConfigurationManager.getInstance().save();
      }
    }
  }

  /**
   * This method removes a auditing logger from the auditing server.
   * @param loggerId The logger id.
   */
  public void removeAuditingLogger(String loggerId) {
    if ( AuditingManager.getInstance().containsLogger(loggerId) ) {
      log4j.debug("logger removed -- " + loggerId);
      /**
       * The following Added by Gang Wu:
       * bean is the AuditingListenerBean
       * It should be called before Auditing manager remove
       */
      ListeningManager.getInstance().unregisterListener(
              ListeningUtils.AUDITING_LISTENER, loggerId );
      /**
       * Add finish
       */
      // This method was called by ListeningManager.unregisterListener().
      //AuditingManager.getInstance().removeAuditingLogger(loggerId);

      if ( ConfigurationManager.getInstance().isAutoSave() ) {
        ConfigurationManager.getInstance().save();
      }
    }
  }

  /**
   * This method updates the existed logger.
   * @param xml The xml that contains the logger data.
   */
  public void updateAuditingLogger(String xml) {
    AuditingListenerBean bean = BeansHelper.auditingListenerFromXml(null, xml);
    if ( bean != null ) {
      log4j.debug("bean != null");
      updateAuditingLogger(bean);
    }
    else {
      log4j.debug("bean == null");
    }
  }

  void updateAuditingLogger(AuditingListenerBean bean) {
    if ( !AuditingManager.getInstance().containsLogger(bean.getId()) ) {
      log4j.error("The auditing logger does not exsit.");
      return;
    }
    log4j.debug("do update logger -- " + bean.getId());
    if ( AuditingManager.getInstance().updateAuditingLogger(bean) ) {
      log4j.debug("logger updated -- " + bean.getId());
      /**
       * The following Added by Gang Wu:
       * bean is the AuditingListenerBean
       * It should be called after Auditing manager update
       */
      ListeningManager.getInstance().updateAuditingListener(bean);
      /**
       * Add finish
       */
      if ( ConfigurationManager.getInstance().isAutoSave() ) {
        ConfigurationManager.getInstance().save();
      }
    }
  }

  /**
   * This method returns the particluar logger's mail subject text.
   * @param loggerId The logger id.
   * @return The subject text.
   */
  public String obtainSubjectText(String loggerId) {
    return AuditingManager.getInstance().getLogger(loggerId).getSubject();
  }
  /**
   * This method sets the the particular logger's subject text.
   * @param loggerId The logger id.
   * @param text The subject text.
   */
  public void putSubjectText(String loggerId, String text) {
    AuditingLogger logger = AuditingManager.getInstance().getLogger(loggerId);
    if ( logger != null && !logger.getSubject().equals(text) ) {
      AuditingManager.getInstance().getLogger(loggerId).setSubject(text);
      logger.saveMailTemplate();
    }
  }

  /**
   * This method returns the particular logger's mail body template text.
   * @param loggerId The logger id.
   * @return The body template text.
   */
  public String obtainBodyTemplateText(String loggerId) {
    return AuditingManager.getInstance().getLogger(loggerId).getBody();
  }

  /**
   * This method sets the particular logger's mail body template text.
   * @param loggerId loggerId The logger id.
   * @param text The body template text.
   */
  public void putBodyTemplateText(String loggerId, String text) {
    AuditingLogger logger = AuditingManager.getInstance().getLogger(loggerId);
    if ( logger != null && !logger.getBody().equals(text) ) {
      AuditingManager.getInstance().getLogger(loggerId).setBody(text);
      logger.saveMailTemplate();
    }
  }

  /**
   * This method returns the current record number of the particular logger and
   * particular message type (table).
   * @param merchantId The merchant id.
   * @return The current record number in the database table.
   */
  public int obtainCurrentNumber(String merchantId) {
    //MerchantInfoBean bean = AuditingManager.getInstance().getMerchantInfoBean(merchantId)
    //AuditingLogger logger = AuditingManager.getInstance().getLogger(loggerId);
    //if ( logger == null ) {
    //  return -1;
    //}
    //return logger.getCurrentRecordNumber(AuditingUtils.getMessageTypeIndex(messageType));
    return AuditingManager.getInstance().getCurrentRecordNumber(
      AuditingManager.getInstance().getMerchantInfoBean(merchantId)  );
  }

  /**
   * This method sets the SMTP name.
   * @param smtpName Tne SMTP name.
   */
  public void putMailInet(String smtpName) {
    String oldValue = AuditingManager.getInstance().getBean().getMailInet();
    if ( !oldValue.equals(smtpName) && smtpName != null ) {
      AuditingManager.getInstance().getBean().setMailInet(smtpName);
      if ( ConfigurationManager.getInstance().isAutoSave() ) {
        ConfigurationManager.getInstance().save();
      }
    }
  }

  /**
   * This method sets the the performance counter sampling time.
   * @param samplingTime The sampling time.
   */
  public void putSamplingTime(int samplingTime) {
    long oldValue = AuditingManager.getInstance().getBean().getSamplingTime();
    if ( oldValue != samplingTime && samplingTime > 0 ) {
      AuditingManager.getInstance().getBean().setSamplingTime(samplingTime);
      if ( ConfigurationManager.getInstance().isAutoSave() ) {
        ConfigurationManager.getInstance().save();
      }
    }
  }

  /**
   * This method starts the particular auditing logger.
   * @param loggerId The logger id.
   * @param reason - stop reason
   */
  public void start(String loggerId, String reason) {

      /**
      * The following Added by Gang Wu:
      * bean is the AuditingListenerBean
      * It should be called before Auditing manager remove
      */
      ListeningManager.getInstance().startListener(
               ListeningUtils.AUDITING_LISTENER, loggerId, reason);
     /**
      * Add finish
      */
  }

  /**
   * This method stops the particular auditing logger.
   * @param loggerId The logger id.
   * @param reason - stop reason
   */
  public void stop(String loggerId, String reason) {
     /**
      * The following Added by Gang Wu:
      * bean is the AuditingListenerBean
      * It should be called before Auditing manager remove
      */
      ListeningManager.getInstance().stopListener(
              ListeningUtils.AUDITING_LISTENER, loggerId, reason);
     /**
      * Add finish
      */
  }

  /**
   * This method returns the auditing status.
   * @return The auditing status string.
   */
  public String obtainStatus(String loggerId) {
    return ListeningManager.getInstance().getListenerStatus(
			ListeningUtils.AUDITING_LISTENER, loggerId);
  }

  /**
   * This method reset the current number of the auditing logger database table.
   * @param loggerId The auditing status string.
   */
  public void initCurrentNumber(String jndi, String schema) {
	  /**
	   *[Gang Wu's Note: June 19, 2003] This method useless
	   */
    //AuditingManager.getInstance().initCurrentNumber(jndi, schema);
  }

  /**
   * This method remove data source from the application server.
   * (This method is used for doing test and debug.)
   * @param jndi The jndi name.
   */
  public void removeDataSource(String jndi) {
    AuditingManager.getInstance().removeDataSource(jndi);
  }

  /**
   * Obtain the start stop reason
   * @param loggerId - the the loger handler id
   */
  public String obtainStartStopReason( String loggerId )
  {
	  return ListeningManager.getInstance().getStartStopReason(
			  ListeningUtils.AUDITING_LISTENER, loggerId );
  }

  /**
   * Obtain the start stop time
   * @param loggerId - the the loger handler id
   */
  public long obtainStartStopTime(  String loggerId )
  {
	  return ListeningManager.getInstance().getStartStopTime(
			  ListeningUtils.AUDITING_LISTENER, loggerId );
  }


}