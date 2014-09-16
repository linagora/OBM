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
package org.obm.servlet.filter.resource;

import java.util.Deque;

import org.obm.servlet.filter.resource.ResourceManagementModule.Loggers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.google.common.collect.Queues;
import com.google.inject.servlet.RequestScoped;

@RequestScoped
public class ResourcesHolder {

	private final Logger logger = LoggerFactory.getLogger(Loggers.RESOURCES);
	
	private ClassToInstanceMap<Resource> resources;
	private Deque<Resource> lifo; 

	public ResourcesHolder() {
		resources = MutableClassToInstanceMap.create();
		lifo = Queues.newArrayDeque();
	}

	public void remove(Class<? extends Resource> clazz) {
		logger.debug("remove {}", clazz.getSimpleName());
		Resource resource = resources.remove(clazz);
		if (resource != null) {
			lifo.remove(resource);
			logger.debug("close {}", resource);
			resource.closeResource();
		}
	}
	
	public <T extends Resource> void put(Class<T> clazz, T resource) {
		logger.debug("put {}:{}", clazz.getSimpleName(), resource);
		Resource previousResource = resources.putInstance(clazz, resource);
		if (previousResource != null) {
			throwAlreadyHeldResourceException(clazz, previousResource);
		}
		lifo.push(resource);
	}

	private <T extends Resource> void throwAlreadyHeldResourceException(Class<T> clazz, Resource previousResource) {
		try {
			throw new IllegalStateException(String.format(
				"Resource type already held %s:%s", clazz.getSimpleName(), previousResource));
		} finally {
			previousResource.closeResource();
		}
	}

	public <T extends Resource> T get(Class<T> clazz) {
		T resource = resources.getInstance(clazz);
		logger.debug("get {}:{}", clazz.getSimpleName(), resource);
		return resource;
	}
	
	public void close() {
		for (Resource resource: lifo) {
			try {
				logger.debug("close {}:{}", resource.getClass().getSimpleName(), resource);
				resource.closeResource();
			} catch (RuntimeException exception) {
				logger.error("fail to close resource {}, exception occured {}", resource, exception);
			}
		}
	}
	
}
