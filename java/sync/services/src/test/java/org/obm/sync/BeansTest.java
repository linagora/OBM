package org.obm.sync;

import org.junit.Before;
import org.junit.Test;
import org.obm.sync.bean.EqualsVerifierUtils;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventRecurrence;

import com.google.common.collect.ImmutableList;

public class BeansTest {

	private EqualsVerifierUtils equalsVerifierUtilsTest;
	
	@Before
	public void init() {
		equalsVerifierUtilsTest = new EqualsVerifierUtils();
	}
	
	@Test
	public void test() {
		ImmutableList<Class<?>> list = 
				ImmutableList.<Class<?>>builder()
					.add(EventObmId.class)
					.add(EventExtId.class)
					.add(Event.class)
					.add(EventRecurrence.class)
					.build();
		equalsVerifierUtilsTest.test(list);
	}
	
}
