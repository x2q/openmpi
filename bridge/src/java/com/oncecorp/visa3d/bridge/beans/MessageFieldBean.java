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
 * <p>Title: MessageFieldBean</p>
 * <p>Description: Holds message fields related issues </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */

public class MessageFieldBean implements Cloneable, Serializable
{
    protected String name;
    protected boolean encryption = false;
    protected String format;
    protected boolean selected = true;

    /**
     * Default constructor
     */
    public MessageFieldBean()
    {
    }

    /**
     * Constructor with field name, encryption attribute and format attribute
     * @param name - field name
     * @param encryption - encryption flag
     * @param fmt - format string
     */
    public MessageFieldBean( String name, String encryption, String fmt)
    {
        setName(name);
        setEncryption(encryption);
        setFormat(fmt);
    }

    /**
    *
    * @return the String value of name.
    */
    public String getName(){
        return name;
    }

    /**
    *
    * @param aName - the new value for name
    */
    public void setName(String aName){
        name = aName;
    }


    /**
    *
    * @return true if encryption is set to true.
    */
    public boolean isEncryption(){
        return encryption;
    }

    /**
    *
    * @param aEncryption - the new value for encryption
    */
    public void setEncryption(boolean aEncryption){
        encryption = aEncryption;
    }


    /**
    *
    * @param aEncryption - the new value for encryption
    */
    public void setEncryption(String sEncryption){
        encryption = Boolean.valueOf( sEncryption ).booleanValue();
    }


    /**
    *
    * @return the String value of format.
    */
    public String getFormat(){
        return format;
    }

    /**
    *
    * @param aFormat - the new value for format
    */
    public void setFormat(String aFormat){
        format = aFormat;
    }


    /**
     * Wheather a given obejct equals to the current message field instance
     * @param obj - a given object
     * @return - true or false
     */
    public boolean equals( Object obj )
    {
        if ( obj == null )
            return false;

        if ( obj instanceof MessageFieldBean )
        {
            MessageFieldBean mf = (MessageFieldBean) obj;

            if ( name == null || mf.getName() == null )
                return false;

            return name.equals( mf.getName() );
        }

        return false;
    }

    /**
    *
    * @return true if selected is set to true.
    */
    public boolean isSelected(){
        return selected;
    }

    /**
    *
    * @param aSelected - the new value for selected
    */
    public void setSelected(boolean aSelected){
        selected = aSelected;
    }

    /**
     * Clone this message field bean
     * @return - cloned message field bean
     */
    public Object clone()
    {
        MessageFieldBean mfb = new MessageFieldBean( this.getName(),
                "" + this.isEncryption(), this.getFormat() );
        mfb.setSelected( this.isSelected() );

        return mfb;
    }

}