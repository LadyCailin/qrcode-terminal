package io.github.ladycailin.qrterminal;

/**
 *
 */
public enum QRMode {
	@SuppressWarnings("PointlessBitwiseExpression")
	MODE_NUMBER(1 << 0),
	MODE_ALPHA_NUM(1 << 1),
	MODE_8BIT_BYTE(1 << 2),
	MODE_KANJI(1 << 3);

	private final int i;

	private QRMode(int i) {
		this.i = i;
	}

	public int asInt() {
		return i;
	}
}
