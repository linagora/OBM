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

public class CommandArgument {

	private String CommandString;
	private byte[] literalData;

	public CommandArgument(String s, byte[] literalData) {
		this.CommandString = s;
		this.literalData = literalData;
	}

	public String getCommandString() {
		return CommandString;
	}

	public void setCommandString(String s) {
		this.CommandString = s;
	}

	public byte[] getLiteralData() {
		return literalData;
	}

	public void setLiteralData(byte[] literalData) {
		this.literalData = literalData;
	}

	public boolean hasLiteralData() {
		return literalData != null;
	}

}
