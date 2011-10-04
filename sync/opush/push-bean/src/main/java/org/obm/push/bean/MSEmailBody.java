package org.obm.push.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Objects;

public class MSEmailBody implements Serializable {

	private final Map<MSEmailBodyType, String> formatValueMap;
	private String charset;

	public MSEmailBody() {
		formatValueMap = new HashMap<MSEmailBodyType, String>();
	}

	public MSEmailBody(MSEmailBodyType mime, String value) {
		this();
		formatValueMap.put(mime, value);
	}

	public void addConverted(MSEmailBodyType mime, String value) {
		formatValueMap.put(mime, value);
	}

	public Set<MSEmailBodyType> availableFormats() {
		return formatValueMap.keySet();
	}

	public String getValue(MSEmailBodyType format) {
		return formatValueMap.get(format);
	}
	
	public void addMailPart(MSEmailBodyType mime, String part){
		String body = this.formatValueMap.get(mime);
		if(body!=null){
			body += part;
			this.addConverted(mime, body);
		}
	}
	
	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(formatValueMap, charset);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof MSEmailBody) {
			MSEmailBody that = (MSEmailBody) object;
			return Objects.equal(this.formatValueMap, that.formatValueMap)
				&& Objects.equal(this.charset, that.charset);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("formatValueMap", formatValueMap)
			.add("charset", charset)
			.toString();
	}

	
}
