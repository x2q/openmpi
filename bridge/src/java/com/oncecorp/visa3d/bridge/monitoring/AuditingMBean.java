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
 * <p>Title: ONCE MPI Data Bridge</p>
 * <p>Description: This is the MBean interface for dynamically configuring the
 * auditing service.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Once Corporation</p>
 * @author yge@oncecorp.com
 * @version 1.0
 */

public interface AuditingMBean {

  /**
   * This method returns the auditing server xml string.
   * @return The auditing server xml string.
   */
  public String obtainAuditingServerXml();
  /**
   * This method sets the auditing server xml string.
   * @param xml The auditing server xml string.
   */
  public void putAuditingServerXml(String xml);

  /**
   * This method add new auditing logger object to the auditing service.
   * @param xml The xml string that contains the new logger data.
   */
  public void addAuditingLogger(String xml);
  /**
   * This method removes a auditing logger from the auditing server.
   * @param loggerId The logger id.
   */
  public void removeAuditingLogger(String loggerId);

  /**
   * This method updates the existed logger.
   * @param xml The xml that contains the logger data.
   */
  public void updateAuditingLogger(String xml);

  /**
   * This method returns the particluar logger's mail subject text.
   * @param loggerId The logger id.
   * @return The subject text.
   */
  public String obtainSubjectText(String loggerId);
  /**
   * This method sets the the particular logger's subject text.
   * @param loggerId The logger id.
   * @param text The subject text.
   */
  public void putSubjectText(String loggerId, String text);

  /**
   * This method returns the particular logger's mail body template text.
   * @param loggerId The logger id.
   * @return The body template text.
   */
  public String obtainBodyTemplateText(String loggerId);
  /**
   * This method sets the particular logger's mail body template text.
   * @param loggerId loggerId The logger id.
   * @param text The body template text.
   */
  public void putBodyTemplateText(String loggerId, String text);

  /**
   * This method returns the current record number of the particular logger and
   * particular message type (table).
   * @param loggerId The logger id.
   * @return The current record number in the database table.
   */
  public int obtainCurrentNumber(String merchantId);

  /**
   * This method sets the SMTP name.
   * @param smtpName Tne SMTP name.
   */
  public void putMailInet(String smtpName);

  /**
   * This method sets the the performance counter sampling time.
   * @param samplingTime The sampling time.
   */
  public void putSamplingTime(int samplingTime);

  /**
   * This method starts the particular auditing logger.
   * @param loggerId The logger id.
   * @param reason - stop reason
   */
  public void start(String loggerId, String reason);

  /**
   * This method stops the particular auditing logger.
   * @param loggerId The logger id.
   * @param reason - stop reason
   */
  public void stop(String loggerId, String reason);

  /**
   * This method returns the auditing status string.
   * @return The auditing status string.
   */
  public String obtainStatus(String loggerId);

  /**
   * This method reset the current number of the auditing logger database table.
   * @param loggerId The auditing status string.
   */
  public void initCurrentNumber(String jndi, String schema);

  /**
   * This method remove data source from the application server.
   * (This method is used for doing test and debug.)
   * @param jndi The jndi name.
   */
  public void removeDataSource(String jndi);

  /**
   * Obtain the start stop reason
   * @param loggerId - the loger handler id
   */
  public String obtainStartStopReason(  String loggerId );

  /**
   * Obtain the start stop time
   * @param loggerId - the the loger handler id
   */
  public long obtainStartStopTime( String loggerId );

}
