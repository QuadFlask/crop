package net.flask.crop.ui;

public enum BTN {
	GALLERY("Gallery"),
	ROTATE("Rotate"),
	FIT2SCREEN("Fit to screen"),
	CANCEL("Cancel"),
	APPLY("Apply");

	private String text;

	private BTN(String text) {
		this.text = text;
	}

	public static BTN getBTN(String text) {
		for (BTN btn : values()) {
			if (btn.getText().equals(text))
				return btn;
		}
		return null;
	}

	public String getText() {
		return text;
	}
}