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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.minig.imap.InternalDate;
import org.minig.imap.impl.IMAPResponse;
import org.minig.imap.impl.MessageSet;

public class UIDFetchInternalDateCommand extends Command<InternalDate[]> {

	private Collection<Long> uids;
	DateFormat df;

	public UIDFetchInternalDateCommand(Collection<Long> uid) {
		this.uids = uid;
		//22-Mar-2010 14:26:18 +0100
		df = new SimpleDateFormat("d-MMM-yyyy HH:mm:ss Z", Locale.ENGLISH);
	}

	@Override
	protected CommandArgument buildCommand() {

		StringBuilder sb = new StringBuilder();
		if (!uids.isEmpty()) {
			sb.append("UID FETCH ");
			sb.append(MessageSet.asString(uids));
			sb.append(" (UID INTERNALDATE)");
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
			data = new InternalDate[0];
			return;
		}
		
		IMAPResponse ok = rs.get(rs.size() - 1);
		if (ok.isOk()) {
			data = new InternalDate[rs.size() - 1];
			Iterator<IMAPResponse> it = rs.iterator();
			for (int i = 0; i < rs.size() - 1; i++) {
				IMAPResponse r = it.next();
				String payload = r.getPayload();

				int fidx = payload.indexOf("INTERNALDATE \"") + "INTERNALDATE \"".length();
				
				if (fidx == -1 + "INTERNALDATE \"".length()) {
					continue;
				}
				
				int endDate = payload.indexOf("\"", fidx);
				String internalDate = "";
				if (fidx > 0 && endDate >= fidx) {
					internalDate = payload.substring(fidx, endDate);
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

				data[i] = new InternalDate(uid,parseDate(internalDate));
			}
		} else {
			logger.warn("error on fetch: " + ok.getPayload());
			data = new InternalDate[0];
		}
	}

	private Date parseDate(String date) {
		try {
			return df .parse(date);
		} catch (ParseException e) {
			logger.error("Can't parse "+date);
		}
		return new Date();
	}

}
