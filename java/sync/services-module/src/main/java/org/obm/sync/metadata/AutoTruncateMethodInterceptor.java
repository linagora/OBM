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
package org.obm.sync.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.obm.annotations.database.DatabaseEntity;
import org.obm.annotations.database.DatabaseField;

import com.google.inject.Inject;

import fr.aliacom.obm.services.constant.ObmSyncConfigurationService;

public class AutoTruncateMethodInterceptor implements MethodInterceptor {

	@Inject
	private DatabaseTruncationService truncationService;
	@Inject
	private ObmSyncConfigurationService configurationService;

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		if (configurationService.isAutoTruncateEnabled()) {
			Method method = invocation.getMethod();
			Object[] args = invocation.getArguments();
			Class<?>[] paramTypes = method.getParameterTypes();
			Annotation[][] paramsAnnotations = method.getParameterAnnotations();

			for (int i = 0; i < args.length; i++) {
				Annotation[] annotations = paramsAnnotations[i];

				for (Annotation annotation : annotations) {
					Object arg = args[i];

					if (annotation instanceof DatabaseEntity) {
						args[i] = truncationService.getTruncatingEntity(arg);
					} else if (annotation instanceof DatabaseField && String.class.equals(paramTypes[i])) {
						DatabaseField dbField = (DatabaseField) annotation;

						args[i] = truncationService.truncate((String) arg, dbField.table(), dbField.column());
					}
				}
			}
		}

		return invocation.proceed();
	}

}
