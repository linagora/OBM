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

public class IMAPException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 673222847178210893L;

	public IMAPException(Throwable e) {
		super(e);
	}
	
	public IMAPException(String s, Throwable e) {
		super(s, e);
	}
	
	public IMAPException(String s) {
		super(s);
	}
	

}
