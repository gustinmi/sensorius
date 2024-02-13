package com.gustinmi.sensorius.healthcheck;

import java.util.ArrayList;
import java.util.List;

public class HttpModel {
	
	public static enum HttpPartType {
		/** Request line in format: METHOD PATH PROTOCOL */
		REQUEST_LINE,
		
		/** any line ended with \r\n */
		HEADER,                
	}
		
	/**
	 * Specialized HTTP part header in format:
	 * HEADER_NAME : HEADER VALUE 
	 * @author mgustin
	 *
	 */
	public static class HttpHeaderPart {
		
		private final String headerName;
		private final String headerValue;
		
		private HttpHeaderPart(String headerName, String headerValue) {
			this.headerName = headerName;
			this.headerValue = headerValue;
		}

		public String getHeaderName() {
			return headerName;
		}

		public String getHeaderValue() {
			return headerValue;
		}

		/**
		 * Factory metohod for building new header
		 * @param raw
		 * @return
		 */
		public static HttpHeaderPart parse(String raw) {
			final String headerName = raw.substring(0, raw.indexOf(":")).trim().toLowerCase();
			final String headerData = raw.substring(raw.indexOf(":") + 1, raw.length()).trim();
			return new HttpHeaderPart(headerName, headerData);
		}
		
	}

	/**
	 * Generic part of HTTP message
	 * @author mgustin
	 *
	 */
	public static class HttpPart {
		
		private final HttpPartType part;
		private final String contentRaw;
		private final HttpHeaderPart header;
		
		public HttpPart(HttpPartType part, String content) {
			super();
			this.part = part;
			this.contentRaw = content;
			
			if (part.equals(HttpPartType.HEADER)) header = HttpHeaderPart.parse(content);
			else header = null; // its a body
			
		}
		
		public HttpPartType getPart() {
			return part;
		}
		
		public String getContent() {
			return contentRaw;
		}
		
		public String getHeaderValue(String headerName) {
			if (!this.part.equals(HttpPartType.HEADER)) return null;
			
			if (this.header.getHeaderName().equalsIgnoreCase(headerName)) 
					return header.getHeaderValue();
			
			return null;
		}
		
		@Override
		public String toString() {
			return "HttpPart [part=" + part + ", content=" + contentRaw + "]";
		}
		
	}
	
	/**
	 * Represents anatomy of a HTTP request object (non chuncked encoding)
	 * @author mgustin
	 */
	public static class HttpRequest {
		
		// Its a list, because we need to preserve order
		final List<HttpPart> httpContent = new ArrayList<HttpPart>();
		
		public HttpRequest() {}
		
		public void addPart(HttpPart p) {
			httpContent.add(p);
		}
		
		/**
		 * Iterate all header elements, see for matching header name
		 * @param headerName
		 * @return
		 */
		public String getHeaderValue(String headerName) {
			for (HttpPart part : httpContent) {
				if (part.getPart().equals(HttpPartType.HEADER)) {
					String val = part.getHeaderValue(headerName);
					if ( val != null && !val.isEmpty())
						return val; 
				}
			}
			return null;
		}
		
	}

}
