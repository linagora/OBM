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
package org.obm.jersey.injection;

import java.lang.reflect.Type;

import javax.ws.rs.Path;
import javax.ws.rs.core.Feature;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.ResourceConfig;

import com.google.inject.Injector;
import com.google.inject.Key;

public class JerseyResourceConfig extends ResourceConfig {

	public JerseyResourceConfig(Injector injector) {
		super();

		registerClasses(JerseyEventListener.class);
		register(new JerseyDiBinder(injector));

		register(injector);
	}

	private void register(Injector injector) {
		while (injector != null) {
			for (Key<?> key : injector.getBindings().keySet()) {
				Type type = key.getTypeLiteral().getType();
				if (type instanceof Class) {
					Class<?> c = (Class<?>)type;
					if (c.isAnnotationPresent(Path.class)) {
						register(c);
					} else if (c.isAnnotationPresent(Provider.class)) {
						register(c);
					} else if (Feature.class.isAssignableFrom(c)) {
						register(c);
					}
				}
			}
			injector = injector.getParent();
		}
	}

}