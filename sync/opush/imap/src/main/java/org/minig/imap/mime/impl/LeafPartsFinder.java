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

package org.minig.imap.mime.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

import org.minig.imap.mime.IMimePart;
import org.minig.imap.mime.MimeAddress;

public class LeafPartsFinder {

	private Collection<IMimePart> leaves;
	private final boolean filterNested;
	private final IMimePart root;
	
	public LeafPartsFinder(IMimePart root, boolean depthFirst, boolean filterNested) {
		this.root = root;
		this.filterNested = filterNested;
		if (depthFirst) {
			leaves = new ArrayList<IMimePart>();
		} else {
			leaves = new TreeSet<IMimePart>(new Comparator<IMimePart>() {
				@Override
				public int compare(IMimePart o1, IMimePart o2) {
					MimeAddress firstAddr = o1.getAddress();
					MimeAddress secondAddr = o2.getAddress();
					int diffLevel = firstAddr.compareNestLevel(secondAddr);
					if (diffLevel != 0) {
						return diffLevel;
					}
					return firstAddr.getLastIndex() - secondAddr.getLastIndex();
				}
			});
		}
		buildLeafList(root);
	}

	
	private void buildLeafList(IMimePart mp) {
		if (mp.getChildren().isEmpty()) {
			leaves.add(mp);
		} else {
			if (mp != root && mp.isNested() && filterNested) {
				return;
			}
			for (IMimePart m : mp.getChildren()) {
				buildLeafList(m);
			}
		}
	}

	public Collection<IMimePart> getLeaves() {
		return leaves;
	}
	
}
