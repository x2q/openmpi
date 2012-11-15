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

import java.text.MessageFormat;

/**
 * <p>Title: MaskToken</p>
 * <p>Description: Holds parsed result of message field format </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */


public class MaskToken
{

    /**
     * The following defines the Java message format type.
     */
    public final static int STRING_FMT     =    1;
    public final static int NUMBER_FMT     =    2;
    public final static int DATE_FMT       =    3;

    protected boolean       maskFlag = true;
    protected MessageFormat javaMessageFormat = null;
    protected StringBuffer  parsedArray = null;
    protected int           wildCharPlace = -1;
    protected int           messageObjectType = -1;

    /**
     * Java style message format constructor
     * @param fmt - Java Message format
     * @param type - the java message format type
     */
    public MaskToken( MessageFormat fmt, int type )
    {
        setJavaMessageFormat( fmt );
        setMessageObjectType( type );
        setMaskFlag( false );
    }

    /**
     * Mask string token
     * @param sbuff - string token buffer
     * @param wplace - wild char place
     */
    public MaskToken( StringBuffer sbuff, int wplace )
    {
        setMaskFlag( true );
        setParsedArray( sbuff );
        setWildCharPlace( wplace );
    }

    /**
    *
    * @return the MessageFormat value of javaMessageFormat.
    */
    public MessageFormat getJavaMessageFormat(){
        return javaMessageFormat;
    }

    /**
    *
    * @param aJavaMessageFormat - the new value for javaMessageFormat
    */
    public void setJavaMessageFormat(MessageFormat aJavaMessageFormat){
        javaMessageFormat = aJavaMessageFormat;
    }


    /**
    *
    * @return the StringBuffer value of parsedArray.
    */
    public StringBuffer getParsedArray(){
        return parsedArray;
    }

    /**
    *
    * @param aParsedArray - the new value for parsedArray
    */
    public void setParsedArray(StringBuffer aParsedArray){
        parsedArray = aParsedArray;
    }


    /**
    *
    * @return the int value of wildCharPlace.
    */
    public int getWildCharPlace(){
        return wildCharPlace;
    }

    /**
    *
    * @param aWildCharPlace - the new value for wildCharPlace
    */
    public void setWildCharPlace(int aWildCharPlace){
        wildCharPlace = aWildCharPlace;
    }

    /**
    *
    * @return the int value of messageObjectType.
    */
    public int getMessageObjectType(){
        return messageObjectType;
    }

    /**
    *
    * @param aMessageObjectType - the new value for messageObjectType
    */
    public void setMessageObjectType(int aMessageObjectType){
        messageObjectType = aMessageObjectType;
    }


    /**
    *
    * @return true if maskFlag is set to true.
    */
    public boolean isMaskFlag(){
        return maskFlag;
    }

    /**
    *
    * @param aMaskFlag - the new value for maskFlag
    */
    public void setMaskFlag(boolean aMaskFlag){
        maskFlag = aMaskFlag;
    }


}