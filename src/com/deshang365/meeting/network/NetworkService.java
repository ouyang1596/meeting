package com.deshang365.meeting.network;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

import com.deshang365.meeting.model.VersionInfo;

public interface NetworkService {

	@POST(Config.API_LOGIN)
	public void login(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_REGISTER)
	public void register(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_CREATE_GROUP)
	public void createGroup(@Body String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_GROUPLIST)
	public void getGroupList(OnResponse<NetworkReturn> cb);

	@GET(Config.API_UPDATE)
	public void checkUpdate(@Query("userId") int userId, @Query("userName") String userName, @Query("schoolId") String schoolId,
			@Query("deviceId") String deviceId, OnResponse<VersionInfo> cb);

	@GET(Config.API_GROUP_PAGE_TO_PERSONAL_MEETINGLIST)
	public void getGroupSignRecord(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_USER_SIGN)
	public void userSign(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_BLUETOOTH_JOIN_SIGN)
	public void bluetoothJoinSign(@Body String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_PERSONAL_PAGE_TO_PERSONAL_MEETINGLIST)
	public void getPersonalSignRecord(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_START_BLUETOOTH_SIGN)
	public void createBlueSign(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_CREATE_SIGN)
	public void createSign(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_UPLOAD_BLUETOOTH_INFO)
	public void uploadBlueToothInfo(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_SET_DEFAULT_NICKNAME)
	public void setDefaultNickname(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_SET_EMAIL)
	public void setEmail(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_FEED_BACK)
	public void feedBack(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_IS_CAN_JOIN_GROUP)
	public void isCanJoinGroup(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_SET_PASSWORD)
	public void setPassword(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_UPDATE_GROUPNAME)
	public void setGroupName(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_UPDATE_SHOWNAME)
	public void setShowName(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_GROUP_EXIT)
	public void groupExit(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_GROUP_DISMISS)
	public void groupDismiss(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_JOIN_GROUP)
	public void joinGroup(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_STOP_SIGN)
	public void stopSign(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_DELETE)
	public void deleteMember(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_SET_NEWPWD)
	public void setNewPwd(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_UPLOAD_DATA)
	public void uploadMessage(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_CHANGE_USERINFO)
	public void changeUserInfo(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_CHANGE_STATES)
	public void changeState(@Body String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_ALL_SIGN_RESULT)
	public void exportAllSignResult(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_GROUPMEMBERS)
	public void getGroupMember(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_GROUPINFO)
	public void getGroupInfo(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_SIGNLIST)
	public void getSignList(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_GROUPINFO_BY_MOB)
	public void getGroupInfoByHxGroupId(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_ABSENT_RESULT)
	public void getAbsentResult(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_IS_SIGNING)
	public void isSigning(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_UID_IN_GROUP)
	public void uidInGroup(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_GET_NEAR_SIGNGROUPS)
	public void getNearSignGroups(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_IS_REGISTER)
	public void isRegister(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_GET_GROUPMEMBERS_BY_HXID)
	public void getMembersByHxGroupId(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_SINGLE_RESULT)
	public void exportSingleResult(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_SEND_TO_SMS)
	public void sendToSMS(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_MY_SIGNRECORD)
	public void myRecUnSigned(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_MY_SIGNRECORD)
	public void myRecSigned(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_MEETING_COUNT_DETAIL)
	public void getMeetingCountDetail(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_ABSENT_DETAILS)
	public void getAbsentDetail(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_EXPORT_MEETINGREC_BYTIME)
	public void exportMeetingRecBytime(@Query("p") String data, OnResponse<NetworkReturn> cb);
}
