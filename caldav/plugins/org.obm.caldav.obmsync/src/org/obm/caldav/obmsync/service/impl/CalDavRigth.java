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
 *   obm.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.obm.caldav.obmsync.service.impl;

public class CalDavRigth {
	private Boolean isReadable;
	private Boolean isWritable;
	
	public CalDavRigth() {
		this.isReadable = false;
		this.isWritable = false;
	}
	
	public CalDavRigth(Boolean isReadable, Boolean isWritable) {
		super();
		this.isReadable = isReadable;
		this.isWritable = isWritable;
	}
	
	public Boolean isReadable() {
		return isReadable;
	}

	public Boolean isWritable() {
		return isWritable;
	}
}
