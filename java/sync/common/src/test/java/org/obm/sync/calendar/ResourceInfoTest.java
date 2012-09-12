package org.obm.sync.calendar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.fest.assertions.api.Assertions;

@RunWith(SlowFilterRunner.class)
public class ResourceInfoTest {

	@Test
	public void testBuild() {
		ResourceInfo resourceInfo = ResourceInfo.builder().id(2).name("resource_name")
				.mail("resource@domain").description("description").write(true).read(false).build();
		Assertions.assertThat(resourceInfo.getId()).isEqualTo(2);
		Assertions.assertThat(resourceInfo.getName()).isEqualTo("resource_name");
		Assertions.assertThat(resourceInfo.getMail()).isEqualTo("resource@domain");
		Assertions.assertThat(resourceInfo.getDescription()).isEqualTo("description");
		Assertions.assertThat(resourceInfo.isWrite()).isTrue();
		Assertions.assertThat(resourceInfo.isRead()).isFalse();
	}

	@Test(expected = IllegalStateException.class)
	public void testBuilderNeedsId() {
		ResourceInfo.builder().name("resource_name").mail("resource@domain")
				.description("description").write(true).read(false).build();

	}

	@Test(expected = IllegalStateException.class)
	public void testBuilderNeedsName() {
		ResourceInfo.builder().id(2).mail("resource@domain").description("description").write(true)
				.read(false).build();
	}

	@Test(expected = IllegalStateException.class)
	public void testBuilderNeedsNonEmptyName() {
		ResourceInfo.builder().id(2).name("").mail("resource@domain").description("description")
				.write(true).read(false).build();
	}

	@Test(expected = IllegalStateException.class)
	public void testBuilderNeedsNonEmptyMail() {
		ResourceInfo.builder().id(2).name("resource_name").mail("").description("description")
				.write(true).read(false).build();
	}

	@Test(expected = IllegalStateException.class)
	public void testBuilderNeedsMail() {
		ResourceInfo.builder().id(2).name("resource_name").description("description").write(true)
				.read(false).build();
	}

	@Test
	public void testDescriptionIsOptional() {
		ResourceInfo resourceInfo = ResourceInfo.builder().id(2).name("resource_name")
				.mail("resource@domain").write(true).read(false).build();
		Assertions.assertThat(resourceInfo.getId()).isEqualTo(2);
		Assertions.assertThat(resourceInfo.getName()).isEqualTo("resource_name");
		Assertions.assertThat(resourceInfo.getMail()).isEqualTo("resource@domain");
		Assertions.assertThat(resourceInfo.getDescription()).isNull();
		Assertions.assertThat(resourceInfo.isWrite()).isTrue();
		Assertions.assertThat(resourceInfo.isRead()).isFalse();
	}

	@Test(expected = IllegalStateException.class)
	public void testBuilderNeedsWritel() {
		ResourceInfo.builder().id(2).name("resource_name").mail("resource@domain")
				.description("description").read(false).build();
	}

	@Test(expected = IllegalStateException.class)
	public void testBuilderNeedsReadl() {
		ResourceInfo.builder().id(2).name("resource_name").mail("resource@domain")
				.description("description").write(true).build();
	}

}
