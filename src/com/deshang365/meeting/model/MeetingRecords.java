package com.deshang365.meeting.model;

import java.util.List;

public class MeetingRecords {
	private List<MeetingRecordsItem> meeting_records;

	public List<MeetingRecordsItem> getMeeting_records() {
		return meeting_records;
	}

	public void setMeeting_records(List<MeetingRecordsItem> meeting_records) {
		this.meeting_records = meeting_records;
	}

	@Override
	public String toString() {
		return "MeetingRecords [meeting_records=" + meeting_records + "]";
	}

}
