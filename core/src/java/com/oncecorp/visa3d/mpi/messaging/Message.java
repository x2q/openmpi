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

import com.oncecorp.visa3d.mpi.utility.XMLSerializable;

/**
 * Description: This super class define an abstract method, which has 
 * to be implemented by each individual Message class (Deprecated). 
 * Message "id" "type" "version" are common fields of each message. 
 * Also each Message has to implement../utility/[XMLSerializable.toXML] 
 * method
 *  
 * @version 0.1 Aug 1, 2002
 * @author	Jun Shi
 */

public abstract class Message implements XMLSerializable {

    // Message type
    protected String type;

    // Message id
    protected String id;

    // Message version
    protected String version;

        /** Getter and Setter methods */
	public String getType() 
	{
	   return type;
	}
	final public void setType(String type)
	{
	   this.type = type;
	}
	public String getId() 
	{
	   return id;
	}
	public void setId(String id) throws MessagingException 
	{
       this.id = id;
	}
	public String getVersion() 
	{
	   return version;
	}
	final public void setVersion(String version)
	{
      this.version = version;
	}

    // validate message fields dependant relationship     
    public boolean validate() throws MessagingException
    {
      return true;     
    }

}

