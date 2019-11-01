package io.github.ladycailin.qrterminal;

/**
 *
 */
@SuppressWarnings("PointlessBitwiseExpression")
public class QRUtil {

	public static Boolean[][] copyBooleanArray(Boolean[][] matrix) {
		Boolean[][] output = new Boolean[matrix.length][];
		for (int i = 0; i < matrix.length; i++) {
			Boolean[] aMatrix = matrix[i];
			int aLength = aMatrix.length;
			output[i] = new Boolean[aLength];
			System.arraycopy(aMatrix, 0, output[i], 0, aLength);
		}
		return output;
	}

	private static final int[][] PATTERN_POSITION_TABLE = new int[][]{
		new int[0],
		new int[]{6, 18},
		new int[]{6, 22},
		new int[]{6, 26},
		new int[]{6, 30},
		new int[]{6, 34},
		new int[]{6, 22, 38},
		new int[]{6, 24, 42},
		new int[]{6, 26, 46},
		new int[]{6, 28, 50},
		new int[]{6, 30, 54},
		new int[]{6, 32, 58},
		new int[]{6, 34, 62},
		new int[]{6, 26, 46, 66},
		new int[]{6, 26, 48, 70},
		new int[]{6, 26, 50, 74},
		new int[]{6, 30, 54, 78},
		new int[]{6, 30, 56, 82},
		new int[]{6, 30, 58, 86},
		new int[]{6, 34, 62, 90},
		new int[]{6, 28, 50, 72, 94},
		new int[]{6, 26, 50, 74, 98},
		new int[]{6, 30, 54, 78, 102},
		new int[]{6, 28, 54, 80, 106},
		new int[]{6, 32, 58, 84, 110},
		new int[]{6, 30, 58, 86, 114},
		new int[]{6, 34, 62, 90, 118},
		new int[]{6, 26, 50, 74, 98, 122},
		new int[]{6, 30, 54, 78, 102, 126},
		new int[]{6, 26, 52, 78, 104, 130},
		new int[]{6, 30, 56, 82, 108, 134},
		new int[]{6, 34, 60, 86, 112, 138},
		new int[]{6, 30, 58, 86, 114, 142},
		new int[]{6, 34, 62, 90, 118, 146},
		new int[]{6, 30, 54, 78, 102, 126, 150},
		new int[]{6, 24, 50, 76, 102, 128, 154},
		new int[]{6, 28, 54, 80, 106, 132, 158},
		new int[]{6, 32, 58, 84, 110, 136, 162},
		new int[]{6, 26, 54, 82, 110, 138, 166},
		new int[]{6, 30, 58, 86, 114, 142, 170}
	};

	private final static int G15 = (1 << 10) | (1 << 8) | (1 << 5) | (1 << 4) | (1 << 2) | (1 << 1) | (1 << 0);
	private final static int G18 = (1 << 12) | (1 << 11) | (1 << 10) | (1 << 9) | (1 << 8) | (1 << 5) | (1 << 2) | (1 << 0);
	private final static int G15_MASK = (1 << 14) | (1 << 12) | (1 << 10) | (1 << 4) | (1 << 1);

	public static int getBCHTypeInfo(int data) {
		int d = data << 10;
		while (QRUtil.getBCHDigit(d) - QRUtil.getBCHDigit(QRUtil.G15) >= 0) {
			d ^= (QRUtil.G15 << (QRUtil.getBCHDigit(d) - QRUtil.getBCHDigit(QRUtil.G15)));
		}
		return ((data << 10) | d) ^ QRUtil.G15_MASK;
	}

	public static int getBCHTypeNumber(int data) {
		int d = data << 12;
		while (QRUtil.getBCHDigit(d) - QRUtil.getBCHDigit(QRUtil.G18) >= 0) {
			d ^= (QRUtil.G18 << (QRUtil.getBCHDigit(d) - QRUtil.getBCHDigit(QRUtil.G18)));
		}
		return (data << 12) | d;
	}

	public static int getBCHDigit(int data) {

		int digit = 0;

		while (data != 0) {
			digit++;
			data >>>= 1;
		}

		return digit;
	}

	public static int[] getPatternPosition(int typeNumber) {
		return QRUtil.PATTERN_POSITION_TABLE[typeNumber - 1];
	}

	public static boolean getMask(QRMaskPattern maskPattern, int i, int j) {
		switch (maskPattern) {
			case PATTERN000:
				return (i + j) % 2 == 0;
			case PATTERN001:
				return i % 2 == 0;
			case PATTERN010:
				return j % 3 == 0;
			case PATTERN011:
				return (i + j) % 3 == 0;
			case PATTERN100:
				return (Math.floor(i / 2) + Math.floor(j / 3)) % 2 == 0;
			case PATTERN101:
				return (i * j) % 2 + (i * j) % 3 == 0;
			case PATTERN110:
				return ((i * j) % 2 + (i * j) % 3) % 2 == 0;
			case PATTERN111:
				return ((i * j) % 3 + (i + j) % 2) % 2 == 0;
		}
		throw new Error("bad maskPattern:" + maskPattern);
	}

	public static QRPolynomial getErrorCorrectPolynomial(int errorCorrectLength) {
		QRPolynomial a = new QRPolynomial(new int[]{1}, 0);

		for (int i = 0; i < errorCorrectLength; i++) {
			a = a.multiply(new QRPolynomial(new int[]{1, QRMath.gexp(i)}, 0));
		}

		return a;
	}

	public static int getLengthInBits(QRMode mode, int type) {
		if (1 <= type && type < 10) {
			// 1 - 9
			switch (mode) {
				case MODE_NUMBER:
					return 10;
				case MODE_ALPHA_NUM:
					return 9;
				case MODE_8BIT_BYTE:
					return 8;
				case MODE_KANJI:
					return 8;
				default:
					throw new Error("mode:" + mode);
			}

		} else if (type < 27) {

			// 10 - 26
			switch (mode) {
				case MODE_NUMBER:
					return 12;
				case MODE_ALPHA_NUM:
					return 11;
				case MODE_8BIT_BYTE:
					return 16;
				case MODE_KANJI:
					return 10;
				default:
					throw new Error("mode:" + mode);
			}

		} else if (type < 41) {

			// 27 - 40
			switch (mode) {
				case MODE_NUMBER:
					return 14;
				case MODE_ALPHA_NUM:
					return 13;
				case MODE_8BIT_BYTE:
					return 16;
				case MODE_KANJI:
					return 12;
				default:
					throw new Error("mode:" + mode);
			}

		} else {
			throw new Error("type:" + type);
		}
	}

	public static int getLostPoint(QRCode qrCode) {
		int moduleCount = qrCode.getModuleCount();
		int lostPoint = 0;
		int row;
		int col;

		// LEVEL1
		for (row = 0; row < moduleCount; row++) {
			for (col = 0; col < moduleCount; col++) {

				int sameCount = 0;
				boolean dark = qrCode.isDark(row, col);

				for (int r = -1; r <= 1; r++) {

					if (row + r < 0 || moduleCount <= row + r) {
						continue;
					}

					for (int c = -1; c <= 1; c++) {

						if (col + c < 0 || moduleCount <= col + c) {
							continue;
						}

						if (r == 0 && c == 0) {
							continue;
						}

						if (dark == qrCode.isDark(row + r, col + c)) {
							sameCount++;
						}
					}
				}

				if (sameCount > 5) {
					lostPoint += (3 + sameCount - 5);
				}
			}
		}

		// LEVEL2
		for (row = 0; row < moduleCount - 1; row++) {
			for (col = 0; col < moduleCount - 1; col++) {
				int count = 0;
				if (qrCode.isDark(row, col)) {
					count++;
				}
				if (qrCode.isDark(row + 1, col)) {
					count++;
				}
				if (qrCode.isDark(row, col + 1)) {
					count++;
				}
				if (qrCode.isDark(row + 1, col + 1)) {
					count++;
				}
				if (count == 0 || count == 4) {
					lostPoint += 3;
				}
			}
		}

		// LEVEL3
		for (row = 0; row < moduleCount; row++) {
			for (col = 0; col < moduleCount - 6; col++) {
				if (qrCode.isDark(row, col)
						&& !qrCode.isDark(row, col + 1)
						&& qrCode.isDark(row, col + 2)
						&& qrCode.isDark(row, col + 3)
						&& qrCode.isDark(row, col + 4)
						&& !qrCode.isDark(row, col + 5)
						&& qrCode.isDark(row, col + 6)) {
					lostPoint += 40;
				}
			}
		}

		for (col = 0; col < moduleCount; col++) {
			for (row = 0; row < moduleCount - 6; row++) {
				if (qrCode.isDark(row, col)
						&& !qrCode.isDark(row + 1, col)
						&& qrCode.isDark(row + 2, col)
						&& qrCode.isDark(row + 3, col)
						&& qrCode.isDark(row + 4, col)
						&& !qrCode.isDark(row + 5, col)
						&& qrCode.isDark(row + 6, col)) {
					lostPoint += 40;
				}
			}
		}

		// LEVEL4
		int darkCount = 0;

		for (col = 0; col < moduleCount; col++) {
			for (row = 0; row < moduleCount; row++) {
				if (qrCode.isDark(row, col)) {
					darkCount++;
				}
			}
		}

		int ratio = Math.abs(100 * darkCount / moduleCount / moduleCount - 50) / 5;
		lostPoint += ratio * 10;

		return lostPoint;
	}
}
