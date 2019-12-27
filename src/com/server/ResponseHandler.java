package com.server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Scanner;
import java.util.TimeZone;

public class ResponseHandler extends Thread {
	Socket mySocket;
	DataBaseConnection dataBaseConnection = new DataBaseConnection();

	public ResponseHandler(Socket socket) {
		super();
		mySocket = socket;
	}

	public void run() {
		try {
			InputStream inputToServer = mySocket.getInputStream();
			OutputStream outputFromServer = mySocket.getOutputStream();

			Scanner scanner = new Scanner(inputToServer, String.valueOf(StandardCharsets.UTF_8));
			PrintWriter serverPrintOut = new PrintWriter(new OutputStreamWriter(outputFromServer, StandardCharsets.UTF_8), true);

			serverPrintOut.println("Hello! Print \"exit\" to exit");

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();

				if (line.toLowerCase().trim().equals("exit"))
					break;

				if (line.split("\\s+")[0].toLowerCase().trim().equals("getalluserdata")) {
					String result = dataBaseConnection.getAllUserData(Integer.parseInt(line.split("\\s+")[1]));
					if(!result.equals("")) serverPrintOut.println(result);
					else serverPrintOut.println("Error whlie executing procedur");
				}
			}

			mySocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void sendOutput(String body) throws IOException {
		OutputStream outputFromServer = mySocket.getOutputStream();

		PrintWriter serverPrintOut = new PrintWriter(new OutputStreamWriter(outputFromServer, StandardCharsets.UTF_8), true);

		printHeadersSuccess(serverPrintOut, body.length());
		serverPrintOut.println(body);
		serverPrintOut.flush();
	}

	void printHeadersSuccess(PrintWriter serverPrintOut, int bodyLenght) {
		serverPrintOut.println("HTTP/1.1 200 Success");
		serverPrintOut.println("Date: " + getServerTime());
		serverPrintOut.println("Content-Type: text/html");
		serverPrintOut.println("Content-Length: " + bodyLenght);
		serverPrintOut.println();
	}

	String getServerTime() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(calendar.getTime());
	}
}