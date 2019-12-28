package com.server;

import java.sql.*;
import java.text.SimpleDateFormat;

@SuppressWarnings({"SqlDialectInspection"})
public class DataBaseConnection {

	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static Connection connection;
	public static Statement statement;
	public static CallableStatement callableStatement;
	public static ResultSet resultSet;

	public DataBaseConnection() {
		try {
			DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
			connection = DriverManager.getConnection("jdbc:mysql://149.202.31.190:3306/data", "bazodanowiec",
					"OlaKuc17");
			statement = connection.createStatement();
		} catch (Exception ex) {
			System.out.println("Exception: " + ex.getMessage());
		}
	}

	String getAllTaskData(int taskId) {
		String result = "";
		try {
			resultSet = statement.executeQuery("Select * from tasks_data where task_id = " + taskId);
			if (resultSet.next()) {
				result = "Takes place in: " + resultSet.getString("hotel_name")
						+ ",\nhotel address: " + resultSet.getString("address")
						+ ",\ntask starts on: " + dateFormat.format(resultSet.getTimestamp("start_date"))
						+ ",\nand end on: " + dateFormat.format(resultSet.getTimestamp("end_date"));
			}
		} catch (SQLException e) {
			System.out.println("Troubles with connecting to database. Please try one more time later");
			e.printStackTrace();
		}
		return result;
	}

	String getAllUserData(int curUserIndex) {
		String result = "";
		try {
			callableStatement = connection.prepareCall("{CALL GetAllUserData(?)}");
			callableStatement.setInt(1, curUserIndex);
			resultSet = callableStatement.executeQuery();
			if (resultSet.next()) {
				result = "Username: " + resultSet.getString("username") + "\n"
						+ "Password: " + resultSet.getString("password") + "\n"
						+ "Name: " + resultSet.getString("name") + "\n"
						+ "Lastname: " + resultSet.getString("Lastname") + "\n"
						+ "Pesel: " + resultSet.getInt("pesel");
			}
		} catch (SQLException e) {
			System.out.println("Troubles with connecting to database. Please try one more time later");
			e.printStackTrace();
		}
		return result;
	}

	static void changeUserData(int curUserIndex, String name, String lastname, String username, String password){
		try {
			callableStatement = connection.prepareCall("{CALL ChangeUserData(?, ?, ?, ?, ?)}");
			callableStatement.setInt(1, curUserIndex);
			callableStatement.setString(2, name);
			callableStatement.setString(3, lastname);
			callableStatement.setString(4, username);
			callableStatement.setString(5, password);
			resultSet = callableStatement.executeQuery();
		} catch (SQLException e) {
			System.out.println("Troubles with connecting to database. Please try one more time later");
			e.printStackTrace();
		}
	}

	static String showAllTasks() {
		String result = "";
		Timestamp currentDate = new Timestamp(System.currentTimeMillis());

		try {
			resultSet = statement.executeQuery("Select * from tasks_data");
			while(resultSet.next()){
				if(resultSet.getTimestamp("start_date").after(currentDate)){
					if(resultSet.getInt("amount_people_needed") != 0){
						result = "Task's id: " + resultSet.getInt("task_id") + "\n";
						result += "Takes place in: " + resultSet.getString("hotel_name")
								+ ",\nhotel address: " + resultSet.getString("address")
								+ ",\ntask starts on: " + resultSet.getTimestamp("start_date")
								+ ",\nand end on: " + resultSet.getTimestamp("end_date");
					}
					else{
						System.out.println("brak miejsc");
					}
				}
				else{
					System.out.println("Było! ");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}

}
