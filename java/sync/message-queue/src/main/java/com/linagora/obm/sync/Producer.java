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
package com.linagora.obm.sync;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.google.common.base.Throwables;

public class Producer {

	private final static int MAX_AVAILABLE = 1;
	private final static long TIMEOUT_IN_SECONDS = 5;

	private final Session session;
	private final MessageProducer producer;
	private final Semaphore mutex = new Semaphore(MAX_AVAILABLE, true);


	public Producer(Session session, MessageProducer producer) {
		this.session = session;
		this.producer = producer;
	}
	
	public void write(String message) throws JMSException, TimeoutException {
		TextMessage messageSent = session.createTextMessage(message);
		this.send(messageSent);
	}
	
	public void send(Message message) throws JMSException, TimeoutException {
		try {
			if (mutex.tryAcquire(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)) {
				try {
					producer.send(message);
				} finally {
					mutex.release();
				}
			}
			else {
				throw new TimeoutException("Unable to acquire lock over the JMS producer in a reasonable amount of time");
			}
		} catch (InterruptedException ex) {
			throw Throwables.propagate(ex);
		}
	}
	
	public void close() throws JMSException {
		producer.close();
	}
}
