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

package com.oncecorp.visa3d.bridge.listening;

import org.apache.log4j.Logger;
import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;

import java.util.Timer;
import java.util.TimerTask;

/**
 * <p>Title: TimerService </p>
 * <p>Description: Provide time service to all timer listener, which is mainly
 * used to notify the plugin listener to sweep out the unfinished waiting transaction.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */

public class TimerService
{
    private long interval = 3 * 60;
    private boolean running = false;
    private Timer   m_timer = null;

    private static Logger m_logger = DataBridgeLoger.getLogger(
            TimerService.class.getName() );

    private static TimerService m_instance = new TimerService();

    /**
     * Default constructor
     */
    private TimerService()
    {
        super();
    }

    /**
     * The Singleton pattern calling method to get the service instance
     * @return - timer service instance
     */
    public static TimerService getInstance()
    {
        return m_instance;
    }

    /**
    *
    * @return - the long value of interval in minute.
    */
    synchronized public long getInterval(){
        return interval;
    }

    /**
    *
    * @param aInterval - the new value for interval in minute
    */
    synchronized public void setInterval(long aInterval){
        interval = aInterval ;
        if ( m_timer != null )
        {
            stopService();
            startService();
        }
    }


    /**
    *
    * @return - running status.
    */
    public boolean isRunning(){
        return running;
    }

    /**
     * Start the timer service
     */
    public void startService()
    {
        m_logger.debug("Enter start timer service.");
        if ( !isRunning() )
        {
            try {
                m_timer = new Timer( true );

                m_timer.scheduleAtFixedRate( new PluginTimerTask(),
                        getInterval()  * 60 * 1000,
                        getInterval()  * 60 * 1000);
            } catch ( Exception e )
            {
                m_logger.warn("Exception when reschedule the timer.", e);
            }
            running = true;
        }

        m_logger.debug("Exit start timer service.");

    }

    /**
     * Stop the timer service
     */
    public void stopService()
    {
        m_logger.debug("Enter stop timer service.");
        if ( isRunning() )
        {
            m_timer.cancel();
            m_timer = null;
            running = false;
        }

        m_logger.debug("Exit stop timer service.");


    }

    /**
     *
     * <p>Title: PluginTimerTask</p>
     * <p>Description: Inner class implement Timer Task</p>
     * <p>Copyright: Copyright (c) ONCE Corporation 2002</p>
     * <p>Company: ONCE Corporation</p>
     * @author Gang Wu ( gwu@oncecorp.com )
     * @version 1.0
     */
    static class PluginTimerTask extends TimerTask
    {
        /**
         * Default constructor
         */
        public PluginTimerTask()
        {
            super();
        }

        /**
         * This metod cancel the timer and this object.
         */
        public boolean cancel()
        {
            return super.cancel();
        }

        /**
         * Thread run method
         */
        public void run()
        {
            m_logger.debug("Execute timer service.");
            ListeningManager.getInstance().notifyTimerService();
            m_logger.debug("End execute timer service.");
        }
    }
}