package com.gustinmi.sensorius.healtcheck;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.gustinmi.sensorius.RealtimeRetriever;
import com.gustinmi.sensorius.SensorRegistry;

/** Simple generic health server - responds to healthcheck urls.  
 * <code>curl -H "Content-Type: application/json" -H "Connection: close" -v  http://localhost:8097/</code>
 * @see https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/
 * @author mgustin
*/
public class HealthServer {
	
	public static final long MIN_THROTTLE_MS = 5_000; 
	
	// HTTP protocol ESCAPE SEQUENCES 
	public static final char CR = (char)13;
	public static final char LF = (char)10;
	
	private final int port; 
	private final ServerSocket serverSocket;
	Socket clientSocket = null;
		
	public HealthServer(int port) throws IOException {
		super();
		this.port = port;
		
		try {
			System.out.println("starting healtcheck HTTP Server on port " + this.port);
			serverSocket = new ServerSocket(this.port);
			// serverSocket.setSoTimeout(timeout); // 3 minutes timeout
		} catch (IOException e) {
			System.err.println(e);
			throw e;
		}
	}

	public void start() throws IOException {
	
		final StringBuilder httpRawLine = new StringBuilder(1000);
		
		while (true) {
			
			final long timeMilis = RealtimeRetriever.currentTimeMillis();
			
			final HttpRequest request = new HttpRequest();
			
			try {
				clientSocket = serverSocket.accept(); // blocking 
			} catch (java.net.SocketTimeoutException e) {
				System.err.print("Socket timeout - we close down server");
				break;
			} 
			
			if (RealtimeRetriever.currentTimeMillis() - timeMilis < MIN_THROTTLE_MS) return; // basic bounce (DoS protection)
			
			final PrintWriter clientSocketOut = new PrintWriter(clientSocket.getOutputStream(), true);

			try {
				int dataBuffer;
				
				HttpPartType currentPart = HttpPartType.REQUEST_LINE;
				final InputStream in = clientSocket.getInputStream();
				while ((dataBuffer = in.read()) != -1) {

					// separator characters	
					if (dataBuffer == CR) { // CR
						
						if (in.read() == LF) { // read in advance to check if LF 
							
							final String lineContent = httpRawLine.toString();
							System.out.println(lineContent);
							if (lineContent.length() == 0) break; // EOF
							
							// We have end of line
							request.addPart(new HttpPart(currentPart, lineContent)); 
							httpRawLine.setLength(0); // reset line
						
							if (currentPart.equals(HttpPartType.REQUEST_LINE)) // switch parts, there is onyl one request line
								currentPart = HttpPartType.HEADER;

						}
						
					} else { // appending mode 
						httpRawLine.append((char) dataBuffer);
					}
					
				} // end of stream reading while
				
				// response headers
				clientSocketOut.println("HTTP/1.1 200 OK"); // Status line
				clientSocketOut.println("Connection: close"); // Headers
				clientSocketOut.println(); // must have empty line for HTTP headers end
				
				// TODO add healthcheck
				final Runtime runtime = Runtime.getRuntime();
		        final String memStat = (String.format("Memory free: %.3f MB, total: %.3f MB, max: %.3f MB", runtime.freeMemory() / 1048576.0, runtime.totalMemory() / 1048576.0, runtime.maxMemory() / 1048576.0));
		        clientSocketOut.println(memStat);
      
		        final String sensNum = (String.format("Number of detected sensors %s", SensorRegistry.INSTANCE.getSensorCount()));
		        clientSocketOut.println(sensNum);
		        
		        clientSocketOut.println();
				clientSocketOut.flush(); 
				clientSocketOut.close(); // close clients output strems
				
				clientSocket.close(); // close socket 
			

			} catch (IOException ioEx) { // io exception was thrown, abandon stream
				System.out.println(ioEx.getMessage());
			}
			
		} // end of outer while true
		
		serverSocket.close();

		System.out.println("Stopping server");
		
	} // end of method

	public void stop() {
		try {
			if (clientSocket != null) clientSocket.close();
		} catch (Exception e) {} // we dont care
		
		try {
			if (serverSocket != null) serverSocket.close();
		} catch (Exception e) {} // we dont care
	}
	
	
	public static enum HttpPartType {
		/** Request line in format: METHOD PATH PROTOCOL */
		REQUEST_LINE,
		
		/** any line ended with \r\n */
		HEADER,                
	}
		
	/** Specialised HTTP part header in format: HEADER_NAME : HEADER VALUE */
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

	/** Generic part of HTTP message */
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
	
	/** Represents anatomy of a HTTP request object (non chuncked encoding) */
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
