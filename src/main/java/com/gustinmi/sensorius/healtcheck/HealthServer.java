package com.gustinmi.sensorius.healthcheck;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import static com.gustinmi.sensorius.healthcheck.HttpModel.*;

/** Generic healt server - responds do healtcheck url 
 * <code>curl -H "Content-Type: application/json" -H "Connection: close" -v  http://localhost:8070/</code>
 * @see https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/
 * @author mgustin
*/
public class HealthServer {
	
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
			
			final HttpRequest request = new HttpRequest();
			
			try {
				clientSocket = serverSocket.accept(); // blocking 
			} catch (java.net.SocketTimeoutException e) {
				System.err.print("Socket timeout - we close down server");
				break;
			} 
			
			final PrintWriter clientSocketOut = new PrintWriter(clientSocket.getOutputStream(), true);

			try {
				int dataBuffer;
				
				HttpPartType currentPart = HttpPartType.REQUEST_LINE;
				final InputStream in = clientSocket.getInputStream();
				while ((dataBuffer = in.read()) != -1) {

					// separartor characters	
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
				
				final Runtime runtime = Runtime.getRuntime();
		        String memStat = (String.format("Memory free: %.3f MB, total: %.3f MB, max: %.3f MB", runtime.freeMemory() / 1048576.0, runtime.totalMemory() / 1048576.0, runtime.maxMemory() / 1048576.0));
		        clientSocketOut.println(memStat);
		        clientSocketOut.println();
				clientSocketOut.flush(); 
				clientSocketOut.close(); // close clients output strems
				
				clientSocket.close(); // close socket 
			

			} catch (IOException ioEx) { // io exception was thrown, abandon stream
				System.out.println(ioEx.getMessage());
			}
			
			System.out.println(request.print());
			
			
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
	
}
