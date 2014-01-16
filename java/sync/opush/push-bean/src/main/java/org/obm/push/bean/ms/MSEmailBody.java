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
package org.obm.push.bean.ms;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;

import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.utils.SerializableInputStream;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;

public class MSEmailBody implements Serializable {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {

		private SerializableInputStream mimeData;
		private MSEmailBodyType bodyType;
		private int estimatedDataSize;
		private Charset charset;
		private boolean truncated;
		
		private Builder() {
			super();
		}

		public Builder mimeData(SerializableInputStream mimeData) {
			this.mimeData = mimeData;
			return this;
		}

		public Builder bodyType(MSEmailBodyType bodyType) {
			this.bodyType = bodyType;
			return this;
		}

		public Builder estimatedDataSize(int estimatedDataSize) {
			this.estimatedDataSize = estimatedDataSize;
			return this;
		}

		public Builder charset(Charset charset) {
			this.charset = charset;
			return this;
		}

		public Builder truncated(boolean truncated) {
			this.truncated = truncated;
			return this;
		}
		
		public MSEmailBody build() {
			Charset charset = Objects.firstNonNull(this.charset, Charsets.UTF_8);
			return new MSEmailBody(mimeData, bodyType, estimatedDataSize, charset, truncated);
		}
	}
	
	private static final long serialVersionUID = -1451600272523495944L;
	
	private SerializableInputStream mimeData;
	private MSEmailBodyType bodyType;
	private int estimatedDataSize;
	private Charset charset;
	private boolean truncated;
	
	private MSEmailBody(SerializableInputStream mimeData, MSEmailBodyType bodyType, 
			int estimatedDataSize, Charset charset, boolean truncated) {
		
		this.mimeData = mimeData;
		this.bodyType = bodyType;
		this.estimatedDataSize = estimatedDataSize;
		this.charset = charset;
		this.truncated = truncated;
	}
	
	public SerializableInputStream getMimeData() {
		return mimeData;
	}

	public MSEmailBodyType getBodyType() {
		return bodyType;
	}

	public int getEstimatedDataSize() {
		return estimatedDataSize;
	}
	
	public boolean isTruncated() {
		return truncated;
	}
	
	public Charset getCharset() {
		return charset;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(bodyType, estimatedDataSize, charset, truncated);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof MSEmailBody) {
			MSEmailBody that = (MSEmailBody) object;
			return Objects.equal(this.bodyType, that.bodyType)
				&& Objects.equal(this.estimatedDataSize, that.estimatedDataSize)
				&& Objects.equal(this.charset, that.charset)
				&& Objects.equal(this.truncated, that.truncated);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("bodyType", bodyType)
			.add("mimeData", mimeData)
			.add("truncationSize", estimatedDataSize)
			.add("charset", charset)
			.add("truncated", truncated)
			.toString();
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(mimeData);
		out.writeObject(bodyType);
		out.writeObject(estimatedDataSize);
		out.writeUTF(charset.name());
		out.writeObject(truncated);
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		mimeData = (SerializableInputStream) in.readObject();
		bodyType = (MSEmailBodyType) in.readObject();
		estimatedDataSize = (Integer) in.readObject();
		charset = Charset.forName(in.readUTF());
		truncated = (Boolean) in.readObject();
	}
}
