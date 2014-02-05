package net.flask.crop.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class RoundButton {
	private static final Paint defaultPaint;
	private static final Paint defaultPressedPaint;

	static {
		defaultPaint = new Paint();
		defaultPaint.setColor(0xaa111111);
		defaultPaint.setAntiAlias(true);
		defaultPaint.setStyle(Paint.Style.FILL);

		defaultPressedPaint = new Paint();
		defaultPressedPaint.setColor(0x88ffffff);
		defaultPressedPaint.setAntiAlias(true);
		defaultPressedPaint.setStyle(Paint.Style.FILL);
	}

	private Paint paint = defaultPaint;
	private Paint textPaint;
	private RectF rect;
	private PointF center;

	private Rect iconBound;
	private Drawable icon;
	private String text;
	private Paint.Align align;

	private int topPadding = 0;
	private int leftPadding = 0;
	private int iconSize = 32;

	private int topTextPadding = 0;
	private int leftTextPadding = 0;

	private boolean isPressed = false;

	public RoundButton(String text, Drawable icon, Paint.Align align) {
		this(0, 0, 0, 0, text, icon, align);
	}

	public RoundButton(int x, int y, int width, int height, String text, Drawable icon, Paint.Align align) {
		this.rect = new RectF(x, y, x + width, y + height);
		this.align = align;

		this.textPaint = new Paint();
		this.textPaint.setColor(0xffffffff);
		this.textPaint.setAntiAlias(true);
		this.textPaint.setTextAlign(align);
		this.textPaint.setTextSize(height / 5f);

		setText(text);
		setIcon(icon, align);
	}

	public void setText(String text) {
		this.text = text;
		refreshText();
	}

	public void setIcon(Drawable icon, Paint.Align align) {
		this.icon = icon;
		this.iconBound = new Rect();
		refreshIcon();
	}

	public void setPaint(Paint paint) {
		this.paint = paint;
	}

	private void refreshIcon() {
		int cx = (int) rect.centerX();
		int cy = (int) rect.centerY();

		switch (align) {
			case RIGHT:
				leftPadding = (int) (rect.width() / 2 - iconSize);
				leftTextPadding = leftPadding - iconSize;
				break;
			case LEFT:
				leftPadding = (int) (rect.width() / 2 - iconSize);
				leftTextPadding = leftPadding - iconSize;
				leftPadding *= -1;
				leftTextPadding *= -1;
				break;
			case CENTER:
				topPadding = iconSize / 2;
				topTextPadding = (int) (-rect.bottom / 2 + topPadding * 1.5f);//-topPadding;
				break;
		}

		this.iconBound.set(cx - iconSize / 2 + leftPadding, cy - iconSize / 2 - topPadding, cx + iconSize / 2 + leftPadding, cy + iconSize / 2 - topPadding);
		this.icon.setBounds(iconBound);
	}

	private void refreshText() {
		this.center = new PointF(rect.centerX(), rect.centerY() - ((textPaint.descent() + textPaint.ascent()) / 2));
	}

	public void draw(Canvas canvas) {
		canvas.drawRoundRect(rect, 5, 5, paint);
		canvas.drawText(text, center.x + leftTextPadding, center.y - topTextPadding, textPaint);
		if (isPressed)
			canvas.drawRoundRect(rect, 5, 5, defaultPressedPaint);

		if (icon != null) icon.draw(canvas);
	}

	public boolean contains(int x, int y) {
		return rect.contains(x, y);
	}

	public void setRect(int x, int y, int width, int height) {
		this.rect = new RectF(x, y, x + width, y + height);
		this.textPaint.setTextSize(height / 5f);
		refreshIcon();
		refreshText();
	}

	public void showOnPress() {
		isPressed = true;
	}

	public void hideOnPress() {
		isPressed = false;
	}

	public String getText() {
		return text;
	}
}


