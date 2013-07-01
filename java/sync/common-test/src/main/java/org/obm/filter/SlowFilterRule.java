/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.filter;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

public final class SlowFilterRule implements TestRule {

	@VisibleForTesting SlowFilterConfiguration slowFilterConfiguration;
	
	public SlowFilterRule() {
		slowFilterConfiguration = new SlowFilterConfiguration();
	}
	
	@Override
	public Statement apply(final Statement test, final Description description) {
		return new Statement() {
			
			@Override
			public void evaluate() throws Throwable {
				Slow methodAnnotation = description.getAnnotation(Slow.class);
				if (hasToRunTest(methodAnnotation)) {
					test.evaluate();
				}
			}
			
		};
	}

	@VisibleForTesting boolean hasToRunTest(Slow methodAnnotation) {
		return (fastTest(methodAnnotation) && enableFastTests())
				|| (slowTest(methodAnnotation) && enableSlowTests());
	}

	private boolean enableFastTests() {
		return keyEnabled(Slow.FAST_CONFIGURATION_ENVIRONMENT_KEY, true);
	}

	private boolean enableSlowTests() {
		return keyEnabled(Slow.SLOW_CONFIGURATION_ENVIRONMENT_KEY, true);
	}

	private boolean keyEnabled(String key, boolean defaultValue) {
		String slowProperty = slowFilterConfiguration.getConfigurationValue(key);
		if (Strings.isNullOrEmpty(slowProperty)) {
			return defaultValue;
		} else {
			return  Boolean.parseBoolean(slowProperty);
		}
	}
	
	private boolean fastTest(Slow methodAnnotation) {
		return methodAnnotation == null;
	}

	private boolean slowTest(Slow methodAnnotation) {
		return methodAnnotation != null;
	}
	
}
