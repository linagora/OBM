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

package org.minig.imap.command;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.minig.imap.impl.IMAPResponse;

public class UIDFetchPartCommand extends Command<InputStream> {

	private long uid;
	private String section;

	public UIDFetchPartCommand(long uid, String section) {
		this.uid = uid;
		this.section = section;
	}

	@Override
	protected CommandArgument buildCommand() {
		String cmd = "UID FETCH " + uid + " (UID BODY.PEEK[" + section + "])";
		CommandArgument args = new CommandArgument(cmd, null);
		return args;
	}

	@Override
	public void responseReceived(List<IMAPResponse> rs) {
		if (logger.isDebugEnabled()) {
			for (IMAPResponse r : rs) {
				logger.debug("r: " + r.getPayload() + " [stream:"
						+ (r.getStreamData() != null) + "]");
			}
		}
		IMAPResponse stream = rs.get(0);
		IMAPResponse ok = rs.get(rs.size() - 1);
		if (ok.isOk() && stream.getStreamData() != null) {
			data = stream.getStreamData();
		} else {
			if (ok.isOk()) {
				data = new ByteArrayInputStream("[empty]".getBytes());
			} else {
				logger.warn("Fetch of part " + section + " in uid " + uid
						+ " failed: " + ok.getPayload());
			}
		}
	}

}
