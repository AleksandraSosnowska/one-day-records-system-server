package com.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

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
					case "getfuturetasks": {
						String result = dataBaseConnection.getFutureTasks(Integer.parseInt(splittedInput[1]));
						serverPrintOut.println(result);
						break;
					}
					case "gethistorytasks": {
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
						String result = dataBaseConnection.validLoginData(splittedInput[1], splittedInput[2]);
						serverPrintOut.println(result);
						break;
					}
					case "addnewuser": {
						boolean result = dataBaseConnection.addNewUser(splittedInput[1], splittedInput[2], splittedInput[3],
								splittedInput[4], splittedInput[5]);
						serverPrintOut.println(result);
						break;
					}
					case "addnewtask": {
						SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss");
						Date parsedDateBegin = dateFormat.parse(splittedInput[3]);
						Date parsedDateEnd = dateFormat.parse(splittedInput[4]);
						Timestamp timestampBegin = new java.sql.Timestamp(parsedDateBegin.getTime());
						Timestamp timestampEnd = new java.sql.Timestamp(parsedDateEnd.getTime());

						boolean result = dataBaseConnection.addNewTask(splittedInput[1], splittedInput[2], timestampBegin,
								timestampEnd, splittedInput[5]);
						serverPrintOut.println(result);
						break;
					}
					case "validateusername": {
						boolean result = dataBaseConnection.validateUsername(splittedInput[1]);
						serverPrintOut.println(result);
						break;
					}
					case "getnewtask": {
						String result = dataBaseConnection.getNewTask(Integer.parseInt(splittedInput[1]),
								Integer.parseInt(splittedInput[2]),
								splittedInput[3]);
						serverPrintOut.println(result);
						break;
					}
					case "ifjoinyet": {
						boolean result = dataBaseConnection.ifJoinYet(Integer.parseInt(splittedInput[1]),
								Integer.parseInt(splittedInput[2]));
						serverPrintOut.println(result);
						break;
					}
					case "getpass": {
						String result = dataBaseConnection.getPass(Integer.parseInt(splittedInput[1]));
						serverPrintOut.println(result);
						break;
					}
					case "savetotask": {
						boolean result = dataBaseConnection.saveToTask(Integer.parseInt(splittedInput[1]),
								Integer.parseInt(splittedInput[2]));
						serverPrintOut.println(result);
						break;
					}
					case "getfuturetasksuser": {
						String result = dataBaseConnection.getFutureTasksUser(Integer.parseInt(splittedInput[1]));
						serverPrintOut.println(result);
						break;
					}
					case "deletetaskandrecords": {
						boolean result = dataBaseConnection.deleteTaskAndRecords(Integer.parseInt(splittedInput[1]));
						serverPrintOut.println(result);
						break;
					}
					case "getnoadminusers": {
						String result = dataBaseConnection.getNoAdminUsers();
						serverPrintOut.println(result);
						break;
					}
					case "updatetask": {
						boolean result = dataBaseConnection.updateTask(Integer.parseInt(splittedInput[1]), splittedInput[2], splittedInput[3]);
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
}