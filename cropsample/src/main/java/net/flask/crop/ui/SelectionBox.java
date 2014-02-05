package net.flask.crop.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import static java.lang.Math.round;

public class SelectionBox {
	private static final Paint dotPaint, circlePaint, linePaint;
	private MarqueeBox marqueeBox;

	static {
		dotPaint = new Paint();
		dotPaint.setColor(0xcc33b5e5);
		dotPaint.setAntiAlias(true);
		dotPaint.setStyle(Paint.Style.FILL);

		circlePaint = new Paint();
		circlePaint.setColor(0x330099cc);
		circlePaint.setAntiAlias(true);
		circlePaint.setStyle(Paint.Style.FILL);

		linePaint = new Paint();
		linePaint.setColor(0x22656d78);
		linePaint.setStrokeWidth(1);
		linePaint.setStyle(Paint.Style.FILL);
	}

	private Rect bound;
	private Rect area;

	private Rect anchorT, anchorL, anchorR, anchorB;

	private float radius = 20f;
	private float aspectRatio = 1;
	private int ppi;

	public SelectionBox(Rect bound) {
		this.bound = bound;

		int halfWidth = bound.width() / 4;
		int halfHeight = bound.height() / 4;
		this.area = new Rect(
				bound.centerX() - halfWidth, bound.centerY() - halfHeight,
				bound.centerX() + halfWidth, bound.centerY() + halfHeight);

		anchorT = new Rect();
		anchorL = new Rect();
		anchorR = new Rect();
		anchorB = new Rect();

		marqueeBox = new MarqueeBox(area);
	}

	public void draw(Canvas canvas) {
		drawDivideLine(canvas);

		marqueeBox.setRect(area);
		marqueeBox.draw(canvas);

		arrangeAnchors();
		drawAnchors(canvas);
	}

	private void drawDivideLine(Canvas canvas) {
		float tw = area.width() / 3f;
		float th = area.height() / 3f;
		canvas.drawLine(area.left + tw, area.top, area.left + tw, area.bottom, linePaint);
		canvas.drawLine(area.left + tw * 2, area.top, area.left + tw * 2, area.bottom, linePaint);
		canvas.drawLine(area.left, area.top + th, area.right, area.top + th, linePaint);
		canvas.drawLine(area.left, area.top + th * 2, area.right, area.top + th * 2, linePaint);
	}

	private void drawAnchors(Canvas canvas) {
		drawDot(canvas, anchorL.centerX(), anchorL.centerY());
		drawDot(canvas, anchorT.centerX(), anchorT.centerY());
		drawDot(canvas, anchorR.centerX(), anchorR.centerY());
		drawDot(canvas, anchorB.centerX(), anchorB.centerY());
	}

	private void drawDot(Canvas canvas, int x, int y) {
//		canvas.drawCircle(x, y, radius, selectionPaint);
		canvas.drawCircle(x, y, radius, circlePaint);
		canvas.drawCircle(x, y, radius / 3f, dotPaint);
	}

	private void arrangeAnchors() {
		anchorL.set((int) (area.left - radius), (int) (area.centerY() - radius), (int) (area.left + radius), (int) (area.centerY() + radius));
		anchorT.set((int) (area.centerX() - radius), (int) (area.top - radius), (int) (area.centerX() + radius), (int) (area.top + radius));
		anchorR.set((int) (area.right - radius), (int) (area.centerY() - radius), (int) (area.right + radius), (int) (area.centerY() + radius));
		anchorB.set((int) (area.centerX() - radius), (int) (area.bottom - radius), (int) (area.centerX() + radius), (int) (area.bottom + radius));
	}

	public void move(int x, int y) {
		int hw = area.width() / 2;
		int hh = area.height() / 2;

		int cx = area.centerX();
		int cy = area.centerY();
		cx += x;
		cy += y;

		if (cx + hw > bound.right)
			cx = bound.right - hw;
		if (cy + hh > bound.bottom)
			cy = bound.bottom - hh;
		if (cx - hw < bound.left)
			cx = bound.left + hw;
		if (cy - hh < bound.top)
			cy = bound.top + hh;

		area.set(
				cx - hw,
				cy - hh,
				cx + hw,
				cy + hh
		);
	}

	public boolean contains(int mx, int my) {
		return area.contains(mx, my);
	}

	public boolean horizontalAnchorContains(int mx, int my) {
		return anchorL.contains(mx, my) | anchorR.contains(mx, my);
	}

	public boolean verticalAnchorContains(int mx, int my) {
		return anchorT.contains(mx, my) | anchorB.contains(mx, my);
	}

	public boolean isLeftTopAnchorContains(int mx, int my) {
		return anchorL.contains(mx, my) | anchorT.contains(mx, my);
	}

	public void setAspectRatio(int width, int height) {
		this.aspectRatio = width / (float) height;
		increaseHorizontal(0);
		increaseVertical(0);
	}

	public void increaseHorizontal(int amount) {
		int height = area.height() + amount * 2;

		height = limitHeight(height);
		int width = round(height * aspectRatio);

		width = limitWidth(width);
		height = round(width / aspectRatio);

		rearrange(width, height);
	}

	public void increaseVertical(int amount) {
		int width = area.width() + amount * 2;

		width = limitWidth(width);
		int height = round(width / aspectRatio);

		height = limitHeight(height);
		width = round(height * aspectRatio);

		rearrange(width, height);
	}

	private int limitHeight(int height) {
		if (height > bound.height()) height = bound.height();
		else if (height < radius * 2) height = (int) radius * 2;
		return height;
	}

	private int limitWidth(int width) {
		if (width > bound.width()) width = bound.width();
		else if (width < radius * 2) width = (int) (radius * 2);
		return width;
	}

	private void rearrange(int width, int height) {
		int cx = area.centerX();
		int cy = area.centerY();
		area.set(cx - width / 2, cy - height / 2, cx + width / 2, cy + height / 2);
		move(0, 0);
	}

	@Override
	public String toString() {
		return "SelectionBox{" +
				"bound=" + bound +
				", area=" + area +
				'}';
	}

	public Rect getRect() {
		Rect cloned = new Rect();
		int offsetX = bound.left;
		int offsetY = bound.top;
		cloned.set(area.left - offsetX, area.top - offsetY, area.right - offsetX, area.bottom - offsetY);
		Log.e("SelectionBox", "bound : " + bound.toShortString() + " // " + bound.width() + "x" + bound.height());
		return cloned;
	}

	public RectF getRatioRect() {
		float top = area.top - bound.top;
		float left = area.left - bound.left;
		float bottom = area.bottom - bound.top;
		float right = area.right - bound.left;

		left /= bound.width();
		top /= bound.height();
		right /= bound.width();
		bottom /= bound.height();

		return new RectF(left, top, right, bottom);
	}

	public void setPpi(int ppi) {
		this.ppi = ppi;
		this.radius = ppi / 24f;
	}
}