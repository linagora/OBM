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

import java.io.Serializable;

public class QuotaInfo implements Serializable{

	private static final long serialVersionUID = 7172033843599691627L;
	private boolean enable;
	private int usage;
	private int limit;
	
	public QuotaInfo(){
		this.enable = false;
		this.usage=0;
		this.limit=0;
	}
	
	public QuotaInfo(int usages, int limites){
		this.enable = true;
		this.usage=usages;
		this.limit=limites;
	}
	
	public boolean isEnable() {
		return enable;
	}

	public int getUsage() {
		return usage;
	}

	public int getLimit() {
		return limit;
	}
}
