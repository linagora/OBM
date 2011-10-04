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

package org.obm.mail.conversation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Objects;

public class MailBody {

	Map<String, String> formatValueMap;

	public MailBody() {
		formatValueMap = new HashMap<String, String>();
	}

	public MailBody(String mime, String value) {
		this();
		formatValueMap.put(mime, value);
	}

	public void addConverted(String mime, String value) {
		formatValueMap.put(mime, value);
	}

	public Set<String> availableFormats() {
		return formatValueMap.keySet();
	}

	public String getValue(String format) {
		return formatValueMap.get(format);
	}
	
	public void addMailPart(String mime, String part){
		String body = this.formatValueMap.get(mime);
		if(body!=null){
			body += part;
			this.addConverted(mime, body);
		}
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MailBody other = (MailBody) obj;
		if (formatValueMap == null) {
			if (other.formatValueMap != null)
				return false;
		} else {
			for(Iterator<String> it = formatValueMap.keySet().iterator();it.hasNext();){
				String key = it.next();
				if(!formatValueMap.get(key).equals(other.formatValueMap.get(key))){
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(formatValueMap);
	}
	
	public void clear(){
		this.formatValueMap.clear();
	}
	
	
}
