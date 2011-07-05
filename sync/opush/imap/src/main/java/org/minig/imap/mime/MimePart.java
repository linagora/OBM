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

package org.minig.imap.mime;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;


public class MimePart extends AbstractMimePart implements IMimePart {

	private String mimeType;
	private String mimeSubtype;
	private IMimePart parent;
	private int idx;
	private String contentTransfertEncoding;
	private String contentId;
	private String multipartSubtype;
	
	public MimePart() {
		super();
	}

	@Override
	public void defineParent(IMimePart parent, int index) {
		idx = index;
		this.parent = parent;
	}
	
	@Override
	public String getMimeType() {
		return mimeType;
	}
	
	@Override
	public void setMimeType(MimeType mimetype) {
		this.mimeSubtype = mimetype.getSubtype();
		this.mimeType = mimetype.getType();
	}
	
	@Override
	public String getMimeSubtype() {
		return mimeSubtype;
	}

	public void setMimeSubtype(String mimeSubtype) {
		this.mimeSubtype = mimeSubtype;
	}

	@Override
	public MimeAddress getAddressInternal() {
		return MimeAddress.concat(getParentAddressInternal(), selfAddress());
	}

	private MimeAddress getParentAddressInternal() {
		if (parent != null) {
			return parent.getAddressInternal();
		}
		return null;
	}
	
	@Override
	public MimeAddress getAddress() {
		if (!isMultipart()) {
			return getAddressInternal();
		}
		return null;
	}

	@Override
	public boolean isMultipart() {
		return getMimeType() == null || getMimeType().equals("multipart");
	}
	
	private Integer selfAddress() {
		if (parent == null) {
			if (isMultipart()) {
				return null;
			}
			return 1;
		}
		return idx;
	}

	public String getFullMimeType() {
		StringBuilder sb = new StringBuilder(50);
		sb.append(getMimeType() != null ? getMimeType().toLowerCase() : "null");
		sb.append("/");
		sb.append(getMimeSubtype() != null ? getMimeSubtype().toLowerCase()
				: "null");
		return sb.toString();
	}

	public String getContentTransfertEncoding() {
		return contentTransfertEncoding;
	}

	public void setContentTransfertEncoding(String contentTransfertEncoding) {
		this.contentTransfertEncoding = contentTransfertEncoding;
	}

	@Override
	public String getCharset() {
		BodyParam bodyParam = getBodyParam("charset");
		if (bodyParam != null) {
			return bodyParam.getValue();
		}
		return null;
	}
	
	public boolean isAttachment() {
		return (idx > 1 && getMimeType() != null && !"html".equalsIgnoreCase(getMimeSubtype()))
				|| !"text".equalsIgnoreCase(getMimeType());
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public int getIdx() {
		return idx;
	}

	@Override
	public IMimePart getParent() {
		return parent;
	}

	private String retrieveMethodFromCalendarPart() {
		if ("text/calendar".equals(getFullMimeType())) {
			BodyParam method = getBodyParam("method");
			if (method != null) {
				return method.getValue();
			}
		}
		return null;
	}
	
	public boolean isInvitation() {
		String method = retrieveMethodFromCalendarPart();
		return "REQUEST".equalsIgnoreCase(method);
	}
	
	@Override
	public boolean isNested() {
		return getFullMimeType().equalsIgnoreCase("message/rfc822");
	}
	
	public boolean isCancelInvitation() {
		String method = retrieveMethodFromCalendarPart();
		return "CANCEL".equalsIgnoreCase(method);
	}
	
	@Override
	public String getName() {
		BodyParam name = getBodyParam("name");
		if (name != null && name.getValue() != null) {
			return name.getValue();
		}
		BodyParam filename = getBodyParam("filename");
		if (filename != null && filename.getValue() != null) {
			return filename.getValue();
		}
		return null;
	}
	
	@Override
	public String getMultipartSubtype() {
		if (multipartSubtype != null) {
			return multipartSubtype;
		}
		return getMimeSubtype();
	}
	
	@Override
	public void setMultipartSubtype(String subtype) {
		this.multipartSubtype = subtype;
	}

	@Override
	public List<IMimePart> getSibling() {
		if (parent != null) {
			ArrayList<IMimePart> copy = Lists.newArrayList(parent.getChildren());
			copy.remove(this);
			return copy;
		}
		return ImmutableList.of();
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(getClass())
			.add("mime-type", getFullMimeType())
			.add("addr", getAddress()).toString();
	}
}
