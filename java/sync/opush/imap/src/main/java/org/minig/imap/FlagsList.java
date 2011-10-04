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

package org.minig.imap;

import java.util.HashSet;
import java.util.Iterator;

public class FlagsList extends HashSet<Flag> {

	private long uid;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8557645090248136216L;

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");

		Iterator<Flag> it = iterator();
		for (int i = size() - 1; i >= 0; i--) {
			if (i > 0) {
				sb.append(' ');
			}
			sb.append(it.next().toString());
		}

		sb.append(")");
		return sb.toString();
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

}
