package net.flask.crop;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import net.flask.crop.ui.BTN;

import java.io.File;

import de.greenrobot.event.EventBus;

public class CropActivity extends Activity {

	private static String TAG = "CropActivity";
	private CropSurface cropSurface;
	private ProgressDialog pd;
	private Handler uiHandler = new Handler();

	private int width, height;
	private String appStorage = Environment.getExternalStorageDirectory().getPath();
	private String path = appStorage + "/crop/background.png";
	private BitmapImageCropper cropper;
	private int orientation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		EventBus.getDefault().register(this);
		assignScreenSize();
		makeDir("crop");

		Bitmap pickedBitmap = getScaledBitmapFromURI(getUriFromIntent());
		if (pickedBitmap == null) {
			Toast.makeText(this, "Invalid Bitmap", Toast.LENGTH_LONG).show();
			this.onBackPressed();
			return;
		}
		orientation = getOrientation(getUriFromIntent());

		cropper = new BitmapImageCropper(width, height);
		cropSurface = new CropSurface(this, pickedBitmap, width, height, orientation);
		addContentView(cropSurface, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
	}

	protected void makeDir(String path) {
		try {
			File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), path);
			if (!dir.exists())
				dir.mkdirs();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String cropping(final Rect selection) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				uiHandler.post(showProgressDialog);

				Bitmap cropped = cropper.crop(getFilePath(getUriFromIntent()), selection, orientation);

				Bitmap scaled = cropper.scaleDown(cropped, width, height);
				cropped.recycle();

				cropper.save(scaled, path);
				scaled.recycle();

				uiHandler.post(hideProgressDialog);
			}
		};

		Thread thread = new Thread(runnable);
		thread.start();

		return path;
	}

	public void onEvent(PressEvent pressEvent) {
		BTN btn = pressEvent.getBtn();

		Intent intent = getIntent();
		intent.putExtra("RESULT", btn.getText());

		switch (btn) {
			case APPLY:
				Rect selection = cropSurface.getSelectionRect();
				String path = cropping(selection);
				intent.putExtra("PATH", path);
				Toast.makeText(this, "background image saved : " + path, Toast.LENGTH_LONG).show();
			case GALLERY:
			case CANCEL:
				setResult(RESULT_OK, intent);
				finish();
				break;
			default:
				break;
		}
	}

	@Override
	protected void onDestroy() {
		EventBus.getDefault().unregister(this);
		if (pd != null)
			pd.dismiss();

		super.onDestroy();
		finish();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		getIntent().putExtra("RESULT", BTN.CANCEL.getText());
		setResult(RESULT_OK);
		finish();
	}

	private void assignScreenSize() {
		WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		width = windowManager.getDefaultDisplay().getWidth();
		height = windowManager.getDefaultDisplay().getHeight();
	}

	private Bitmap getScaledBitmapFromURI(Uri selectedImage) {
		String filePath = getFilePath(selectedImage);

		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = getOptimizedSampleSize(filePath, Math.max(width, height));
		Log.e(TAG, "sample size:" + opts.inSampleSize);

		return BitmapFactory.decodeFile(filePath, opts);
	}

	private String getFilePath(Uri selectedImage) {
		String[] filePathColumn = {MediaStore.Images.Media.DATA};

		Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
		cursor.moveToFirst(); // TODO null pointer exception

		int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		String filePath = cursor.getString(columnIndex);
		cursor.close();
		return filePath;
	}

	private Uri getUriFromIntent() {
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		return extras.getParcelable("data");
	}

	private int getOrientation(Uri photoUri) {
		Cursor cursor = getContentResolver().query(photoUri, new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);
		if (cursor.getCount() != 1) return -1;

		cursor.moveToFirst();
		int orientation = cursor.getInt(0);
		cursor.close();

		return orientation;
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

	private void add2ImageGallery(String path) {
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.DATA, path);
		values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
		getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
	}


	private Runnable showProgressDialog = new Runnable() {
		@Override
		public void run() {
			pd = new ProgressDialog(CropActivity.this);
			pd.setMessage("please wait...");
			pd.setCancelable(false);
			pd.show();
		}
	};

	private Runnable hideProgressDialog = new Runnable() {
		@Override
		public void run() {
			if (pd != null) {
				pd.hide();
				pd.dismiss();
			}
		}
	};

}