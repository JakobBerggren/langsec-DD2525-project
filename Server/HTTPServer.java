import java.io.*;
import java.net.*;
import java.util.*;

public class HTTPServer implements Runnable{ 
	
	static final File WEB_ROOT = new File(".");
	static final String DEFAULT_FILE = "index.html";
	
    // port to listen connection
	static final int PORT = 6880;
	
	// Client Connection via Socket Class
	private Socket connect;
	
	public HTTPServer(Socket c) {
		connect = c;
	}
	
	public static void main(String[] args) {
		try {
			ServerSocket serverConnect = new ServerSocket(PORT);
			System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");
			
			while (true) {
				HTTPServer myServer = new HTTPServer(serverConnect.accept());
				// create dedicated thread to manage the client connection
				Thread thread = new Thread(myServer);
				thread.start();
			}
			
		} catch (IOException e) {
			System.err.println("Server Connection error : " + e.getMessage());
		}
	}

	@Override
	public void run() {
		BufferedReader in = null; 
        PrintWriter out = null; 
        BufferedOutputStream dataOut = null;
		String fileRequested = null;
		
		try {
			in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			out = new PrintWriter(connect.getOutputStream());
			dataOut = new BufferedOutputStream(connect.getOutputStream());
			
			String input = in.readLine();
			StringTokenizer parse = new StringTokenizer(input);
			String method = parse.nextToken().toUpperCase();
			System.out.println(method);
			// we get file requested
			fileRequested = parse.nextToken().toLowerCase();

			//Parse
			if(fileRequested.contains("?")){	
				String[] parts = fileRequested.split("=");
				fileRequested = parts[1].replace("%2f", "/");	//fix to make search bar input work
			}

			//The added security by using a whitelist for valid files
			//Hashtable<String, String> whitelist = new Hashtable<String, String>();					//MIT2
			//whitelist.put("01", "allowed.txt");														//MIT2

			//if(whitelist.containsKey(fileRequested)){													//MIT2
			//	fileRequested = whitelist.get(fileRequested);											//MIT2


			if (fileRequested.endsWith("/")) {
				fileRequested += DEFAULT_FILE;
			}
			
			File file = new File(WEB_ROOT, fileRequested);

			//The added security by validating the canonical path
			//if (file.getCanonicalPath().startsWith(WEB_ROOT.getCanonicalPath())) {					// MIT1
				
				int fileLength = (int) file.length();
				String content = getContentType(fileRequested);
				
				if (method.equals("GET")) { // GET method so we return content
					byte[] fileData = readFileData(file, fileLength);
					
					//HTTP Headers
					out.println("HTTP/1.1 200 OK");
					out.println("Server: Java HTTP Server : 1.0");
					out.println("Date: " + new Date());
					out.println("Content-type: " + content);
					out.println("Content-length: " + fileLength);
					out.println(); // blank line between headers and content
					out.flush(); // flush character output stream buffer
					
					dataOut.write(fileData, 0, fileLength);
					dataOut.flush();
				}	

				System.out.println("File " + fileRequested + " of type " + content + " returned");
		//}																							// MIT1 & MIT2
			
			
		} catch (FileNotFoundException fnfe) {
			System.err.println("Error with file not found exception : " + fnfe);
			
			
		} catch (IOException ioe) {
			System.err.println("Server error : " + ioe);
		} finally {
			try {
				in.close();
				out.close();
				dataOut.close();
				connect.close(); // we close socket connection
			} catch (Exception e) {
				System.err.println("Error closing stream : " + e.getMessage());
			} 
			
			
			System.out.println("Connection closed.\n");
			
		}
		
		
	}
	
	private byte[] readFileData(File file, int fileLength) throws IOException {
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];
		
		try {
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		} finally {
			if (fileIn != null) 
				fileIn.close();
		}
		
		return fileData;
	}
	
	// return supported MIME Types
	private String getContentType(String fileRequested) {
		if (fileRequested.endsWith(".html"))
			return "text/html";
		else
			return "text/plain";
	}	
}