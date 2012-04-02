/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.minig.imap.mime;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.minig.imap.mime.impl.LeafPartsFinder;

import com.google.common.collect.ImmutableMap;

public abstract class AbstractMimePart implements IMimePart {

	private List<IMimePart> children;
	private Map<String, BodyParam> bodyParams;

	protected AbstractMimePart() {
		children = new LinkedList<IMimePart>();
		bodyParams = ImmutableMap.of();
	}
	
	@Override
	public void addPart(IMimePart child) {
		children.add(child);
		child.defineParent(this, children.size());
	}

	@Override
	public List<IMimePart> getChildren() {
		return Collections.unmodifiableList(children);
	}
	
	public void setChildren(List<IMimePart> children) {
		int i = 1;
		for (IMimePart child: children) {
			child.defineParent(this, i++);
		}
		this.children = children;
	}
	
	@Override
	public Collection<IMimePart> listLeaves(boolean depthFirst, boolean filterNested) {
		return new LeafPartsFinder(this, depthFirst, filterNested).getLeaves();
	}
	
	@Override
	public Collection<BodyParam> getBodyParams() {
		return bodyParams.values();
	}

	@Override
	public BodyParam getBodyParam(final String param) {
		return bodyParams.get(param.toLowerCase());
	}
	
	public void setBodyParams(Collection<BodyParam> bodyParams) {
		HashMap<String, BodyParam> params = new HashMap<String, BodyParam>();
		for (BodyParam param: bodyParams) {
			params.put(param.getKey(), param);
		}
		this.bodyParams = params;
	}
	
	@Override
	public IMimePart getInvitation() {
		Collection<IMimePart> mimeParts = findRootMimePartInTree().listLeaves(true, true);
		for (IMimePart mimePart: mimeParts) {
			if (mimePart.isInvitation() || mimePart.isCancelInvitation()) {
				return mimePart;
			} 	
		}
		return null;
	}
	
	@Override
	public IMimePart findRootMimePartInTree() {
		if (this.getParent() != null) {
			return this.getParent().findRootMimePartInTree();
		} else {
			return this;
		}
	}
}