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


package  com.oncecorp.visa3d.bridge.auditing;

import java.util.Properties;
import java.util.Map;

import  com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;


/**
 * Title:        ONCE MPI Data Bridge
 * Description:  This is the performance counter class that can calculate the
 * peak and average transaction number.  This class contains a timer that can
 * fire periodically to pickup the sampling time counting number.
 * Copyright:    Copyright (c) 2002
 * Company:      Once Corporation
 * @author yge@oncecorp.com
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */
public class PerformanceCounter
        implements MessageCountable, PeriodCountable {
    private AuditingManager manager;
    private int periodNumber;
    private int peakNumber;
    //  private int                   averageNumber;
    private PeriodCounter periodCounter;
    private int totalMessageNumber = 0;
    private int totalMessageNumberAtLastFlush = 0;
    private int samplingTime;
    private long lastFlushTime = System.currentTimeMillis();
    private long peakTime = System.currentTimeMillis();

    /**
     * The default constructor.
     */
    public PerformanceCounter (AuditingManager manager) {
        this.manager = manager;
        samplingTime = manager.getBean().getSamplingTime();
    }

    /**
     * This method returns the current peak transaction number per second.
     * @return The peak transaction number per second.
     */
    public float getPeakTPS () {
        return  (float)peakNumber/samplingTime;
    }

    /**
     * This method returns the current average transaction number per second.
     * @return The average transaction number per second.
     */
    public float getAverageTPS () {
        return  (float)totalMessageNumber/(System.currentTimeMillis() - lastFlushTime)*1000;
    }

    /**
     * This method returns the counter name.
     * @return The counter name.
     */
    public String getName () {
        return  PropertiesConstants.PERFORMANCE;
    }

    /**
     * This method returns the given message type's current counting value.
     * @param name the counter name.
     * @param msgType - The message type.
     * @param msgVersion - The message version.
     * @return The current counting value.
     */
    public synchronized Map getCountingValue (String name,
            String msgType, String msgVersion ) {

        return null;
    }

    /**
     * This method counts the message based on the message properties.
     * @param - message properties
     */
    public void count( Properties props )
    {
        totalMessageNumber++;
        periodNumber++;
    }

    /**
     * This method flush the counter.
     */
    public synchronized void flush () {
        DataBridgeLoger.getLogger(this).debug("Flush the counter.");
        lastFlushTime = System.currentTimeMillis();
        totalMessageNumberAtLastFlush = totalMessageNumber;
        periodNumber = 0;
        peakNumber = 0;
        totalMessageNumber = 0;
        if (periodCounter != null) {
            periodCounter.cancel();
        }
        periodCounter = new PeriodCounter(manager.getBean().getSamplingTime()*1000,
                this);
    }

    /**
     * This method picks up the period counting data.  The method is fired by
     * the timer object.
     */
    public synchronized void pickUpPeriodData () {
        DataBridgeLoger.getLogger(this).debug("Pick up the period sample data.");
        //averageNumber = periodNumber;
        if (periodNumber > peakNumber) {
            peakTime = System.currentTimeMillis();
            peakNumber = periodNumber;
        }
        periodNumber = 0;
        DataBridgeLoger.getLogger(this).debug("Counter: [Perfromance." + PropertiesConstants.PEAK_NUMBER
                + "] = " + peakNumber);
    }

    /**
     * This method sets the sampling time for getting the peak number.
     * @param samplingTime The sampling time (in second).
     */
    public synchronized void setSamplingTime (int samplingTime) {
        this.samplingTime = samplingTime;
    }

    /**
     * This method returns the sampling time.
     * @return The sampling time (in second).
     */
    public int getSamplingTime () {
        return  samplingTime;
    }

    /**
     * This method returns the last flush time.
     * @return The last flush time.
     */
    public long getLastFlushTime () {
        return  lastFlushTime;
    }

    /**
     * This method returns the peak time.
     * @return The peak time.
     */
    public long getPeakTime () {
        return  peakTime;
    }

    /**
     * This method returns the total message number form the last flush.
     * @return The total message number.
     */
    public int getTotalMessageNumber () {
        return  totalMessageNumber;
    }

    /**
     * This method returns the total message number at the last flush time.
     * @return The total message number at the last flush time.
     */
    public int getTotalMessageNumberAtLastFlush () {
        return  totalMessageNumberAtLastFlush;
    }
}



