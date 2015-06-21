package com.deshang365.meeting.baselib;

import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import com.deshang365.util.Base64;
import com.deshang365.util.MD5Util;

public class Encrypt {
	private final static String DES = "DES";
	private final static byte[] iv = { 1, 2, 3, 4, 5, 6, 7, 8 };

	/**
	 * Description 根据键值进行加密
	 * 
	 * @param data
	 * @param key
	 *            加密键byte数组
	 * @return
	 * @throws Exception
	 */
	public static String encrypt(String data, String key) throws Exception {
		if (data == null || key == null)
			return null;

		IvParameterSpec zeroIv = new IvParameterSpec(iv);

		// 从原始密钥数据创建DESKeySpec对象
		// key MD5 保证位数是8的倍数
		DESKeySpec dks = new DESKeySpec(MD5Util.getMD5String(key).getBytes());
		// 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
		SecretKey securekey = keyFactory.generateSecret(dks);

		// Cipher对象实际完成加密操作
		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		// 用密钥初始化Cipher对象
		cipher.init(Cipher.ENCRYPT_MODE, securekey, zeroIv);

		return new String(Base64.encode(cipher.doFinal(data.getBytes())));
	}

	/**
	 * Description 根据键值进行解密
	 * 
	 * @param data
	 * @param key
	 *            加密键byte数组
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */
	public static String decrypt(String data, String key) throws Exception {
		if (data == null || key == null)
			return null;

		IvParameterSpec zeroIv = new IvParameterSpec(iv);
		// 从原始密钥数据创建DESKeySpec对象
		DESKeySpec dks = new DESKeySpec(MD5Util.getMD5String(key).getBytes());
		// 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
		SecretKey securekey = keyFactory.generateSecret(dks);

		// Cipher对象实际完成解密操作
		// Cipher cipher = Cipher.getInstance(DES);
		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		// 用密钥初始化Cipher对象
		cipher.init(Cipher.DECRYPT_MODE, securekey, zeroIv);
		return new String(cipher.doFinal(Base64.decode(data)));
	}
}
