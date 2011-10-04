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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.minig.imap.IMAPHeaders;
import org.minig.imap.command.parser.HeadersParser;
import org.minig.imap.impl.IMAPResponse;
import org.minig.imap.impl.MessageSet;

public class UIDFetchHeadersCommand extends Command<Collection<IMAPHeaders>> {

	private Collection<Long> uids;
	private String[] headers;

	public UIDFetchHeadersCommand(Collection<Long> uid, String[] headers) {
		this.uids = uid;
		this.headers = headers;
	}

	@Override
	protected CommandArgument buildCommand() {
		StringBuilder sb = new StringBuilder();
		if (!uids.isEmpty()) {
			sb.append("UID FETCH ");
			sb.append(MessageSet.asString(uids));
			sb.append(" (UID BODY.PEEK[HEADER.FIELDS (");
			for (int i = 0; i < headers.length; i++) {
				if (i > 0) {
					sb.append(" ");
				}
				sb.append(headers[i].toUpperCase());
			}
			sb.append(")])");
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
			data = new ArrayList<IMAPHeaders>(uids.size());
			Iterator<IMAPResponse> it = rs.iterator();
			for (int i = 0; it.hasNext() && i < uids.size(); ) {
				IMAPResponse r = it.next();
				String payload = r.getPayload();
				if (!payload.contains(" FETCH")) {
					logger.warn("not a fetch: "+payload);
					continue;
				}
				int uidIdx = payload.indexOf("(UID ") + "(UID ".length();
				int endUid = payload.indexOf(' ', uidIdx);
				String uidStr = payload.substring(uidIdx, endUid);
				long uid = 0;
				try {
					uid = Long.parseLong(uidStr);
				} catch (NumberFormatException nfe) {
					logger.error("cannot parse uid for string '" + uid
							+ "' (payload: " + payload + ")");
					continue;
				}

				Map<String, String> rawHeaders = Collections.emptyMap();

				InputStream in = r.getStreamData();
				if (in != null) {
					try {
						InputStreamReader reader = new InputStreamReader(in);
						rawHeaders = new HeadersParser().parseRawHeaders(reader);
					} catch (IOException e) {
						logger.error("Error reading headers stream", e);
					} catch (Throwable t) {
						logger.error("error parsing headers stream", t);
					}
				} else {
					// cyrus search command can return uid's that no longer exist in the mailbox
					logger.warn("cyrus did not return any header for uid " + uid);
				}

				IMAPHeaders imapHeaders = new IMAPHeaders();
				imapHeaders.setUid(uid);
				imapHeaders.setRawHeaders(rawHeaders);
				data.add(imapHeaders);
				i++;
			}
		} else {
			logger.warn("error on fetch: " + ok.getPayload());
			data = Collections.emptyList();
		}
	}

}
