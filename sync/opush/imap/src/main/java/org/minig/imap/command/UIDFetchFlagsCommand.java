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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.minig.imap.Flag;
import org.minig.imap.FlagsList;
import org.minig.imap.impl.IMAPResponse;
import org.minig.imap.impl.MessageSet;

public class UIDFetchFlagsCommand extends Command<Collection<FlagsList>> {

	private Collection<Long> uids;

	public UIDFetchFlagsCommand(Collection<Long> uid) {
		this.uids = uid;
	}

	@Override
	protected CommandArgument buildCommand() {

		StringBuilder sb = new StringBuilder();
		if (!uids.isEmpty()) {
			sb.append("UID FETCH ");
			sb.append(MessageSet.asString(uids));
			sb.append(" (UID FLAGS)");
		} else {
			sb.append("NOOP");
		}
		String cmd = sb.toString();
		if (logger.isDebugEnabled()) {
			logger.debug("cmd: " + cmd);
		}
		CommandArgument args = new CommandArgument(cmd, null);
		return args;
	}

	@Override
	public void responseReceived(List<IMAPResponse> rs) {
		if (uids.isEmpty()) {
			data = Collections.emptyList();
			return;
		}
		
		IMAPResponse ok = rs.get(rs.size() - 1);
		if (ok.isOk()) {
			ArrayList<FlagsList> list = new ArrayList<FlagsList>(rs.size() - 1);
			Iterator<IMAPResponse> it = rs.iterator();
			for (int i = 0; i < rs.size() - 1; i++) {
				IMAPResponse r = it.next();
				String payload = r.getPayload();

				int fidx = payload.indexOf("FLAGS (") + "FLAGS (".length();
				
				if (fidx == -1 + "FLAGS (".length()) {
					continue;
				}
				
				int endFlags = payload.indexOf(")", fidx);
				String flags = "";
				if (fidx > 0 && endFlags >= fidx) {
					flags = payload.substring(fidx, endFlags);
				} else {
					logger.error("Failed to get flags in fetch response: "
							+ payload);
				}

				int uidIdx = payload.indexOf("UID ") + "UID ".length();
				int endUid = uidIdx;
				while (Character.isDigit(payload.charAt(endUid))) {
					endUid++;
				}
				long uid = Long.parseLong(payload.substring(uidIdx, endUid));

				// logger.info("payload: " + r.getPayload()+" uid: "+uid);

				FlagsList flagsList = new FlagsList();
				parseFlags(flags, flagsList);
				flagsList.setUid(uid);
				list.add(flagsList);
			}
			data = list;
		} else {
			logger.warn("error on fetch: " + ok.getPayload());
			data = Collections.emptyList();
		}
	}

	private void parseFlags(String flags, FlagsList flagsList) {
		// TODO this is probably slow as hell
		if (flags.contains("\\Seen")) {
			flagsList.add(Flag.SEEN);
		}
		if (flags.contains("\\Flagged")) {
			flagsList.add(Flag.FLAGGED);
		}
		if (flags.contains("\\Deleted")) {
			flagsList.add(Flag.DELETED);
		}
		if (flags.contains("\\Answered")) {
			flagsList.add(Flag.ANSWERED);
		}
	}

}
