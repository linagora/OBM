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
package org.obm.sync;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import net.fortuna.ical4j.model.property.Method;

import org.assertj.core.api.StringAssert;

import com.google.common.collect.Lists;

public class IntegrationTestICSUtils {

	public static StringAssert assertIcsReplyFormat(Message message) throws JMSException {
		return assertIcsMethodFormat(message, Method.REPLY);
	}
	
	public static StringAssert assertIcsCancelFormat(Message message) throws JMSException {
		return assertIcsMethodFormat(message, Method.CANCEL);
	}

	public static StringAssert assertIcsMethodFormat(Message message, Method method) throws JMSException {
		return assertThat(((TextMessage)message).getText())
			.startsWith(
				"BEGIN:VCALENDAR\r\n" +
				"PRODID:-//Aliasource Groupe LINAGORA//OBM Calendar //FR\r\n" +
				"VERSION:2.0\r\n" +
				"CALSCALE:GREGORIAN\r\n" +
				"METHOD:" + method.getValue() + "\r\n" +
				"BEGIN:VEVENT\r\n")
			.endsWith(
				"X-OBM-ORIGIN:integration-testing\r\n" +
				"X-OBM-DOMAIN:domain.org\r\n" +
				"X-OBM-DOMAIN-UUID:b55911e6-6848-4f16-abd4-52d94b6901a6\r\n" +
				"END:VEVENT\r\n" +
				"END:VCALENDAR\r\n");
	}
	
	public static class StoreMessageReceivedListener implements MessageListener {

		final List<Message> messages;
		CountDownLatch countDownLatch;
		
		public StoreMessageReceivedListener() {
			messages = Lists.newArrayList();
			countDownLatch = new CountDownLatch(0);
		}

		public List<Message> messages() {
			return messages;
		}
		
		@Override
		public void onMessage(Message message) {
			messages.add(message);
			countDownLatch.countDown();
		}
		
		public void waitForMessageCount(int messageCount, int timeoutInMs) throws InterruptedException, TimeoutException {
			if (messages.size() < messageCount) {
				int missingMessageCount = messageCount - messages.size();
				countDownLatch = new CountDownLatch(missingMessageCount);
				if (!countDownLatch.await(timeoutInMs, TimeUnit.MILLISECONDS)) {
					throw new TimeoutException(String.format(
							"Not enough message received, timeout:%s expected:%d received:%d", 
							timeoutInMs, messageCount, messages.size()));
				}
			}
		}
	}
}
