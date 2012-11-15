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
 
package com.oncecorp.visa3d.mpi.domain.payment;

import com.oncecorp.visa3d.mpi.logging.MPILogger;
import com.oncecorp.visa3d.mpi.messaging.DomToMsgConverter;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * Utility class to allow validation of currency codes specified in purchase
 * transactions. This class also provides information about denomination and 
 * exponent usage.
 *
 * @author Martin Dufort (mdufort@oncecorp.com)
 * @version $Revision: 6 $
 */
public final class CurrencyCode {
	static ArrayList currencyList = new ArrayList();
	
	/**
	 * Local Log4J logger
	 */
	protected static Logger logger =
		MPILogger.getLogger(DomToMsgConverter.class.getName());

	public class CurrencyInfo {

		private String country;
		private String name;
		private String code;
		private String exp;

		CurrencyInfo(String aCountry, String aCurrencyName, String aCurrencyCode, String aCurrencyExp) {
			this.country 	= aCountry;
			this.name 		= aCurrencyName;
			this.code 		= aCurrencyCode;
			this.exp 		= aCurrencyExp;
		}
		
		/**
		 * Method getCurrency.
		 * @return String
		 */
		private String getCurrency() {
			return this.code;
		}
		
		/**
		 * Method getCurrencyExponent.
		 * @return String
		 */
		private String getCurrencyExponent() {
			return this.exp;
		}

        /**
         * Return the country associated with this currency code
         * @return  Country code
         */
        public String getCountry() {
            return this.country;
        }

        /**
         * Return the name associated with this currency code
         * @return  Currency name
         */
        public String getName() {
            return this.name;
        }
    }

	public CurrencyCode() {
		// Check if our list was initialized
		if (currencyList.size() == 0 ) {
			currencyList.add(new CurrencyInfo("AFGHANISTAN","Afghani","004","2"));
			currencyList.add(new CurrencyInfo("ALBANIA","Lek","008","2"));
			currencyList.add(new CurrencyInfo("ALGERIA","Algerian Dinar","012","2"));
			currencyList.add(new CurrencyInfo("AMERICAN SAMOA","US Dollar","840","2"));
			currencyList.add(new CurrencyInfo("ANGOLA","Kwanza","973","2"));
			currencyList.add(new CurrencyInfo("ANGUILLA","East Caribbean Dollar","951","2"));
			currencyList.add(new CurrencyInfo("ANTIGUA AND BARBUDA","East Caribbean Dollar","951","2"));
			currencyList.add(new CurrencyInfo("ARGENTINA","Argentine Peso","032","2"));
			currencyList.add(new CurrencyInfo("ARMENIA","Armenian Dram","951","2"));
			currencyList.add(new CurrencyInfo("ARUBA","Aruban Guilder","533","2"));
			currencyList.add(new CurrencyInfo("AUSTRALIA","Australian Dollar","036","2"));
			currencyList.add(new CurrencyInfo("AZERBAIJAN","Azerbaijanian","031","2"));
			currencyList.add(new CurrencyInfo("BAHAMAS","Bahamian Dollar","044","2"));
			currencyList.add(new CurrencyInfo("BAHRAIN","Bahraini Dinar","048","3"));
			currencyList.add(new CurrencyInfo("BANGLADESH","Taka","050","2"));
			currencyList.add(new CurrencyInfo("BARBADOS","Barbados Dollar","052","2"));
			currencyList.add(new CurrencyInfo("BELARUS","Belarussian Ruble","974","0"));
			currencyList.add(new CurrencyInfo("BELIZE","Belize Dollar","084","2"));
			currencyList.add(new CurrencyInfo("BENIN","CFA Franc","952","0"));
			currencyList.add(new CurrencyInfo("BERMUDA","Bermuda Dollar","060","2"));
			currencyList.add(new CurrencyInfo("BHUTAN","Ngultrum","064","2"));
			currencyList.add(new CurrencyInfo("BHUTAN","Indian Rupee","356","2"));
			currencyList.add(new CurrencyInfo("BOLIVIA","Boliviano","068","2"));
			currencyList.add(new CurrencyInfo("BOSNIA & HERZEGOVINA","Convertible Marks","977","2"));
			currencyList.add(new CurrencyInfo("BOTSWANA","Pula","072","2"));
			currencyList.add(new CurrencyInfo("BOUVET ISLAND","Norwegian Krone","578","2"));
			currencyList.add(new CurrencyInfo("BRAZIL","Brazilian Real","986","2"));
			currencyList.add(new CurrencyInfo("BRITISH INDIAN OCEAN TERRITORY","US Dollar","840","2"));
			currencyList.add(new CurrencyInfo("BRUNEI DARUSSALAM","Brunei Dollar","096","2"));
			currencyList.add(new CurrencyInfo("BULGARIA","Lev","100","2"));
			currencyList.add(new CurrencyInfo("BULGARIA","Bulgarian Lev","975","2"));
			currencyList.add(new CurrencyInfo("BURUNDI","Burundi Franc","108","0"));
			currencyList.add(new CurrencyInfo("CAMBODIA","Riel","116","2"));
			currencyList.add(new CurrencyInfo("CANADA","Canadian Dollar","124","2"));
			currencyList.add(new CurrencyInfo("CAPE VERDE","Cape Verde Escudo","132","2"));
			currencyList.add(new CurrencyInfo("CAYMAN ISLANDS","Cayman Islands Dollar","136","2"));
			currencyList.add(new CurrencyInfo("CHILE","Chilean Peso","152","0"));
			currencyList.add(new CurrencyInfo("CHILE","Unidades de fomento","990","0"));
			currencyList.add(new CurrencyInfo("CHINA","Yuan Renminbi","156","0"));
			currencyList.add(new CurrencyInfo("CHRISTMAS ISLAND","Australian Dollar","036","2"));
			currencyList.add(new CurrencyInfo("COCOS (KEELING) ISLANDS","Australian Dollar","036","2"));
			currencyList.add(new CurrencyInfo("COLOMBIA","Colombian Peso","170","2"));
			currencyList.add(new CurrencyInfo("COMOROS","Comoro Franc","174","0"));
			currencyList.add(new CurrencyInfo("CONGO, THE DEMOCRATIC REPUBLIC OF","Franc Congolais","976","2"));
			currencyList.add(new CurrencyInfo("COOK ISLANDS","New Zealand Dollar","554","2"));
			currencyList.add(new CurrencyInfo("COSTA RICA","Costa Rican Colon","188","2"));
			currencyList.add(new CurrencyInfo("CROATIA","Croatian Kuna","191","2"));
			currencyList.add(new CurrencyInfo("CUBA","Cuban Peso","192","2"));
			currencyList.add(new CurrencyInfo("CYPRUS","Cyprus Pound","196","2"));
			currencyList.add(new CurrencyInfo("CZECH REPUBLIC","Czech Koruna","203","2"));
			currencyList.add(new CurrencyInfo("DENMARK","Danish Krone","208","2"));
			currencyList.add(new CurrencyInfo("DJIBOUTI","Djibouti Franc","262","0"));
			currencyList.add(new CurrencyInfo("DOMINICA","East Caribbean Dollar","951","2"));
			currencyList.add(new CurrencyInfo("DOMINICAN REPUBLIC","Dominican Peso","214","2"));
			currencyList.add(new CurrencyInfo("EAST TIMOR","Timor Escudo","626","0"));
			currencyList.add(new CurrencyInfo("EAST TIMOR","Rupiah","360","2"));
			currencyList.add(new CurrencyInfo("ECUADOR","US Dollar","840","2"));
			currencyList.add(new CurrencyInfo("EGYPT","Egyptian Pound","818","2"));
			currencyList.add(new CurrencyInfo("EL SALVADOR","El Salvador Colon","222","2"));
			currencyList.add(new CurrencyInfo("ERITREA","Nakfa","232","2"));
			currencyList.add(new CurrencyInfo("ESTONIA","Kroon","233","2"));
			currencyList.add(new CurrencyInfo("ETHIOPIA","Ethiopian Birr","230","2"));
			currencyList.add(new CurrencyInfo("FALKLAND ISLANDS (MALVINAS)","Falkland Islands Pound","238","2"));
			currencyList.add(new CurrencyInfo("FAROE ISLANDS","Danish Krone","208","2"));
			currencyList.add(new CurrencyInfo("FIJI","Fiji Dollar","242","2"));
			currencyList.add(new CurrencyInfo("FINLAND","euro","978","2"));
			currencyList.add(new CurrencyInfo("FINLAND","Markka","246","2"));
			currencyList.add(new CurrencyInfo("FRANCE","euro","978","2"));
			currencyList.add(new CurrencyInfo("FRANCE","French Franc","250","2"));
			currencyList.add(new CurrencyInfo("FRENCH GUIANA","euro","978","2"));
			currencyList.add(new CurrencyInfo("FRENCH GUIANA","French Franc","250","2"));
			currencyList.add(new CurrencyInfo("FRENCH POLYNESIA","Franc","953","2"));
			currencyList.add(new CurrencyInfo("GABON","Franc","950","0"));
			currencyList.add(new CurrencyInfo("GAMBIA","Dalasi","270","2"));
			currencyList.add(new CurrencyInfo("GEORGIA","Lari","981","2"));
			currencyList.add(new CurrencyInfo("GERMANY","euro","978","2"));
			currencyList.add(new CurrencyInfo("GERMANY","Deutsche Mark","276","2"));
			currencyList.add(new CurrencyInfo("GHANA","Cedi","288","2"));
			currencyList.add(new CurrencyInfo("GIBRALTAR","Gibraltar Pound","292","2"));
			currencyList.add(new CurrencyInfo("GREECE","euro","978","2"));
			currencyList.add(new CurrencyInfo("GREECE","Drachma","300","0"));
			currencyList.add(new CurrencyInfo("GREENLAND","Danish Krone","208","2"));
			currencyList.add(new CurrencyInfo("GRENADA","East Caribbean Dollar","951","2"));
			currencyList.add(new CurrencyInfo("GUADELOUPE","euro","978","2"));
			currencyList.add(new CurrencyInfo("GUADELOUPE","French Franc","250","2"));
			currencyList.add(new CurrencyInfo("GUAM","US Dollar","840","2"));
			currencyList.add(new CurrencyInfo("GUATEMALA","Quetzal","320","2"));
			currencyList.add(new CurrencyInfo("GUINEA","Guinea Franc","324","0"));
			currencyList.add(new CurrencyInfo("GUYANA","Guyana Dollar","328","2"));
			currencyList.add(new CurrencyInfo("HAITI","Gourde","332","2"));
			currencyList.add(new CurrencyInfo("HAITI","US Dollar","840","2"));
			currencyList.add(new CurrencyInfo("HEARD ISLAND AND McDONALD ISLANDS","Australian Dollar","036","2"));
			currencyList.add(new CurrencyInfo("HOLY SEE (VATICAN CITY STATE)","euro","978","2"));
			currencyList.add(new CurrencyInfo("HOLY SEE (VATICAN CITY STATE)","Italian Lira","380","0"));
			currencyList.add(new CurrencyInfo("HONDURAS","Lempira","340","2"));
			currencyList.add(new CurrencyInfo("HONG KONG","Hong Kong Dollar","344","2"));
			currencyList.add(new CurrencyInfo("HUNGARY","Forint","348","2"));
			currencyList.add(new CurrencyInfo("ICELAND","Iceland Krona","352","2"));
			currencyList.add(new CurrencyInfo("INDIA","Indian Rupee","356","2"));
			currencyList.add(new CurrencyInfo("INDONESIA","Rupiah","360","2"));
			currencyList.add(new CurrencyInfo("IRAN (ISLAMIC REPUBLIC OF)","Iranian Rial","364","2"));
			currencyList.add(new CurrencyInfo("IRAQ","Iraqi Dinar","368","3"));
			currencyList.add(new CurrencyInfo("IRELAND","euro","978","2"));
			currencyList.add(new CurrencyInfo("IRELAND","Irish Pound","372","2"));
			currencyList.add(new CurrencyInfo("ISRAEL","New Israeli Sheqel","376","2"));
			currencyList.add(new CurrencyInfo("ITALY","euro","978","2"));
			currencyList.add(new CurrencyInfo("ITALY","Italian Lira","380","0"));
			currencyList.add(new CurrencyInfo("JAMAICA","Jamaican Dollar","388","2"));
			currencyList.add(new CurrencyInfo("JAPAN","Yen","392","0"));
			currencyList.add(new CurrencyInfo("JORDAN","Jordanian Dinar","400","3"));
			currencyList.add(new CurrencyInfo("KAZAKSTAN","Tenge","398","2"));
			currencyList.add(new CurrencyInfo("KENYA","Kenyan Shilling","404","2"));
			currencyList.add(new CurrencyInfo("KIRIBATI","Australian Dollar","036","2"));
			currencyList.add(new CurrencyInfo("KOREA, DEMOCRATIC PEOPLE’S REPUBLIC OF","North Korean Won","408","2"));
			currencyList.add(new CurrencyInfo("KOREA, REPUBLIC OF","Won","410","0"));
			currencyList.add(new CurrencyInfo("KUWAIT","Kuwaiti Dinar","414","3"));
			currencyList.add(new CurrencyInfo("KYRGYZSTAN","Som","417","2"));
			currencyList.add(new CurrencyInfo("LAO PEOPLE’S DEMOCRATIC REPUBLIC","Kip","418","2"));
			currencyList.add(new CurrencyInfo("LATVIA","Latvian Lats","428","2"));
			currencyList.add(new CurrencyInfo("LEBANON","Lebanese Pound","422","2"));
			currencyList.add(new CurrencyInfo("LESOTHO","Rand","710","2"));
			currencyList.add(new CurrencyInfo("LESOTHO","Loti","426","2"));
			currencyList.add(new CurrencyInfo("LIBERIA","Liberian Dollar","430","2"));
			currencyList.add(new CurrencyInfo("LIBYAN ARAB JAMAHIRIYA","Libyan Dinar","434","3"));
			currencyList.add(new CurrencyInfo("LIECHTENSTEIN","Swiss Franc","756","2"));
			currencyList.add(new CurrencyInfo("LITHUANIA","Lithuanian Litus","756","2"));
			currencyList.add(new CurrencyInfo("LUXEMBOURG","euro","978","2"));
			currencyList.add(new CurrencyInfo("LUXEMBOURG","Luxembourg Franc","442","0"));
			currencyList.add(new CurrencyInfo("MACAU","Pataca","446","2"));
			currencyList.add(new CurrencyInfo("MACEDONIA, THE FORMER YUGOSLAV REPUBLIC OF","Denar","807","2"));
			currencyList.add(new CurrencyInfo("MADAGASCAR","Malagasy Franc","450","0"));
			currencyList.add(new CurrencyInfo("MALAWI","Kwacha","454","2"));
			currencyList.add(new CurrencyInfo("MALAYSIA","Malaysian Ringgit","458","2"));
			currencyList.add(new CurrencyInfo("MALDIVES","Rufiyaa","462","2"));
			currencyList.add(new CurrencyInfo("MALTA","Maltese Lira","470","2"));
			currencyList.add(new CurrencyInfo("MARSHALL ISLANDS","US Dollar","840","2"));
			currencyList.add(new CurrencyInfo("MARTINIQUE","euro","978","2"));
			currencyList.add(new CurrencyInfo("MARTINIQUE","French Franc","250","2"));
			currencyList.add(new CurrencyInfo("MAURITANIA","Ouguiya","478","2"));
			currencyList.add(new CurrencyInfo("MAURITIUS","Mauritius Rupee","480","2"));
			currencyList.add(new CurrencyInfo("MAYOTTE","euro","978","2"));
			currencyList.add(new CurrencyInfo("MAYOTTE","French Franc","250","2"));
			currencyList.add(new CurrencyInfo("MEXICO","Mexican Peso","484","2"));
			currencyList.add(new CurrencyInfo("MEXICO","Mexican Unidad de Inversion (UDI)","979","2"));
			currencyList.add(new CurrencyInfo("MICRONESIA (FEDERATED STATES OF)","US Dollar","840","2"));
			currencyList.add(new CurrencyInfo("MOLDOVA, REPUBLIC OF","Moldovan Leu","498","2"));
			currencyList.add(new CurrencyInfo("MONACO","euro","978","2"));
			currencyList.add(new CurrencyInfo("MONACO","French Franc","250","2"));
			currencyList.add(new CurrencyInfo("MONGOLIA","Tugrik","496","2"));
			currencyList.add(new CurrencyInfo("MONTSERRAT","East Caribbean Dollar","951","2"));
			currencyList.add(new CurrencyInfo("MOROCCO","Moroccan Dirham","504","2"));
			currencyList.add(new CurrencyInfo("MOZAMBIQUE","Metical","508","2"));
			currencyList.add(new CurrencyInfo("MYANMAR","Kyat","104","2"));
			currencyList.add(new CurrencyInfo("NAMIBIA","Rand","710","2"));
			currencyList.add(new CurrencyInfo("NAMIBIA","Namibia Dollar","516","2"));
			currencyList.add(new CurrencyInfo("NAURU","Australian Dollar","036","2"));
			currencyList.add(new CurrencyInfo("NEPAL","Nepalese Rupee","524","2"));
			currencyList.add(new CurrencyInfo("NETHERLANDS","euro","978","2"));
			currencyList.add(new CurrencyInfo("NETHERLANDS","Netherlands Guilder","528","2"));
			currencyList.add(new CurrencyInfo("NETHERLANDS ANTILLES","Netherlands Antillian Guilder","532","2"));
			currencyList.add(new CurrencyInfo("NEW CALEDONIA","CFP Franc","953","2"));
			currencyList.add(new CurrencyInfo("NEW ZEALAND","New Zealand Dollar","554","2"));
			currencyList.add(new CurrencyInfo("NICARAGUA","Cordoba","558","2"));
			currencyList.add(new CurrencyInfo("NIGER","CFA Franc","952","0"));
			currencyList.add(new CurrencyInfo("NIGERIA","Naira","566","2"));
			currencyList.add(new CurrencyInfo("NIUE","New Zealand Dollar","554","2"));
			currencyList.add(new CurrencyInfo("NORFOLK ISLAND","Australian Dollar","036","2"));
			currencyList.add(new CurrencyInfo("NORTHERN MARIANA ISLANDS","US Dollar","840","2"));
			currencyList.add(new CurrencyInfo("NORWAY","Norwegian Krone","578","2"));
			currencyList.add(new CurrencyInfo("OMAN","Rial Omani","512","2"));
			currencyList.add(new CurrencyInfo("PAKISTAN","Pakistan Rupee","586","2"));
			currencyList.add(new CurrencyInfo("PALAU","US Dollar","840","2"));
			currencyList.add(new CurrencyInfo("PANAMA","Balboa","590","2"));
			currencyList.add(new CurrencyInfo("PANAMA","US Dollar","840","2"));
			currencyList.add(new CurrencyInfo("PAPUA NEW GUINEA","Kina","598","2"));
			currencyList.add(new CurrencyInfo("PARAGUAY","Guarani","600","0"));
			currencyList.add(new CurrencyInfo("PERU","Nuevo Sol","604","2"));
			currencyList.add(new CurrencyInfo("PHILIPPINES","Philippine Peso","608","2"));
			currencyList.add(new CurrencyInfo("PITCAIRN","New Zealand Dollar","554","2"));
			currencyList.add(new CurrencyInfo("POLAND","Zloty","985","2"));
			currencyList.add(new CurrencyInfo("PORTUGAL","euro","978","2"));
			currencyList.add(new CurrencyInfo("PORTUGAL","Portuguese Escudo","620","0"));
			currencyList.add(new CurrencyInfo("PUERTO RICO","US Dollar","951","2"));
			currencyList.add(new CurrencyInfo("QATAR","Qatari Rial","634","2"));
			currencyList.add(new CurrencyInfo("RÉUNION","euro","978","2"));
			currencyList.add(new CurrencyInfo("RÉUNION","French Franc","250","2"));
			currencyList.add(new CurrencyInfo("ROMANIA","Leu","642","2"));
			currencyList.add(new CurrencyInfo("RUSSIAN FEDERATION","Russian Ruble","810","2"));
			currencyList.add(new CurrencyInfo("RUSSIAN FEDERATION","Russian Ruble","643","2"));
			currencyList.add(new CurrencyInfo("RWANDA","Rwanda Franc","646","0"));
			currencyList.add(new CurrencyInfo("SAINT HELENA","Saint Helena Pound","654","2"));
			currencyList.add(new CurrencyInfo("SAINT KITTS AND NEVIS","East Caribbean Dollar","951","2"));
			currencyList.add(new CurrencyInfo("SAINT LUCIA","East Caribbean Dollar","951","2"));
			currencyList.add(new CurrencyInfo("SAINT PIERRE AND MIQUELON","euro","978","2"));
			currencyList.add(new CurrencyInfo("SAINT PIERRE AND MIQUELON","French Franc","250","2"));
			currencyList.add(new CurrencyInfo("SAINT VINCENT AND THE GRENADINES","East Caribbean Dollar","951","2"));
			currencyList.add(new CurrencyInfo("SAMOA","Tala","882","2"));
			currencyList.add(new CurrencyInfo("SAN MARINO","euro","978","2"));
			currencyList.add(new CurrencyInfo("SAN MARINO","Italian Lira","380","0"));
			currencyList.add(new CurrencyInfo("SÃO TOME AND PRINCIPE","Dobra","678","2"));
			currencyList.add(new CurrencyInfo("SAUDI ARABIA","Saudi Riyal","682","2"));
			currencyList.add(new CurrencyInfo("SENEGAL","CFA Franc","952","0"));
			currencyList.add(new CurrencyInfo("SEYCHELLES","Seychelles Rupee","690","2"));
			currencyList.add(new CurrencyInfo("SIERRA LEONE","Leone","694","2"));
			currencyList.add(new CurrencyInfo("SINGAPORE","Singapore Dollar","702","2"));
			currencyList.add(new CurrencyInfo("SLOVAKIA","Slovak Koruna","703","2"));
			currencyList.add(new CurrencyInfo("SLOVENIA","Tolar","705","2"));
			currencyList.add(new CurrencyInfo("SOLOMON ISLANDS","Solomon Islands Dollar","090","2"));
			currencyList.add(new CurrencyInfo("SOMALIA","Somali Shilling","706","2"));
			currencyList.add(new CurrencyInfo("SOUTH AFRICA","Rand","710","2"));
			currencyList.add(new CurrencyInfo("SPAIN","euro","978","2"));
			currencyList.add(new CurrencyInfo("SPAIN","Spanish Peseta","724","0"));
			currencyList.add(new CurrencyInfo("SRI LANKA","Sri Lanka Rupee","144","2"));
			currencyList.add(new CurrencyInfo("SUDAN","Sudanese Dinar","736","2"));
			currencyList.add(new CurrencyInfo("SURINAME","Suriname Guilder","740","2"));
			currencyList.add(new CurrencyInfo("SVALBARD AND JAN MAYEN","Norwegian Krone","578","2"));
			currencyList.add(new CurrencyInfo("SWAZILAND","Lilangeni","748","2"));
			currencyList.add(new CurrencyInfo("SWEDEN","Swedish Krona","752","2"));
			currencyList.add(new CurrencyInfo("SWITZERLAND","Swiss Franc","756","2"));
			currencyList.add(new CurrencyInfo("SYRIAN ARAB REPUBLIC","Syrian Pound","760","2"));
			currencyList.add(new CurrencyInfo("TAIWAN, PROVINCE OF CHINA","New Taiwan Dollar","901","2"));
			currencyList.add(new CurrencyInfo("TAJIKISTAN","Somoni","972","2"));
			currencyList.add(new CurrencyInfo("TANZANIA, UNITED REPUBLIC OF","Tanzanian Shilling","834","2"));
			currencyList.add(new CurrencyInfo("THAILAND","Baht","764","2"));
			currencyList.add(new CurrencyInfo("TOGO","CFA Franc","952","0"));
			currencyList.add(new CurrencyInfo("TOKELAU","New Zealand Dollar","554","2"));
			currencyList.add(new CurrencyInfo("TONGA","Pa’anga","776","2"));
			currencyList.add(new CurrencyInfo("TRINIDAD AND TOBAGO","Trinidad and Tobago Dollar","780","2"));
			currencyList.add(new CurrencyInfo("TUNISIA","Tunisian Dinar","788","2"));
			currencyList.add(new CurrencyInfo("TURKEY","Turkish Lira","792","0"));
			currencyList.add(new CurrencyInfo("TURKMENISTAN","Manat","795","2"));
			currencyList.add(new CurrencyInfo("TURKS AND CAICOS ISLANDS","US Dollar","840","2"));
			currencyList.add(new CurrencyInfo("TUVALU","Australian Dollar","036","2"));
			currencyList.add(new CurrencyInfo("UGANDA","Uganda Shilling","800","2"));
			currencyList.add(new CurrencyInfo("UKRAINE","Hryvnia","980","2"));
			currencyList.add(new CurrencyInfo("UNITED ARAB EMIRATES","Dirham","784","2"));
			currencyList.add(new CurrencyInfo("UNITED KINGDOM","Pound Sterling","826","2"));
			currencyList.add(new CurrencyInfo("UNITED STATES","US Dollar","840","2"));
			currencyList.add(new CurrencyInfo("URUGUAY","Peso Uruguayo","858","2"));
			currencyList.add(new CurrencyInfo("UZBEKISTAN","Uzbekistan Sum","860","2"));
			currencyList.add(new CurrencyInfo("VANUATU","Vatu","548","0"));
			currencyList.add(new CurrencyInfo("VENEZUELA","Bolivar","862","2"));
			currencyList.add(new CurrencyInfo("VIET NAM","Dong","704","2"));
			currencyList.add(new CurrencyInfo("VIRGIN ISLANDS (BRITISH)","US Dollar","840","2"));
			currencyList.add(new CurrencyInfo("VIRGIN ISLANDS (US)","US Dollar","840","2"));
			currencyList.add(new CurrencyInfo("WALLIS AND FUTUNA","CFP Franc","953","0"));
			currencyList.add(new CurrencyInfo("WESTERN SAHARA","Moroccan Dirham","504","2"));
			currencyList.add(new CurrencyInfo("YEMEN","Yemeni Rial","886","2"));
			currencyList.add(new CurrencyInfo("YUGOSLAVIA","Yugoslavian Dinar","891","2"));
			currencyList.add(new CurrencyInfo("ZAMBIA","Kwacha","894","2"));
			currencyList.add(new CurrencyInfo("ZIMBABWE","Zimbabwe Dollar","716","2"));
		}
	}
	
	public static boolean isCodeValid(String currencyCode) {
		Iterator it = new CurrencyCode().getCurrencyList().iterator();
		while (it.hasNext()) {
			CurrencyInfo code = (CurrencyInfo) it.next();
			logger.debug("Comparing currency information with " + code);
			if (code.getCurrency().equals(currencyCode)) {
				logger.debug("Currency supported");
				return true;
			}				
		}
		logger.debug("Currency NOT SUPPORTED");
		return false;
	}

	public static boolean isExponentValid(String currencyCode, String currencyExponent) {
		Iterator it = new CurrencyCode().getCurrencyList().iterator();
		while (it.hasNext()) {
			CurrencyInfo code = (CurrencyInfo) it.next();
			if ((code.getCurrency().equals(currencyCode)) && 
				(code.getCurrencyExponent().equals(currencyExponent))) {
				return true;
			}				
		}
		return false;
	}

	/**
	 * Method getExponentForCurrency.
	 * @param aCurrency
	 * @return String
	 */
	public static String getExponentForCurrency(String aCurrency) {
		Iterator it = new CurrencyCode().getCurrencyList().iterator();
		while (it.hasNext()) {
			CurrencyInfo code = (CurrencyInfo) it.next();
			if (code.getCurrency().equals(aCurrency)) {
				return code.getCurrencyExponent();
			}
		}				
		return "0";
	}

	/**
	 * Method getCurrencyList.
	 */
	private ArrayList getCurrencyList() {
		return CurrencyCode.currencyList;
	}

}
