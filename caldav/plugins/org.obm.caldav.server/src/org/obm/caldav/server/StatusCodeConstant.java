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

package org.obm.caldav.server;

import java.util.HashMap;
import java.util.Map;

public class StatusCodeConstant {
	
	private static Map<Integer, String> messages;
	
	static{
		messages = new HashMap<Integer, String>();
		messages.put(207, "Multi-Status");
		
		messages.put(403, "Forbidden");
		messages.put(404, "Not Found");
		messages.put(405, "Not Allowed");
		messages.put(424, "Method failure");

		messages.put(500, "Internal Server Error");
		messages.put(501, "Not Implemented");
	}
	
	
	public static int SC_MULTI_STATUS = 207;
	
	
	public static int SC_FORBIDDEN = 403;
	public static int SC_NOT_FOUND = 404;
	public static int SC_NOT_ALLOWED = 405;
	public static int SC_METHOD_FAILURE = 424;
	
	public static int SC_INTERNAL_SERVER_ERROR = 500;
	public static int SC_NOT_IMPLEMENTED= 501;


	public static String getStatusMessage(int code){
		return messages.get(code);
	}
}
