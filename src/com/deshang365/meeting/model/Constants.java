package com.deshang365.meeting.model;

import java.io.File;

import com.deshang365.util.FileUtils;

public class Constants {
	public static final String OLD_VERSION = "old_version";
	public static final String KEY_VIDEO_URL = "vedio_url";
	public static final String KEY_REMEMBER_PWD = "remember_pwd";
	public static final String KEY_REMEMBER_NAME = "remember_name";
	public static final String KEY_LOGGEDIN = "logged_in";
	public static final String KEY_3G_REMIND = "3g_remind";
	public static final String KEY_IS_PUSH = "is_push";
	public static final String KEY_WEB_URL = "web_url";
	public static final String KEY_SIGN_MODE = "sign_mode";// 签到模式 0口令签到 1蓝牙签到
	public static final String KEY_PARAMS = "params";// 储存一些参数信息的文件名

	public static final String DOWNLOAD_PATH = FileUtils.getExternalSdCardPath() + "/meeting/downloads";
	public static final String PICTURES_ROOT_PATH = FileUtils.getExternalSdCardPath() + "/meeting/pictures";

	public static final String AVATAR_PATH = PICTURES_ROOT_PATH + "/avatar/";
	public static final String COURSE_COVER_PATH = PICTURES_ROOT_PATH + "/course";
	public static final String FORUM_PIC_PATH = PICTURES_ROOT_PATH + "/forum";
	public static final String POST_PHOTO_PATH = PICTURES_ROOT_PATH + "/post";
	public static final String TOPIC_PATH = PICTURES_ROOT_PATH + "/topic";
	public static final String QUESTION_PHOTO_PATH = PICTURES_ROOT_PATH + "/question";
	public static final String LECTURE_PHOTO_PATH = PICTURES_ROOT_PATH + "/lecture";
	public static final String GAME_PHOTO_PATH = PICTURES_ROOT_PATH + "/game";
	public static final String ICON_PATH = PICTURES_ROOT_PATH + "/icon/";
	public static final String BUILDING_PATH = PICTURES_ROOT_PATH + "/building";
	public static final String TAKEPHOTO_PATH = PICTURES_ROOT_PATH + "/takephotos";
	public static final String QRCODE_PATH = PICTURES_ROOT_PATH + "/qrcode/";
	public static final String SCALE_IMAGE = PICTURES_ROOT_PATH + "/scale";
	public static final String KEY_HXPWD = "d$e%s^^h^ang";
	public static final String KEY_GROUP = "dD%esEha$%n^g";
	public static final String KEY_MESSAGE = "des&h%a*n^g";
	public static final String KEY_NICKNAME = "d]e$sh%an^g[";
	public static final String KEY_NETWORK_OUT = "d]e$123sh$1123%an^g[";
	public static final String KEY_NETWORK_IN = "d!#@]e$VSshW%an^g[";
	public static final String KEY_BLUETOOTH = "*$%@dj&*j!@oz*^&no*";
	public static final String NET_WRONG = "网络错误";
	public static final String KEY_TIP_SHOW = "tip";

	static {
		File scaleFile = new File(Constants.SCALE_IMAGE);
		if (!scaleFile.exists()) {
			scaleFile.mkdirs();
		}
	}
}
