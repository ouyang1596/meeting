package com.deshang365.meeting.model;

import java.io.Serializable;

public class NetworkReturnBase implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 19492698L;
	public int rescode;// 服务器返回码
	public String message = "获取详情失败！";

}
