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
 *   MiniG.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.obm.caldav.obmsync;

import org.obm.caldav.utils.IniFile;


public class ObmSyncConfIni extends IniFile{

	public static final String OBM_SYNC_CONF = "/etc/minig/obmsync_conf.ini";
	
	public ObmSyncConfIni() {
		super(OBM_SYNC_CONF);
	}

	public String getPropertyValue(String property) {
		return getSetting(property);
	}

	@Override
	public String getCategory() {
		return "obmSync";
	}

}
