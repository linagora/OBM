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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.minig.imap.impl.IMAPResponse;

public class CapabilityCommand extends SimpleCommand<Set<String>> {

	public CapabilityCommand() {
		super("CAPABILITY");
	}


	@Override
	public void responseReceived(List<IMAPResponse> rs) {
		data = new HashSet<String>();
		String[] parts = rs.get(0).getPayload().split(" ");
		for (int i = 2; i < parts.length; i++) {
			data.add(parts[i]);
		}
	}

}
