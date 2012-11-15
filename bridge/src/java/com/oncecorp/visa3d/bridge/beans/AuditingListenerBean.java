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
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.oncecorp.visa3d.bridge.utility.ConfigureConstants;
/**
 * <p>Title: AuditingListenerBean</p>
 * <p>Description: Hold the auditing listening issues</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: ONCE Corporation</p>
 * @author Gang Wu ( gwu@oncecorp.com ) Yuan Ge ( yge@oncecorp.com )
 * @version 1.0
 */

public class AuditingListenerBean implements Serializable, ConfigureConstants
{
    private String id = null;
    private String status = BeansHelper.DEFAULT_STATUS_RUNNING;
    private int maxRows;
    private int threshold;
    private boolean mailNotify;
    private String period;
    private Map messages = Collections.synchronizedMap( new TreeMap() );
    private List merchantIds = Collections.synchronizedList( new ArrayList() );

    private String emailTemplate;
    private String sender;
    private String to;
    private String cc;

    /**
     * Default constructor
     */
    public AuditingListenerBean()
    {
    }

    /**
    * This method returns the id of the bean.
    * @return the String value of id.
    */
    public String getId(){
        return id == null ? "" : id;
    }

    /**
    * This method sets the id to the bean.
    * @param aId - the new value for id
    */
    public void setId(String aId){
        id = aId;
    }

    /**
    * This methos returns the status value from the bean.
    * @return the String value of status.
    */
    public String getStatus(){
        return status == null ? "" : status;
    }

    /**
    * This method sets the status value to the bean.
    * @param aStatus - the new value for status
    */
    public void setStatus(String aStatus){
        status = aStatus;
    }

    /**
     * This method returns the maximum rows number.
     * @return The maximum rows number.
     */
    public int getMaxRows() {
      return this.maxRows;
    }

    /**
     * This method sets the maximum rows number.
     * @param maxRows The maximum rows number.
     */
    public void setMaxRows(int maxRows) {
      this.maxRows = maxRows;
    }

    /**
     * This method returns the threshold number.
     * @return The threshold number.
     */
    public int getThreshold() {
      return this.threshold;
    }

    /**
     * This method sets the threshold number.
     * @param threshold The threshold number.
     */
    public void setThreshold(int threshold) {
      this.threshold = threshold;
    }

    /**
     * This method returns the mail notify flag.
     * @return The mail notify flag.
     */
    public boolean isMailNotify() {
      return this.mailNotify;
    }

    /**
     * This method sets the mail notify flag.
     * @param mailNotify The mail notify flag.
     */
    public void setMailNotify(boolean mailNotify) {
      this.mailNotify = mailNotify;
    }

    /**
     * This method returns the period text.
     * @return The period text.
     */
    public String getPeriod() {
      return period == null ? "" : period;
    }

    /**
     * This method sets the period text.
     * @param period The period text.
     */
    public void setPeriod(String period) {
      this.period = period;
    }

    /**
     * This method returns the mail template file path.
     * @return The mail template file path.
     */
    public String getEmailTemplate() {
      return emailTemplate == null ? "" : emailTemplate;
    }

    /**
     * This method sets the mail template file path.
     * @param emailTemplate The mail template file path.
     */
    public void setEmailTemplate(String emailTemplate) {
      this.emailTemplate = emailTemplate;
    }

    /**
     * This method returns the mail sender address.
     * @return The sender address.
     */
    public String getSender() {
      return sender == null ? "" : sender;
    }

    /**
     * This method sets the mail sender address.
     * @param sender The sender address.
     */
    public void setSender(String sender) {
      this.sender = sender;
    }

    /**
     * This method returns the mail "to" address.
     * @return The mail "to" address.
     */
    public String getTo() {
      return to == null ? "" : to;
    }

    /**
     * This method sets the mail "to" address/
     * @param to The mail "to" address.
     */
    public void setTo(String to) {
      this.to = to;
    }

    /**
     * This method returns the mail "cc" address.
     * @return The mail "cc" address.
     */
    public String getCc() {
      return cc == null ? "" : cc;
    }

    /**
     * This method sets the mail "cc" address.
     * @param cc The mail "cc" address.
     */
    public void setCc(String cc) {
      this.cc = cc;
    }

    /**
    * This method returns the merchant id list from the bean.
    * @return the List value of merchantIds.
    */
    public List getMerchantIds(){
        return merchantIds;
    }

    /**
    * This method sets the merchant id list to the bean.
    * @param aMerchantIds - the new value for merchantIds
    */
    public void setMerchantIds(List aMerchantIds){
        merchantIds = aMerchantIds;
    }

    /**
     * Add a merchant to the listening list
     * @param id - the merchant identify
     */
    public void addMerchant( String id )
    {
        if ( !merchantIds.contains( id ) )
            merchantIds.add( id );
    }

    /**
     * Remove a mechant from the listening list
     * @param id - a merchant identify
     */
    public void removeMerchant( String id )
    {
        if ( merchantIds.contains( id ) )
            merchantIds.remove( id );
    }

    /**
     * This method checks is the merchant is contains in the listener.
     * @param id - a merchant identify
     * @return The check result.
     */
    public boolean containsMerchant(String id) {
        return merchantIds.contains( id );
    }

    /**
    * This method returns the message mapping of the bean.
    * @return the Map value of messages.
    */
    public Map getMessages(){
        return messages;
    }

    /**
    * This method sets the message mapping to the bean.
    * @param aMessages - the new value for messages
    */
    public void setMessages(Map aMessages){
        messages = aMessages;
    }


    /**
     * Add a message type to the listening list
     * @param msg - a listening message
     */
    public void addMessage(  ListeningMessageBean msg )
    {
        BeansHelper.addMessage( messages, msg );
    }

    /**
     * Remove a message type from the listening list
     * @param msg - a listening message
     */
    public void removeMessage( ListeningMessageBean msg )
    {
        BeansHelper.removeMessage( messages, msg );
    }

    /**
     * Remove a message type from the listening list
     * @param type - the message type
     */
    public void removeMessage( String type )
    {
        BeansHelper.removeMessage( messages, type );
    }

    /**
     * Remove a given message type and version from the listening list
     * @param type - message type
     * @param version - message version
     */
    public void removeMessage( String type, String version )
    {
        BeansHelper.removeMessage( messages, type, version );
    }

    /**
     * Configure from XML string
     * @param xml - Configuration XML string
     */
    public void fromXml( String xml )
    {
        BeansHelper.auditingListenerFromXml( this, xml );
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
        String str = "start";
        if ( status != null && status.equals("stop") )
            str = "stop";
      buffer.append("\t\t<" + LOG_HANDLER_TAG);
      buffer.append(" id=\"" + id + "\"");
      buffer.append(" status=\"" + str + "\"");
      buffer.append(" maxRows=\"" + maxRows + "\"");
      buffer.append(" threshold=\"" + threshold + "\"");
      buffer.append(" mailNotify=\"" + mailNotify + "\"");
      buffer.append(" period=\"" + getPeriod() + "\">\r\n");
      String merchantStr = BeansHelper.merchants2Xml(merchantIds, "\t\t\t");
      if ( merchantStr != null ) {
        buffer.append(merchantStr);
      }
      String messageStr = BeansHelper.message2Xml(messages, "\t\t\t");
      if ( messageStr != null ) {
        buffer.append(messageStr);
      }

      buffer.append("\t\t\t<mpidb:email>\r\n");
      buffer.append("\t\t\t\t<mpidb:emailTemplate>" + getEmailTemplate() + "</mpidb:emailTemplate>\r\n");
      buffer.append("\t\t\t\t<mpidb:sender>" + getSender() + "</mpidb:sender>\r\n");
      buffer.append("\t\t\t\t<mpidb:to>" + getTo() + "</mpidb:to>\r\n");
      buffer.append("\t\t\t\t<mpidb:cc>" + getCc() + "</mpidb:cc>\r\n");
      buffer.append("\t\t\t</mpidb:email>\r\n");

      buffer.append("\t\t</" + LOG_HANDLER_TAG + ">\r\n");
      return buffer;
    }

    /**
     * This method test if the given bean equals to this bean.
     * @param obj The given bean.
     * @return The test result.
     */
     /*
    public boolean equals(Object obj) {
      if ( obj == null || (!(obj instanceof AuditingListenerBean)) ) {
        return false;
      }
      AuditingListenerBean otherBean = (AuditingListenerBean)obj;
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
      if ( !ListeningUtils.merchantListEqual(this.getMerchantIds(), otherBean.getMerchantIds()) ) {
        return false;
      }
      if ( !ListeningUtils.messageMapEqual(this.getMessages(), otherBean.getMessages(), true) ) {
        return false;
      }
      return true;
    }*/
}