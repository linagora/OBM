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
import java.util.List;

import org.minig.imap.sieve.SieveArg;
import org.minig.imap.sieve.SieveCommand;
import org.minig.imap.sieve.SieveResponse;

/**
 * cyrus 2.3.X only :'(
 * 
 * @author tom
 *
 */
public class SieveUnauthenticate extends SieveCommand<Boolean> {

	@Override
	protected List<SieveArg> buildCommand() {
		List<SieveArg> args = new ArrayList<SieveArg>(1);
		args.add(new SieveArg("UNAUTHENTICATE".getBytes(), false));
		return args;
	}

	@Override
	public void responseReceived(List<SieveResponse> rs) {
		if (!commandSucceeded(rs)) {
			for (SieveResponse sr : rs) {
				logger.error(sr.getData());
			}
		}
	}

}
