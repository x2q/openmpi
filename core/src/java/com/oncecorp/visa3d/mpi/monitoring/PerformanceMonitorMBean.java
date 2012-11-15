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

package com.oncecorp.visa3d.mpi.monitoring;

import java.util.Map;

/**
 * Class description
 *
 * @author Jun Shi
 * @version 1.0 3-Dec-02
 */
public interface PerformanceMonitorMBean {

	/**
	 * Returns the statistic per message type.
	 * @return The map of the counters, key is the type___version,
     * values are the counter for different protocol.
	 */
	public Map optMsgTypeStatistic();

    /**
     * Returns the statistic per status.
     * @return The map of the counters, key is the status___type,
     * values are the counter for different protocol.
     */
	public Map optMsgStatusStatistic();

    /**
     * Returns the statistic per merchant.
     * @return The map of the counters, key is the merchant id,
     * values are the counter for different protocol.
     */
	public Map optMsgMerchantIDStatistic();

	/**
	 * Flush total counter
	 */
	public void optFlush();

	/**
	 * Returns the averageTPS.
	 * @return float
	 */
	public float getAverageTPS();

	/**
	 * Returns the peakTPS.
	 * @return float
	 */
	public float getPeakTPS();

	/**
	 * Returns the samplingTime.
	 * @return long
	 */
	public long getSamplingTime();

	/**
	 * Set samplingTime
	 * @param time The samplingTime
	 */
	public void setSamplingTime(long time);

	/**
	 * Returns the totalCounter.
	 * @return long
	 */
	public long getTotalCounter();

	/**
	 * Returns the lastFlushTime.
	 * @return long
	 */
	public long getLastFlushTime();

	/**
	 * Returns the statupTime.
	 * @return long
	 */
	public long getStatupTime();

	/**
	 * Returns the peakTime
	 * @return long
	 */
	public long getPeakTime();

	/**
	 * Return last flush counter
	 * @return long
	 */
	public long getLastFlushCounter();


}
