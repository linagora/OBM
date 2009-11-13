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

package org.obm.caldav.obmsync;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.server.IBackend;
import org.obm.caldav.server.IBackendFactory;
import org.obm.caldav.server.share.Token;

public class BackendFactory implements IBackendFactory {

	private static final Log logger = LogFactory.getLog(BackendFactory.class);

	@Override
	public IBackend loadBackend(Token token) throws Exception {
		logger.info("Loading OBM backend");
		return new OBMBackend(token);
	}

}
