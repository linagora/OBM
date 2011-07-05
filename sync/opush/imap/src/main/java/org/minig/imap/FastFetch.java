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

import java.util.Date;
import java.util.Set;

public class FastFetch {

	private long uid;
	private Date internalDate;
	private Set<Flag> flags;
	public FastFetch(long uid, Date internalDate, Set<Flag> flags){
		this.uid = uid;
		this.internalDate = internalDate;
		this.flags = flags;
	}

	public long getUid() {
		return uid;
	}

	public Date getInternalDate() {
		return internalDate;
	}

	public Set<Flag> getFlags() {
		return flags;
	}
	
	public boolean isRead(){
		return flags != null && flags.contains(Flag.SEEN);
	}
	
}
