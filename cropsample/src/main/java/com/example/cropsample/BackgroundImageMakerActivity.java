package com.example.cropsample;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;

import net.flask.crop.ui.BTN;
import net.flask.crop.CropActivity;

public class BackgroundImageMakerActivity extends Activity {
	private static final int REQUEST_CODE_FROM_GALLERY = 0;
	private static final int REQUEST_CODE_FROM_CROP = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		startGallery();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (intent == null) {
			finish();
			return;
		}

		switch (requestCode) {
			case REQUEST_CODE_FROM_GALLERY: {
				Uri selectedImage = intent.getData();
				callCropActivity(selectedImage);
				break;
			}
			case REQUEST_CODE_FROM_CROP: {
				String btn = intent.getStringExtra("RESULT");

				switch (BTN.getBTN(btn)) {
					case GALLERY:
						startGallery();
						break;
					case APPLY:
					case CANCEL:
						setResult(RESULT_OK, intent);
						finish();
						break;
				}

				break;
			}
		}
	}

	protected void startGallery() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, REQUEST_CODE_FROM_GALLERY);
	}

	private void callCropActivity(Uri selectedImage) {
		Intent cropIntent = new Intent(getApplicationContext(), CropActivity.class);
		cropIntent.putExtra("data", selectedImage);
		startActivityForResult(cropIntent, REQUEST_CODE_FROM_CROP);
	}

}
