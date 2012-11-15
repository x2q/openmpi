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

package com.oncecorp.visa3d.mpi.utility;

import java.util.ArrayList;

/**
 * This utility class performs the validation process for multiple credit cards by
 * applying the MOD10 check algorithm on the provided card number.
 * 
 * To use this utility, you must supply:
 * 		CreditCardNumber
 *		AcceptedType:
 * 			All 
 * or any combination of:
 * 			Diners Club
 * 			American Express
 * 			JCB
 * 			Carte Blanche
 * 			Visa
 * 			MasterCard
 * 			Discover/Novus
 * 
 * @author Martin Dufort (mdufort@oncecorp.com)
 */
public final class CardValidator {
	// Definition of constants for accepted cards
	public static final String ACCEPT_ALL 		= "All";
	public static final String ACCEPT_DINERS 	= "Diners Club";
	public static final String ACCEPT_AUST 		= "Australian Bankcard";
	public static final String ACCEPT_AMEX 		= "American Express";
	public static final String ACCEPT_JCB 		= "JCB";
	public static final String ACCEPT_CB 		= "Carte Blanche";
	public static final String ACCEPT_VISA 		= "Visa";
	public static final String ACCEPT_MC 		= "MasterCard";
	public static final String ACCEPT_DISCOVER 	= "Discover/Novus";

	// Internal instance variable for validation processing
	private String cardNumber = "";
	private String type = "";
	private String error = "";
	private int numberLeft = 0;

	public CardValidator() {
		super();
	}

	/**
	 * Validate the credit card number against a list of accepted types.
	 * If card is not valid, requesting task can retrieve error message by executing
	 * <code>cardvalidator.getError()</code>
	 * 
	 * @param aCardNumber	Credit Card Number to validate
	 * @param acceptedType	List of accepted types for validation
	 * @return Card is valid (true) or not (false)
	 */
	public boolean isValid(String aCardNumber, ArrayList acceptedType) {
		this.cardNumber = aCardNumber;
		return checkCardNumber(acceptedType);
	}

	/**
	 * Return error message associated with the card validation
	 * @return Validation error message
	 */
	public String getError() {
		return error;
	}

	/**
	 * Validatio is done with the MOD10 Validation algorithm
	 * Here's the algorithm:
	 * 1) Strips out all the non numeric characters and checks the number of digits.  
	 *    If number of digits isn't 13, 15, or 16, rejects the card. 
	 * 2) Checks the length of the number versus the type.  
	 * 3) Checks the first 4-digit of the card number versus the type.  
	 * 4) Checks the card number versus the MOD10 algorithm. 
	 * 	  The MOD10 algorithm works by utilizing a single digit doubling system on 
	 *    every other digit starting on the first digit on sixteen digit cards, 
	 *    and on the second digit on thirteen and fifteen digit cards.   
	 *    [i.e. five doubles into one. (5x2 = 10, 1+0=1)  Eight doubles into seven.  (8x2=16, 1+6=7)  
	 *    Four doubles into eight. (4x2=8, 0+8=8)]  
	 *    Next, the MOD10 algorithm adds the resulting doubled digits together along with every other
	 *    digits and verifies the result is divisible by ten
	 *
	 * @return Valid or Not
	 */
	private boolean checkCardNumber(ArrayList accepted) {
		this.numberLeft = 0;

		//  Catch malformed input.
		if (this.cardNumber == null || this.cardNumber.length() == 0) {
			this.cardNumber = "";
			this.error = "The credit card number is not formed properly.";
			return false;
		}

		// Ensure accepted types is valid
		if (accepted == null) {
			this.cardNumber = "";
			this.error = "Invalid accepted credit cards supplied.";
			return false;
		}

		//  Ensure number doesn't overflow.
		if (this.cardNumber.length() > 30) {
			this.cardNumber = this.cardNumber.substring(0, 30);
		}

		//  Remove non-numeric characters.
		int numberLength = this.cardNumber.length();
		String Temp = this.cardNumber.toString();
		this.cardNumber = "";
		int digit = 0;
		for (; digit < numberLength; digit++) {
			String present = Temp.substring(digit, digit + 1);
			try {
				Integer.parseInt(present);
				this.cardNumber = this.cardNumber.concat(present);
			}
			catch (NumberFormatException e) {
				//  Drop it.
			}
		}

		// Set up variables.
		numberLength = this.cardNumber.length(); // recalculate after stripping non-numeric if any
		this.numberLeft = Integer.parseInt(this.cardNumber.substring(0, 4));
		int shouldLength = 0;

		//  Determine the card type and appropriate length.
		if (this.numberLeft >= 3000 && this.numberLeft <= 3059) {
			this.type = ACCEPT_DINERS;
			shouldLength = 14;
		}
		else if (this.numberLeft >= 3600 && this.numberLeft <= 3699) {
			this.type = ACCEPT_DINERS;
			shouldLength = 14;
		}
		else if (this.numberLeft >= 3800 && this.numberLeft <= 3889) {
			this.type = ACCEPT_DINERS;
			shouldLength = 14;

		}
		else if (this.numberLeft >= 3400 && this.numberLeft <= 3499) {
			this.type = ACCEPT_AMEX;
			shouldLength = 15;
		}
		else if (this.numberLeft >= 3700 && this.numberLeft <= 3799) {
			this.type = ACCEPT_AMEX;
			shouldLength = 15;

		}
		else if (this.numberLeft >= 3528 && this.numberLeft <= 3589) {
			this.type = ACCEPT_JCB;
			shouldLength = 16;

		}
		else if (this.numberLeft >= 3890 && this.numberLeft <= 3899) {
			this.type = ACCEPT_CB;
			shouldLength = 14;

		}
		else if (this.numberLeft >= 4000 && this.numberLeft <= 4999) {
			this.type = ACCEPT_VISA;
			/* 
			 * [Martin's Note: Dec 11, 2002 2:53:27 PM]
			 * Adding validation support for 19 digits Visa card numbers 
			 * 
			 */
			if (numberLength > 18) {
				shouldLength = 19;
			}
			else if (numberLength > 14) {
				shouldLength = 16;
			}
			else if (numberLength < 14) {
				shouldLength = 13;
			}
		}
		else if (this.numberLeft >= 5100 && this.numberLeft <= 5599) {
			this.type = ACCEPT_MC;
			shouldLength = 16;

		}
		else if (this.numberLeft == 5610) {
			this.type = ACCEPT_AUST;
			shouldLength = 16;

		}
		else if (this.numberLeft == 6011) {
			this.type = ACCEPT_DISCOVER;
			shouldLength = 16;

		}
		else {
			this.type = "";
			this.error =
				"First four digits, " + this.numberLeft + ", indicate we don't accept that type of card.";
			return false;
		}

		//  Do you accept this type of card?
		if (!accepted.contains(ACCEPT_ALL)) {
			if (!accepted.contains(this.type)) {
				this.error = "We don't accept " + this.type + " cards.";
				return false;
			}
		}

		//  Is the length correct?
		if (numberLength != shouldLength) {
			int missing = numberLength - shouldLength;
			if (missing < 0) {
				this.error = "Number is missing " + Math.abs(missing) + " digit(s).";
			}
			else {
				this.error = "Number has " + missing + " too many digit(s).";
			}
			return false;
		}

		//  Begin the MOD10 checksum process.
		int checksum = 0;

		//  Add odd digits in even length strings or even digits in odd length strings.
		int location = 0;
		for (location = 1 - (numberLength % 2); location < numberLength; location += 2) {
			digit = Integer.parseInt(this.cardNumber.substring(location, location + 1));
			checksum += digit;
		}

		//  Double (and normalize) odd digits in odd length strings or even digits in even length strings.
		for (location = (numberLength % 2); location < numberLength; location += 2) {
			digit = Integer.parseInt(this.cardNumber.substring(location, location + 1)) * 2;
			if (digit < 10) {
				checksum += digit;
			}
			else {
				checksum += digit - 9;
			}
		}

		//  If the checksum is divisible by 10, the number passes.
		if (checksum % 10 == 0) {
			this.error = "";
			return true;
		}
		else {
			this.error = "Card failed the checksum test.";
			return false;
		}
	}
	
	/**
	 * Returns the type.
	 * @return String
	 */
	public String getType() {
		return type;
	}

	public static void main(String args[]) {
		CardValidator v = new CardValidator();
		ArrayList types = new ArrayList();
		types.add(ACCEPT_ALL);
		 
		if (v.isValid(args[0], types) == false) {
			System.out.println("VALIDATION ERROR: " + v.getError());
		}
		else {
			System.out.println("Validation for card type:"+v.getType()+" "+args[0]+" OK");
		}
	}
	

}