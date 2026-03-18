package com.hana8.hanaro.common.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.hana8.hanaro.common.util.AccountUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AccountNumSerializer extends JsonSerializer<String> {

	private final AccountUtil accountUtil;

	@Override
	public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		if (value == null) {
			gen.writeNull();
			return;
		}

		String cleanNum = value.replaceAll("[\\s-]", "");
		String masked = accountUtil.mask(cleanNum);
		String formatted = accountUtil.format(masked);
		gen.writeString(formatted);
	}
}
