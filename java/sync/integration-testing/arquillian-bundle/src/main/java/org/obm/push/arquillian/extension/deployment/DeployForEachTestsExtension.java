/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013  Linagora
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
package org.obm.push.arquillian.extension.deployment;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.Before;

public class DeployForEachTestsExtension implements LoadableExtension {
	
   @Override
   public void register(ExtensionBuilder builder) {
      builder.observer(Handler.class);
   }
   
   public static class Handler {

		public void executeOnJUnitBefore(@SuppressWarnings("unused") @Observes Before event,
				Deployer deployer, TestClass testClass) {
			Deployment deployment = findRequiredAnnotation(testClass, Deployment.class);
			if (hasToManage(testClass, deployment)) {
				deployer.deploy(deployment.name());
			}
		}

		public void executeOnJUnitAfter(@SuppressWarnings("unused") @Observes After event,
				Deployer deployer, TestClass testClass) {
			Deployment deployment = findRequiredAnnotation(testClass, Deployment.class);
			if (hasToManage(testClass, deployment)) {
				deployer.undeploy(deployment.name());
			}
		}
		
		private boolean hasToManage(TestClass testClass, Deployment deployment) {
			boolean isDeployForEachTests = findAnnotation(testClass, DeployForEachTests.class) != null;
			if (deployment.managed() && isDeployForEachTests) {
				throw new IllegalStateException("DeployForEachTests found but the Deployment is managed by Arquillian");
			}
			return !deployment.managed() && isDeployForEachTests;
		}

		private <T extends Annotation> T findRequiredAnnotation(TestClass testClass, Class<T> annotation) {
			Method deploymentMethod = findAnnotation(testClass, annotation);
			if (deploymentMethod != null) {
				return deploymentMethod.getAnnotation(annotation);
			}
			throw new IllegalStateException(annotation.getName() + " not found in given test class");
		}

		private <T extends Annotation> Method findAnnotation(TestClass testClass, Class<T> annotation) {
			return testClass.getMethod(annotation);
		}
   }
}