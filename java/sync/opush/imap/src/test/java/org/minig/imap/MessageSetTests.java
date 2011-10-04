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

import java.util.Arrays;
import java.util.Collection;

import org.minig.imap.impl.MessageSet;

public class MessageSetTests extends IMAPTestCase {

	private void testParse(Collection<Long> data, String expectedSet, Collection<Long> expectedCollection) {
		String set = MessageSet.asString(data);
		assertEquals(expectedSet, set);
		assertEquals(expectedCollection, MessageSet.asLongCollection(set, data.size()));
	}
	
	public void testParse1() {
		testParse(Arrays.asList(1l, 2l, 3l, 8l, 9l, 10l, 12l), "1:3,8:10,12", 
				Arrays.asList(1l, 2l, 3l, 8l, 9l, 10l, 12l));
	}

	public void testParse2() {
		testParse(Arrays.asList(8l, 2l, 3l, 4l, 9l, 10l, 12l, 13l), "2:4,8:10,12:13",
				Arrays.asList(2l, 3l, 4l, 8l, 9l, 10l, 12l, 13l));
	}
	
	public void testParse3() {
		testParse(Arrays.asList(1l, 2l), "1:2", Arrays.asList(1l, 2l));
	}
	
	public void testParse4() {
		testParse(Arrays.asList(1l), "1", Arrays.asList(1l));
	}
	
}
