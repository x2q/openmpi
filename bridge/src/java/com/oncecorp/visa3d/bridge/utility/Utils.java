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

package com.oncecorp.visa3d.bridge.utility;

/**
 * <p>Title: Utils</p>
 * <p>Description: Help functions</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com )
 * @version 1.0
 */
public class Utils
{

	/**
	 * Define listening status
	 */
	public final static String LISTENTING_START          = "start";
	public final static String LISTENING_STOP            = "stop";
	public final static String LISTENING_EXCEPTION       = "exception";

	/**
	 * Define start stop reason
	 */
	public final static int         LISTENING_START_STOP_BY_SERVER =        0;
	public final static int         LISTENING_INITIAL_ERROR      =          1;
	public final static int         PLUGIN_CLASS_NOT_INIT         =         2;
	public final static int         CONNECTION_FACTORY_NOT_FOUND  =          3;
	public final static int         JMS_SETUP_EXCEPTION           =          4;
	public final static int         JMS_START_EXCEPTION           =          5;
	public final static int         RUNNING_EXCEPTION           =            6;

	public final static String         REASON_SUFFIX                 =         "___";

	/**
	 * Default Constructor
	 */
    public Utils()
    {
    }

    /**
     * Return empty string if the input string is null
     * @param str - input string
     * @return - converted string
     */
    public static String convertNullString2Empty( Object str )
    {
        if ( str == null )
            return "";
        else
            return str.toString();
    }

	/**
	 * Retrive the id part of reason message
	 * @param reason - start stop reason
	 * @return - the reason id
	 */
	public static int getStartStopID ( String reason )
	{
		if ( reason == null || reason.indexOf( REASON_SUFFIX ) == -1 )
	         return LISTENING_START_STOP_BY_SERVER;
        else
		{
			try {
				String id = reason.substring( 0, reason.indexOf(REASON_SUFFIX ) );
				return Integer.parseInt( id );
			} catch ( Exception e )
			{
				return LISTENING_START_STOP_BY_SERVER;
			}
		}
	}

	/**
	 * Retrive the message of the reason message which is a combo message constructed
	 * by reason id and message body
	 * @param reason - reason message
	 * @return - reason message body
	 */
	public static String getStartStopMsg( String reason )
	{
		if ( reason == null || reason.indexOf( REASON_SUFFIX ) == -1 )
			 return reason;
		else
		{
			 return reason.substring(
					 reason.indexOf(REASON_SUFFIX ) + REASON_SUFFIX.length() );
		}

	}
}