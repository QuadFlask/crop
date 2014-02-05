package com.example.cropsample;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent cropIntent = new Intent(getApplicationContext(), BackgroundImageMakerActivity.class);
				startActivityForResult(cropIntent, 0x1234);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (data != null && requestCode == 0x1234) {
			String path = data.getStringExtra("PATH");
			ImageView imageView = (ImageView) findViewById(R.id.imageView);
			imageView.setImageBitmap(BitmapFactory.decodeFile(path));
		}
	}
}
