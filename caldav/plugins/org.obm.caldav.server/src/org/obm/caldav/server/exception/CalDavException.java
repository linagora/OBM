/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   obm.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.obm.caldav.server.exception;

import org.obm.caldav.server.StatusCodeConstant;

/**
 * 
 * @author adrienp
 *
 */
public class CalDavException extends Exception{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7956416583930247999L;
	
	private int httpStatusCode;
	
	public CalDavException(int httpStatusCode) {
		this.httpStatusCode = httpStatusCode;
	}
	
	public int getHttpStatusCode() {
		return httpStatusCode;
	}
	
	public String getMessage(){
		return StatusCodeConstant.getStatusMessage(httpStatusCode);
	}

}
