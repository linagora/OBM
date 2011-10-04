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

import java.util.List;
import java.util.concurrent.Semaphore;

import org.apache.mina.common.IoSession;
import org.minig.imap.impl.IMAPResponse;
import org.minig.imap.impl.TagProducer;

public interface ICommand<T> {

	void execute(IoSession s, TagProducer tp, Semaphore lock,
			List<IMAPResponse> lastResponses);

	void responseReceived(List<IMAPResponse> rs);

	T getReceivedData();

}
