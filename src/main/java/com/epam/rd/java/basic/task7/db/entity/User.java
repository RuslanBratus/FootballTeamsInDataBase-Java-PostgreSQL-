package com.epam.rd.java.basic.task7.db.entity;

public class User {

	public User() {}

	public User(int id, String login) {
		this.id = id;
		this.login = login;
	}

	private int id;

	private String login;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public static User createUser(String login) {
		return new User(0, login);
	}

	@Override
	public String toString() {
		return "User{" +
				"login='" + login + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		User user = (User) o;
		return id == user.id;
	}

	@Override
	public int hashCode() {
		return 0;
	}
}
