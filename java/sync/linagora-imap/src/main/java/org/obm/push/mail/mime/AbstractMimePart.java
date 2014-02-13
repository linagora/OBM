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
package org.obm.push.mail.mime;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.obm.push.mail.bean.IMAPHeaders;
import org.obm.push.mail.bean.IMAPHeadersImpl;


public abstract class AbstractMimePart implements MimePart {

	private List<MimePart> children;
	private BodyParams bodyParams;

	protected AbstractMimePart(List<MimePart> children, BodyParams bodyParams) {
		setChildren(children);
		this.bodyParams = bodyParams;
	}
	
	protected AbstractMimePart() {
		this(new LinkedList<MimePart>(), BodyParams.builder().build());
	}

	@Override
	public List<MimePart> getChildren() {
		return Collections.unmodifiableList(children);
	}
	
	private void setChildren(List<MimePart> children) {
		int i = 1;
		for (MimePart child: children) {
			child.defineParent(this, i++);
		}
		this.children = children;
	}
	
	@Override
	public Collection<MimePart> listLeaves(boolean depthFirst, boolean filterNested) {
		return new LeafPartsFinder(this, depthFirst, filterNested).getLeaves();
	}
	
	@Override
	public BodyParams getBodyParams() {
		return bodyParams;
	}

	@Override
	public BodyParam getBodyParam(final String param) {
		return bodyParams.get(param);
	}
	
	@Override
	public MimePart getInvitation() {
		Collection<MimePart> mimeParts = findRootMimePartInTree().listLeaves(true, true);
		for (MimePart mimePart: mimeParts) {
			if (mimePart.isInvitation() || mimePart.isCancelInvitation()) {
				return mimePart;
			} 	
		}
		return null;
	}
	
	@Override
	public MimePart findRootMimePartInTree() {
		if (this.getParent() != null) {
			return this.getParent().findRootMimePartInTree();
		} else {
			return this;
		}
	}
	
	@Override
	public MimePart findMainMessage(ContentType contentType) {
		if (contentType != null) {
			Collection<MimePart> mimeParts = findRootMimePartInTree().listLeaves(true, true);
			for (MimePart mimePart: mimeParts) {
				if (isMatching(contentType, mimePart)) {
					return mimePart;
				}
			}
		}
		return null;
	}

	private boolean isMatching(ContentType contentType, MimePart mimePart) {
		if (mimePart.hasMimePart(contentType)) {
			if (mimePart.hasMultiPartMixedParent()) {
				return mimePart.isFirstElementInParent();
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	@Override
	public IMAPHeaders decodeHeaders(InputStream is) throws IOException {
		InputStreamReader reader = new InputStreamReader(is, getHeaderCharsetDecoder());
		Map<String, String> rawHeaders = new HeadersParser().parseRawHeaders(reader);
		IMAPHeadersImpl h = new IMAPHeadersImpl();
		h.setRawHeaders(rawHeaders);
		return h;
	}
	
	/**
	 * Tries to return a suitable {@link Charset} to decode the headers
	 */
	private Charset getHeaderCharsetDecoder() {
		String encoding = getContentTransfertEncoding();
		if (encoding == null) {
			return Charset.forName("utf-8");
		} else if (encoding.equalsIgnoreCase("8bit")) {
			return Charset.forName("iso-8859-1");
		} else {
			try {
				return Charset.forName(encoding);
			} catch (UnsupportedCharsetException uee) {
				return Charset.forName("utf-8");
			}
		}
	}

}