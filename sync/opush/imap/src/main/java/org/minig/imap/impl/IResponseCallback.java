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

package org.minig.imap.impl;

import org.minig.imap.IMAPException;

public interface IResponseCallback {

	void connected();

	void disconnected();

	void imapResponse(MinaIMAPMessage imapResponse);

	void setClient(ClientSupport cs);

	void exceptionCaught(IMAPException cause) throws IMAPException;

}
