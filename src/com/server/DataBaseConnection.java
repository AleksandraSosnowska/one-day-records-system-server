package com.server;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;

@SuppressWarnings({"SqlDialectInspection"})
public class DataBaseConnection {

	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
	public static Connection connection;
	public static Statement statement;
	public static CallableStatement callableStatement;
	public static ResultSet resultSet;
	public static PreparedStatement preparedStatement;

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

		try {
			resultSet = statement.executeQuery("Select * from tasks_data");
			while (resultSet.next()) {
				result.append(resultSet.getInt("task_id")).append(';')
						.append(resultSet.getString("hotel_name")).append(';')
						.append(resultSet.getString("address")).append(';')
						.append(dateFormat.format(resultSet.getTimestamp("start_date"))).append(';')
						.append(dateFormat.format(resultSet.getTimestamp("end_date"))).append(';')
						.append(resultSet.getInt("amount_people_needed")).append('/');
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
						+ dateFormat.format(resultSet.getTimestamp("end_date")) + ';'
						+ resultSet.getString("amount_people_needed");
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

	String getFutureTasks(int userId) {
		String result = "";
		try {
			resultSet = statement.executeQuery("Select * from tasks_data join records on records.task_id = tasks_data.task_id where records.user_id = " + userId);
			while (resultSet.next()) {
				if (resultSet.getTimestamp("start_date").after(new Timestamp(System.currentTimeMillis()))) {
					result = resultSet.getInt("task_id") + ";" +
							resultSet.getString("hotel_name") + ";" +
							resultSet.getString("address") + ";" +
							new SimpleDateFormat("dd-MM-yyyy HH:mm").format(resultSet.getTimestamp("start_date")) + ";" +
							new SimpleDateFormat("dd-MM-yyyy HH:mm").format(resultSet.getTimestamp("end_date")) + ";" +
							resultSet.getInt("amount_people_needed");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	String getHistoryTasks(int userId) {
		String result = "";
		try {
			resultSet = statement.executeQuery("Select * from tasks_data join records on records.task_id = tasks_data.task_id where records.user_id = " + userId);
			if (resultSet.next()) {
				if (resultSet.getTimestamp("start_date").before(new Timestamp(System.currentTimeMillis()))) {
					result = resultSet.getString("hotel_name") + ';'
							+ resultSet.getString("address") + ';'
							+ new SimpleDateFormat("dd-MM-yyyy HH:mm").format(resultSet.getTimestamp("start_date")) + ';'
							+ new SimpleDateFormat("dd-MM-yyyy HH:mm").format(resultSet.getTimestamp("end_date")) + ";" +
							resultSet.getInt("amount_people_needed") + "/";
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	String validLoginData(String username, String password) {
		try {
			resultSet =
					statement.executeQuery("Select * from users_data where username = \"" + username + "\" and password = \"" + password + "\"");
			if (resultSet.next()) {
				if (resultSet.getString("username").equals(username) && resultSet.getString("password").equals(password)) {
					return Integer.toString(resultSet.getInt("user_id")) + ' ' + resultSet.getInt("isAdmin");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
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

	boolean addNewTask(String hotel_name, String address, Timestamp start_date, Timestamp end_date, String amount_people_needed) {
		boolean result = false;
		try {
			callableStatement = connection.prepareCall("INSERT INTO tasks_data (hotel_name, address, start_date, end_date, amount_people_needed)" + "VALUES(?, ?, ?, ?, ?)");
			callableStatement.setString(1, hotel_name);
			callableStatement.setString(2, address);
			callableStatement.setTimestamp(3, start_date);
			callableStatement.setTimestamp(4, end_date);
			callableStatement.setInt(5, Integer.parseInt(amount_people_needed));
			int count = callableStatement.executeUpdate();
			result = (count > 0);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	boolean validateUsername(String username) {
		boolean result = true;
		try {
			resultSet = statement.executeQuery("Select * from users_data where username = \"" + username + "\"");
			if (!resultSet.next())
				result = false;
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

	boolean ifJoinYet(int userId, int taskId) {
		try {
			resultSet = statement.executeQuery("Select * from records where user_id = \"" + userId + "\" and task_id = \"" + taskId);
			if (resultSet.next()) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	String getFutureTasksUser(int userId) {
		String result = "";
		try {
			preparedStatement = connection.prepareStatement("Select * from tasks_data WHERE tasks_data.task_id NOT IN (SELECT task_id FROM records WHERE user_id = ?)");
			preparedStatement.setInt(1, userId);
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				if (resultSet.getInt("amount_people_needed") > 0) {
					result = resultSet.getInt("task_id") + ';' +
							resultSet.getString("hotel_name") + ';' +
							resultSet.getString("address") + ';' +
							dateFormat.format(resultSet.getTimestamp("start_date")) + ';' +
							dateFormat.format(resultSet.getTimestamp("end_date")) + ';' +
							resultSet.getInt("amount_people_needed") + "\n";
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	boolean deleteTaskAndRecords(int taskId) {
		try {
			preparedStatement = connection.prepareStatement("DELETE FROM tasks_data WHERE task_id = ?");
			preparedStatement.setInt(1, taskId);
			int count = preparedStatement.executeUpdate();

			if (count > 0) {
				preparedStatement = connection.prepareStatement("DELETE FROM records WHERE task_id = ?");
				preparedStatement.setInt(1, taskId);
				int count2 = preparedStatement.executeUpdate();
				return (count2 > 0);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	String getNoAdminUsers() {
		String result = "";
		try {
			resultSet = statement.executeQuery("Select * from users_data WHERE isAdmin = '0'");
			while (resultSet.next()) {
				if (resultSet.getInt("amount_people_needed") > 0) {
					result = resultSet.getInt("user_id") + ';' +
							resultSet.getString("username") + ';' +
							resultSet.getString("password") + ';' +
							resultSet.getString("name") + ';' +
							resultSet.getString("lastname") + ';' +
							resultSet.getString("pesel") + "\n";
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	boolean updateTask(int taskId, int toChange, String data) {
		boolean result = false;
		try {
			switch(toChange){
				case 1: {
					preparedStatement = connection.prepareStatement("UPDATE tasks_data SET hotel_name = ? WHERE task_id = ?");
					preparedStatement.setString(1, data);
					preparedStatement.setInt(2, taskId);
					break;
				}
				case 2: {
					preparedStatement = connection.prepareStatement("UPDATE tasks_data SET address = ? WHERE task_id = ?");
					preparedStatement.setString(1, data);
					preparedStatement.setInt(2, taskId);
					break;
				}
				case 3: {
					Timestamp start = new Timestamp(new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(data).getTime());
					preparedStatement = connection.prepareStatement("UPDATE tasks_data SET start_date = ? WHERE task_id = ?");
					preparedStatement.setTimestamp(1, start);
					preparedStatement.setInt(2, taskId);
					break;
				}
				case 4: {
					Timestamp end = new Timestamp(new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(data).getTime());
					preparedStatement = connection.prepareStatement("UPDATE tasks_data SET end_date = ? WHERE task_id = ?");
					preparedStatement.setTimestamp(1, end);
					preparedStatement.setInt(2, taskId);
					break;
				}
				case 5: {
					preparedStatement = connection.prepareStatement("UPDATE tasks_data SET amount_people_needed = ? WHERE task_id = ?");
					preparedStatement.setInt(1, Integer.parseInt(data));
					preparedStatement.setInt(2, taskId);
					break;
				}

			}
			int count = preparedStatement.executeUpdate();
			result = (count > 0);
		} catch (SQLException | ParseException e) {
			e.printStackTrace();
		}
		return result;
	}
}