package io.github.ladycailin.qrterminal;

/**
 *
 */
public enum QRMaskPattern {
	PATTERN000(0),
	PATTERN001(1),
	PATTERN010(2),
	PATTERN011(3),
	PATTERN100(4),
	PATTERN101(5),
	PATTERN110(6),
	PATTERN111(7);

	private final int i;

	private QRMaskPattern(int i) {
		this.i = i;
	}

	public int asInt() {
		return i;
	}
}
