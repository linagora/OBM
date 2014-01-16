/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.calendar;

import java.io.Serializable;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class Participation implements Serializable{

	public static Builder builder() {
		return new Builder();
	}
	
	public static Participation declined() {
		return builder().state(State.DECLINED).build();
	}
	
	public static Participation needsAction() {
		return builder().state(State.NEEDSACTION).build();
	}
	
	public static Participation accepted() {
		return builder().state(State.ACCEPTED).build();
	}
	
	public static Participation tentative() {
		return builder().state(State.TENTATIVE).build();
	}
	
	public static Participation delegated() {
		return builder().state(State.DELEGATED).build();
	}
	
	public static Participation completed() {
		return builder().state(State.COMPLETED).build();
	}
	
	public static Participation inProgress() {
		return builder().state(State.INPROGRESS).build();
	}

	public static class Builder {
		private State state;
		private Comment comment;

		private Builder() {
			super();
		}

		public Builder comment(String comment) {
			this.comment = new Comment(comment);
			return this;
		}

		public Builder state(State state) {
			this.state = state;
			return this;
		}

		public Participation build() {
			Preconditions.checkState(state != null);
			return new Participation(state, comment);
		}
	}

	private Comment comment;
	private State state;

	public enum State {

		NEEDSACTION("NEEDS-ACTION"),
		ACCEPTED("ACCEPTED"),
		DECLINED("DECLINED"),
		TENTATIVE("TENTATIVE"),
		DELEGATED("DELEGATED"),
		COMPLETED("COMPLETED"),
		INPROGRESS("IN-PROGRESS");

		private String state;

		private State(String state) {
			this.state = state;
		}

		public static final State getValueOf(String s) {
			if ("NEEDS-ACTION".equals(s)) {
				return NEEDSACTION;
			} else if ("IN-PROGRESS".equals(s)) {
				return INPROGRESS;
			} else {
				try {
					return State.valueOf(s);
				} catch (IllegalArgumentException iae) {
					throw new IllegalArgumentException("Unknown value for the participation state.");
				}
			}
		}

		@Override
		public String toString() {
			return state;
		}
	}

	private Participation(State state, Comment comment) {
		this.state = state;
		this.comment = comment;
	}

	private Participation(State state) {
		this(state, null);
	}

	public Comment getComment() {
		return comment;
	}

	public void setComment(Comment comment) {
		this.comment = comment;
	}

	public State getState() {
		return state;
	}

	public void setState(State enumState) {
		this.state = enumState;
	}

	public boolean hasDefinedComment() {
		return comment != null;
	}

	public String getSerializedCommentToString() {
		if (this.hasDefinedComment()) {
			return comment.serializeToString();
		} else {
			return null;
		}
	}

	public static final Participation getValueOf(String value) {
		return new Participation(State.getValueOf(value));
	}

	public void resetComment() {
		comment = Comment.EMPTY;
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj instanceof Participation) {
			Participation other = (Participation) obj;
			return Objects.equal(state, other.state);
		}
		return false;
	}

	@Override
	public final int hashCode() {
		return Objects.hashCode(state);
	}

	@Override
	public String toString() {
		return state.toString();
	}
}
