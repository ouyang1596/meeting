package com.deshang365.meeting.model;

public class MeetingRecordsItem {
	private String createtime;
	private int id;
	private String name;

	public String getCreatetime() {
		return createtime;
	}

	public void setCreatetime(String createtime) {
		this.createtime = createtime;
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

	@Override
	public String toString() {
		return "Da [createtime=" + createtime + ", id=" + id + ", name=" + name + "]";
	}

}
