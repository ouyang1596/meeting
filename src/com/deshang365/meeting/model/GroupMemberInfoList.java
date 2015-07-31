package com.deshang365.meeting.model;

import java.io.Serializable;
import java.util.List;

public class GroupMemberInfoList extends NetworkReturnBase implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 10907089L;
	public List<GroupMemberInfo> mGroupMemberInfosList;
	public List<List<GroupMemberInfo>> mGroupMemberSignInfosList;// list第一个是签到，第二个未签到人员
	public GroupMemberInfo mGroupMemberInfo;
	public int m_state = -1;// 最近发起的签到用户是否已签到 0未签 1已签
	public int meeting_count;
}
