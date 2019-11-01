package io.github.ladycailin.qrterminal;

import java.util.ArrayList;
import java.util.List;

//---------------------------------------------------------------------
// QRCode for JavaScript
//
// Copyright (c) 2009 Kazuhiko Arase
//
// URL: http://www.d-project.com/
//
// Licensed under the MIT license:
//   http://www.opensource.org/licenses/mit-license.php
//
// The word "QR Code" is registered trademark of
// DENSO WAVE INCORPORATED
//   http://www.denso-wave.com/qrcode/faqpatent-e.html
//
//---------------------------------------------------------------------
// Ported to Java
//---------------------------------------------------------------------
public class QRCode {

	private int typeNumber;
	private final QRErrorCorrectLevel errorCorrectLevel;
	private Boolean[][] modules;
	private int moduleCount;
	private int[] dataCache;
	private final List<QR8bitByte> dataList;

	public QRCode(int typeNumber, QRErrorCorrectLevel errorCorrectLevel) {
		this.typeNumber = typeNumber;
		this.errorCorrectLevel = errorCorrectLevel;
		this.modules = null;
		this.moduleCount = 0;
		this.dataCache = null;
		this.dataList = new ArrayList<>();
	}

	public Boolean[][] getModules() {
		return this.modules;
	}

	public void addData(String data) {
		QR8bitByte newData = new QR8bitByte(data);
		this.dataList.add(newData);
		this.dataCache = null;
	}

	;

	public boolean isDark(int row, int col) {
		if (row < 0 || this.moduleCount <= row || col < 0 || this.moduleCount <= col) {
			throw new Error(row + "," + col);
		}
		return this.modules[row][col];
	}

	public int getModuleCount() {
		return this.moduleCount;
	}

	public void make() {
		// Calculate automatically typeNumber if provided is < 1
		if (this.typeNumber < 1) {
			int lTypeNumber;
			for (lTypeNumber = 1; lTypeNumber < 40; lTypeNumber++) {
				List<QRRSBlock> rsBlocks = QRRSBlock.getRSBlocks(lTypeNumber, this.errorCorrectLevel);

				QRBitBuffer buffer = new QRBitBuffer();
				int totalDataCount = 0;
				for (int i = 0; i < rsBlocks.size(); i++) {
					totalDataCount += rsBlocks.get(i).dataCount;
				}

				for (int x = 0; x < this.dataList.size(); x++) {
					QR8bitByte data = this.dataList.get(x);
					buffer.put(data.getMode().asInt(), 4);
					buffer.put(data.getLength(), QRUtil.getLengthInBits(data.getMode(), lTypeNumber));
					data.write(buffer);
				}
				if (buffer.getLengthInBits() <= totalDataCount * 8) {
					break;
				}
			}
			this.typeNumber = lTypeNumber;
		}
		this.makeImpl(false, this.getBestMaskPattern());
	}

	private void makeImpl(boolean test, QRMaskPattern maskPattern) {
		this.moduleCount = this.typeNumber * 4 + 17;
		this.modules = new Boolean[this.moduleCount][];

		for (int row = 0; row < this.moduleCount; row++) {
			this.modules[row] = new Boolean[this.moduleCount];

			for (int col = 0; col < this.moduleCount; col++) {
				this.modules[row][col] = null;//(col + row) % 3;
			}
		}

		this.setupPositionProbePattern(0, 0);
		this.setupPositionProbePattern(this.moduleCount - 7, 0);
		this.setupPositionProbePattern(0, this.moduleCount - 7);
		this.setupPositionAdjustPattern();
		this.setupTimingPattern();
		this.setupTypeInfo(test, maskPattern);

		if (this.typeNumber >= 7) {
			this.setupTypeNumber(test);
		}

		if (this.dataCache == null) {
			this.dataCache = QRCode.createData(this.typeNumber, this.errorCorrectLevel, this.dataList);
		}

		this.mapData(this.dataCache, maskPattern);
	}

	private void setupPositionProbePattern(int row, int col) {
		for (int r = -1; r <= 7; r++) {
			if (row + r <= -1 || this.moduleCount <= row + r) {
				continue;
			}

			for (int c = -1; c <= 7; c++) {
				if (col + c <= -1 || this.moduleCount <= col + c) {
					continue;
				}

				if ((0 <= r && r <= 6 && (c == 0 || c == 6))
						|| (0 <= c && c <= 6 && (r == 0 || r == 6))
						|| (2 <= r && r <= 4 && 2 <= c && c <= 4)) {
					this.modules[row + r][col + c] = true;
				} else {
					this.modules[row + r][col + c] = false;
				}
			}
		}
	}

	private QRMaskPattern getBestMaskPattern() {
		int minLostPoint = 0;
		QRMaskPattern pattern = QRMaskPattern.PATTERN000;

		for (QRMaskPattern i : QRMaskPattern.values()) {
			this.makeImpl(true, i);

			int lostPoint = QRUtil.getLostPoint(this);

			if (i == QRMaskPattern.PATTERN000 || minLostPoint > lostPoint) {
				minLostPoint = lostPoint;
				pattern = i;
			}
		}

		return pattern;
	}

//	private Object createMovieClip(Object target_mc, Object instance_name, Object depth) {
//
//		var qr_mc = target_mc.createEmptyMovieClip(instance_name, depth);
//		int cs = 1;
//
//		this.make();
//
//		for (int row = 0; row < this.modules.length; row++) {
//
//			int y = row * cs;
//
//			for (int col = 0; col < this.modules[row].length; col++) {
//
//				int x = col * cs;
//				Boolean dark = this.modules[row][col];
//
//				if (true == dark) {
//					qr_mc.beginFill(0, 100);
//					qr_mc.moveTo(x, y);
//					qr_mc.lineTo(x + cs, y);
//					qr_mc.lineTo(x + cs, y + cs);
//					qr_mc.lineTo(x, y + cs);
//					qr_mc.endFill();
//				}
//			}
//		}
//
//		return qr_mc;
//	}
	private void setupTimingPattern() {
		for (int r = 8; r < this.moduleCount - 8; r++) {
			if (this.modules[r][6] != null) {
				continue;
			}
			this.modules[r][6] = (r % 2 == 0);
		}

		for (int c = 8; c < this.moduleCount - 8; c++) {
			if (this.modules[6][c] != null) {
				continue;
			}
			this.modules[6][c] = (c % 2 == 0);
		}
	}

	private void setupPositionAdjustPattern() {
		int[] pos = QRUtil.getPatternPosition(this.typeNumber);

		for (int i = 0; i < pos.length; i++) {
			for (int j = 0; j < pos.length; j++) {
				int row = pos[i];
				int col = pos[j];

				if (this.modules[row][col] != null) {
					continue;
				}

				for (int r = -2; r <= 2; r++) {
					for (int c = -2; c <= 2; c++) {
						if (Math.abs(r) == 2
								|| Math.abs(c) == 2
								|| (r == 0 && c == 0)) {
							this.modules[row + r][col + c] = true;
						} else {
							this.modules[row + r][col + c] = false;
						}
					}
				}
			}
		}
	}

	private void setupTypeNumber(boolean test) {
		int bits = QRUtil.getBCHTypeNumber(this.typeNumber);
		Boolean mod;

		for (int i = 0; i < 18; i++) {
			mod = (!test && ((bits >> i) & 1) == 1);
			this.modules[(int) Math.floor(i / 3)][i % 3 + this.moduleCount - 8 - 3] = mod;
		}

		for (int x = 0; x < 18; x++) {
			mod = (!test && ((bits >> x) & 1) == 1);
			this.modules[x % 3 + this.moduleCount - 8 - 3][(int) Math.floor(x / 3)] = mod;
		}
	}

	private void setupTypeInfo(boolean test, QRMaskPattern maskPattern) {
		int data = (this.errorCorrectLevel.getLevel() << 3) | maskPattern.asInt();
		int bits = QRUtil.getBCHTypeInfo(data);
		boolean mod;

		// vertical
		for (int v = 0; v < 15; v++) {
			mod = (!test && ((bits >> v) & 1) == 1);

			if (v < 6) {
				this.modules[v][8] = mod;
			} else if (v < 8) {
				this.modules[v + 1][8] = mod;
			} else {
				this.modules[this.moduleCount - 15 + v][8] = mod;
			}
		}

		// horizontal
		for (int h = 0; h < 15; h++) {
			mod = (!test && ((bits >> h) & 1) == 1);

			if (h < 8) {
				this.modules[8][this.moduleCount - h - 1] = mod;
			} else if (h < 9) {
				this.modules[8][15 - h - 1 + 1] = mod;
			} else {
				this.modules[8][15 - h - 1] = mod;
			}
		}

		// fixed module
		this.modules[this.moduleCount - 8][8] = (!test);

	}

	private void mapData(int[] data, QRMaskPattern maskPattern) {
		int inc = -1;
		int row = this.moduleCount - 1;
		int bitIndex = 7;
		int byteIndex = 0;

		for (int col = this.moduleCount - 1; col > 0; col -= 2) {
			if (col == 6) {
				col--;
			}

			while (true) {
				for (int c = 0; c < 2; c++) {
					if (this.modules[row][col - c] == null) {
						boolean dark = false;

						if (byteIndex < data.length) {
							dark = (((data[byteIndex] >>> bitIndex) & 1) == 1);
						}

						boolean mask = QRUtil.getMask(maskPattern, row, col - c);

						if (mask) {
							dark = !dark;
						}

						this.modules[row][col - c] = dark;
						bitIndex--;

						if (bitIndex == -1) {
							byteIndex++;
							bitIndex = 7;
						}
					}
				}

				row += inc;

				if (row < 0 || this.moduleCount <= row) {
					row -= inc;
					inc = -inc;
					break;
				}
			}
		}

	}

	private static final int PAD0 = 0xEC;
	private static final int PAD1 = 0x11;

	private static int[] createData(int typeNumber, QRErrorCorrectLevel errorCorrectLevel, List<QR8bitByte> dataList) {
		List<QRRSBlock> rsBlocks = QRRSBlock.getRSBlocks(typeNumber, errorCorrectLevel);

		QRBitBuffer buffer = new QRBitBuffer();

		for (int i = 0; i < dataList.size(); i++) {
			QR8bitByte data = dataList.get(i);
			buffer.put(data.getMode().asInt(), 4);
			buffer.put(data.getLength(), QRUtil.getLengthInBits(data.getMode(), typeNumber));
			data.write(buffer);
		}

		// calc num max data.
		int totalDataCount = 0;
		for (int x = 0; x < rsBlocks.size(); x++) {
			totalDataCount += rsBlocks.get(x).dataCount;
		}

		if (buffer.getLengthInBits() > totalDataCount * 8) {
			throw new Error("code length overflow. ("
					+ buffer.getLengthInBits()
					+ ">"
					+ totalDataCount * 8
					+ ")");
		}

		// end code
		if (buffer.getLengthInBits() + 4 <= totalDataCount * 8) {
			buffer.put(0, 4);
		}

		// padding
		while (buffer.getLengthInBits() % 8 != 0) {
			buffer.putBit(false);
		}

		// padding
		while (true) {
			if (buffer.getLengthInBits() >= totalDataCount * 8) {
				break;
			}
			buffer.put(QRCode.PAD0, 8);

			if (buffer.getLengthInBits() >= totalDataCount * 8) {
				break;
			}
			buffer.put(QRCode.PAD1, 8);
		}

		return QRCode.createBytes(buffer, rsBlocks);
	}

	private static int[] createBytes(QRBitBuffer buffer, List<QRRSBlock> rsBlocks) {
		int offset = 0;

		int maxDcCount = 0;
		int maxEcCount = 0;

		int[][] dcdata = new int[rsBlocks.size()][];
		int[][] ecdata = new int[rsBlocks.size()][];

		for (int r = 0; r < rsBlocks.size(); r++) {
			int dcCount = rsBlocks.get(r).dataCount;
			int ecCount = rsBlocks.get(r).totalCount - dcCount;

			maxDcCount = Math.max(maxDcCount, dcCount);
			maxEcCount = Math.max(maxEcCount, ecCount);

			dcdata[r] = new int[dcCount];

			for (int i = 0; i < dcdata[r].length; i++) {
				dcdata[r][i] = 0xff & buffer.getBufferInt(i + offset);
			}
			offset += dcCount;

			QRPolynomial rsPoly = QRUtil.getErrorCorrectPolynomial(ecCount);
			QRPolynomial rawPoly = new QRPolynomial(dcdata[r], rsPoly.getLength() - 1);

			QRPolynomial modPoly = rawPoly.mod(rsPoly);
			ecdata[r] = new int[rsPoly.getLength() - 1];
			for (int x = 0; x < ecdata[r].length; x++) {
				int modIndex = x + modPoly.getLength() - ecdata[r].length;
				ecdata[r][x] = (modIndex >= 0) ? modPoly.get(modIndex) : 0;
			}

		}

		int totalCodeCount = 0;
		for (int y = 0; y < rsBlocks.size(); y++) {
			totalCodeCount += rsBlocks.get(y).totalCount;
		}

		int[] data = new int[totalCodeCount];
		int index = 0;

		for (int z = 0; z < maxDcCount; z++) {
			for (int s = 0; s < rsBlocks.size(); s++) {
				if (z < dcdata[s].length) {
					data[index++] = dcdata[s][z];
				}
			}
		}

		for (int xx = 0; xx < maxEcCount; xx++) {
			for (int t = 0; t < rsBlocks.size(); t++) {
				if (xx < ecdata[t].length) {
					data[index++] = ecdata[t][xx];
				}
			}
		}

		return data;

	}

}
