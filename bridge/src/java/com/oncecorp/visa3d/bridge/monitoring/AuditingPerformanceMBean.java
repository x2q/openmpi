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

import java.util.Map;
/**
 * <p>Title: ONCE MPI Data Bridge</p>
 * <p>Description: This is the MBean interface for accessing the statistics
 * counter numbers.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Once Corporation</p>
 * @author yge@oncecorp.com
 * @version 1.0
 */

public interface AuditingPerformanceMBean {

  /**
   * This method returns the message type counting results map.
   * @return The message type counting results map.
   */
  public Map obtainMessageTypeCountMap();

  /**
   * This method returns the message status counting results map.
   * @return The message status counting results map.
   */
  public Map obtainMessageStatusCountMap();

  /**
   * This method returns the given merchant counting results map.
   * @return The merchant counting results map.
   */
  public Map obtainMerchantCountMap( );

  /**
   * This method return the peak number of the message processed per second
   * @return The peak number.
   */
  public float obtainPeakTPS();

  /**
   * This method returns the average number of the message processed per second
   * @return The average number.
   */
  public float obtainAverageTPS();

  /**
   * This method set all counter indicators to "zero".
   */
  public void flush();

  /**
   * This method returns the current supporting merchant id list array.
   * @return The current supporting merchant id list array.
   */
  public String[] merchantIdList();

  /**
   * This mehtod returns the Data Bridge start time.
   * @return The Data Bridge start time.
   */
  public long obtainStartTime();
  /**
   * This method returns the performance counter sampling time.
   * @return The sampling time.
   */
  public int obtainSamplingTime();

  /**
   * This method sets the performance counter sampling time.
   * @param samplingTime The sampling time.
   */
  public void putSamplingTime(int samplingTime);

  /**
   * This method retrns the total message number from the last flush.
   * @return The total message number.
   */
  public int obtainTotalMessageNumber();

  /**
   * This method return the last flush time.
   * @return The last flush time.
   */
  public long obtainLastFlushTime();

  /**
   * This method returns the peak time.
   * @return The peak time.
   */
  public long obtainPeakTime();

  /**
   * This method returns the total count number at the last flush.
   * @return The total count number at the last flush.
   */
  public int totalMessageNumberAtLastFlush();
}