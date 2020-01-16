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
			int count = callableStatement.executeUpdate();
			result = (count > 0);
		} catch (SQLException e) {
			System.out.println("Troubles with connecting to database. Please try one more time later");
			e.printStackTrace();
		}
		return result;
	}

	String getAllFutureTasks() {
		StringBuilder result = new StringBuilder();
		Timestamp currentDate = new Timestamp(System.currentTimeMillis());

		try {
			resultSet = statement.executeQuery("Select * from tasks_data");
			while (resultSet.next()) {
				if (resultSet.getTimestamp("start_date").after(currentDate)) {
					if (resultSet.getInt("amount_people_needed") != 0) {
						result.append(resultSet.getInt("task_id")).append(';')
								.append(resultSet.getString("hotel_name")).append(';')
								.append(resultSet.getString("address")).append(';')
								.append(resultSet.getTimestamp("start_date")).append(';')
								.append(resultSet.getTimestamp("end_date")).append('\n');
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result.toString();
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

	String getTaskData(int taskId) {
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

	String getUserData(int userId) {
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

	String getFutureTask(int userId) {
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
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	String getHistoryTasks(int userId) {
		Timestamp currentDate = new Timestamp(System.currentTimeMillis());
		String result = "";
		try {
			resultSet = statement.executeQuery("Select * from tasks_data join records on records.task_id = tasks_data.task_id " +
					"where records.user_id = " + userId);
			if (resultSet.next()) {
				if (resultSet.getTimestamp("start_date").before(currentDate)) {
					result = resultSet.getString("hotel_name") + ';'
							+ resultSet.getString("address") + ';'
							+ resultSet.getTimestamp("start_date") + ';'
							+ resultSet.getTimestamp("end_date");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	int validLoginData(String username, String password) {
		int userId = -1;
		try {
			resultSet =
					statement.executeQuery("Select * from users_data where username = \"" + username + "\" and password = \"" + password + "\"");
			if (resultSet.next()) {
				if (resultSet.getString("username").equals(username) && resultSet.getString("password").equals(password)) {
					return resultSet.getInt("user_id");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return userId;
	}

	boolean addNewUser(String username, String password, String firstname, String lastname, String pesel) {
		boolean result = false;
		try {
			callableStatement = connection.prepareCall("INSERT INTO users_data (username, password, name, lastname, pesel)" + "VALUES(?, ?, ?, ?, ?)");
			callableStatement.setString(1, username);
			callableStatement.setString(2, password);
			callableStatement.setString(3, firstname);
			callableStatement.setString(4, lastname);
			callableStatement.setString(5, pesel);
			int count = callableStatement.executeUpdate();
			result = (count > 0);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	boolean validateUsername(String username) {
		boolean result = false;
		try {
			resultSet = statement.executeQuery("Select * from users_data where username = \"" + username + "\"");
			result = resultSet.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	String getNewTask(int userId, int taskId, String password) {
		int freePlaces, tasks_amount = 0;
		try {
			resultSet = statement.executeQuery("select count(*) from tasks_data");
			if (resultSet.next()) {
				tasks_amount = resultSet.getInt(1);
			}
			/*if (tasks_amount >= taskId) {*/
			freePlaces = getFreePlaces(taskId);
			if (freePlaces != 0) {
				if (getPass(userId).equals(password)) {
					if (saveToTask(userId, taskId)) {
						return getTaskData(taskId);
					} else {
						return "error while saving to task";
					}
				} else {
					return "bad password";
				}
			} else {
				return "not enough";
			}
			/*} else {
				return "bad task id";
			}*/
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	int getFreePlaces(int taskId) {
		try {
			resultSet = statement.executeQuery("Select * from tasks_data where task_id = " + taskId);
			if (resultSet.next()) {
				return resultSet.getInt("amount_people_needed");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	String getPass(int userId) {
		try {
			resultSet = statement.executeQuery("select * from users_data where user_id = " + userId);
			if (resultSet.next()) {
				return resultSet.getString("password");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	boolean saveToTask(int userId, int taskId) {
		try {
			callableStatement = connection.prepareCall("INSERT INTO records (user_id, task_id)" + "VALUES(?, ?)");
			callableStatement.setInt(1, userId);
			callableStatement.setInt(2, taskId);
			int count = callableStatement.executeUpdate();

			if (count > 0) {
				/*callableStatement = connection.prepareCall("{?= CALL changePeopleNeeded(?)}");
				callableStatement.registerOutParameter(1, Types.INTEGER);
				callableStatement.setInt(1, taskId);
				resultSet = callableStatement.executeQuery();*/

				callableStatement = connection.prepareCall("{CALL changePeopleNum(?)}");
				callableStatement.setInt(1, taskId);
				int count2 = callableStatement.executeUpdate();
				return (count2 > 0);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	boolean ifJoinYet(int userId, int taskId){
		try {
			resultSet = statement.executeQuery("Select * from records where user_id = " + userId);
			while(resultSet.next()){
				if(resultSet.getInt("task_id") == taskId)
					return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
}