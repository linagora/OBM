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

package org.obm.caldav.utils;

import java.net.MalformedURLException;
import java.util.UUID;


public class CalDavUtils {
	
	public static String getExtIdFromURL(String url) throws MalformedURLException{
		String extId = "";
		int indexIcs= url.lastIndexOf("/");
		if(indexIcs != -1){
			extId = url.substring(indexIcs+1);
			extId = extId.replace(".ics", "");
		}
		if(extId == null && "".equals(extId)){
			throw new MalformedURLException("Invalid URI[ "+url+ "]");	
		}
		return extId;
	}
	
	public static String generateExtId() {
			
		return "caldav-"+UUID.randomUUID();
	}
}
