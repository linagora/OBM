/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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
package org.obm.push.bean;

import java.util.PriorityQueue;

import org.obm.logger.LoggerModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;

public class ResourcesHolder {

	private final Logger logger = LoggerFactory.getLogger(LoggerModule.RESOURCES);
	
	private ClassToInstanceMap<Resource> resources;

	public ResourcesHolder() {
		resources = MutableClassToInstanceMap.create();
	}

	public void remove(Class<? extends Resource> clazz) {
		logger.info("remove {}", clazz.getSimpleName());
		Resource resource = resources.remove(clazz);
		if (resource != null) {
			logger.info("close {}", resource);
			resource.close();
		}
	}
	
	public <T extends Resource> void put(Class<T> clazz, T resource) {
		logger.info("put {}:{}", clazz.getSimpleName(), resource);
		Resource previousResource = resources.putInstance(clazz, resource);
		if (previousResource != null) {
			throwAlreadyHeldResourceException(clazz, previousResource);
		}
	}

	private <T extends Resource> void throwAlreadyHeldResourceException(Class<T> clazz, Resource previousResource) {
		try {
			throw new IllegalStateException(String.format(
				"Resource type already held %s:%s", clazz.getSimpleName(), previousResource));
		} finally {
			previousResource.close();
		}
	}

	public <T extends Resource> T get(Class<T> clazz) {
		T resource = resources.getInstance(clazz);
		logger.info("get {}:{}", clazz.getSimpleName(), resource);
		return resource;
	}
	
	public void close() {
		
		PriorityQueue<Resource> queue = new PriorityQueue<Resource>(resources.values());
		Resource resource = queue.poll();
		while (resource != null) {
			try {
				logger.info("close {}:{}", resource.getClass().getSimpleName(), resource);
				resource.close();
			} catch (RuntimeException exception) {
				logger.error("fail to close resource {}, exception occured {}", resource, exception);
			}
			resource = queue.poll();
		}
	}
	
}
