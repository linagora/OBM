/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
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

public class Participation implements Serializable{
	public static final Participation DECLINED = new Participation(State.DECLINED);
	public static final Participation NEEDSACTION = new Participation(State.NEEDSACTION);
	public static final Participation ACCEPTED = new Participation(State.ACCEPTED);
	public static final Participation TENTATIVE = new Participation(State.TENTATIVE);
	public static final Participation DELEGATED = new Participation(State.DELEGATED);
	public static final Participation COMPLETED = new Participation(State.COMPLETED);
	public static final Participation INPROGRESS = new Participation(State.INPROGRESS);

	private Comment comment;
	private State state;

	public Participation(Comment comment, State state) {
		this.comment = comment;
		this.state = state;
	}

	public Participation(State state) {
		this.comment = new Comment();
		this.state = state;
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
		return comment.getComment() != null;
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
