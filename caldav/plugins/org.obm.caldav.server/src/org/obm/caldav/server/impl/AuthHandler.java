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

package org.obm.caldav.server.impl;

import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.server.share.Token;
import org.obm.caldav.utils.Base64;

public class AuthHandler {

	private final Log logger = LogFactory.getLog(AuthHandler.class);

	public Token doAuth(DavRequest request) {
		String authHeader = request.getHeader("Authorization");
		Token t = null;
		if (authHeader != null) {
			StringTokenizer st = new StringTokenizer(authHeader);
			if (st.hasMoreTokens()) {
				String basic = st.nextToken();
				if (basic.equalsIgnoreCase("Basic")) {
					String credentials = st.nextToken();
					String userPass = new String(Base64.decode(credentials
							.toCharArray()));
					int p = userPass.indexOf(":");
					if (p != -1) {
						String userId = userPass.substring(0, p);
						String loginAtDomain = getLoginAtDomain(userId);
						String password = userPass.substring(p + 1);
						t = new Token(loginAtDomain, password, request.getCalendarComponantName());
					}
				}
			}
		}
		return t;
	}

	private String getLoginAtDomain(String userID) {
		String uid = userID;
		int idx = uid.indexOf("\\");
		if (idx > 0) {
			if (!uid.contains("@")) {
				String domain = uid.substring(0, idx);
				logger.info("uid: " + uid + " domain: " + domain);
				uid = uid.substring(idx + 1) + "@" + domain;
			} else {
				uid = uid.substring(idx + 1);
			}
		}
		return uid;
	}
}
