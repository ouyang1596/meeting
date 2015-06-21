package com.deshang365.meeting.network;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

import com.deshang365.meeting.model.VersionInfo;

public interface NetworkService {

	@POST(Config.API_LOGIN)
	public void login(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_REGISTER)
	public void register(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_CREATEGROUP)
	public void createGroup(@Body String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_GROUPLIST)
	public void getGroupList(OnResponse<NetworkReturn> cb);

	@GET(Config.API_UPDATE)
	public void checkUpdate(@Query("userId") int userId, @Query("userName") String userName, @Query("schoolId") String schoolId,
			@Query("deviceId") String deviceId, OnResponse<VersionInfo> cb);

	@GET(Config.API_GROUPPAGETOPERSONALMEETINGLIST)
	public void getGroupSignRecord(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_USERSIGN)
	public void userSign(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_BLUETOOTHJOINSIGN)
	public void bluetoothJoinSign(@Body String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_PERSONALPAGETOPERSONALMEETINGLIST)
	public void getPersonalSignRecord(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_STARTBLUETOOTHSIGN)
	public void createBlueSign(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_CREATE_SIGN)
	public void createSign(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_UPLOADBLUETOOTHINFO)
	public void uploadBlueToothInfo(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_SETDEFAULTNICKNAME)
	public void setDefaultNickname(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_SETEMAIL)
	public void setEmail(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_IS_CAN_JOIN_GROUP)
	public void isCanJoinGroup(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_SETPASSWORD)
	public void setPassword(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_UPDATEGROUPNAME)
	public void setGroupName(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_UPDATESHOWNAME)
	public void setShowName(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_GROUPEXIT)
	public void groupExit(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_GROUPDISMISS)
	public void groupDismiss(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_JOINGROUP)
	public void joinGroup(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_STOPSIGN)
	public void stopSign(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_DELETE)
	public void deleteMember(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_SETNEWPWD)
	public void setNewPwd(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_UPLOADDATA)
	public void uploadMessage(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_CHANGE_USERINFO)
	public void changeUserInfo(@Body String data, OnResponse<NetworkReturn> cb);

	@POST(Config.API_CHANGESTATES)
	public void changeState(@Body String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_ALLSIGNRESULT)
	public void exportAllSignResult(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_GROUPMEMBERS)
	public void getGroupMember(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_GROUPINFO)
	public void getGroupInfo(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_SIGNLIST)
	public void getSignList(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_GROUPINFOBYMOB)
	public void getGroupInfoByHxGroupId(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_ABSENTRESULT)
	public void getAbsentResult(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_ISSIGNING)
	public void isSigning(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_UIDINGROUP)
	public void uidInGroup(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_GETNEARSIGNGROUPS)
	public void getNearSignGroups(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_ISREGISTER)
	public void isRegister(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_GETGROUPMEMBERSBYHXID)
	public void getMembersByHxGroupId(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_SINGLERESULT)
	public void exportSingleResult(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_SENDTOSMS)
	public void sendToSMS(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_MY_SIGNRECORD)
	public void myRecUnSigned(@Query("p") String data, OnResponse<NetworkReturn> cb);

	@GET(Config.API_MY_SIGNRECORD)
	public void myRecSigned(@Query("p") String data, OnResponse<NetworkReturn> cb);

}
