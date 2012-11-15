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

public final class URLValidator {

	public static boolean isValid(String urlSpec) {
		/* 
		 * [Martin's Note: Oct 4, 2002 11:31:28 AM]
		 * 
		 * The following code to validate the URL has been copied form the URL.java source code
		 * provided with the JDK1.3.1_04...Copyright and acknowledgements (Sun Microsystems)
		 * 
		 */
		String original = urlSpec;
		int i, limit, c;
		int start = 0;
		String protocol = null;
		boolean aRef = false;

		if (urlSpec == null) return false;
		limit = urlSpec.length();
		while ((limit > 0) && (urlSpec.charAt(limit - 1) <= ' ')) {
			limit--; //eliminate trailing whitespace
		}
		while ((start < limit) && (urlSpec.charAt(start) <= ' ')) {
			start++; // eliminate leading whitespace
		}

		if (urlSpec.regionMatches(true, start, "url:", 0, 4)) {
			start += 4;
		}

		if (start < urlSpec.length() && urlSpec.charAt(start) == '#') {
			/* we're assuming this is a ref relative to the context URL.
			 * This means protocols cannot start w/ '#', but we must parse
			 * ref URL's like: "hello:there" w/ a ':' in them.
			 */
			aRef = true;
		}
		for (i = start; !aRef && (i < limit) && ((c = urlSpec.charAt(i)) != '/'); i++) {
			if (c == ':') {
				protocol = urlSpec.substring(start, i).toLowerCase();
			}
		}

		// Validate protocol. Cannot be null and must be either http or https
		if (protocol == null) {
			return false;
		}
		
		if (!protocol.equalsIgnoreCase("http") &&
			(!protocol.equalsIgnoreCase("https")) ) {
			return false;
		}
		return true;
	}
}
