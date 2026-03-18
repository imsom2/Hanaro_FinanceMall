package com.hana8.hanaro.common.converter;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class AccountNumSerializer extends StdSerializer<String> {
	public AccountNumSerializer() {
		super(String.class);
	}

	@Override
	public void serialize(String value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
		System.out.println("serializer called: " + value);

		if (value == null) {
			gen.writeNull();
			return;
		}

		String cleanNum = value.replaceAll("[\\s-]", "");
		String masked = AccountUtil.mask(cleanNum);

		gen.writeString(masked);
	}
}
