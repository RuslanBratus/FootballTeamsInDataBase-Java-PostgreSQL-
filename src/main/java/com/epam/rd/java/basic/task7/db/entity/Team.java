package com.epam.rd.java.basic.task7.db.entity;

import java.util.Objects;

public class Team {

	private int id;

	private String name;

	public Team() {}

	public Team(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static Team createTeam(String name) {
		return new Team(0, name);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Team team = (Team) o;
		return name.equals(team.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
