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
package org.obm.guice;

import java.util.List;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Module;

public class GuiceRunnerDelegation {

	public List<MethodRule> rules(TestClass testClass, List<MethodRule> rules) {
		return ImmutableList.<MethodRule>builder()
				.addAll(rules)
				.add(createInjectionMethodRule(testClass)).build();
	}

	private MethodRule createInjectionMethodRule(final TestClass testClass) {
		return new MethodRule() {
			@Override
			public Statement apply(Statement base, FrameworkMethod method, Object target) {
				GuiceModule moduleAnnotation = testClass.getJavaClass().getAnnotation(GuiceModule.class);
				if (moduleAnnotation != null) {
					return new GuiceStatement(moduleAnnotation.value(), target, base);
				}
				return base;
			}
		};
	}

	public static class GuiceStatement extends Statement { 
		private Class<? extends Module> module;
		private Object target;
		private Statement next;

		public GuiceStatement(Class<? extends Module> module, Object target, Statement next) {
			this.module = module;
			this.target = target;
			this.next = next;
		}

		@Override
		public void evaluate() throws Throwable {
			Guice.createInjector(module.newInstance()).injectMembers(target);
			next.evaluate();
		}
	}
}
