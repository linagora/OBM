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

package org.minig.imap.impl;

import java.io.InputStream;

public class IMAPResponse {

	private String status;
	private boolean clientDataExpected;
	private String payload;
	private String tag;
	private InputStream streamData;

	public IMAPResponse() {
	}

	public IMAPResponse(String status, String payload) {
		setStatus(status);
		setPayload(payload);
	}

	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isOk() {
		return "OK".equals(status);
	}

	public boolean isNo() {
		return "NO".equals(status);
	}

	public boolean isBad() {
		return "BAD".equals(status);
	}

	public boolean isClientDataExpected() {
		return clientDataExpected;
	}
	
	public boolean isContinuation() {
		return "+".equals(tag);
	}

	public void setClientDataExpected(boolean clientDataExpected) {
		this.clientDataExpected = clientDataExpected;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public InputStream getStreamData() {
		return streamData;
	}

	public void setStreamData(InputStream streamData) {
		this.streamData = streamData;
	}
}
