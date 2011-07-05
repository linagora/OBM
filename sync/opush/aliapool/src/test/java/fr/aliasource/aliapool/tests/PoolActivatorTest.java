package fr.aliasource.aliapool.tests;

import org.junit.Assert;
import org.junit.Test;

import fr.aliasource.obm.aliapool.PoolActivator;

public class PoolActivatorTest {

	@Test
	public void testActivator() {
		Assert.assertNotNull(PoolActivator.getInstance());
	}
	
}
