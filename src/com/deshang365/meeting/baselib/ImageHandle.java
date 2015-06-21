package com.deshang365.meeting.baselib;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageHandle {
	public static void saveFile(Bitmap bm, String savePath, String fileName) throws IOException {
		File dirFile = new File(savePath);
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}
		File imageFile = new File(savePath + fileName);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(imageFile));
		if (bm != null) {
			bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
		}
		bos.flush();
		bos.close();
	}

	public static Bitmap getLoacalBitmap(String url) {
		try {
			FileInputStream fis = new FileInputStream(url);
			return BitmapFactory.decodeStream(fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
}
