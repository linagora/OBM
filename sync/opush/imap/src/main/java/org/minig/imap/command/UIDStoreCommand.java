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

import java.util.Collection;
import java.util.List;

import org.minig.imap.FlagsList;
import org.minig.imap.impl.IMAPResponse;
import org.minig.imap.impl.MessageSet;

public class UIDStoreCommand extends Command<Boolean> {

	private Collection<Long> uids;
	private FlagsList fl;
	private boolean set;

	public UIDStoreCommand(Collection<Long> uids, FlagsList fl, boolean set) {
		this.uids = uids;
		this.fl = fl;
		this.set = set;
	}

	@Override
	public void responseReceived(List<IMAPResponse> rs) {
		IMAPResponse ok = rs.get(rs.size() - 1);
		data = ok.isOk();
		if (logger.isDebugEnabled()) {
			logger.debug(ok.getPayload());
		}
	}

	@Override
	protected CommandArgument buildCommand() {
		String cmd = "UID STORE " + MessageSet.asString(uids) + " "
				+ (set ? "+" : "-") + "FLAGS.SILENT " + fl.toString();

		if (logger.isDebugEnabled()) {
			logger.debug("cmd: " + cmd);
		}

		return new CommandArgument(cmd, null);
	}

}
