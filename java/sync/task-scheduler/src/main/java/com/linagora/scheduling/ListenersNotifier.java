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
package com.linagora.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

public class ListenersNotifier<T extends Task> {

	private final Logger logger;
	private final ImmutableList<Listener<T>> listeners;

	public ListenersNotifier(Class<?> loggerClass, ImmutableList<Listener<T>> listeners) {
		this.logger = LoggerFactory.getLogger(loggerClass);
		this.listeners = listeners;
	}
	
	public void notifyCanceled(final ScheduledTask<T> task) {
		notify(new Function<Listener<T>, Void>() {

			@Override
			public Void apply(Listener<T> listener) {
				listener.canceled(task);
				return null;
			}
		});
	}
	public void notifyFailed(final ScheduledTask<T> task, final Throwable t) {
		notify(new Function<Listener<T>, Void>() {
			
			@Override
			public Void apply(Listener<T> listener) {
				listener.failed(task, t);
				return null;
			}
		});
	}
	
	public void notifyRunning(final ScheduledTask<T> task) {
		notify(new Function<Listener<T>, Void>() {
			
			@Override
			public Void apply(Listener<T> listener) {
				listener.running(task);
				return null;
			}
		});
	}
	
	public void notifyScheduled(final ScheduledTask<T> task) {
		notify(new Function<Listener<T>, Void>() {
			
			@Override
			public Void apply(Listener<T> listener) {
				listener.scheduled(task);
				return null;
			}
		});
	}
	
	public void notifyTerminated(final ScheduledTask<T> task) {
		notify(new Function<Listener<T>, Void>() {
			
			@Override
			public Void apply(Listener<T> listener) {
				listener.terminated(task);
				return null;
			}
		});
	}
	
	public void notify(Function<Listener<T>, Void> function) {
		for (Listener<T> listener: listeners) {
			try {
				function.apply(listener);
			} catch (Exception listenerException) {
				logger.error("Error notifying a listener", listenerException);
			}
		}
	}
}
