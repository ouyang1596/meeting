package com.deshang365.meeting.network;

import java.net.URLEncoder;

import android.content.Context;

import com.deshang365.meeting.baselib.Encrypt;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.Constants;
import com.deshang365.meeting.model.VersionInfo;
import com.deshang365.util.Installation;

public class NewNetwork {

	private static NetworkService sNetworkService;

	public static boolean Init(Context context) {
		sNetworkService = RetrofitUtils.createApi(context, NetworkService.class);
		return true;
	}

	public static boolean login(String userName, String password, OnResponse<NetworkReturn> cb) {
		String param = String.format("{\"mobile\":\"%s\",\"password\":\"%s\"}", userName, password);
		try {
			String data = "p=" + URLEncoder.encode(Encrypt.encrypt(param, Constants.KEY_NETWORK_OUT));
			sNetworkService.login(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean register(String userName, String password, String hxid, OnResponse<NetworkReturn> cb) {
		String param = String.format("{\"mobile\":\"%s\",\"password\":\"%s\",\"mob_code\":\"%s\"}", userName, password, hxid);
		try {
			String data = "p=" + URLEncoder.encode(Encrypt.encrypt(param, Constants.KEY_NETWORK_OUT));
			sNetworkService.register(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean setNewPwd(String mobile, String password, OnResponse<NetworkReturn> cb) {
		String param = String.format("{\"mobile\":\"%s\",\"password\":\"%s\"}", mobile, password);
		try {
			String data = "p=" + URLEncoder.encode(Encrypt.encrypt(param, Constants.KEY_NETWORK_OUT));
			sNetworkService.setNewPwd(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean joinGroup(String nickname, String groupid, OnResponse<NetworkReturn> cb) {
		String param = String.format("{\"showname\":\"%s\",\"group_id\":\"%s\"}", nickname, groupid);
		try {
			String data = "p=" + URLEncoder.encode(Encrypt.encrypt(param, Constants.KEY_NETWORK_OUT));
			sNetworkService.joinGroup(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean createSign(String groupId, String signCode, double lat, double lng, int meeting_time, OnResponse<NetworkReturn> cb) {
		String param = String.format(
				"{\"groupname\":\"\",\"group_id\":\"%s\",\"answer\":\"%s\",\"lat\":\"%f\",\"lng\":\"%f\",\"meeting_time\":\"%d\"}",
				groupId, signCode, lat, lng, meeting_time);
		try {
			String data = "p=" + URLEncoder.encode(Encrypt.encrypt(param, Constants.KEY_NETWORK_OUT));
			sNetworkService.createSign(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean setEmail(String email, OnResponse<NetworkReturn> cb) {
		String param = String.format("{\"email\":\"%s\"}", email);
		try {
			String data = "p=" + URLEncoder.encode(Encrypt.encrypt(param, Constants.KEY_NETWORK_OUT));
			sNetworkService.setEmail(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean setPassword(String originalPwd, String newPwd, String ensureNewPwd, OnResponse<NetworkReturn> cb) {
		String param = String.format("{\"old_pwd\":\"%s\",\"new_pwd\":\"%s\",\"renew_pwd\":\"%s\"}", originalPwd, newPwd, ensureNewPwd);
		try {
			String data = "p=" + URLEncoder.encode(Encrypt.encrypt(param, Constants.KEY_NETWORK_OUT));
			sNetworkService.setPassword(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean setGroupName(String groupid, String uid, String groupname, OnResponse<NetworkReturn> cb) {
		String param = String.format("{\"groupid\":\"%s\",\"uid\":\"%s\",\"groupname\":\"%s\"}", groupid, uid, groupname);
		try {
			String data = "p=" + URLEncoder.encode(Encrypt.encrypt(param, Constants.KEY_NETWORK_OUT));
			sNetworkService.setGroupName(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean setShowName(String groupid, String uid, String showname, OnResponse<NetworkReturn> cb) {
		String param = String.format("{\"groupid\":\"%s\",\"uid\":\"%s\",\"showname\":\"%s\"}", groupid, uid, showname);
		try {
			String data = "p=" + URLEncoder.encode(Encrypt.encrypt(param, Constants.KEY_NETWORK_OUT));
			sNetworkService.setShowName(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean createBlueSign(String groupId, int meeting_time, OnResponse<NetworkReturn> cb) {
		String param = String.format("{\"group_id\":\"%s\",\"mobile_type\":\"%d\",\"meeting_time\":\"%d\"}", groupId, 0, meeting_time);// 0表示Android
		try {
			String data = "p=" + URLEncoder.encode(Encrypt.encrypt(param, Constants.KEY_NETWORK_OUT));
			sNetworkService.createBlueSign(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean uploadBlueToothInfo(String groupid, String mobilecode, String bluetooths, String myBluetoothData,
			OnResponse<NetworkReturn> cb) {
		String param = String.format("{\"group_id\":\"%s\",\"mobile_type\":\"%d\",\"mobile_code\":\"%s\",%s,\"my_bluetooth\":\"%s\"}",
				groupid, 0, mobilecode, bluetooths, myBluetoothData);
		try {
			String data = "p=" + URLEncoder.encode(Encrypt.encrypt(param, Constants.KEY_NETWORK_OUT));
			sNetworkService.uploadBlueToothInfo(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean createGroup(String groupName, String showName, String mobCode, OnResponse<NetworkReturn> cb) {
		String param = String.format("{\"groupname\":\"%s\",\"showname\":\"%s\",\"mob_code\":\"%s\"}", groupName, showName, mobCode);
		try {
			String data = "p=" + URLEncoder.encode(Encrypt.encrypt(param, Constants.KEY_NETWORK_OUT));
			sNetworkService.createGroup(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isCanJoinGroup(String group_id, int is_allow, String app_version, OnResponse<NetworkReturn> cb) {
		String param = String.format("{\"group_id\":\"%s\",\"is_allow\":%d,\"mobile_type\":%d,\"app_version\":\"%s\"}", group_id, is_allow,
				0, app_version);
		try {
			String data = "p=" + URLEncoder.encode(Encrypt.encrypt(param, Constants.KEY_NETWORK_OUT));
			sNetworkService.isCanJoinGroup(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean userSign(String groupid, String answer, double lat, double lng, String uuid, OnResponse<NetworkReturn> cb) {
		String data = String.format(
				"{\"groupname\":\"\",\"group_id\":\"%s\",\"answer\":\"%s\",\"lat\":\"%f\",\"lng\":\"%f\",\"mobile_code\":\"%s\"}", groupid,
				answer, lat, lng, uuid);
		try {
			data = "p=" + URLEncoder.encode(Encrypt.encrypt(data, Constants.KEY_NETWORK_OUT));
			sNetworkService.userSign(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean bluetoothJoinSign(String groupId, String mobileCode, OnResponse<NetworkReturn> cb) {
		String data = String.format("{\"group_id\":\"%s\",\"mobile_type\":\"%d\",\"mobile_code\":\"%s\"}", groupId, 0, mobileCode);
		try {
			data = "p=" + URLEncoder.encode(Encrypt.encrypt(data, Constants.KEY_NETWORK_OUT));
			sNetworkService.bluetoothJoinSign(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean setDefaultNickname(String nickname, OnResponse<NetworkReturn> cb) {
		String data = String.format("{\"username\":\"%s\"}", nickname);
		try {
			data = "p=" + URLEncoder.encode(Encrypt.encrypt(data, Constants.KEY_NETWORK_OUT));
			sNetworkService.setDefaultNickname(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean changeUserInfo(String nickname, String email, OnResponse<NetworkReturn> cb) {
		String data = String.format("{\"username\":\"%s\",\"email\":\"%s\"}", nickname, email);
		try {
			data = "p=" + URLEncoder.encode(Encrypt.encrypt(data, Constants.KEY_NETWORK_OUT));
			sNetworkService.changeUserInfo(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean groupExit(String groupid, OnResponse<NetworkReturn> cb) {
		String data = String.format("{\"group_id\":\"%s\"}", groupid);
		try {
			data = "p=" + URLEncoder.encode(Encrypt.encrypt(data, Constants.KEY_NETWORK_OUT));
			sNetworkService.groupExit(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean groupDismiss(String groupid, OnResponse<NetworkReturn> cb) {
		String data = String.format("{\"group_id\":\"%s\"}", groupid);
		try {
			data = "p=" + URLEncoder.encode(Encrypt.encrypt(data, Constants.KEY_NETWORK_OUT));
			sNetworkService.groupDismiss(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean stopSign(String groupid, String meetingid, OnResponse<NetworkReturn> cb) {
		String data = String.format("{\"group_id\":\"%s\",\"meeting_id\":\"%s\"}", groupid, meetingid);
		try {
			data = "p=" + URLEncoder.encode(Encrypt.encrypt(data, Constants.KEY_NETWORK_OUT));
			sNetworkService.stopSign(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean changeState(String datas, OnResponse<NetworkReturn> cb) {
		String data = datas;
		try {
			data = "p=" + URLEncoder.encode(Encrypt.encrypt(data, Constants.KEY_NETWORK_OUT));
			sNetworkService.changeState(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean uploadMessage(String mob_code, String message, OnResponse<NetworkReturn> cb) {
		String data = String.format("{\"mob_code\":\"%s\",\"message\":\"%s\"}", mob_code, message);
		try {
			data = "p=" + URLEncoder.encode(Encrypt.encrypt(data, Constants.KEY_NETWORK_OUT));
			sNetworkService.uploadMessage(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean deleteMember(String groupid, String uid, OnResponse<NetworkReturn> cb) {
		String data = String.format("{\"group_id\":\"%s\",\"menber_id\":\"%s\"}", groupid, uid);
		try {
			data = "p=" + URLEncoder.encode(Encrypt.encrypt(data, Constants.KEY_NETWORK_OUT));
			sNetworkService.deleteMember(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static void getUpdatedVersion(Context context, OnResponse<VersionInfo> cb) {
		sNetworkService.checkUpdate(MeetingApp.userInfo.uid, MeetingApp.username, "school", Installation.id(context), cb);
	}

	public static boolean getGroupSignRecord(String groupid, String appversion, String page, OnResponse<NetworkReturn> cb) {
		String data = String.format("{\"group_id\":\"%s\",\"mobile_type\":\"%s\",\"app_version\":\"%s\",\"page\":\"%s\"}", groupid, "0",
				appversion, page);
		try {
			data = URLEncoder.encode(Encrypt.encrypt(data, Constants.KEY_NETWORK_OUT));
			sNetworkService.getGroupSignRecord(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean getGroupList(OnResponse<NetworkReturn> cb) {
		sNetworkService.getGroupList(cb);
		return true;
	}

	public static boolean exportAllSignResult(String groupid, String email, OnResponse<NetworkReturn> cb) {
		String data = String.format("{\"group_id\":\"%s\",\"email\":\"%s\"}", groupid, email);
		try {
			data = URLEncoder.encode(Encrypt.encrypt(data, Constants.KEY_NETWORK_OUT));
			sNetworkService.exportAllSignResult(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean exportSingleResult(String meeting_id, String email, OnResponse<NetworkReturn> cb) {
		String data = String.format("{\"meeting_id\":\"%s\",\"email\":\"%s\"}", meeting_id, email);
		try {
			data = URLEncoder.encode(Encrypt.encrypt(data, Constants.KEY_NETWORK_OUT));
			sNetworkService.exportSingleResult(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean getAbsentResult(String groupid, String appVersion, String pageCount, OnResponse<NetworkReturn> cb) {
		String data = String.format("{\"group_id\":\"%s\",\"mobile_type\":\"%s\",\"app_version\":\"%s\",\"page\":\"%s\"}", groupid, "0",
				appVersion, pageCount);
		try {
			data = URLEncoder.encode(Encrypt.encrypt(data, Constants.KEY_NETWORK_OUT));
			sNetworkService.getAbsentResult(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean getPersonalSignRecord(String groupid, String appversion, String page, OnResponse<NetworkReturn> cb) {
		String data = String.format("{\"group_id\":\"%s\",\"mobile_type\":\"%s\",\"app_version\":\"%s\",\"page\":\"%s\"}", groupid, "0",
				appversion, page);
		try {
			data = URLEncoder.encode(Encrypt.encrypt(data, Constants.KEY_NETWORK_OUT));
			sNetworkService.getPersonalSignRecord(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean getNearSignGroups(double lat, double lng, OnResponse<NetworkReturn> cb) {
		String data = String.format("{\"lat\":\"%f\",\"lng\":%f}", lat, lng);
		try {
			data = URLEncoder.encode(Encrypt.encrypt(data, Constants.KEY_NETWORK_OUT));
			sNetworkService.getNearSignGroups(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isRegister(String mobile, OnResponse<NetworkReturn> cb) {
		String data = String.format("{\"mobile\":\"%s\"}", mobile);
		try {
			data = URLEncoder.encode(Encrypt.encrypt(data, Constants.KEY_NETWORK_OUT));
			sNetworkService.isRegister(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean sendToSMS(String mobile, OnResponse<NetworkReturn> cb) {
		String data = String.format("{\"mobile\":\"%s\"}", mobile);
		try {
			data = URLEncoder.encode(Encrypt.encrypt(data, Constants.KEY_NETWORK_OUT));
			sNetworkService.sendToSMS(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean getMembersByHxGroupId(String hxGroupId, OnResponse<NetworkReturn> cb) {
		String data = String.format("{\"mob_code\":%s}", hxGroupId);
		try {
			data = URLEncoder.encode(Encrypt.encrypt(data, Constants.KEY_NETWORK_OUT));
			sNetworkService.getMembersByHxGroupId(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean getGroupInfo(String idcard, OnResponse<NetworkReturn> cb) {
		String data = String.format("{\"idcard\":\"%s\"}", idcard);
		try {
			data = URLEncoder.encode(Encrypt.encrypt(data, Constants.KEY_NETWORK_OUT));
			sNetworkService.getGroupInfo(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean getGroupMember(String groupid, OnResponse<NetworkReturn> cb) {
		String data = String.format("{\"group_id\":%s}", groupid);
		try {
			data = URLEncoder.encode(Encrypt.encrypt(data, Constants.KEY_NETWORK_OUT));
			sNetworkService.getGroupMember(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean getSignList(String groupid, String meetingid, OnResponse<NetworkReturn> cb) {
		String data = String.format("{\"group_id\":\"%s\",\"meeting_id\":%s}", groupid, meetingid);
		try {
			data = URLEncoder.encode(Encrypt.encrypt(data, Constants.KEY_NETWORK_OUT));
			sNetworkService.getSignList(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isSigning(String groupid, String mobile_type, String app_version, OnResponse<NetworkReturn> cb) {
		String data = String.format("{\"group_id\":\"%s\",\"mobile_type\":\"%s\",\"app_version\":\"%s\"}", groupid, mobile_type,
				app_version);
		try {
			data = URLEncoder.encode(Encrypt.encrypt(data, Constants.KEY_NETWORK_OUT));
			sNetworkService.isSigning(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean uidInGroup(String groupid, OnResponse<NetworkReturn> cb) {
		String data = String.format("{\"groupid\":\"%s\"}", groupid);
		try {
			data = URLEncoder.encode(Encrypt.encrypt(data, Constants.KEY_NETWORK_OUT));
			sNetworkService.uidInGroup(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean getGroupInfoByHxGroupId(String hxGroupId, OnResponse<NetworkReturn> cb) {
		String data = String.format("{\"mob_code\":%s}", hxGroupId);
		try {
			data = URLEncoder.encode(Encrypt.encrypt(data, Constants.KEY_NETWORK_OUT));
			sNetworkService.getGroupInfoByHxGroupId(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean myRecUnSigned(String app_version, int page, OnResponse<NetworkReturn> cb) {
		String data = String.format("{\"rec_type\":%d,\"mobile_type\":%d,\"app_version\":\"%s\",\"page\":%d}", 0, 0, app_version, page);
		try {
			data = URLEncoder.encode(Encrypt.encrypt(data, Constants.KEY_NETWORK_OUT));
			sNetworkService.myRecUnSigned(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean myRecSigned(String app_version, int page, OnResponse<NetworkReturn> cb) {
		String data = String.format("{\"rec_type\":%d,\"mobile_type\":%d,\"app_version\":\"%s\",\"page\":%d}", 1, 0, app_version, page);
		try {
			data = URLEncoder.encode(Encrypt.encrypt(data, Constants.KEY_NETWORK_OUT));
			sNetworkService.myRecSigned(data, cb);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static String getAvatarUrl(int uid) {
		String uidStr = lpad(9, Integer.valueOf(uid));
		return Config.IMAGE_HOST + "/avatar/" + uidStr.substring(0, 3) + "/" + uidStr.substring(3, 5) + "/" + uidStr.substring(5, 7) + "/"
				+ uidStr.substring(7, 9) + ".jpg";
	}

	private static String lpad(int length, int number) {
		String f = "%0" + length + "d";
		return String.format(f, number);
	}
}
