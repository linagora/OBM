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

package org.minig.imap.sieve.commands;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.minig.imap.sieve.SieveArg;
import org.minig.imap.sieve.SieveCommand;
import org.minig.imap.sieve.SieveResponse;
import org.minig.imap.sieve.SieveScript;

public class SieveListscripts extends SieveCommand<List<SieveScript>> {

	public SieveListscripts() {
		retVal = new LinkedList<SieveScript>();
	}

	@Override
	protected List<SieveArg> buildCommand() {
		List<SieveArg> args = new ArrayList<SieveArg>(1);
		args.add(new SieveArg("LISTSCRIPTS".getBytes(), false));
		return args;
	}

	@Override
	public void responseReceived(List<SieveResponse> rs) {
		if (commandSucceeded(rs)) {
			String[] list = rs.get(0).getData().split("\r\n");
			for (int i = 0; i < list.length - 1; i++) {
				boolean active = list[i].endsWith("ACTIVE");
				int idx = list[i].lastIndexOf("\"");
				if (idx > 0) {
					String name = list[i].substring(1, idx);
					retVal.add(new SieveScript(name, active));
				} else {
					logger.warn("receveid from listscripts: '" + list[i] + "'");
				}
			}
		} else {
			reportErrors(rs);
		}
		logger
				.info("returning a list of " + retVal.size()
						+ " sieve script(s)");
	}

}
