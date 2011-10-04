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

import org.minig.imap.NameSpaceInfo;
import org.minig.imap.command.parser.NamespaceParser;
import org.minig.imap.impl.IMAPResponse;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.support.ParsingResult;

public class NamespaceCommand extends SimpleCommand<NameSpaceInfo> {

	private static final NamespaceParser parser = Parboiled.createParser(NamespaceParser.class);
	
	public NamespaceCommand() {
		super("NAMESPACE");
	}

	@Override
	public void responseReceived(List<IMAPResponse> rs) {
		if (isOk(rs)) {
			IMAPResponse nsr = lookForResponse(rs);
			NamespaceParser parserInstance = parser.newInstance();
			RecoveringParseRunner<NameSpaceInfo> runner = new RecoveringParseRunner<NameSpaceInfo>(parserInstance.rule());
			ParsingResult<NameSpaceInfo> result = runner.run(nsr.getPayload());
			data = result.resultValue;
		}
	}

	private IMAPResponse lookForResponse(List<IMAPResponse> rs) {
		for (IMAPResponse ir : rs) {
			if (ir.getPayload().startsWith(NamespaceParser.expectedResponseStart)) {
				return ir;
			}
		}
		return null;
	}

}
