package com.deshang365.meeting.network;

public class Config {
	public static final boolean DEBUG = true;
	public static final String IMAGE_HOST = "http://img.wlyeah.com";
	// public static final String HOST = "http://meeting.deshang365.com";
	// public static final String HOST = "http://api2.wlyeah.com";
	public static final String HOST = "http://10.13.1.3:8703";
	// public static final String HOST = "http://10.13.1.3:3333";
	public static final int HTTP_CONNECT_TIMEOUT = 10 * 1000;
	public static final int HTTP_READ_TIMEOUT = 10 * 1000;
	public static final int RESPONSE_CACHE_SIZE = 1 * 1024 * 1024;
	public static final String RESPONSE_CACHE = "http_cache";
	static final String API_LOGIN = "/api/user_des2/login";
	static final String API_CREATEGROUP = "/api/group_des2/groupadd";
	static final String API_JOINGROUP = "/api/group_des2/groupuseradd";
	// public static final String API_GROUPLIST = "/api/group_des2/grouplist";
	static final String API_GROUPLIST = "/api/group_des2/mix_grouplist";
	static final String API_GROUPINFO = "/api/group_des2/groupinfo_byidcard";
	// static final String API_ABSENTRESULT =
	// "/api/meeting_des2/absent_results";
	static final String API_ABSENTRESULT = "/api/meeting_des2/paging_absent_results_v1";
	// public static final String API_SIGNLIST =
	// "/api/meeting_des2/meetinglist";
	public static final String API_SIGNLIST = "/api/meeting_des2/meetinglist_v1";
	static final String API_GROUPMEMBERS = "/api/group_des2/groupmemberlist";
	public static final String API_GROUPINFOBYMOB = "/api/group_des2/groupinfo_bymobcode";
	public static final String API_GROUPINFOBYID = "/api/group_des2/groupinfo_byid?p={\"id\":%s}";
	public static final String API_GETNEARSIGNGROUPS = "/api/group_des2/near_groups";
	public static final String API_GETGROUPMEMBERSBYHXID = "/api/group_des2/groupdetail_bymobcode";
	public static final String API_UPLOADIMAGE = "/api/avatar_des2/avatar/";
	public static final String API_SETPASSWORD = "/api/user_des2/change_pwd";
	public static final String API_REGISTER = "/api/user_des2/register";
	public static final String API_GROUPEXIT = "/api/group_des2/groupexit";
	public static final String API_GROUPDISMISS = "/api/group_des2/groupdismiss";
	public static final String API_PERSONALSIGNRECORD = "/api/meeting_des2/historymeetinglist_personal?p={\"group_id\":%s}";
	public static final String API_GROUPSIGNRECORD = "/api/meeting_des2/historymeetinglist_group?p={\"group_id\":%s}";
	// public static final String API_CREATE_SIGN =
	// "/api/meeting_des2/meetingadd";
	public static final String API_CREATE_SIGN = "/api/meeting_des2/meetingadd_v1";
	public static final String API_STOPSIGN = "/api/meeting_des2/end_meeting";
	public static final String API_CHANGESIGNSTATE = "/api/meeting_des2/change_state_byuid";
	public static final String API_SINGLERESULT = "/api/meeting_des2/export_single_meeting_results";
	public static final String API_ALLSIGNRESULT = "/api/meeting_des2/export_multiple_meeting_results";
	public static final String API_GETUSERNAMEBYHXID = "/api/group_des2/ginfo_bymobcode?p={\"mob_code\":\"%s\"}";
	public static final String API_SETNEWPWD = "/api/user_des2/fchange_pwd";
	public static final String API_SETLATLNG = "/api/meeting_des2/meetingadd";
	public static final String API_USERSIGN = "/api/meeting_des2/sign";
	public static final String API_LOGOUT = "/api/user_des2/logout";
	public static final String API_USERINFO = "/api/user_des2/getstudentinfo";
	// public static final String API_UPDATE =
	// "http://resource.deshang365.com/ugc/meeting_update.json?userId=%s&userName=%s&schoolId=%s&deviceId=%s";
	public static final String API_UPDATE = "/api/user_des2/check_update";
	public static final String API_UPLOADDATA = "/api/group_des2/bak_msg";
	public static final String API_DELETE = "/api/group_des2/memberdelete";
	public static final String API_UIDINGROUP = "/api/meeting_des2/uidingroup";
	public static final String API_UPDATESHOWNAME = "/api/meeting_des2/updateshowname";
	public static final String API_UPDATEGROUPNAME = "/api/meeting_des2/updategroupname";
	public static final String API_GETIMAGE = "/api/avatar_des2/avatar/";
	public static final String API_SETEMAIL = "/api/user_des2/set_email";
	public static final String API_SETDEFAULTNICKNAME = "/api/user_des2/ChangeUserName";
	public static final String API_CHANGESTATES = "/api/meeting_des2/change_status";
	public static final String API_ISREGISTER = "/api/user_des2/user_exists";
	public static final String API_SENDTOSMS = "/api/user_des2/send_xsms";
	// public static final String API_STARTBLUETOOTHSIGN =
	// "/api/meeting_des2/bluetooth_meetingadd";
	public static final String API_STARTBLUETOOTHSIGN = "/api/meeting_des2/bluetooth_meetingadd_v1";
	public static final String API_UPLOADBLUETOOTHINFO = "/api/meeting_des2/upload_bluetooth_info2";
	public static final String API_GETBLUETOOTHINFO = "/api/meeting_des2/bluetooth_info?p={\"group_id\":\"%s\"}";
	public static final String API_ISBLUETOOTHSIGNED = "/api/meeting_des2/bluetooth_issign?p={\"group_id\":\"%s\"}";
	public static final String API_BLUETOOTHJOINSIGN = "/api/meeting_des2/bluetooth_sign";
	public static final String API_BLUETOOTHSTOPSIGN = "/api/meeting_des2/bluetooth_end_meeting";
	public static final String API_UPLOADMYSELFBLUETOOTHDATA = "/api/meeting_des2/my_bluetoothinfo";
	public static final String API_PERSONALPAGETOPERSONALMEETINGLIST = "/api/meeting_des2/paging_historymeetinglist_personal_v1";
	public static final String API_GROUPPAGETOPERSONALMEETINGLIST = "/api/meeting_des2/paging_historymeetinglist_group_v1";
	public static final String API_ISSIGNING = "/api/meeting_des2/issign_v1";
	public static final String API_CHANGE_USERINFO = "/api/user_des2/change_userinfo";
	public static final String API_MY_SIGNRECORD = "/api/meeting_des2/my_signrec";
	public static final String API_IS_CAN_JOIN_GROUP = "/api/group_des2/set_groupentrance";

}
