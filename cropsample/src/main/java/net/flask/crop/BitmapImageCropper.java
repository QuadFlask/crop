package net.flask.crop;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapImageCropper {
	private int width, height;

	public BitmapImageCropper(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public Bitmap crop(String path, Rect region, int orientation) {
		Bitmap bitmap = getSampledSizeBitmap(path);
		if (orientation % 180 == 90)
			bitmap = BitmapUtil.rotate(bitmap, orientation);
		return Bitmap.createBitmap(bitmap, region.left, region.top, region.width() - 1, region.height() - 1, null, true);
	}

	public Bitmap scaleDown(Bitmap bitmap, int width, int height) {
		return BitmapUtil.scale(bitmap, width, height);
	}

	public boolean save(Bitmap bitmap, String path) {
		FileOutputStream outStream = null;

		try {
			outStream = new FileOutputStream(path);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
			Log.d("SettingAct.saveBitmap", "onPictureTaken - wrote bytes: " + bitmap.getRowBytes() * bitmap.getHeight());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (outStream != null) {
				try {
					outStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private Bitmap getSampledSizeBitmap(String path) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inSampleSize = getOptimizedSampleSize(path, Math.max(width, height));
		return resolveBitmap(path, opt);
	}

	private Bitmap resolveBitmap(String path, BitmapFactory.Options options) {
		return BitmapFactory.decodeFile(path, options);
	}

	private int getOptimizedSampleSize(String fileName, int targetSize) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(fileName, options);
		return calcRate(targetSize, options);
	}

	private int calcRate(int targetSize, BitmapFactory.Options options) {
		int bitmapMaxSize = Math.min(options.outWidth, options.outHeight);
		int result = 1;
		if (targetSize < bitmapMaxSize)
			result = getBinaryNumberLessThan((int) (((float) bitmapMaxSize) / targetSize));
		return result;
	}

	private int getBinaryNumberLessThan(int n) {
		int result;
		for (result = 1; result < 32; result++)
			if (n >> result == 0)
				break;
		return (int) Math.pow(2, result - 1);
	}
}