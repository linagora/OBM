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

import com.google.common.base.Objects;

public class ListInfo {

	private String name;

	private boolean selectable;
	private boolean createSubfolder;

	public ListInfo(String name, boolean selectable, boolean noInferiors) {
		super();
		this.name = name;
		this.selectable = selectable;
		this.createSubfolder = noInferiors;
	}

	public String getName() {
		return name;
	}

	public boolean isSelectable() {
		return selectable;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSelectable(boolean selectable) {
		this.selectable = selectable;
	}

	public boolean canCreateSubfolder() {
		return createSubfolder;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(getClass())
				.add("name", name)
				.toString();
	}
	
}
