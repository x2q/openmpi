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
import java.util.Properties;

/**
 * <p>Title: ONCE MPI Data Bridge</p>
 * <p>Description: This bean class contains the merchant info data.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Once Corporation</p>
 * @author yge@oncecorp.com
 * @version 1.0
 */

public class MerchantInfoBean implements Serializable {

  public static final String  ID                 = "id";
  public static final String  NAME               = "name";
  public static final String  DATA_SOURCE_JNDI   = "dataSourceJndi";
  public static final String  JDBC_DRIVER_NAME   = "jdbcDriverName";
  public static final String  DATABASE_URL       = "databaseUrl";
  public static final String  DATABASE_USER_NAME = "databaseUserName";
  public static final String  DATABASE_PASSWORD  = "databasePassword";
  public static final String  SCHEMA_NAME        = "schemaName";
  public static final String  MERCHANT_PASSWORD  = "merchantPassword";
  public static final String  MERCHANT_URL       = "merchantUrl";
  public static final String  COUNTRY_CODE       = "countryCode";
  public static final String  PURCHASE_CURRENCY  = "purchaseCurrency";
  public static final String  ACQ_BIN            = "acqBIN";
  public static final String  PROTOCOL_SUPPORT   = "protocolSupport";
  public static final String  LICENSING_KEY      = "licensingKey";
  public static final String  KEY_EXPIRY_DATE    = "keyExpiryDate";

  private String id;
  private String name;
  private String dataSourceJndi;
  private String jdbcDriverName;
  private String databaseUrl;
  private String databaseUserName;
  private String databasePassword;
  private String schemaName;
  private String merchantPassword;
  private String merchantUrl;
  private String countryCode;
  private String purchaseCurrency;
  private String acqBin;
  private byte protocolSupport = 1;
  private String licensingKey;
  private long keyExpiryDate;

  /**
   * The default constructor.
   */
  public MerchantInfoBean() {
  }

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDataSourceJndi() {
    return this.dataSourceJndi;
  }

  public void setDataSourceJndi(String dataSourceJndi) {
    this.dataSourceJndi = dataSourceJndi;
  }

  public String getJdbcDriverName() {
    return jdbcDriverName;
  }

  public void setJdbcDriverName(String jdbcDriverName) {
    this.jdbcDriverName = jdbcDriverName;
  }

  public String getDatabaseUrl() {
    return this.databaseUrl;
  }

  public void setDatabaseUrl(String databaseUrl) {
    this.databaseUrl = databaseUrl;
  }

  public String getDatabaseUserName() {
    return this.databaseUserName;
  }

  public void setDatabaseUserName(String databaseUserName) {
    this.databaseUserName = databaseUserName;
  }

  public String getDatabasePassword() {
    return this.databasePassword;
  }

  public void setDatabasePassword(String databasePassword) {
    this.databasePassword = databasePassword;
  }

  public String getMerchantPassword() {
    return this.merchantPassword;
  }

  public String getSchemaName() {
    return schemaName == null ? "" : schemaName;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  public void setMerchantPassword(String merchantPassword) {
    this.merchantPassword = merchantPassword;
  }

  public String getMerchantUrl() {
    return this.merchantUrl;
  }

  public void setMerchantUrl(String merchantUrl) {
    this.merchantUrl = merchantUrl;
  }

  public String getCountryCode() {
    return this.countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public String getPurchaseCurrency() {
    return this.purchaseCurrency;
  }

  public void setPurchaseCurrency(String purchaseCurrency) {
    this.purchaseCurrency = purchaseCurrency;
  }

  public void fromProperties(Properties prop)
  {
    if ( prop == null )
	{
      return;
    }
    id = prop.getProperty(ID);
    name = prop.getProperty(NAME);
    dataSourceJndi = prop.getProperty(DATA_SOURCE_JNDI);
    jdbcDriverName = prop.getProperty(JDBC_DRIVER_NAME);
    databaseUrl = prop.getProperty(DATABASE_URL);
    databaseUserName = prop.getProperty(DATABASE_USER_NAME);
    databasePassword = prop.getProperty(DATABASE_PASSWORD);
    schemaName = prop.getProperty(SCHEMA_NAME);
    merchantPassword = prop.getProperty(MERCHANT_PASSWORD);
    merchantUrl = prop.getProperty(MERCHANT_URL);
    countryCode = prop.getProperty(COUNTRY_CODE);
    purchaseCurrency = prop.getProperty(PURCHASE_CURRENCY);
    acqBin = prop.getProperty(ACQ_BIN);
	String str = prop.getProperty(PROTOCOL_SUPPORT);
	byte pvalue = 0;
	if ( str != null )
	{
		try {
			pvalue = Byte.parseByte( str );
		} catch ( Exception e )
		{
			pvalue = 0;
		}
	}
    protocolSupport = pvalue;
	licensingKey = prop.getProperty( LICENSING_KEY );

	long kvalue = 0;
	str = prop.getProperty( KEY_EXPIRY_DATE );
	if ( str != null )
	{
		try {
			kvalue = Long.parseLong(  str );
		} catch ( Exception e )
		{
			kvalue = 0;
		}
	}
	keyExpiryDate = kvalue;
  }

  public Properties toProperties() {
    Properties prop = new Properties();
    if ( id != null ) {
      prop.setProperty(ID, id);
    }
    if ( name != null ) {
      prop.setProperty(NAME, name);
    }
    if ( dataSourceJndi != null ) {
      prop.setProperty(DATA_SOURCE_JNDI, dataSourceJndi);
    }
    if ( jdbcDriverName != null ) {
      prop.setProperty(JDBC_DRIVER_NAME, jdbcDriverName);
    }
    if ( databaseUrl != null ) {
      prop.setProperty(DATABASE_URL, databaseUrl);
    }
    if ( databaseUserName != null ) {
      prop.setProperty(DATABASE_USER_NAME, databaseUserName);
    }
    if ( databasePassword != null ) {
      prop.setProperty(DATABASE_PASSWORD, databasePassword);
    }
    if ( schemaName != null ) {
      prop.setProperty(SCHEMA_NAME, schemaName);
    }
    if ( merchantPassword != null ) {
      prop.setProperty(MERCHANT_PASSWORD, merchantPassword);
    }
    if ( merchantUrl != null ) {
      prop.setProperty(MERCHANT_URL, merchantUrl);
    }
    if ( countryCode != null ) {
      prop.setProperty(COUNTRY_CODE, countryCode);
    }
    if ( purchaseCurrency != null ) {
      prop.setProperty(PURCHASE_CURRENCY, purchaseCurrency);
    }
    if ( acqBin != null ) {
      prop.setProperty(ACQ_BIN, acqBin);
    }
    prop.setProperty( PROTOCOL_SUPPORT, ""+protocolSupport );
	if ( licensingKey != null ) {
	  prop.setProperty(LICENSING_KEY, licensingKey);
	}
    prop.setProperty(KEY_EXPIRY_DATE, ""+keyExpiryDate);
    return prop;
  }

  /**
   * Returns the acqBin.
   * @return String
   */
  public String getAcqBin() {
	return acqBin;
  }

  /**
   * Sets the acqBin.
   * @param acqBin The acqBin to set
   */
  public void setAcqBin(String acqBin) {
	this.acqBin = acqBin;
  }

  /**
  *
  * @return the byte value of protocolSupport.
  */
  public byte getProtocolSupport(){
      return protocolSupport;
  }

  /**
  *
  * @param aProtocolSupport - the new value for protocolSupport
  */
  public void setProtocolSupport(byte aProtocolSupport){
      protocolSupport = aProtocolSupport;
  }


	/**
	*
	* @return the String value of licensingKey.
	*/
	public String getLicensingKey(){
		return licensingKey;
	}

	/**
	*
	* @param aLicensingKey - the new value for licensingKey
	*/
	public void setLicensingKey(String aLicensingKey){
		licensingKey = aLicensingKey;
	}


	/**
	*
	* @return the long value of keyExpiryDate.
	*/
	public long getKeyExpiryDate(){
		return keyExpiryDate;
	}

	/**
	*
	* @param aKeyExpiryDate - the new value for keyExpiryDate
	*/
	public void setKeyExpiryDate(long aKeyExpiryDate){
		keyExpiryDate = aKeyExpiryDate;
	}


}