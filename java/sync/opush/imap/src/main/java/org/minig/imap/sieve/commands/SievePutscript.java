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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.minig.imap.sieve.SieveArg;
import org.minig.imap.sieve.SieveCommand;
import org.minig.imap.sieve.SieveResponse;
import org.obm.push.utils.FileUtils;

public class SievePutscript extends SieveCommand<Boolean> {

	private String name;
	private byte[] data;

	public SievePutscript(String name, InputStream scriptContent) {
		retVal = false;
		this.name = name;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			FileUtils.transfer(scriptContent, out, true);
			this.data = out.toByteArray();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

	}

	@Override
	protected List<SieveArg> buildCommand() {
		List<SieveArg> args = new ArrayList<SieveArg>(1);
		args
				.add(new SieveArg(("PUTSCRIPT \"" + name + "\"").getBytes(),
						false));
		args.add(new SieveArg(data, true));
		return args;
	}

	@Override
	public void responseReceived(List<SieveResponse> rs) {
		logger.info("putscript response received.");
		if (commandSucceeded(rs)) {
			retVal = true;
		} else {
			for (SieveResponse sr : rs) {
				logger.error(sr.getData());
			}
		}
	}

}
