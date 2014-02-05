package net.flask.crop.ui;

import android.graphics.Canvas;

import java.util.ArrayList;
import java.util.List;

public class LinearButtonLayout {
	private int btnGap = 5;
	private int btnSize = 80;
	private int width, height;
	private int x, y;
	private List<RoundButton> btns;
	// Basically left aligned, stack layout.

	public LinearButtonLayout(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.btns = new ArrayList<RoundButton>();
	}

	public void addButton(RoundButton btn) {
		btns.add(btn);
	}

	public void setStackLayout() {
		rearrange(btnGap, btnSize);
	}

	public void setFit2ScreenLayout() {
		int count = btns.size();
		int halfBtnWidth = (width - (count + 1) * btnGap) / count;
		rearrange(btnGap, halfBtnWidth);
	}

	private void rearrange(int baseX, int width) {
		for (RoundButton btn : btns) {
			btn.setRect(x + baseX, y, width, btnSize);
			baseX += btnGap + width;
		}
	}

	public void draw(Canvas canvas) {
		for (int i = 0; i < btns.size(); i++)
			btns.get(i).draw(canvas);
	}

	public List<RoundButton> getButtonList() {
		return btns;
	}
}
