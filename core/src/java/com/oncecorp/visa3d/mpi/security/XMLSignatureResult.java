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



package com.oncecorp.visa3d.mpi.security;

import org.w3c.dom.Document;

/**
 * Description: The XMLSignatureResult class encapsulates the return results 
 * from XMLSignature operations. 
 * 
 * @version 0.1 July 29, 2002
 * @author	Alan Zhang
 */
public class XMLSignatureResult {
	/** 
	 * Signed Element
	 * This attribute used by XML Signature signer
	 * */
	private Document signedDoc;

	/**
	 * Verify flag
	 */
	private boolean verified;

	/**
	 * Gets the signedDoc
	 * @return Returns a Document
	 */
	public Document getSignedDoc() {
		return signedDoc;
	}

	/**
	 * Sets the signedDoc
	 * @param signedDoc The signedDoc to set
	 */
	public void setSignedDoc(Document signedDoc) {
		this.signedDoc = signedDoc;
	}

	/**
	 * Gets the verified
	 * @return Returns a boolean
	 */
	public boolean getVerified() {
		return verified;
	}
	/**
	 * Sets the verified
	 * @param verified The verified to set
	 */
	public void setVerified(boolean verified) {
		this.verified = verified;
	}

}