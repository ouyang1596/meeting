package com.deshang365.meeting.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;

import android.R.string;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

public class JacksonConverter implements Converter {
	private final ObjectMapper objectMapper;

	public JacksonConverter(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public Object fromBody(TypedInput body, Type type) throws ConversionException {
		JavaType javaType = objectMapper.getTypeFactory().constructType(type);
		try {
			return objectMapper.readValue(body.in(), javaType);
		} catch (IOException e) {
			throw new ConversionException(e);
		}
	}

	@Override
	public TypedOutput toBody(Object object) {
		try {
			String charset = "UTF-8";
			if (object instanceof String) {
				return new StringOutput((String) object, charset);
			} else {
				return new JsonTypedOutput(objectMapper.writeValueAsBytes(object), charset);
			}
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	private static class StringOutput implements TypedOutput {
		private final String data;
		private final String mimeType;

		public StringOutput(String data, String charset) {
			this.data = data;
			this.mimeType = "application/json; charset=" + charset;
		}

		@Override
		public String fileName() {
			return null;
		}

		@Override
		public String mimeType() {
			return mimeType;
		}

		@Override
		public long length() {
			return data.length();
		}

		@Override
		public void writeTo(OutputStream out) throws IOException {
			out.write(data.getBytes());
		}
	}

	private static class JsonTypedOutput implements TypedOutput {
		private final byte[] jsonBytes;
		private final String mimeType;

		public JsonTypedOutput(byte[] jsonBytes, String charset) {
			this.jsonBytes = jsonBytes;
			this.mimeType = "application/json; charset=" + charset;
		}

		@Override
		public String fileName() {
			return null;
		}

		@Override
		public String mimeType() {
			return mimeType;
		}

		@Override
		public long length() {
			return jsonBytes.length;
		}

		@Override
		public void writeTo(OutputStream out) throws IOException {
			out.write(jsonBytes);
		}
	}
}