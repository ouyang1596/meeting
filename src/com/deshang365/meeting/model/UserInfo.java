package com.deshang365.meeting.model;

import java.io.Serializable;

public class UserInfo extends NetworkReturnBase implements Serializable {
	private static final long serialVersionUID = -7299039578328859427L;
	public int uid;
	public String name; // 昵称
	public String email; // 邮箱
	public int rolelevel; // 帐号级别
	public String avatar; // 头像
	public String schoolId; // 学校
	public String major; // 班级
	public String department; // 院系
	public String mcode; // 学号
	public String slogan; // 个性签名
	public String regtime; // 注册时间
	public String cidcard; // 身份证
	public String cname; // 真实姓名
	public String gender; // 性别
	public String mobile; // 电话
	public String hxid;// 环信ID
	public String auth;// 短信验证码

}
