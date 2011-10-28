package org.obm.sync.bean;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import com.google.common.collect.ImmutableList;

public class EqualsVerifierUtils {
			
	public void test(ImmutableList<Class<?>> list) {
		for (Class<?> clazz: list) {
			createEqualsVerifier(clazz).verify();
		}
	}
	
	private EqualsVerifier<?> createEqualsVerifier(Class<?> clazz) {
		return EqualsVerifier.forClass(clazz).suppress(Warning.NONFINAL_FIELDS).debug();
	}
	
}
