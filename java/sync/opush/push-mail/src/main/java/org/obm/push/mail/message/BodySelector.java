/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.push.mail.message;

import java.util.Collection;
import java.util.List;

import org.obm.push.mail.mime.IMimePart;

import com.google.common.collect.ImmutableList;

public class BodySelector {

	private Collection<IMimePart> parts;
	private final List<String> mimeSubtypeInPriorityOrder;

	public BodySelector(IMimePart root, List<String> mimeSubtypeInPriorityOrder) {
		this.mimeSubtypeInPriorityOrder = mimeSubtypeInPriorityOrder;
		parts = root.listLeaves(true, true);
	}

	public IMimePart findBodyTextPart() {
		final IMimePart part = findTextPart(parts, mimeSubtypeInPriorityOrder);
		if (part != null && (!mimeSubtypeInPriorityOrder.get(0).equalsIgnoreCase(part.getSubtype()))) {
			return lookForAlternativePart(part);
		}
		return part;
	}
	
	private IMimePart lookForAlternativePart(final IMimePart mimePart) {
		final IMimePart mimePartParent = mimePart.getParent();
		if (mimePartParent != null && (mimePartParent.isMultipart() || mimePartParent.isNested()) && mimePartParent.getMultipartSubtype().equalsIgnoreCase("ALTERNATIVE")) {
			final Collection<IMimePart> listParentParts = mimePartParent.listLeaves(true, true);
			for (String type: mimeSubtypeInPriorityOrder) {
				if (type.equalsIgnoreCase(mimePart.getSubtype())) {
					return mimePart;
				}
				final IMimePart part = findTextPart(listParentParts, ImmutableList.of(type));
				if (part != null) {
					return part;
				}
			}
		}
		return mimePart;
	}

	private IMimePart findTextPart(Collection<IMimePart> parts, List<String> mimeSubtypeInPriorityOrder) {
		for (IMimePart part: parts) {
			final String mimeSubtype = part.getSubtype();
			for (final String firstMimeSubtypePriority: mimeSubtypeInPriorityOrder) {
				if (mimeSubtype.equalsIgnoreCase(firstMimeSubtypePriority)) {
					return part;
				}
			}
		}
		return null;
	}

}
