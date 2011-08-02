package org.obm.push.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


import com.google.common.base.Objects;

public class MSEmailBody implements Serializable {

	private String charset;

	Map<MSEmailBodyType, String> formatValueMap;

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

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MSEmailBody other = (MSEmailBody) obj;
		if (formatValueMap == null) {
			if (other.formatValueMap != null)
				return false;
		} else {
			for(Iterator<MSEmailBodyType> it = formatValueMap.keySet().iterator();it.hasNext();){
				MSEmailBodyType key = it.next();
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
	
}
