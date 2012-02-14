package org.obm.push.utils;

import org.apache.james.mime4j.dom.address.AddressList;
import org.apache.james.mime4j.field.address.LenientAddressBuilder;
import org.fest.assertions.Assertions;
import org.junit.Test;

public class Mime4jUtilsTest {

	@Test
	public void parseInvalidEmailDontThrowIndexOutOfBound() {
		AddressList addressList = LenientAddressBuilder.DEFAULT.parseAddressList("To: <@domain.com>");
		Assertions.assertThat(addressList).isNotNull();
	}

}
