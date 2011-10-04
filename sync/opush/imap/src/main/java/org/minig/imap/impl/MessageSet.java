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

package org.minig.imap.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageSet {

	private final static Logger logger = LoggerFactory
			.getLogger(MessageSet.class);

	public static final String asString(Collection<Long> uids) {
		TreeSet<Long> sortedUids = new TreeSet<Long>(uids);
		StringBuilder sb = new StringBuilder(uids.size() * 7);
		long firstUid = 0;
		long lastUid = 0;
		boolean firstLoop = true;
		for (Long currentValue: sortedUids) {
			if (firstUid > 0 && currentValue == lastUid + 1) {
				lastUid = currentValue;
				firstLoop = false;
				continue;
			}
			if (firstUid > 0 && lastUid > 0 && lastUid > firstUid) {
				sb.append(':');
				sb.append(lastUid);
				firstUid = 0;
				lastUid = 0;
			}
			if (!firstLoop) {
				sb.append(',');
			}
			sb.append(currentValue);
			firstUid = currentValue;
			lastUid = currentValue;
			firstLoop = false;
		}
		if (firstUid > 0 && lastUid > 0 && lastUid > firstUid) {
			sb.append(':');
			sb.append(lastUid);
		}

		String ret = sb.toString();
		if (logger.isDebugEnabled()) {
			logger.debug("computed set string: " + ret);
		}
		return ret;

	}

	public static ArrayList<Long> asLongCollection(String set, int sizeHint) {
		String[] parts = set.split(",");
		ArrayList<Long> ret = new ArrayList<Long>(sizeHint > 0 ? sizeHint
				: parts.length);
		for (String s : parts) {
			if (!s.contains(":")) {
				ret.add(Long.parseLong(s));
			} else {
				String[] p = s.split(":");
				long start = Long.parseLong(p[0]);
				long end = Long.parseLong(p[1]);
				for (long l = start; l <= end; l++) {
					ret.add(l);
				}
			}
		}
		return ret;
	}

}
