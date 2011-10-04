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

public class SieveArg {

	private byte[] raw;
	private boolean literal;

	public SieveArg(byte[] raw, boolean literal) {
		this.raw = raw;
		this.literal = literal;
	}

	public byte[] getRaw() {
		return raw;
	}

	public void setRaw(byte[] raw) {
		this.raw = raw;
	}

	public boolean isLiteral() {
		return literal;
	}

	public void setLiteral(boolean literal) {
		this.literal = literal;
	}

}
