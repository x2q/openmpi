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

import  com.oncecorp.visa3d.bridge.beans.AuditingListenerBean;
import  com.oncecorp.visa3d.bridge.beans.AuditingServiceBean;
import  com.oncecorp.visa3d.bridge.beans.MerchantInfoBean;
import  com.oncecorp.visa3d.bridge.beans.BeansHelper;
import  com.oncecorp.visa3d.bridge.monitoring.AuditingPerformanceMBean;

import  java.util.Iterator;
import  java.util.List;
import  java.util.Map;
import  java.util.TreeMap;
import  java.util.TreeSet;


/**
 * <p>Title: ONCE MPI Data Bridge</p>
 * <p>Description: This is the MBean class that is used for populating the
 * statistics counter result.  The counter flush can also through this MBean
 * class.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Once Corporation</p>
 * @author yge@oncecorp.com
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */
public class AuditingPerformance
        implements AuditingPerformanceMBean, PropertiesConstants {

    /**
     * The default constructor.
     */
    public AuditingPerformance () {
    }

    /**
     * This method returns the message type counting results map.
     * @return The message type counting results map.
     */
    public Map obtainMessageTypeCountMap () {
        Map map = new TreeMap();
        List msgTypes = AuditingUtils.getMessageMappingList();

        if ( msgTypes == null )
            return  map;
        String key, type, version;
        for ( Iterator lt = msgTypes.iterator(); lt.hasNext(); )
        {
            key = (String) lt.next();
            type = BeansHelper.extractTypeFromKey(key);
            version = BeansHelper.extractVersionFromKey(key);
            map.put(key,
                    AuditingManager.getInstance().getCountingValue(ALL,
                        type, version ) );
        }
        return  map;
    }

    /**
     * This method returns the message status counting results map.
     * @return The message status counting results map.
     */
    public Map obtainMessageStatusCountMap () {
        //System.out.println("It's status");
        Map map = new TreeMap();
        for (int i = 0; i < STATUS_CATEGORY_ID_LIST.length; i++) {
            map.put(STATUS_CATEGORY_ID_LIST[i],
                    AuditingManager.getInstance().getCountingValue(
                    STATUS + "." + STATUS_CATEGORY_ID_LIST[i].getStatus(),
                    STATUS_CATEGORY_ID_LIST[i].getMsgType(), ""));
        }
        return  map;
    }

    /**
     * This method returns the given merchant counting results map.
     * @return The merchant counting results map.
     */
    public Map obtainMerchantCountMap () {
        Map map = new TreeMap();

        StatisticsHolder main =
                AuditingManager.getInstance().getMainStatisticsHolder();
        MerchantStatisticsHolder msh = (MerchantStatisticsHolder)
                main.getSubCounter( MERCHANT );
        Map counters = msh.getCounters();

        String key;
        String[] list = merchantIdList();
        if ( list != null && list.length > 0 )
        {
            for ( int i = 0; i < list.length; i++ )
            {
                key = list[i];
                if ( !counters.containsKey( key ) )
                    map.put( key, AuditingUtils.getProtocolCounter() );
            }
        }

        MerchantCounter mc;
        for ( Iterator lt = counters.keySet().iterator(); lt.hasNext(); )
        {
            key = (String)lt.next();
            mc = (MerchantCounter)counters.get( key );
            map.put( key, mc.getCountingValue( key, "", "" ) );
        }
        return  map;
    }

    /**
     * This method return the peak number of the message processed per second
     * @return The peak number.
     */
    public float obtainPeakTPS () {
        return  AuditingManager.getInstance().getPerformanceCounter().getPeakTPS();
    }

    /**
     * This method returns the average number of the message processed per second
     * @return The average number.
     */
    public float obtainAverageTPS () {
        return  AuditingManager.getInstance().getPerformanceCounter().getAverageTPS();
    }

    /**
     * This method set all counter indicators to "zero".
     */
    public void flush () {
        AuditingManager.getInstance().flushCounters();
    }

    /**
     * This method returns the current supporting merchant id list array.
     * @return The current supporting merchant id list array.
     */
    public String[] merchantIdList () {
        MerchantInfo merchantInfoMBean = new MerchantInfo();
        List infoBeanList = merchantInfoMBean.query(null);
        if (infoBeanList == null || infoBeanList.isEmpty()) {
            return  new String[0];
        }
        String[] result = new String[infoBeanList.size()];
        for (int i = 0; i < infoBeanList.size(); i++) {
            MerchantInfoBean bean = (MerchantInfoBean)infoBeanList.get(i);
            result[i] = bean.getId();
        }
        return  result;
    }

    /**
     * This mehtod returns the Data Bridge start time.
     * @return The Data Bridge start time.
     */
    public long obtainStartTime () {
        return  AuditingManager.getInstance().getStartTime();
    }

    /**
     * This method returns the performance counter sampling time.
     * @return The sampling time.
     */
    public int obtainSamplingTime () {
        return  AuditingManager.getInstance().getPerformanceCounter().getSamplingTime();
    }

    /**
     * This method sets the performance counter sampling time.
     * @param samplingTime The sampling time.
     */
    public void putSamplingTime (int samplingTime) {
        AuditingManager.getInstance().getPerformanceCounter().setSamplingTime(samplingTime);
    }

    /**
     * This method retrns the total message number from the last flush.
     * @return The total message number.
     */
    public int obtainTotalMessageNumber () {
        return  AuditingManager.getInstance().getPerformanceCounter().getTotalMessageNumber();
    }

    /**
     * This method return the last flush time.
     * @return The last flush time.
     */
    public long obtainLastFlushTime () {
        return  AuditingManager.getInstance().getPerformanceCounter().getLastFlushTime();
    }

    /**
     * This method returns the peak time.
     * @return The peak time.
     */
    public long obtainPeakTime () {
        return  AuditingManager.getInstance().getPerformanceCounter().getPeakTime();
    }

    /**
     * This method returns the total count number at the last flush.
     * @return The total count number at the last flush.
     */
    public int totalMessageNumberAtLastFlush () {
        return  AuditingManager.getInstance().getPerformanceCounter().getTotalMessageNumberAtLastFlush();
    }
}



