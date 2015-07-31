package com.deshang365.meeting.model;

import java.util.List;

public class AbsentDetail {
	private List<AbsentDetailItem> absent_list;

	public List<AbsentDetailItem> getAbsent_list() {
		return absent_list;
	}

	public void setAbsent_list(List<AbsentDetailItem> absent_list) {
		this.absent_list = absent_list;
	}

	public class AbsentDetailItem {
		private String meeting_date;
		private String meeting_time;
		private int state;

		public String getMeeting_date() {
			return meeting_date;
		}

		public void setMeeting_date(String meeting_date) {
			this.meeting_date = meeting_date;
		}

		public String getMeeting_time() {
			return meeting_time;
		}

		public void setMeeting_time(String meeting_time) {
			this.meeting_time = meeting_time;
		}

		public int getState() {
			return state;
		}

		public void setState(int state) {
			this.state = state;
		}

	}
}
