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

package org.obm.caldav.client.httpmethod;


import org.apache.commons.httpclient.methods.PostMethod;

/**
 * Adds a PROPFIND method to Apache Commons HTTPClient library. It's logically
 * just like a POST request, so we can just extend that class and change its
 * protocol verb (getName()).
 */
public class PropfindMethod extends PostMethod {
	/**
	 * @param url
	 *            target WebDAV resource
	 */
	public PropfindMethod(String url) {
		super(url);
	}

	public String getName() {
		return "PROPFIND";
	}
}
