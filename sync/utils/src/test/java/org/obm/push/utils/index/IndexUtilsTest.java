package org.obm.push.utils.index;

import java.util.ArrayList;
import java.util.List;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class IndexUtilsTest {

	private static class IntIndexed implements Indexed<Integer> {
		
		private final int index;

		public IntIndexed(int index) {
			this.index = index;
		}
		
		@Override
		public Integer getIndex() {
			return index;
		}
	}
	
	@Test
	public void testEmptyList() {
		ImmutableList<Indexed<Integer>> emptyList = ImmutableList.of();
		ArrayList<Integer> listIndexes = IndexUtils.listIndexes(emptyList);
		Assertions.assertThat(listIndexes).isEmpty();
	}
	
	@Test(expected=NullPointerException.class)
	public void testNullList() {
		@SuppressWarnings("unused")
		ArrayList<Integer> listIndexes = IndexUtils.listIndexes((List<Indexed<Integer>>)null);
	}

	@Test
	public void testSimpleList() {
		ImmutableList<IntIndexed> emptyList = 
				ImmutableList.of(new IntIndexed(0), new IntIndexed(1), new IntIndexed(2));
		ArrayList<Integer> listIndexes = IndexUtils.listIndexes(emptyList);
		Assertions.assertThat(listIndexes).containsExactly(0, 1, 2);
	}
	
	
}
