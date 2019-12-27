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
			Class.forName("com.mysql.cj.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://149.202.31.190:3306/data", "bazodanowiec",
					"OlaKuc17");
			statement = connection.createStatement();
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
		} catch (Exception ex) {
			System.out.println("Exception: " + ex.getMessage());
		}
	}

	/*void getAllTaskData() {
		try {
			resultSet = statement.executeQuery("Select * from tasks_data where task_id = 1");
			if (resultSet.next()) {
				System.out.println("Takes place in: " + resultSet.getString("hotel_name")
						+ ",\nhotel address: " + resultSet.getString("address")
						+ ",\ntask starts on: " + resultSet.getTimestamp("start_date")
						+ ",\nand end on: " + dateFormat.format(resultSet.getTimestamp("end_date")) + "\n\n");
			}
		} catch (SQLException e) {
			System.out.println("Troubles with connecting to database. Please try one more time later");
			e.printStackTrace();
		}
	}*/

	String getAllUserData(int curUserIndex) {
		String result = "";
		try {
			callableStatement = connection.prepareCall("{CALL GetAllUserData(?)}");
			callableStatement.setInt(1, curUserIndex);
			resultSet = callableStatement.executeQuery();
			result = "Username: " + resultSet.getString("username") + "\n"
					+ "Password: " + resultSet.getString("password") + "\n"
					+ "Name: " + resultSet.getString("name") + "\n"
					+ "Lastname: " + resultSet.getString("Lastname") + "\n"
					+ "Pesel: " + resultSet.getInt("pesel");
		} catch (SQLException e) {
			System.out.println("Troubles with connecting to database. Please try one more time later");
			e.printStackTrace();
		}
		return result;
	}
}
