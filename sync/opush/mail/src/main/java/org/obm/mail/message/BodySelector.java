package org.obm.mail.message;

import java.util.Collection;
import java.util.List;

import org.minig.imap.mime.IMimePart;

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
		if (part != null && (!mimeSubtypeInPriorityOrder.get(0).equalsIgnoreCase(part.getMimeSubtype()))) {
			return lookForAlternativePart(part);
		}
		return part;
	}
	
	private IMimePart lookForAlternativePart(final IMimePart mimePart) {
		final IMimePart mimePartParent = mimePart.getParent();
		if (mimePartParent != null && (mimePartParent.isMultipart() || mimePartParent.isNested()) && mimePartParent.getMultipartSubtype().equalsIgnoreCase("ALTERNATIVE")) {
			final Collection<IMimePart> listParentParts = mimePartParent.listLeaves(true, true);
			for (String type: mimeSubtypeInPriorityOrder) {
				if (type.equalsIgnoreCase(mimePart.getMimeSubtype())) {
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
			final String mimeSubtype = part.getMimeSubtype();
			for (final String firstMimeSubtypePriority: mimeSubtypeInPriorityOrder) {
				if (mimeSubtype.equalsIgnoreCase(firstMimeSubtypePriority)) {
					return part;
				}
			}
		}
		return null;
	}

}
