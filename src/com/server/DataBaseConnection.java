package com.server;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;

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

	boolean changeUserData(int userId, int whatIsToChangeNumber, String newData) {
		boolean result = false;
		try {
			callableStatement = connection.prepareCall("{CALL ChangeUserData(?, ?, ?)}");
			callableStatement.setInt(1, userId);
			callableStatement.setInt(2, whatIsToChangeNumber);
			callableStatement.setString(3, newData);
			resultSet = callableStatement.executeQuery();
			result = resultSet.next();
		} catch (SQLException e) {
			System.out.println("Troubles with connecting to database. Please try one more time later");
			e.printStackTrace();
		}
		return result;
	}

	String getAllFutureTasks() {
		String result = "";
		Timestamp currentDate = new Timestamp(System.currentTimeMillis());

		try {
			resultSet = statement.executeQuery("Select * from tasks_data");
			while (resultSet.next()) {
				if (resultSet.getTimestamp("start_date").after(currentDate)) {
					if (resultSet.getInt("amount_people_needed") != 0) {
						result = resultSet.getInt("task_id") + ';'
								+ resultSet.getString("hotel_name") + ';'
								+ resultSet.getString("address") + ';'
								+ resultSet.getTimestamp("start_date") + ';'
								+ resultSet.getTimestamp("end_date");
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}

	int getNextTask(int userId) {
		long minPeriod = 365;
		int nextTask = 0;
		long diff_in_days;

		Timestamp currentDate = new Timestamp(System.currentTimeMillis());
		try {
			resultSet = statement.executeQuery("Select * from tasks_data join records on records.task_id = tasks_data.task_id " +
					"where records.user_id = " + userId);
			while (resultSet.next()) {

				if (resultSet.getTimestamp("start_date").after(currentDate)) {

					diff_in_days = Math.abs(Duration.between(LocalDateTime.now(), resultSet.getTimestamp("start_date").toLocalDateTime()).toDays());
					if (diff_in_days < minPeriod) {
						minPeriod = diff_in_days;
						nextTask = resultSet.getInt("task_id");
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return nextTask;
	}

	String getAllTaskData(int taskId) {
		String result = "";
		try {
			resultSet = statement.executeQuery("Select * from tasks_data where task_id = " + taskId);
			if (resultSet.next()) {
				result = resultSet.getString("hotel_name") + ';'
						+ resultSet.getString("address") + ';'
						+ dateFormat.format(resultSet.getTimestamp("start_date")) + ';'
						+ dateFormat.format(resultSet.getTimestamp("end_date"));
			}
		} catch (SQLException e) {
			System.out.println("Troubles with connecting to database. Please try one more time later");
			e.printStackTrace();
		}
		return result;
	}

	String getAllUserData(int userId) {
		String result = "";
		try {
			callableStatement = connection.prepareCall("{CALL GetAllUserData(?)}");
			callableStatement.setInt(1, userId);
			resultSet = callableStatement.executeQuery();
			if (resultSet.next()) {
				result = resultSet.getString("username") + ';'
						+ resultSet.getString("password") + ';'
						+ resultSet.getString("name") + ';'
						+ resultSet.getString("Lastname") + ';'
						+ resultSet.getInt("pesel");
			}
		} catch (SQLException e) {
			System.out.println("Troubles with connecting to database. Please try one more time later");
			e.printStackTrace();
		}
		return result;
	}

	String getFutureTasks(int userId) {
		Timestamp currentDate = new Timestamp(System.currentTimeMillis());
		String result = "";
		try {
			resultSet = statement.executeQuery("Select * from tasks_data join records on records.task_id = tasks_data.task_id " +
					"where records.user_id = " + userId);
			if (resultSet.next()) {
				if (resultSet.getTimestamp("start_date").after(currentDate)) {
					result = resultSet.getString("hotel_name") + ';'
							+ resultSet.getString("address") + ';'
							+ resultSet.getTimestamp("start_date") + ';'
							+ resultSet.getTimestamp("end_date");
				}
			} else {
				System.out.println("You have no tasks to realize");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
}