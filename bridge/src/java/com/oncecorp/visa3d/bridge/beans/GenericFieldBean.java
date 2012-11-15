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

package com.oncecorp.visa3d.bridge.beans;

import java.io.Serializable;

/**
 * <p>Title: GenericFieldBean</p>
 * <p>Description: Holds message fields from FieldDefinitionBean and MessageDefinitionBean </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */

public class GenericFieldBean extends MessageFieldBean implements Cloneable, Serializable
{
    private boolean mandatory = false;
    private boolean mustEncryption = false;

    /**
     * Default constructor
     */
    public GenericFieldBean()
    {
    }

    /**
    *
    * @return true if mandatory is set to true.
    */
    public boolean isMandatory(){
        return mandatory;
    }

    /**
    *
    * @param aMandatory - the new value for mandatory
    */
    public void setMandatory(boolean aMandatory){
        mandatory = aMandatory;
    }


    /**
    *
    * @return true if mustEncryption is set to true.
    */
    public boolean isMustEncryption(){
        return mustEncryption;
    }

    /**
    *
    * @param aMustEncryption - the new value for mustEncryption
    */
    public void setMustEncryption(boolean aMustEncryption){
        mustEncryption = aMustEncryption;
    }

    /**
     * Wheather a given obejct equals to the current message field instance
     * @param obj - a given object
     * @return - true or false
     */
    public boolean equals( Object obj )
    {
        return super.equals( obj );
    }

    /**
     * Clone generic field bean
     * @return - cloned generic field bean
     */
    public Object clone()
    {
        GenericFieldBean gfb = new GenericFieldBean();
        gfb.setMandatory( this.isMandatory() );
        gfb.setMustEncryption( this.isMustEncryption() );

        gfb.setEncryption( this.isEncryption() );
        gfb.setFormat( this.getFormat() );
        gfb.setName( this.getName() );
        gfb.setSelected( this.isSelected() );

        return gfb;

    }
}