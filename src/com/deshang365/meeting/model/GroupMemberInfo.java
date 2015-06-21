package com.deshang365.meeting.model;

import java.io.Serializable;
import java.util.List;

public class GroupMemberInfo extends NetworkReturnBase implements Serializable {
	private static final long serialVersionUID = 8011583277232045457L;
	public String createtime = "";// 创建时间
	public int uid;// 用户ID
	public String valid;// 0有效，1无效
	public String name;// 群组名称
	public String showname;// 用户昵称
	public String group_id;// 群组ID
	public String user_count;// 用户数量
	/**
	 * 0是创建者，1是参与者
	 * */
	public int mtype;
	public String idcard;// 群组码
	public int state = -1;// 用户签到状态 0正常 1请假 2缺席 3补签4迟到
	public String avatar;// 头像
	public String signState = "-1";// 是否正在签到 0是正在签到，1签到已完成
	public String meetingid;// 单次签到的id
	public String hxgroupid = "";// 环信群组id
	public String hxid;// 环信id
	public String mobile;// 用户名
	public List<Integer> uids;
	public boolean endFlag = false;
	public String signcode = "";
	public int meeting_type;// 0口令签到 1蓝牙签到
	public int has_sign = -1;// 正在签到的群组会返回一个has_sign字段表示用户是否签过到 1:用户已签到 0:用户未签到
	public int absent_count;// 缺席次数
	public int allow_join;// 是否允许别人加入 0可以 1不可以
}
