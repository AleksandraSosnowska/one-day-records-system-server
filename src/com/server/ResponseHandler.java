package com.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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

			serverPrintOut.println("ready");

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();

				if (line.toLowerCase().trim().equals("exit"))
					break;


				final String[] splittedInput = line.split("\\s+");
				switch (splittedInput[0].toLowerCase().trim()) {
					case "getuserdata": {
						String result = dataBaseConnection.getUserData(Integer.parseInt(splittedInput[1]));
						if (!result.equals("")) serverPrintOut.println(result);
						else serverPrintOut.println("error");
						break;
					}
					case "gettaskdata": {
						String result = dataBaseConnection.getTaskData(Integer.parseInt(splittedInput[1]));
						if (!result.equals("")) serverPrintOut.println(result);
						else serverPrintOut.println("error");
						break;
					}
					case "getallfuturetasks": {
						String result = dataBaseConnection.getAllFutureTasks();
						serverPrintOut.println(result);
						break;
					}
					case "changeuserdata": {
						boolean result = dataBaseConnection.changeUserData(Integer.parseInt(splittedInput[1]),
								Integer.parseInt(splittedInput[2]), splittedInput[3]);
						serverPrintOut.println(result);
						break;
					}
					case "getfuturetask": {
						String result = dataBaseConnection.getFutureTask(Integer.parseInt(splittedInput[1]));
						serverPrintOut.println(result);
						break;
					}
					case "gethistorytask": {
						String result = dataBaseConnection.getHistoryTasks(Integer.parseInt(splittedInput[1]));
						serverPrintOut.println(result);
						break;
					}
					case "getnexttask": {
						int result = dataBaseConnection.getNextTask(Integer.parseInt(splittedInput[1]));
						if (!(result == 0)) serverPrintOut.println(result);
						else serverPrintOut.println("error");
						break;
					}
					case "validlogindata": {
						int userId = dataBaseConnection.validLoginata(splittedInput[1], splittedInput[2]);
						serverPrintOut.println(userId);
						break;
					}
					case "addnewuser": {
						boolean result = dataBaseConnection.addNewUser(splittedInput[1], splittedInput[2], splittedInput[3],
								splittedInput[4], splittedInput[5]);
						serverPrintOut.println(result);
						break;
					}
					default:
						serverPrintOut.println("Nope");
						mySocket.close();
				}
			}

			mySocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	String getServerTime() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(calendar.getTime());
	}
}