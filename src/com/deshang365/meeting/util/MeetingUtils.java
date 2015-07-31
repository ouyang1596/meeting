package com.deshang365.meeting.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.CollationKey;
import java.text.Collator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore.Images.ImageColumns;

import com.deshang365.meeting.baselib.Encrypt;
import com.deshang365.meeting.baselib.MeetingApp;
import com.deshang365.meeting.model.Constants;
import com.deshang365.meeting.model.GroupMemberInfo;
import com.deshang365.util.Installation;
import com.deshang365.util.MD5Util;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;

public class MeetingUtils {
	/** 获取当前时间 */
	public static String getCurTime(String timeFormat) {
		SimpleDateFormat format = new SimpleDateFormat(timeFormat);
		String curTime = format.format(new Date());
		return curTime;
	}

	public static long timeCastToMillionSecond(String time, String timeFormat) {
		SimpleDateFormat format = new SimpleDateFormat(timeFormat);
		try {
			return format.parse(time).getTime();
		} catch (ParseException e) {
			return 0;
		}
	}

	/**
	 * 按照时间进行排序
	 * */
	public static void sortList(List<GroupMemberInfo> groupMemberInfoList) {
		Collections.sort(groupMemberInfoList, new Comparator<GroupMemberInfo>() {
			@Override
			public int compare(GroupMemberInfo con1, GroupMemberInfo con2) {
				EMMessage lastMessage1 = EMChatManager.getInstance().getConversation(con1.hxgroupid).getLastMessage();
				EMMessage lastMessage2 = EMChatManager.getInstance().getConversation(con2.hxgroupid).getLastMessage();
				// if (lastMessage1 == null || lastMessage2 == null) {
				// return 1;
				// }
				if (lastMessage1 == null && lastMessage2 != null) {
					return 1;
				}
				if (lastMessage1 != null && lastMessage2 == null) {
					return -1;
				}
				if (lastMessage1 == null && lastMessage2 == null) {
					return 0;
				}
				long con1Time = lastMessage1.getMsgTime();
				long con2Time = lastMessage2.getMsgTime();
				if (con1Time == con2Time) {
					return 0;
				} else if (con1Time > con2Time) {
					return -1;
				} else {
					return 1;
				}
			}

		});
	}

	private static Collator collator = Collator.getInstance();

	/**
	 * 只能对汉字进行排序
	 * */
	public static void sortChineseList(List<GroupMemberInfo> groupMemberInfoList) {
		Collections.sort(groupMemberInfoList, new Comparator<GroupMemberInfo>() {

			@Override
			public int compare(GroupMemberInfo object1, GroupMemberInfo object2) {
				// 把字符串转换为一系列比特，它们可以以比特形式与 CollationKeys 相比较
				CollationKey key1 = collator.getCollationKey(object1.showname.toString());// 要想不区分大小写进行比较用o1.toString().toLowerCase()
				CollationKey key2 = collator.getCollationKey(object2.showname.toString());

				return key1.compareTo(key2);// 返回的分别为1,0,-1
											// 分别代表大于，等于，小于。要想按照字母降序排序的话
											// 加个“-”号
			}
		});
	}

	/** 解析uri获取图片绝对路径 */
	public static String getRealFilePath(Context context, Uri uri) {
		if (null == uri)
			return null;
		final String scheme = uri.getScheme();
		String data = null;
		if (scheme == null) {
			data = uri.getPath();
		} else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
			data = uri.getPath();
		} else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
			Cursor cursor = context.getContentResolver().query(uri, new String[] { ImageColumns.DATA }, null, null, null);
			if (null != cursor) {
				if (cursor.moveToFirst()) {
					int index = cursor.getColumnIndex(ImageColumns.DATA);
					if (index > -1) {
						data = cursor.getString(index);
					}
				}
				cursor.close();
			}
		}

		return data;
	}

	/** 保存Bitmap文件 */
	public static String saveBitmap(String picPath, Bitmap bm) {
		File f = new File(picPath);
		try {
			FileOutputStream out = new FileOutputStream(f);
			bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.flush();
			out.close();
			return picPath;
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}

	public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}
		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;
		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));
		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}
		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	/** 图片二次采样 */
	public static Bitmap scalePic(String filePath) {
		Bitmap bitmap = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, opts);
		opts.inSampleSize = computeSampleSize(opts, -1, 128 * 128);
		opts.inJustDecodeBounds = false;
		try {
			bitmap = BitmapFactory.decodeFile(filePath, opts);
		} catch (Exception e) {
		}
		return bitmap;
	}

	/** 计算文件大小 */
	public static int fileLength(String absolutePath) {
		int len = 0;
		try {
			File file = new File(absolutePath);
			FileInputStream fis = new FileInputStream(file);
			len = fis.available();
		} catch (IOException e) {
			return -1;
		}
		return len;
	}

	/** 计算图片的压缩比例 */
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		int height = options.outHeight;
		int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			int heightRatio = Math.round((float) height / (float) reqHeight);
			int widthRatio = Math.round((float) width / (float) reqWidth);
			// 选择长宽高较小的比例，成为压缩比例
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}

	/**
	 * 生成字符串的md5校验值
	 * */
	public static String getMD5HXID() {
		// 产生0和100000之间的整数
		long round = Math.round(Math.random() * 100000);
		// 系统当前时间
		long currentTimeMillis = System.currentTimeMillis();
		String hxid = "" + currentTimeMillis + round;
		return MD5Util.getMD5String(hxid);
	}

	/**
	 * Description 根据键值进行加密
	 * */
	public static String getDESHXPwd(String hxid) {
		// 固定字符：aaaaa
		// key:123456789
		String hxpwd = hxid + "em_key";
		String encrypt;
		try {
			encrypt = Encrypt.encrypt(hxpwd, Constants.KEY_HXPWD);
		} catch (Exception e) {
			return null;
		}
		return encrypt;
	}

	/**
	 * Description 根据键值进行解密
	 * */
	public static String decDESHXPwd(String hxpwd) {
		// key:123456789
		String decrypt = null;
		try {
			decrypt = Encrypt.decrypt(hxpwd, Constants.KEY_HXPWD);
		} catch (Exception e) {
			return null;
		}
		return decrypt;
	}

	public static String getDESGroup(String group) {

		String encrypt = null;
		try {
			encrypt = Encrypt.encrypt(group, Constants.KEY_GROUP);
		} catch (Exception e) {
			return null;
		}
		return encrypt;
	}

	public static String decDESGroup(String group) {

		String decrypt = null;
		try {
			decrypt = Encrypt.decrypt(group, Constants.KEY_GROUP);
		} catch (Exception e) {
			return null;
		}
		return decrypt;

	}

	public static String getDESMessage(String message) {

		String encrypt = null;
		try {
			encrypt = Encrypt.encrypt(message, Constants.KEY_MESSAGE);
		} catch (Exception e) {
			return null;
		}
		return encrypt;
	}

	public static String decDESMessage(String message) {

		String decrypt = null;
		try {
			decrypt = Encrypt.decrypt(message, Constants.KEY_MESSAGE);
		} catch (Exception e) {
			return null;
		}
		return decrypt;
	}

	/** 获取Android唯一标识码 */
	public static String getAndroidID(Context context) {
		return Installation.id(context);
	}

	public static void saveParams(String key, int params) {
		Editor edit = MeetingApp.mParamsSharePrefreces.edit();
		edit.putInt(key, params);
		edit.commit();
	}

	public static int getParams(String key) {
		return MeetingApp.mParamsSharePrefreces.getInt(key, 0);
	}
}
