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
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.oncecorp.visa3d.bridge.utility.ConfigureConstants;
/**
 * <p>Title: AuditingServiceBean</p>
 * <p>Description: Hold the auditing service issues</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com ), Yuan Ge ( yge@oncecorp.com )
 * @version 1.0
 */

public class AuditingServiceBean implements Serializable, ConfigureConstants {
    private int             samplingTime;
    private String          merchantJNDI;
    private String          merchantSchema;
    private boolean         mailAuth;
    private String          mailUsername;
    private String          mailPasswd;
    private String          mailInet;

    private Map listeners = Collections.synchronizedMap( new TreeMap() );

    /**
     * Default constructor
     */
    public AuditingServiceBean()
    {
    }

    /**
     * This method returns the sampling time value (second).
     * @return The sampling time value.
     */
    public int getSamplingTime() {
      return this.samplingTime;
    }

    /**
     * This method sets the sampling time value (second).
     * @param samplingTime The sampling time value.
     */
    public void setSamplingTime(int samplingTime) {
      this.samplingTime = samplingTime;
    }

    /**
     * This method returns the mail authority flag.
     * @return The mail authority flag.
     */
    public boolean isMailAuth() {
      return this.mailAuth;
    }

    /**
     * This method sets the mail authority flag.
     * @param mailAuth The mail authority flag.
     */
    public void setMailAuth(boolean mailAuth) {
      this.mailAuth = mailAuth;
    }

    /**
     * This method returns the mail server's user name.
     * @return The mail server's user name.
     */
    public String getMailUsername() {
      return mailUsername == null ? "" : mailUsername;
    }

    /**
     * This method sets the mail server's user name.
     * @param mailUsername The mail server's user name.
     */
    public void setMailUsername(String mailUsername) {
      this.mailUsername = mailUsername;
    }

    /**
     * This method returns the mail server's password.
     * @return The mail server's password.
     */
    public String getMailPasswd() {
      return mailPasswd == null ? "" : mailPasswd;
    }

    /**
     * This method sets the mail server's password.
     * @param mailPasswd The mail server's password.
     */
    public void setMailPasswd(String mailPasswd) {
      this.mailPasswd = mailPasswd;
    }

    /**
     * This method returns the smpt server's name.
     * @return The smpt server's name.
     */
    public String getMailInet() {
      return mailInet == null ? "" : mailInet;
    }

    /**
     * This method sets the smpt server's name.
     * @param mailInet The smpt server's name.
     */
    public void setMailInet(String mailInet) {
      this.mailInet = mailInet;
    }

    /**
     * This method returns the merchant jndi name.
     * @return The merchant jndi name.
     */
    public String getMerchantJNDI() {
      return merchantJNDI;
    }

    /**
     * This method sets the merchant jndi name.
     * @param merchantJNDI The merchant jndi name.
     */
    public void setMerchantJNDI(String merchantJNDI) {
      this.merchantJNDI = merchantJNDI;
    }

    /**
     * This mehtod returns the merchant schema name.
     * @return the merchant schema name.
     */
    public String getMerchantSchema() {
      return merchantSchema == null ? "" : merchantSchema;
    }

    /**
     * This methos sets the merchant schema name.
     * @param merchantSchema The merchant schema name.
     */
    public void setMerchantSchema(String merchantSchema) {
      this.merchantSchema = merchantSchema;
    }

    /**
     * Add one auditing listener
     * @param listener - auditing listener
     */
    public void addListener( AuditingListenerBean listener )
    {
        if ( listener != null )
            listeners.put( listener.getId(), listener );
    }

    /**
     * Remove one auditing listener
     * @param listener - auditing listener
     */
    public void removeListener(  AuditingListenerBean listener )
    {
        if ( listener != null )
            listeners.remove( listener.getId() );
    }

    /**
     * Remove one auditing listener by listener identify
     * @param listener - listener identify
     */
    public void removeListener(  String listener )
    {
        if ( listener != null )
            listeners.remove( listener );
    }

    /**
     * This method returns the auditing listener bean.
     * @param listener The listener's id.
     * @return The listener bean.
     */
    public AuditingListenerBean getListener(String listener) {
        if ( listener != null ) {
            return (AuditingListenerBean)listeners.get( listener );
        }
        else {
            return null;
        }
    }

    /**
     * Configure from XML string
     * @param xml - Configuration XML string
     */
    public void fromXml( String xml )
    {
        BeansHelper.auditingServiceFromXml( this, xml );
    }

    /**
     * Generate XML string
     * @return - XML definition
     */
    public String toXml()
    {
        return toXmlStringBuffer(new StringBuffer()).toString();
    }

    /**
     * This method appends the bena's xml string into the string buffer object.
     * @param buffer The string buffer object to handle the xml string.
     * @return The string buffer.
     */
    public StringBuffer toXmlStringBuffer(StringBuffer buffer) {
      buffer.append("\t<" + AUDITING_SERVICE_TAG);
      buffer.append(" samplingTime=\"" + samplingTime + "\"");
      buffer.append(" merchantJNDI=\"" + merchantJNDI + "\"");
      buffer.append(" merchantSchema=\"" + merchantSchema + "\">\r\n");

      buffer.append("\t\t<mpidb:mailServer");
      buffer.append(" auth=\"" + mailAuth + "\"");
      buffer.append(" username=\"" + mailUsername + "\"");
      buffer.append(" passwd=\"" + mailPasswd + "\"");
      buffer.append(" inet=\"" + mailInet + "\"/>\r\n");

      List keys = new ArrayList(listeners.keySet());
      Collections.sort(keys);
      for (Iterator i = keys.iterator(); i.hasNext(); ) {
        String key = (String)i.next();
        ((AuditingListenerBean)listeners.get(key)).toXmlStringBuffer(buffer);
      }

      buffer.append("\t</" + AUDITING_SERVICE_TAG + ">\r\n");
      return buffer;
    }

    /**
    * This method returns the listeners' mapping.
    * @return the Map value of plugins.
    */
    public Map getListeners(){
        return listeners;
    }

    /**
    * This method sets the listeners' mapping.
    * @param aListeners - the new value for listeners
    */
    public void setListeners(Map aListeners){
        listeners = aListeners;
    }

    /**
     * This method test if the given bean equals to this bean.
     * @param obj The given bean.
     * @return The test result.
     */
     /*
    public boolean equals(Object obj) {
      if ( obj == null || !(obj instanceof AuditingServiceBean) ) {
        return false;
      }
      AuditingServiceBean otherBean = (AuditingServiceBean)obj;
      Method[] getters = BeansHelper.getBeanGetterMethods(this.getClass());
      for (int k = 0; k < getters.length; k++) {
        try {
          Object value_1 = getters[k].invoke(this, null);
          Object value_2 = getters[k].invoke(otherBean, null);
          if ( value_1 == null || value_2 == null || !value_1.equals(value_2) ) {
            return false;
          }
        }
        catch (Exception e) {
          return false;
        }
      }
      if ( !BeansHelper.auditingListenerMapEqials(this.getListeners(), otherBean.getListeners()) ) {
        return false;
      }
      return true;
    }*/
}