package com.epam.rd.java.basic.task7.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringJoiner;

import com.epam.rd.java.basic.task7.db.entity.*;


public class DBManager  {

	private static final String DB_URL = "connection.url=jdbc:postgresql://localhost:1808/test2db?user=postgres&password=ruslan2002";
	//private static final String DB_URL = "app.properties";
	private Connection connection;




	private DBManager() {
		try (InputStream inputStream = new FileInputStream("app.properties"))
		{
			Properties properties = new Properties();
			properties.load(inputStream);
			connection = DriverManager.getConnection(properties.getProperty("connection.url"));
		} catch (SQLException | IOException e) {
			e.printStackTrace();
			System.err.println("Cannot connect to BD!");
		}
	}

	public static synchronized DBManager getInstance() {
		return new DBManager();
	}




	public List<User> findAllUsers() throws DBException {
		List<User> users = new ArrayList<>();
		try (ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM users"))
		{
			while (rs.next())
			{
				int id = rs.getInt("id");
				String login = rs.getString("login");
				//user.setId(rs.getInt("id"));
				//User user = new User(id, login);
				users.add(new User(id, login));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DBException("findAllUsers(): failed to get list of users", e);
		}
		return users;
	}

	public boolean insertUser(User user) throws DBException {
		if (user == null)
		{
			return false;
		}

		try (Statement st = connection.createStatement();)
		{
			if (1 == st.executeUpdate("INSERT INTO users (login) VALUES ('" + user.getLogin() + "')", Statement.RETURN_GENERATED_KEYS))
			{
				try (ResultSet rs = st.getGeneratedKeys()){
					rs.next();
					user.setId(rs.getInt(1));
					return true;
				}
			}


		} catch (SQLException e) {
			e.printStackTrace();
			throw new DBException("insertUser() failed", e);
		}
		return false;
	}

	public boolean deleteUsers(User... users) throws DBException {
		if (users.length == 0)
		{
			return false;
		}

		StringJoiner sj = new StringJoiner(",", "DELETE FROM users WHERE id IN (", ")");
		for (User u : users)
		{
			if (u != null)
			{
				sj.add(String.valueOf(u.getId()));
			}
		}

		try (Statement statement = connection.createStatement()){
			return statement.executeUpdate(sj.toString()) > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DBException("deleteUsers() failed", e);
		}
		//return false;
	}

	public User getUser(String login) throws DBException {
		if (login == null)
		{
			return null;
		}
		try (ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM users WHERE login = '" + login + "'" )){
				if (rs.next())
				{
					User user = User.createUser(rs.getString("login"));
					if (user != null) {
						user.setId(rs.getInt("id"));
					}
					return user;
				}
				return null;
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DBException("getUser() failed", e);
		}

	}

	public Team getTeam(String name) throws DBException {
		if (name == null)
		{
			return null;
		}
		try (ResultSet rs = connection.createStatement().executeQuery("SELECT id, name FROM teams WHERE name = '" + name + "'" )){
			if (rs.next())
			{
				Team team = Team.createTeam(rs.getString("name"));
				if (team != null) {
					team.setId(rs.getInt("id"));
				}
				return team;
			}
			return null;

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DBException("getTeam() failed", e);
		}

	}

	public List<Team> findAllTeams() throws DBException {
		ArrayList<Team> teams = new ArrayList<>();

		try (ResultSet rs = connection.createStatement().executeQuery("SELECT id, name FROM teams" )){
			while (rs.next())
			{
				Team team = Team.createTeam(rs.getString("name"));
				team.setId(rs.getInt("id"));
				teams.add(team);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DBException("findAllTeam() failed", e);
		}
		return teams;

	}

	public boolean insertTeam(Team team) throws DBException {
		if (team == null)
		{
			return false;
		}
		try (Statement st = connection.createStatement()){
				if (1 == st.executeUpdate("INSERT INTO teams (name) VALUES ('" + team.getName() +  "')", Statement.RETURN_GENERATED_KEYS)){
					try(ResultSet rs = st.getGeneratedKeys()) {
						rs.next();
						team.setId(rs.getInt(1));
						return true;
					}
				}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DBException("getTeam() failed", e);
		}
		return false;

	}

	public boolean setTeamsForUser(User user, Team... teams) throws DBException {
		if (user == null || teams == null)
		{
			return false;
		}
		//PreparedStatement st = null;
		try (PreparedStatement st = connection.prepareStatement("INSERT INTO users_teams (user_id, team_id) VALUES (" + user.getId() + ",?)");
			)
		{
			connection.setAutoCommit(false);
			//st = connection.prepareStatement("INSERT INTO users_teams (user_id, team_id) VALUES (" + user.getId() + ",?)");
			boolean isInserted = false;

			for (Team t : teams)
			{
				if (t!= null)
				{
					st.setInt(1, t.getId());
					isInserted = (st.executeUpdate() > 0) || isInserted;
				}
			}
			connection.commit();
			connection.setAutoCommit(true);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			tryRollBack(connection);
			throw new DBException("setTeamsForUser() failed", e);
			//return false;
		}
//		finally {
//			tryClose(co);
//		}


	}

	private void tryRollBack(Connection con)
	{
		if (con != null)
		{
			try {
				con.rollback();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
	}

	private void tryClose(AutoCloseable con)
	{
		if (con!=null)
		{
			try {
				con.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public List<Team> getUserTeams(User user) throws DBException {
		List<Team> teams = new ArrayList<>();

		try (ResultSet rs = connection.createStatement().executeQuery("SELECT team_id, name FROM users_teams JOIN teams ON team_id = id WHERE user_id = " + user.getId())){

			while (rs.next())
			{
				Team team = Team.createTeam(rs.getString("name"));
				team.setId(rs.getInt("team_id"));
				teams.add(team);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DBException("getUserTeams() failed", e);
		}
		return teams;

	}

	public boolean deleteTeam(Team team) throws DBException {
		if (team == null)
		{
			return false;
		}
		try (Statement st = connection.createStatement()){
			return st.executeUpdate("DELETE FROM teams WHERE name = '" + team.getName() + "'") > 0;

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DBException("deleteTeam() failed", e);
		}
	}

	public boolean updateTeam(Team team) throws DBException {
		if (team == null)
		{
			return false;
		}
		try (Statement st = connection.createStatement()){
			return st.executeUpdate("UPDATE teams SET name = '" + team.getName() + "' WHERE id = " + team.getId()) > 0;

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DBException("deleteTeam() failed", e);
		}
	}

}
