package net.flask.crop.ui;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;

public class MarqueeBox {
	private static final Paint defaultPaintA, defaultPaintB;

	static {
		defaultPaintA = new Paint();
		defaultPaintA.setColor(0xffffffff);
		defaultPaintA.setStrokeWidth(1.0f);
		defaultPaintA.setStyle(Paint.Style.STROKE);

		defaultPaintB = new Paint();
		defaultPaintB.setColor(0xff000000);
		defaultPaintB.setStrokeWidth(1.0f);
		defaultPaintB.setStyle(Paint.Style.STROKE);
	}

	private PathEffect[] pathEffects = new PathEffect[]{
			new DashPathEffect(new float[]{4.0f, 4.0f}, 0),
			new DashPathEffect(new float[]{4.0f, 4.0f}, 1f),
			new DashPathEffect(new float[]{4.0f, 4.0f}, 2f),
			new DashPathEffect(new float[]{4.0f, 4.0f}, 3f),
			new DashPathEffect(new float[]{4.0f, 4.0f}, 4f),
			new DashPathEffect(new float[]{4.0f, 4.0f}, 5f),
			new DashPathEffect(new float[]{4.0f, 4.0f}, 6f),
			new DashPathEffect(new float[]{4.0f, 4.0f}, 7f),
	};

	private float phase = 0;
	private Rect rect;
	private Path path;
	private Paint selectionMarqueePaint, selectionBackPaint;
	private float speed = 0.1f;

	public MarqueeBox(Rect rect) {
		this.rect = rect;
		this.path = new Path();
		selectionMarqueePaint = defaultPaintA;
		selectionBackPaint = defaultPaintB;
	}

	public void draw(Canvas canvas) {
		increasePhase();
		calcPathEffect();
		rect2Path(rect);

		canvas.drawPath(path, selectionBackPaint);
		canvas.drawPath(path, selectionMarqueePaint);
	}

	private void calcPathEffect() {
		selectionMarqueePaint.setPathEffect(pathEffects[((int) phase) % pathEffects.length]);
	}

	private void increasePhase() {
		if (phase >= pathEffects.length) phase = 0;
		phase += speed;
	}

	public void setRect(Rect rect) {
		this.rect = rect;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	private void rect2Path(Rect rect) {
		path.reset();
		path.moveTo(rect.left, rect.top);
		path.lineTo(rect.right, rect.top);
		path.lineTo(rect.right, rect.bottom);
		path.lineTo(rect.left, rect.bottom);
		path.lineTo(rect.left, rect.top);
		path.close();
	}

}
