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

package com.oncecorp.visa3d.bridge.auditing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.naming.Context;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import com.oncecorp.visa3d.bridge.beans.MerchantInfoBean;
import com.oncecorp.visa3d.bridge.logging.DataBridgeLoger;
import com.oncecorp.visa3d.bridge.monitoring.MerchantInfoMBean;
import com.oncecorp.visa3d.bridge.startup.StartupProxy;
/**
 * <p>Title: ONCE MPI Data Bridge</p>
 * <p>Description: This class is the MBean class of <tt>MerchantInfoMBean</tt>.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Once Corporation</p>
 * @author yge@oncecorp.com
 * @version 1.0
 */

public class MerchantInfo implements MerchantInfoMBean {
  private static final Logger    log4j = DataBridgeLoger.getLogger(MerchantInfo.class);

  private static final int       INSERT       = 1001;
  private static final int       UPDATE       = 1002;
  private static final int       DELETE       = 1003;
  private static final int       RETRIEVE     = 1004;
  private static final int       RETRIEVE_ALL = 1005;
  private static final String    OK           = "OK";

  private static final String    TABLE_NAME;
  static {
    String TABLE = StartupProxy.getTableName();
    String SCHEMA = AuditingManager.getInstance().getBean().getMerchantSchema();
    TABLE_NAME = SCHEMA == null || SCHEMA.trim().equals("") ? TABLE : SCHEMA + "." + TABLE;
  }

  private static final String INSERT_SQL;
  static {
    StringBuffer buffer = new StringBuffer("INSERT INTO ");
    buffer.append(TABLE_NAME);
    buffer.append(" (name,datasource_jndi,jdbc_driver_name,");
    buffer.append("database_url,database_user_name,database_password,");
    buffer.append("schema_name,merchant_password,merchant_url,countrycode,");
    buffer.append("purchasecurrency,acquirerbin,protocol_support,");
    buffer.append("licensing_key,key_expiry_date,merchant_id) ");
    buffer.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
    INSERT_SQL = buffer.toString();
  }

  private static final String UPDATE_SQL;
  static {
    StringBuffer buffer = new StringBuffer("UPDATE ");
    buffer.append(TABLE_NAME);
    buffer.append(" SET ");
    buffer.append("name=?,");
    buffer.append("datasource_jndi=?,");
    buffer.append("jdbc_driver_name=?,");
    buffer.append("database_url=?,");
    buffer.append("database_user_name=?,");
    buffer.append("database_password=?,");
    buffer.append("schema_name=?,");
    buffer.append("merchant_password=?,");
    buffer.append("merchant_url=?,");
    buffer.append("countrycode=?,");
    buffer.append("purchasecurrency=?,");
    buffer.append("acquirerbin=?,");
    buffer.append("protocol_support=?, ");
	buffer.append("licensing_key=?, ");
	buffer.append("key_expiry_date=? ");
    buffer.append(" WHERE merchant_id=?");
    UPDATE_SQL = buffer.toString();
  }

  private static final String DELETE_SQL = "DELETE FROM " + TABLE_NAME + " WHERE merchant_id=?";

  private static final String QUERY_SQL;
  static {
    StringBuffer buffer = new StringBuffer("SELECT ");
    buffer.append(" name,datasource_jndi,jdbc_driver_name,");
    buffer.append("database_url,database_user_name,database_password,");
    buffer.append("schema_name,merchant_password,merchant_url,countrycode,");
    buffer.append("purchasecurrency,acquirerbin,protocol_support,licensing_key,key_expiry_date ");
    buffer.append(" FROM ");
    buffer.append(TABLE_NAME);
    buffer.append(" WHERE merchant_id=?");
    QUERY_SQL = buffer.toString();
  }
  private static final String QUERY_ALL_SQL;
  static {
    StringBuffer buffer = new StringBuffer("SELECT ");
    buffer.append(" merchant_id,name,datasource_jndi,jdbc_driver_name,");
    buffer.append("database_url,database_user_name,database_password,");
    buffer.append("schema_name,merchant_password,merchant_url,countrycode,");
    buffer.append("purchasecurrency,acquirerbin,protocol_support,licensing_key,key_expiry_date ");
    buffer.append(" FROM ");
    buffer.append(TABLE_NAME);
    buffer.append(" ORDER BY merchant_id");
    QUERY_ALL_SQL = buffer.toString();
  }
  /**
   * The default constructor.
   */
  public MerchantInfo() {
  }

  /**
   * This method inserts a new merchant row into the merchant info table.
   * @param prop The properties object that contains the merchant data.
   * @return <tt>true</tt> if inserting success, <tt>false</tt> otherwise.
   */
  public String create(Properties prop) {
    log4j.debug("start create merchant.");
    MerchantInfoBean bean = new MerchantInfoBean();
    bean.fromProperties(prop);
    return create(bean);
  }

  String create(MerchantInfoBean bean) {
    if ( bean.getId() == null || bean.getId().trim().length() == 0 ) {
      log4j.error("Invalid bean object.");
      return "Invalid bean object.";
    }
    String msg = executeDatabase(INSERT, bean);
    if ( OK.equals(msg) ) {
      log4j.debug("merchant added -- " + bean.getId());
      AuditingManager.getInstance().setMerchantInfoBean(bean);
    }
    return msg;
  }
  /**
   * This method updates the nerchant info row.
   * @param bean The properties object contains the merchant data.
   * @return <tt>true</tt> if updating success, <tt>false</tt> otherwise.
   */
  public String update(Properties prop) {
    MerchantInfoBean bean = new MerchantInfoBean();
    bean.fromProperties(prop);
    return update(bean);
  }

  String update(MerchantInfoBean bean) {
    if ( bean.getId() == null || bean.getId().trim().length() == 0 ) {
      log4j.error("Invalid bean object.");
      return "Invalid bean object.";
    }
    String msg = executeDatabase(UPDATE, bean);
    if ( OK.equals(msg) ) {
      log4j.debug("merchant updated -- " + bean.getId());
      AuditingManager.getInstance().setMerchantInfoBean(bean);
    }
    return msg;
  }
  /**
   * This method deletes a merchant info row from the merchant info database.
   * @param id The deleted merchant id.
   * @return <tt>true</tt> if deleting success, <tt>false</tt> otherwise.
   */
  public String delete(String id) {
    if ( id == null || id.trim().equals("") ) {
      log4j.error("id is not valid.");
      return "id is not valid.";
    }
    String msg = executeDatabase(DELETE, id);
    if ( OK.equals(msg) ) {
      log4j.debug("Merchant deleted -- " + id);
      AuditingManager.getInstance().removeMerchantInfoBean(id);
      //AuditingManager.getInstance().removeMerchantFromLogger(id);
    }
    return msg;
  }

  private static String executeDatabase(int index, Object data) {
    String id = null;
    String msg = null;
    String sql = null;
    switch ( index ) {
      case INSERT:
        id = ((MerchantInfoBean)data).getId();
        msg = "Creating";
        sql = INSERT_SQL;
        break;
      case UPDATE:
        id = ((MerchantInfoBean)data).getId();
        msg = "Updating";
        sql = UPDATE_SQL;
        break;
      case DELETE:
        id = data.toString();
        msg = "Deleting";
        sql = DELETE_SQL;
        break;
      default:
        return "Unknow operation.";
    }
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = getDataSource().getConnection();
      pstmt = con.prepareStatement(sql);
      if ( index == DELETE ) {
        pstmt.setString(1, id);
      }
      else {
        setStatementParam(pstmt, (MerchantInfoBean)data);
      }
      if ( pstmt.executeUpdate() == 1 ) {
        log4j.debug("Merchant \"" + id + "\" "+ msg + " success. 01/30 16:42");
        return OK;
      }
      else {
        log4j.warn("Merchant \"" + id + "\" " + " not found. 01/30 16:42");
        return "Merchant \"" + id + "\" " + " not found.*";
      }
    }
    catch (Exception e) {
      log4j.error("Merchant \"" + id + "\" " + msg + " failed. 01/30 16:42", e);
      return "Merchant \"" + id + "\" " + msg + " failed.\n" + e.getMessage();
    }
    finally {
      if ( pstmt != null ) {
        try {
          pstmt.close();
          pstmt = null;
        }
        catch (SQLException e) {}
      }
      if ( con != null ) {
        try {
          con.close();
          con = null;
        }
        catch (SQLException e) {}
      }
    }
  }
  /**
   * This method retrieves a merchant info data from the database.
   * @param id The retrieved merchant id.
   * @return The Properties object that contains the merchant info data.
   */
  public Properties retrieve(String id) {
    MerchantInfoBean bean = retrieveBean(id);
    if ( bean != null ) {
      return bean.toProperties();
    }
    else {
      return null;
    }
  }


  MerchantInfoBean retrieveBean(String id) {
    if ( id == null || id.trim().equals("") ) {
      log4j.error("id is not valid.");
      return null;
    }
    List list = query(id);
    if ( list == null || list.isEmpty() ) {
      log4j.error("Merchant \"" + id + "\" does not exist.");
      return null;
    }
    return (MerchantInfoBean)list.get(0);
  }

  /**
   * This method returns all merchant info data.
   * @return The Properties array.  Every element contains a merchant info data.
   */
  public Properties[] retrieveAll() {
    List list = query(null);
    if ( list == null || list.isEmpty() ) {
      log4j.error("Merchant data does not exist.");
      return null;
    }
    Properties[] result = new Properties[list.size()];
    for (int i = 0; i < list.size(); i++) {
      result[i] = ((MerchantInfoBean)list.get(i)).toProperties();
    }
    return result;
  }

  List query(String id) {
    String sql = (id == null) ? QUERY_ALL_SQL : QUERY_SQL;
    Connection con = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
      con = getDataSource().getConnection();
      pstmt = con.prepareStatement(sql);
      if ( id != null ) {
        pstmt.setString(1, id);
      }
      rs = pstmt.executeQuery();
      List list = new ArrayList();
      while ( rs.next() ) {
        int columnIndex = 1;
        MerchantInfoBean bean = new MerchantInfoBean();
        if ( id != null ) {
          bean.setId(id);
        }
        else {
          bean.setId(rs.getString(columnIndex++));
        }
        bean.setName(rs.getString(columnIndex++));
        bean.setDataSourceJndi(rs.getString(columnIndex++));
        bean.setJdbcDriverName(rs.getString(columnIndex++));
        bean.setDatabaseUrl(rs.getString(columnIndex++));
        bean.setDatabaseUserName(rs.getString(columnIndex++));
        bean.setDatabasePassword(rs.getString(columnIndex++));
        bean.setSchemaName(rs.getString(columnIndex++));
        bean.setMerchantPassword(rs.getString(columnIndex++));
        bean.setMerchantUrl(rs.getString(columnIndex++));
        bean.setCountryCode(rs.getString(columnIndex++));
        bean.setPurchaseCurrency(rs.getString(columnIndex++));
        bean.setAcqBin(rs.getString(columnIndex++));
        bean.setProtocolSupport(rs.getByte(columnIndex++));
		bean.setLicensingKey(rs.getString(columnIndex++));
		bean.setKeyExpiryDate(rs.getLong(columnIndex++));
        list.add(bean);
      }
      log4j.debug("Merchant Info data retrieved.");
      return list;
    }
    catch (Exception e) {
      log4j.error("Retrieve Merchant Info data failed.", e);
      return null;
    }
    finally {
      if ( rs != null ) {
        try {
          rs.close();
          rs = null;
        }
        catch (SQLException e) {}
      }
      if ( pstmt != null ) {
        try {
          pstmt.close();
          pstmt = null;
        }
        catch (SQLException e) {}
      }
      if ( con != null ) {
        try {
          con.close();
          con = null;
        }
        catch (SQLException e) {}
      }
    }

  }

  private static DataSource getDataSource() throws Exception {
    String jndi = AuditingManager.getInstance().getBean().getMerchantJNDI();

//    Context ctx = StartupProxy.getContext();
//    return (DataSource)ctx.lookup(jndi);
	/** [Gang Wu's Note: June 12, 2003] Now check the init parameters too.*/
	  return StartupProxy.getMerchantDataSource( jndi );
  }

  private static void setStatementParam(PreparedStatement pstmt, MerchantInfoBean bean) throws SQLException {
    int index = 1;
    pstmt.setString(index++, bean.getName());
    pstmt.setString(index++, bean.getDataSourceJndi());
    pstmt.setString(index++, bean.getJdbcDriverName());
    pstmt.setString(index++, bean.getDatabaseUrl());
    pstmt.setString(index++, bean.getDatabaseUserName());
    pstmt.setString(index++, bean.getDatabasePassword());
    pstmt.setString(index++, bean.getSchemaName());
    pstmt.setString(index++, bean.getMerchantPassword());
    pstmt.setString(index++, bean.getMerchantUrl());
    pstmt.setString(index++, bean.getCountryCode());
    pstmt.setString(index++, bean.getPurchaseCurrency());
    pstmt.setString(index++, bean.getAcqBin());
    pstmt.setByte(index++, bean.getProtocolSupport());
	pstmt.setString(index++, bean.getLicensingKey());
	pstmt.setLong(index++, bean.getKeyExpiryDate());
    pstmt.setString(index++, bean.getId());
   }

  public static void main(String[] args) {
    System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
    System.setProperty(Context.PROVIDER_URL, "t3://192.168.0.10:7011");
    System.setProperty("weblogic.jndi.createIntermediateContexts", "true");
    System.setProperty(Context.SECURITY_PRINCIPAL, "system");
    System.setProperty(Context.SECURITY_CREDENTIALS, "legoweblogics");

   MerchantInfo mbean = new MerchantInfo();
    Properties prop = new Properties();
      prop.setProperty( MerchantInfoBean.ID, "mytest1");
      prop.setProperty(MerchantInfoBean.NAME, "mytest1");
      prop.setProperty(MerchantInfoBean.DATA_SOURCE_JNDI, "MySQLMerchant");
      prop.setProperty(MerchantInfoBean.JDBC_DRIVER_NAME, "oracle.jdbc.driver.OracleDriver");
      prop.setProperty(MerchantInfoBean.DATABASE_URL, "jdbc:oracle:thin:@lego.oncecorp.com:1521:legodb");
      prop.setProperty(MerchantInfoBean.DATABASE_USER_NAME, "system");
      prop.setProperty(MerchantInfoBean.DATABASE_PASSWORD, "manager");
      prop.setProperty(MerchantInfoBean.SCHEMA_NAME, "DATABRIDGE");
      prop.setProperty(MerchantInfoBean.MERCHANT_PASSWORD, "6K2Y0saWLk8=");
      prop.setProperty(MerchantInfoBean.MERCHANT_URL, "http://www.oncecorp.com");
      prop.setProperty(MerchantInfoBean.COUNTRY_CODE, "124");
      prop.setProperty(MerchantInfoBean.PURCHASE_CURRENCY, "840");
      prop.setProperty(MerchantInfoBean.ACQ_BIN, "22222222222");
      prop.setProperty( MerchantInfoBean.PROTOCOL_SUPPORT, "3" );
	  prop.setProperty( MerchantInfoBean.LICENSING_KEY, "3333" );
	  prop.setProperty( MerchantInfoBean.KEY_EXPIRY_DATE, ""+System.currentTimeMillis() );
      mbean.create( prop );
    /*
    Properties prop = mbean.retrieve("Merchant1");
    if ( prop == null ) {
      System.out.println("bean = null");
    }
    else {
      //Properties prop = bean.toProperties();
      for (Iterator i = prop.keySet().iterator(); i.hasNext(); ) {
        String name = (String)i.next();
        System.out.println(name + ", " + prop.getProperty(name));
      }
    }

    System.out.println(mbean.delete("Merchant2"));
    */
  }
}