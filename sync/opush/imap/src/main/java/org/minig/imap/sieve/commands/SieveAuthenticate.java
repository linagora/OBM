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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.minig.imap.sieve.SieveArg;
import org.minig.imap.sieve.SieveCommand;
import org.minig.imap.sieve.SieveResponse;

public class SieveAuthenticate extends SieveCommand<Boolean> {

	private String login;
	private String password;
	private byte[] encoded;

	public SieveAuthenticate(String login, String password) {
		this.login = login;
		this.password = password;
		this.encoded = encodeAuthString(login, password);
		this.retVal = Boolean.FALSE;
	}

	@Override
	public void responseReceived(List<SieveResponse> rs) {
		if (commandSucceeded(rs)) {
			retVal = true;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("response received for login " + login + " " + password
					+ " => " + retVal);
		}
	}

	@Override
	protected List<SieveArg> buildCommand() {
		List<SieveArg> ret = new ArrayList<SieveArg>(2);

		ret.add(new SieveArg("AUTHENTICATE \"PLAIN\"".getBytes(), false));
		ret.add(new SieveArg(encoded, true));

		return ret;
	}

	private byte[] encodeAuthString(String login, String password) {
		byte[] log = login.getBytes();
		byte[] pass = password.getBytes();
		byte[] data = new byte[log.length * 2 + pass.length + 2];
		int i = 0;
		for (int j = 0; j < log.length; j++) {
			data[i++] = log[j];
		}
		data[i++] = 0x0;
		for (int j = 0; j < log.length; j++) {
			data[i++] = log[j];
		}
		data[i++] = 0x0;

		for (int j = 0; j < pass.length; j++) {
			data[i++] = pass[j];
		}

		ByteBuffer encoded = ByteBuffer.allocate(data.length);
		encoded.put(data);
		encoded.flip();

		String ret = Base64.encodeBase64String(encoded.array());
		if (logger.isDebugEnabled()) {
			logger.info("encoded auth string: " + ret);
		}
		return ret.getBytes();
	}

}
