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

package com.oncecorp.visa3d.mpi.messaging;

import com.oncecorp.visa3d.mpi.logging.MPILogger;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Random;
import org.apache.log4j.Logger;

/**
 * Description: This class is reponsible to generate Unique ID for Visa Massage usage    
 *              and Transaction id etc.
 *     
 * @version 0.1 July 25, 2002
 * @author	Jun Shi
 */
public class ID_Generator{

    // Local Log4J logger
	private static Logger logger = MPILogger.getLogger(ID_Generator.class.getName());
	
    /** 
     * The Default UniqueId is 12 bytes, which is structured like following:
     * id (12 bytes) = "SystemTime" (last-4 bytes) +"Obj HashCode (last-4 bytes)"
     *                  +"SecureRandom number" (First-4 bytes)
     */    
    public static String getUniqueId() {
    	// var init
    	String id = null;
 	    SecureRandom seeder = null;

        try{
             // get the hashcode for this object.
           int hashCode = System.identityHashCode(new Random());  
        
           // convert to String and pick the last-4 digits
           String str = Integer.toString(hashCode);
           String thisHashCode = str.substring(str.length()-4);

           // Load up the randomizer and get first-4 digits
           seeder = new SecureRandom();
           int node = seeder.nextInt();
           String random = Integer.toString(node).substring(0,4);

           // get current system time in milliseconds
           long timeNow = System.currentTimeMillis();

           // convert to String and pick the last-4 digits
           String time = Long.toString(timeNow);         
           String sysTime = time.substring(time.length()-4); 
    
           // produce id 
           id = sysTime + thisHashCode + random;		
           logger.debug("New ID: " + id);      
        
        } catch (Exception e) {
   	      e.printStackTrace(System.err);
   	      logger.error("Msg - id cannot be generated", e);
   	    }
      return id;     
    }

    /** 
     * The specified length UniqueId: 
     * id length should be within the scope from 13 to 32 bytes
     */ 
    public static String getUniqueId(int aLength) throws MessagingException {
         // init id
         String id = null;
         
         if(aLength<=12 || aLength>=33){
             throw new MessagingException(
                         "The specified id length is invalid: ["+aLength+"]");         
         }       

         // get default id first: 12 bytes 
         id = getUniqueId();

         // add the rest bytes to meet specified length
         try {
			// make a random string
			long seed = Calendar.getInstance().getTime().getTime();
			Random random = new Random(seed);
			String ranId = Long.toString(Math.abs(random.nextLong())); // 16-19 bytes
            
            // make another string
            long timeNow = System.currentTimeMillis();
            String time = Long.toString(timeNow); // 13 bytes         
            String sysTime = time.substring(time.length()-8); // get last-8 bytes 
            
            // make a big enough string to avoid "index Out Of Range" Exception
            ranId = ranId+sysTime;
            
            // produce id
            id = id + ranId.substring(0, (aLength-12));
			logger.debug("New ID: " + id);

		 } catch (Exception e) {
			e.printStackTrace(System.err);
			logger.error("Msg - id cannot be generated.", e);
    	 }
       return id;
    }

}

