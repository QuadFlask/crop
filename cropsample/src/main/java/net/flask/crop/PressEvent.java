package net.flask.crop;


import net.flask.crop.ui.BTN;

public class PressEvent {
	private final BTN btn;

	public PressEvent(BTN btn) {
		this.btn = btn;
	}

	public BTN getBtn() {
		return btn;
	}
}
