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
package fr.aliacom.obm.common.profile;

import java.util.Map;

import org.obm.sync.Right;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;


public class ModuleCheckBoxStates {

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private Module module;
		private final ImmutableMap.Builder<Right, CheckBoxState> checkBoxStates;

		private Builder() {
			this.checkBoxStates = ImmutableMap.builder();
		}

		public Builder module(Module module) {
			this.module = module;
			return this;
		}

		public Builder checkBoxState(Right right, CheckBoxState state) {
			this.checkBoxStates.put(right, state);
			return this;
		}

		public Builder checkBoxStates(Map<Right, CheckBoxState> checkBoxStates) {
			this.checkBoxStates.putAll(checkBoxStates);
			return this;
		}

		public ModuleCheckBoxStates build() {
			Preconditions.checkState(module != null);

			return new ModuleCheckBoxStates(module, checkBoxStates.build());
		}

	}

	private final Module module;
	private final Map<Right, CheckBoxState> checkBoxStates;

	private ModuleCheckBoxStates(Module module, Map<Right, CheckBoxState> rights) {
		this.module = module;
		this.checkBoxStates = rights;
	}

	public Module getModule() {
		return module;
	}

	public Map<Right, CheckBoxState> getCheckBoxStates() {
		return checkBoxStates;
	}

	public CheckBoxState getCheckBoxState(Right right) {
		return checkBoxStates.get(right);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(module, checkBoxStates);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ModuleCheckBoxStates) {
			ModuleCheckBoxStates other = (ModuleCheckBoxStates) obj;

			return Objects.equal(module, other.module)
					&& Objects.equal(checkBoxStates, other.checkBoxStates);
		}

		return false;
	}

	@Override
	public String toString() {
		return Objects
				.toStringHelper(this)
				.add("module", module)
				.add("checkBoxStates", checkBoxStates)
				.toString();
	}

}
