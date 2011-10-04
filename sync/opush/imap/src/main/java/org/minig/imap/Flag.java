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

public enum Flag {
	SEEN, DRAFT, DELETED, FLAGGED, ANSWERED;

	public String toString() {
		switch (this) {
		case SEEN:
			return "\\Seen";
		case DRAFT:
			return "\\Draft";
		case DELETED:
			return "\\Deleted";
		case FLAGGED:
			return "\\Flagged";
		case ANSWERED:
			return "\\Answered";
		default:
			return "";
		}
	}
}
