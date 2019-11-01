package io.github.ladycailin.qrterminal;

/**
 *
 */
public class QRMath {

	public static int glog(int n) {
		if (n < 1) {
			throw new Error("glog(" + n + ")");
		}

		return LOG_TABLE[n];
	}

	public static int gexp(int n) {
		while (n < 0) {
			n += 255;
		}

		while (n >= 256) {
			n -= 255;
		}

		return EXP_TABLE[n];
	}

	private static final int[] EXP_TABLE = new int[256];
	private static final int[] LOG_TABLE = new int[256];

	static {
		for (int i = 0; i < 8; i++) {
			EXP_TABLE[i] = 1 << i;
		}
		for (int i = 8; i < 256; i++) {
			QRMath.EXP_TABLE[i] = QRMath.EXP_TABLE[i - 4]
					^ QRMath.EXP_TABLE[i - 5]
					^ QRMath.EXP_TABLE[i - 6]
					^ QRMath.EXP_TABLE[i - 8];
		}
		for (int i = 0; i < 255; i++) {
			QRMath.LOG_TABLE[QRMath.EXP_TABLE[i]] = i;
		}
	}
}
