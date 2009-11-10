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

package org.obm.caldav.server.share;

public class Token {

	private String login;
	private String domain;
	private String loginAtDomain;
	private String password;
	private String calendarName;
	private String calendarNameAtDomain;

	public Token(String loginAtDomain, String password, String calendarName) {
		super();
		this.loginAtDomain = loginAtDomain;
		if(loginAtDomain.contains("@")){
			String[] tab = loginAtDomain.split("@");
			login = tab[0];
			domain = tab[1];
		} else {
			this.login = loginAtDomain;
		}
		
		this.password = password;
		
		if(calendarName.contains("@")){
			String[] tab = calendarName.split("@");
			this.calendarName = tab[0];
		} else {
			this.calendarName = calendarName;
		}
		
		this.calendarNameAtDomain = calendarName;
	}

	public String getLogin() {
		return login;
	}

	public String getDomain() {
		return domain;
	}

	public String getLoginAtDomain() {
		return loginAtDomain;
	}

	public void setLoginAtDomain(String loginAtDomain) {
		this.loginAtDomain = loginAtDomain;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getCalendarName() {
		return calendarName;
	}
	
	public String getCalendarNameAtDomain() {
		return calendarNameAtDomain;
	}
	
}
