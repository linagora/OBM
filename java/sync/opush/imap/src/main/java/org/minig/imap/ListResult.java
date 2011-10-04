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

import java.util.ArrayList;

public class ListResult extends ArrayList<ListInfo> {

	private static final long serialVersionUID = -1402141425321463033L;
	
	private char imapSeparator;
	
	public ListResult(int size) {
		super(size);
	}

	public char getImapSeparator() {
		return imapSeparator;
	}

	public void setImapSeparator(char imapSeparator) {
		this.imapSeparator = imapSeparator;
	}
	
	
	
}
