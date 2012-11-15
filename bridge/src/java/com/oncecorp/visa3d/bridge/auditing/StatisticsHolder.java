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

import  java.util.Collections;
import  java.util.HashMap;
import  java.util.Iterator;
import  java.util.Map;
import  java.util.Properties;



/**
 * <p>Title: ONCE MPI Data Bridge</p>
 * <p>Description: This is the counter holder class that holds the single
 * counter and another counter holder.  If The method count() or flush() of this
 * class is called, all the directs or indirect counters held by this holder will
 * be called.
 * same method.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Once Corporation</p>
 * @author yge@oncecorp.com
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */
public class StatisticsHolder
        implements MessageCountable {
    private Map counters;
    private String name;

    /**
     * The default constructor.
     */
    //  public StatisticsHolder() {
    //    this("");
    //  }
    /**
     * The constructor.
     * @param name the name of the holder.
     */
    public StatisticsHolder (String name) {
        this.name = name;
        counters = Collections.synchronizedMap(new HashMap());
    }

    /**
     * This method adds the single counter or the counter holder to the instance.
     * @param counterName The short name counter or the counter holder.
     * @param counter The counter or counter holder to be added.
     */
    public void add (MessageCountable counter) {
        counters.put(counter.getName(), counter);
    }

    /**
     * This method returns the name of the counter.
     * @return The name of the counter.
     */
    public String getName () {
        return  this.name;
    }

    /**
     *
     * @return - return all counters
     */
    public Map getCounters()
    {
        return counters;
    }

    /**
     * This method check if the counter holder contains the counter with given name.
     * @param counterName The counter name.
     * @return The check result.
     */
    public boolean containsSubCounter (String counterName) {
        return  counters.containsKey(counterName);
    }

    /**
     * This method returns the child counter or counter holder which has the given
     * name.
     * @param counterName The name of the counter of counter holder.
     * @return The sub-counter or sub-counter holder of this holder.
     */
    public MessageCountable getSubCounter (String counterName) {
        int dotIndex = counterName.indexOf('.');
        if (dotIndex == -1) {
            return  (MessageCountable)counters.get(counterName);
        }
        else {
            return  (MessageCountable)counters.get(counterName.substring(0,
                    dotIndex));
        }
    }

    /**
     * This method returns the given message type's current counting value.
     * @param conterFullName - the counter name.
     * @param msgType - The message type.
     * @param msgVersion - The message version.
     * @return The current counting value.
     */
    public Map getCountingValue (String conterFullName,
                                 String msgType, String msgVersion) {
        int dotIndex = conterFullName.indexOf('.');
        String subName;
        if (dotIndex != -1) {
            subName = conterFullName.substring(dotIndex + 1);
            if (getSubCounter(subName) == null) {
                return  Collections.synchronizedMap( new HashMap() );
            }
            return  getSubCounter(subName).getCountingValue(subName, msgType,
                    msgVersion);
        }
        return  Collections.synchronizedMap( new HashMap() );
    }

    /**
     * This method counts the message based on the message properties.
     * @param - message properties
     */
    public void count (Properties props) {
        for (Iterator i = counters.values().iterator(); i.hasNext();) {
            ((MessageCountable)i.next()).count(props);
        }
    }

    /**
     * This method flushes the all held counters.
     */
    public void flush () {
        for (Iterator i = counters.values().iterator(); i.hasNext();) {
            ((MessageCountable)i.next()).flush();
        }
    }
}



