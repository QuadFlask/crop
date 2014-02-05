package net.flask.crop;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;

public class BitmapUtil {
	public static Bitmap copy(Bitmap src) {
		return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight());
	}

	public static Rect getScaledRect(Rect src, float rate) {
		Rect result = getCloneRect(src);
		result.left *= rate;
		result.top *= rate;
		result.right *= rate;
		result.bottom *= rate;
		return result;
	}

	private static Rect getCloneRect(Rect src) {
		Rect result = new Rect();
		result.left = src.left;
		result.top = src.top;
		result.right = src.right;
		result.bottom = src.bottom;
		return result;
	}

	public static Bitmap crop(Bitmap bitmap, Rect rect, float multiplier) {
		return crop(bitmap, getScaledRect(rect, multiplier));
	}

	public static Bitmap crop(Bitmap b, Rect rect) {
		Bitmap br = null;
		boolean notNull = false;
		try {
			Log.e("BitmapUtil", "rect : " + rect.toShortString());
			Log.e("BitmapUtil", "bitmap : " + b.getWidth() + ", " + b.getHeight());

			int x = Math.max(rect.left, 1);
			int y = Math.max(rect.top, 1);
			int w = Math.max(rect.right - rect.left - 1, 1);
			int h = Math.max(rect.bottom - rect.top - 1, 1);

			Log.e("BitmapUtil", "created bitmap_rect size : x : " + x + " / y : " + y + " / w : " + w + " / h : " + h);
			br = Bitmap.createBitmap(b, x, y, w, h);
			notNull = true;
		} catch (Exception e) {
			Log.e("BitmapUtil", "crop error" + rect.toShortString());
			e.printStackTrace();
			notNull = false;
		} catch (OutOfMemoryError oome) {
			oome.printStackTrace();
			return null;
		} finally {
			if (notNull) b.recycle();
		}
		return br;
	}

	public static Bitmap rotate(Bitmap b, float rotate) {
		if (rotate == 0) {
			return b;
		}
		Matrix m = new Matrix();
		m.postRotate(rotate);
		Bitmap br = null;
		try {
			br = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
		} finally {
			b.recycle();
		}
		return br;
	}

	public static Bitmap scale(Bitmap b, int width, int height) {
		Bitmap br = null;
		boolean notNull = false;
		try {
			br = Bitmap.createScaledBitmap(b, width, height, true);
			notNull = true;
		} catch (Exception e) {
			e.printStackTrace();
			notNull = false;
		} finally {
			if (notNull) b.recycle();
		}
		return br;
	}
}
