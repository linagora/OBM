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

package org.obm.push.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

public class LdapUtils {

	private DirContext ctx;
	private String baseDn;

	public LdapUtils(DirContext ctx, String baseDn) {
		this.ctx = ctx;
		this.baseDn = baseDn;
	}

	/**
	 * Search ldap attributes using the given filter/query
	 * 
	 * @param filter
	 *            ldap filter where %q is replaced by query
	 * @param query
	 *            replaces %q in filter
	 * @param attributes
	 *            the searched attributes (only first value is returned)
	 * @return
	 * @throws NamingException
	 */
	public List<Map<String, List<String>>> getAttributes(String filter,
			String query, String[] attributes) throws NamingException {
		SearchControls constraints = new SearchControls();
		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
		String attrList[] = attributes;
		constraints.setReturningAttributes(attrList);
		NamingEnumeration<SearchResult> results = ctx.search(baseDn, filter.replace("%q",
				query).replace("**", "*"), constraints);
		List<Map<String, List<String>>> matched = new LinkedList<Map<String, List<String>>>();
		while (results.hasMore()) {
			SearchResult si = (SearchResult) results.next();
			Attributes attrs = si.getAttributes();
			if (attrs == null) {
				continue;
			}
			NamingEnumeration<? extends Attribute> ae = attrs.getAll();
			Map<String, List<String>> ret = new HashMap<String, List<String>>();
			while (ae.hasMoreElements()) {
				Attribute attr = ae.next();
				String id = attr.getID();
				List<String> vals = null;
				if (!ret.containsKey(id)) {
					vals = new LinkedList<String>();
					ret.put(id, vals);
				} else {
					vals = ret.get(id);
				}
				for (int i = 0; i < attr.size(); i++) {
					vals.add((String) attr.get(i));
				}
			}
			matched.add(ret);
		}
		return matched;
	}
}
