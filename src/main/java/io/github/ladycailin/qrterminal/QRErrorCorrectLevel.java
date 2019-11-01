package io.github.ladycailin.qrterminal;

/**
 *
 */
public enum QRErrorCorrectLevel {
	L(1),
	M(0),
	Q(3),
	H(2);

	private final int level;

	private QRErrorCorrectLevel(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}
}
