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

import javax.servlet.http.HttpServletResponse;

public class OptionsHandler extends DavMethodHandler {

	@Override
	public void process(Token token, DavRequest req, HttpServletResponse resp) {

		resp.setStatus(HttpServletResponse.SC_OK);
		resp.addHeader("DAV", "1, calendar-access, calendar-schedule");
		resp.addHeader("Allow", "OPTIONS, GET, HEAD, POST, DELETE, TRACE, PROPPATCH, COPY, MOVE, LOCK, UNLOCK");
		resp.addHeader("MS-Author-Via", "DAV");
	}

}
