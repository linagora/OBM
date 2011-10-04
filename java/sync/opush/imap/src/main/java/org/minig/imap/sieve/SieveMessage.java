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

package org.minig.imap.sieve;

import java.util.LinkedList;
import java.util.List;

public class SieveMessage {
	
	private List<String> lines;
	
	public SieveMessage() {
		lines = new LinkedList<String>();
	}
	
	public void addLine(String s) {
		lines.add(s);
	}

	public List<String> getLines() {
		return lines;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(":\n");
		for (String l :lines) {
			sb.append(l);
			sb.append("\n");
		}
		return sb.toString();
	}

}
