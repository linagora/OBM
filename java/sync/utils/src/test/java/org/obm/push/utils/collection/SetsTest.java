package org.obm.push.utils.collection;

import java.util.Comparator;
import java.util.Set;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

public class SetsTest {

	private final class ComparatorUsingAMember implements Comparator<A> {
		@Override
		public int compare(A o1, A o2) {
			return o1.a - o2.a;
		}
	}

	private final class ComparatorUsingAllMembers implements Comparator<A> {
		@Override
		public int compare(A o1, A o2) {
			return Double.valueOf(Math.pow(o1.a - o2.a, 2) + Math.pow(o1.b - o2.b, 2)).intValue();
		}
	}

	
	private static class A {
		public int a;
		public int b;
		
		public A(int a, int b) {
			super();
			this.a = a;
			this.b = b;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof A) {
				A other = (A) obj;
				return Objects.equal(a, other.a)
					&& Objects.equal(b, other.b);
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return Objects.hashCode(a, b);
		}
	}
	
	private A a(int a, int b) {
		return new A(a, b);
	}
	
	@Test(expected=NullPointerException.class)
	public void testCol1IsNull() {
		ImmutableList<A> col1 = null;
		ImmutableList<A> col2 = ImmutableList.of(a(2, 4));
		Sets.difference(col1, col2, new ComparatorUsingAMember());
	}
	
	@Test(expected=NullPointerException.class)
	public void testCol2IsNull() {
		ImmutableList<A> col1 = ImmutableList.of(a(2, 4));
		ImmutableList<A> col2 = null;
		Sets.difference(col1, col2, new ComparatorUsingAMember());
	}
	
	@Test(expected=NullPointerException.class)
	public void testComparatorIsNull() {
		ImmutableList<A> col1 = ImmutableList.of(a(2, 4));
		ImmutableList<A> col2 = ImmutableList.of(a(2, 4));
		Sets.difference(col1, col2, null);
	}
	
	@Test
	public void testUsingAMemberCol1Empty() {
		ImmutableList<A> col1 = ImmutableList.of();
		ImmutableList<A> col2 = ImmutableList.of(a(2, 4));
		Set<A> result = Sets.difference(col1, col2, new ComparatorUsingAMember());
		Assertions.assertThat(result).isEmpty();
	}
	
	@Test
	public void testUsingAMemberCol2Empty() {
		ImmutableList<A> col1 = ImmutableList.of(a(2, 4));
		ImmutableList<A> col2 = ImmutableList.of();
		Set<A> result = Sets.difference(col1, col2, new ComparatorUsingAMember());
		Assertions.assertThat(result).contains(a(2, 4));
	}
	
	@Test
	public void testUsingAMember() {
		ImmutableList<A> col1 = ImmutableList.of(a(1, 2), a(2, 3));
		ImmutableList<A> col2 = ImmutableList.of(a(2, 4));
		Set<A> result = Sets.difference(col1, col2, new ComparatorUsingAMember());
		Assertions.assertThat(result).containsOnly(a(1, 2));
	}
	
	@Test(expected=IllegalStateException.class)
	public void testUsingAMemberCol1ContainsDuplicates() {
		ImmutableList<A> col1 = ImmutableList.of(a(1, 2), a(1, 3));
		ImmutableList<A> col2 = ImmutableList.of(a(2, 4));
		Sets.difference(col1, col2, new ComparatorUsingAMember());
	}
	
	
	@Test(expected=IllegalStateException.class)
	public void testUsingAMemberCol2ContainsDuplicates() {
		ImmutableList<A> col1 = ImmutableList.of(a(1, 2));
		ImmutableList<A> col2 = ImmutableList.of(a(1, 2), a(1, 3));
		Sets.difference(col1, col2, new ComparatorUsingAMember());
	}
	
	@Test
	public void testUsingAMemberEmptyResult() {
		ImmutableList<A> col1 = ImmutableList.of(a(1, 2), a(2, 3));
		ImmutableList<A> col2 = ImmutableList.of(a(1, 3), a(2, 4));
		Set<A> result = Sets.difference(col1, col2, new ComparatorUsingAMember());
		Assertions.assertThat(result).isEmpty();
	}
	
	@Test
	public void testUsingAllMembers() {
		ImmutableList<A> col1 = ImmutableList.of(a(1, 2), a(2, 3));
		ImmutableList<A> col2 = ImmutableList.of(a(2, 4));
		Set<A> result = Sets.difference(col1, col2, new ComparatorUsingAllMembers());
		Assertions.assertThat(result).containsOnly(a(1, 2), a(2, 3));
	}

}
