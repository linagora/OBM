package org.obm.push.bean;

import java.io.Serializable;

import com.google.common.base.Objects;

public class BodyPreference implements Serializable {

	private Integer truncationSize;
	private MSEmailBodyType type;
	
	public Integer getTruncationSize() {
		return this.truncationSize;
	}

	public void setTruncationSize(Integer truncationSize) {
		this.truncationSize = truncationSize;
	}
	
	public MSEmailBodyType getType() {
		return type;
	}

	public void setType(MSEmailBodyType type) {
		this.type = type;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(truncationSize, type);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof BodyPreference) {
			BodyPreference that = (BodyPreference) object;
			return Objects.equal(this.truncationSize, that.truncationSize)
				&& Objects.equal(this.type, that.type);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("super", super.toString())
			.add("truncationSize", truncationSize)
			.add("type", type)
			.toString();
	}
	
}
