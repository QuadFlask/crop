package net.flask.crop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.example.cropsample.R;

import net.flask.crop.ui.BTN;
import net.flask.crop.ui.LinearButtonLayout;
import net.flask.crop.ui.RoundButton;
import net.flask.crop.ui.SelectionBox;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;

import static android.graphics.Paint.Align;
import static java.lang.Math.round;

public class CropSurface extends SurfaceView implements Callback {
	private static final String TAG = "CropSurface";

	private final Context context;
	private final SurfaceHolder holder;
	private Bitmap scaledBitmap;
	private int width, height;
	private int bw, bh;

	private Paint buttonPaint;
	private Paint buttonPushedPaint;

	private LinearButtonLayout bottomLayout, topLayout;
	private SelectionBox selectionBox;
	private Rect bound;

	private int btnGap = 5;
	private int btnSize = 80;
	private int originalBitmapWidth, originalBitmapHeight;

	private int backgroundColor = 0xff242424;

	public CropSurface(Context context, Bitmap bitmap, int width, int height, int orientation) {
		super(context);
		this.context = context;
		this.holder = getHolder();
		this.holder.addCallback(this);
		this.holder.setFormat(PixelFormat.TRANSLUCENT);
		setFocusable(true);

		this.width = width;
		this.height = height;

		this.originalBitmapWidth = bitmap.getWidth();
		this.originalBitmapHeight = bitmap.getHeight();

		if (isVertical(orientation)) { // vertical
			originalBitmapWidth = bitmap.getHeight();
			originalBitmapHeight = bitmap.getWidth();
		}
		Rect tempRect = getMaxRect(round(width * 0.9f), round(height * 0.9f), originalBitmapWidth, originalBitmapHeight);

		Log.e(TAG, "bitmap screen boundary size (temp): " + tempRect.toShortString());

		this.scaledBitmap = makeScaledBitmap2(bitmap, tempRect.width(), tempRect.height(), orientation);

		this.bw = scaledBitmap.getWidth();
		this.bh = scaledBitmap.getHeight();

		int left = round(width / 2f - bw / 2f);
		int top = round(height / 2f - bh / 2f);
		this.bound = new Rect(left, top, left + bw, top + bh);

		initPaints();
		initUIButtons();

		selectionBox = new SelectionBox(bound);
		selectionBox.setAspectRatio(width, height);
		selectionBox.setPpi(width);

		EventBus.getDefault().register(this);
	}

	private boolean isVertical(int orientation) {
		return orientation % 180 == 90;
	}

	private Rect getMaxRect(int maxWidth, int maxHeight, int aspectWidth, int aspectHeight) {
		float maxVerticalRate = (float) maxHeight / maxWidth;
		float aspectVerticalRate = (float) aspectHeight / aspectWidth;
		int desiredWidth, desiredHeight;

		if (aspectVerticalRate > maxVerticalRate) { // bitmap height is too long
			desiredHeight = maxHeight;
			desiredWidth = round(desiredHeight / aspectVerticalRate);
		} else {
			desiredWidth = maxWidth;
			desiredHeight = round(desiredWidth * aspectVerticalRate);
		}
		return new Rect(0, 0, desiredWidth, desiredHeight);
	}

	private Bitmap makeScaledBitmap2(Bitmap bitmap, int maxWidth, int maxHeight, int orientation) {
		int bitmapWidth = bitmap.getWidth();
		int bitmapHeight = bitmap.getHeight();
		int desiredHeight, desiredWidth;

		if (isVertical(orientation)) { // vertical
			bitmapWidth = bitmap.getHeight();
			bitmapHeight = bitmap.getWidth();
		}

		Rect maxRect = getMaxRect(maxWidth, maxHeight, bitmapWidth, bitmapHeight);
		desiredWidth = maxRect.width();
		desiredHeight = maxRect.height();

		if (isVertical(orientation)) {
			desiredWidth = maxRect.height();
			desiredHeight = maxRect.width();
		}

		Log.e(TAG, "desired size: " + desiredWidth + "x" + desiredHeight);
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, desiredWidth, desiredHeight, true);
		bitmap.recycle();

		if (isVertical(orientation))
			scaledBitmap = BitmapUtil.rotate(scaledBitmap, orientation);

		return scaledBitmap;
	}

	public Rect getSelectionRect() {
		RectF ratioRect = selectionBox.getRatioRect();
		Log.e(TAG, "$$$$  ratioRect box : " + ratioRect);
		Log.e(TAG, "$$$$  original bitmap size : " + originalBitmapWidth + "x" + originalBitmapHeight);

		Rect result = new Rect(
				round(ratioRect.left * originalBitmapWidth),
				round(ratioRect.top * originalBitmapHeight),
				round(ratioRect.right * originalBitmapWidth),
				round(ratioRect.bottom * originalBitmapHeight));
		Log.e(TAG, "$$$$  scaled result box : " + result);

		return result;
	}

	private void initPaints() {
		buttonPaint = new Paint();
		buttonPaint.setColor(0xff333333);
		buttonPaint.setAntiAlias(true);
		buttonPaint.setStyle(Paint.Style.FILL);

		buttonPushedPaint = new Paint();
		buttonPushedPaint.setColor(0xffccff00);
		buttonPushedPaint.setAntiAlias(true);
		buttonPushedPaint.setStrokeWidth(1.2f);
		buttonPushedPaint.setStyle(Paint.Style.STROKE);
	}

	private void initUIButtons() {
		topLayout = new LinearButtonLayout(0, btnGap, width, btnSize);
		topLayout.addButton(new RoundButton(BTN.GALLERY.getText(), getDrawable(R.drawable.gallery), Align.CENTER));
//		topLayout.addButton(new RoundButton(BTN.ROTATE.getText(), getDrawable(R.drawable.rotate), Align.CENTER)); // TODO rotation is not implemented...
		topLayout.addButton(new RoundButton(BTN.FIT2SCREEN.getText(), getDrawable(R.drawable.expend), Align.CENTER));
		topLayout.setStackLayout();

		bottomLayout = new LinearButtonLayout(0, height - btnSize - btnGap, width, btnSize);
		bottomLayout.addButton(new RoundButton(BTN.CANCEL.getText(), getDrawable(R.drawable.no), Align.RIGHT));
		bottomLayout.addButton(new RoundButton(BTN.APPLY.getText(), getDrawable(R.drawable.ok), Align.LEFT));
		bottomLayout.setFit2ScreenLayout();
	}

	Timer timer;

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		draw();
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				draw();
			}
		}, 0, 16);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		EventBus.getDefault().unregister(this);
		if (timer != null) {
			timer.cancel();
			timer.purge();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
//		draw();
//		invalidate();
	}

	private void draw() {
		try {
			Canvas canvas = holder.lockCanvas();
			drawBackground(canvas);
			drawImage(canvas);
			drawUI(canvas);
			holder.unlockCanvasAndPost(canvas);
		} catch (Exception e) {
		}
	}

	private void drawImage(Canvas canvas) {
		canvas.drawBitmap(scaledBitmap, bound.left, bound.top, null);
	}

	private void drawBackground(Canvas canvas) {
		canvas.drawColor(backgroundColor);
	}

	private void drawUI(Canvas canvas) {
		topLayout.draw(canvas);
		bottomLayout.draw(canvas);
		selectionBox.draw(canvas);
	}

	int bmx = -1, bmy = -1;

	boolean move, incVertical, incHorizontal;
	int flag = 1;

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		int mx = (int) e.getX();
		int my = (int) e.getY();
		int action = e.getAction();

		switch (action) {
			case MotionEvent.ACTION_DOWN:
				bmx = mx;
				bmy = my;

				if (selectionBox.horizontalAnchorContains(mx, my)) incHorizontal = true;
				else if (selectionBox.verticalAnchorContains(mx, my)) incVertical = true;
				else if (selectionBox.contains(mx, my)) move = true;
				flag = (selectionBox.isLeftTopAnchorContains(mx, my)) ? -1 : 1;

				checkOnPress(topLayout.getButtonList(), mx, my);
				checkOnPress(bottomLayout.getButtonList(), mx, my);

				break;
			case MotionEvent.ACTION_MOVE:
				int diffX = mx - bmx;
				int diffY = my - bmy;

				if (move)
					selectionBox.move(diffX, diffY);
				else if (incHorizontal)
					selectionBox.increaseHorizontal(diffX * flag);
				else if (incVertical)
					selectionBox.increaseVertical(diffY * flag);

				bmx = mx;
				bmy = my;
				break;
			case MotionEvent.ACTION_UP:
				move = incHorizontal = incVertical = false;
				bmx = bmy = -1;

				for (RoundButton btn : topLayout.getButtonList())
					btn.hideOnPress();
				for (RoundButton btn : bottomLayout.getButtonList())
					btn.hideOnPress();

				break;
		}

//		draw();
//		invalidate();

		return true;
	}

	public void onEvent(PressEvent pressEvent) {
		BTN btn = pressEvent.getBtn();
		switch (btn) {
			// TODO these type have to process in Surface.
			case ROTATE:
				//TODO rotate image
				selectionBox.move(0, 0);
				selectionBox.increaseHorizontal(0);
				break;
			case FIT2SCREEN:
				selectionBox.increaseHorizontal(Math.max(width, height));
				break;
		}
	}

	private void checkOnPress(List<RoundButton> buttonList, int mx, int my) {
		for (RoundButton btn : buttonList) {
			if (btn.contains(mx, my)) {
				btn.showOnPress();
				PressEvent pressEvent = new PressEvent(BTN.getBTN(btn.getText()));
				EventBus.getDefault().post(pressEvent); // ? enum!!
				break;
			}
		}
	}

	private Drawable getDrawable(int rid) {
		return getResources().getDrawable(rid);
	}
}

//	public CropSurface(Context context, Bitmap bitmap, int width, int height, int orientation) {
//		super(context);
//		this.context = context;
//		this.holder = getHolder();
//		this.holder.addCallback(this);
//		this.holder.setFormat(PixelFormat.TRANSLUCENT);
//		setFocusable(true);
//
//		this.width = width;
//		this.height = height;
//
//		this.originalBitmapWidth = bitmap.getWidth();
//		this.originalBitmapHeight = bitmap.getHeight();
//
//		float tempWidth = width;
//		float tempHeight = height;
//		float ratio = 0.9f;
//		if (tempHeight * ratio > height - (btnSize * 2 + btnGap * 4))
//			ratio *= (height - (btnSize * 2f + btnGap * 4f)) / tempHeight;
//		tempWidth = round(tempWidth * ratio);
//		tempHeight = round(tempHeight * ratio);
//
//		this.scaledBitmap = makeScaledBitmap(bitmap, (int) tempWidth, (int) tempHeight, orientation);
//
//		this.bw = scaledBitmap.getWidth();
//		this.bh = scaledBitmap.getHeight();
//
//		int left = round(width / 2f - bw / 2f);
//		int top = round(height / 2f - bh / 2f);
//		this.bound = new Rect(left, top, left + bw, top + bh);
//
//		initPaints();
//		initUIButtons();
//
//		selectionBox = new SelectionBox(bound);
//		selectionBox.setAspectRatio(width, height);
//
//		EventBus.getDefault().register(this);
//	}
//
//	private Bitmap makeScaledBitmap(Bitmap bitmap, int width, int height, int orientation) {
//		int w, h;
//
//		if (orientation % 180 == 90) {
//			w = bitmap.getHeight();
//			h = bitmap.getWidth();
//		} else {
//			w = bitmap.getWidth();
//			h = bitmap.getHeight();
//		}
//		Log.e(TAG, "width:" + width + ", height:" + height + " // w:" + w + ", h:" + h);
//
//		float rateScreen = (float) height / width;
//		float rateBitmap = (float) h / w;
//
//		if (rateScreen > rateBitmap) scaledRatio = (float) w / width;
//		else scaledRatio = (float) h / height;
//
//		w = round(w / scaledRatio);
//		h = round(h / scaledRatio);
//
//		Bitmap scaledBitmap;
//		if (orientation % 180 == 90) {
//			scaledBitmap = Bitmap.createScaledBitmap(bitmap, h, w, true);
//			scaledBitmap = BitmapUtil.rotate(scaledBitmap, orientation);
//		} else {
//			scaledBitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
//		}
//
//		Log.e(TAG, "w:" + w + ", h:" + h + " scaledRatio:" + scaledRatio);
//
//		return scaledBitmap;
//	}
//
//	public Rect getSelectionRect() {
//		Rect selection = selectionBox.getRect();
//		float rate = (bound.width() > bound.height()) ? (float) selection.width() / bound.width() : (float) selection.height() / bound.height();
//
//		Log.e(TAG, "selection box(original                : " + selection);
//		selection.left = Math.max(0, round(selection.left * this.scaledRatio));
//		selection.top = Math.max(0, round(selection.top * this.scaledRatio));
//		selection.right = selection.left + Math.min(originalBitmapWidth, round(originalBitmapWidth * rate));
//		selection.bottom = selection.bottom + Math.min(originalBitmapHeight, round(originalBitmapHeight * rate));
////		selection.left = Math.max(0, round(selection.left * this.scaledRatio));
////		selection.top = Math.max(0, round(selection.top * this.scaledRatio));
////		selection.right = Math.min(originalBitmapWidth, round(selection.right * this.scaledRatio));
////		selection.bottom = Math.min(originalBitmapHeight, round(selection.bottom * this.scaledRatio));
//		Log.e(TAG, "selection box(scale:" + this.scaledRatio + ") : " + selection);
//		return selection;
//	}