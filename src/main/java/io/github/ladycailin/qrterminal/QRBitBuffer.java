package io.github.ladycailin.qrterminal;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class QRBitBuffer {

	private final List<Integer> buffer;
	private int length;

	public QRBitBuffer() {
		this.buffer = new ArrayList<>();
		this.length = 0;
	}

	public int getBufferInt(int index) {
		return buffer.get(index);
	}

	public boolean get(int index) {
		int bufIndex = (int) Math.floor(index / 8);
		return ((this.buffer.get(bufIndex) >>> (7 - index % 8)) & 1) == 1;
	}

	public void put(int num, int length) {
		for (int i = 0; i < length; i++) {
			this.putBit(((num >>> (length - i - 1)) & 1) == 1);
		}
	}

	public int getLengthInBits() {
		return this.length;
	}

	public void putBit(boolean bit) {

		int bufIndex = (int) Math.floor(this.length / 8);
		if (this.buffer.size() <= bufIndex) {
			this.buffer.add(0);
		}

		if (bit) {
			this.buffer.set(bufIndex, this.buffer.get(bufIndex) | (0x80 >>> (this.length % 8)));
		}

		this.length++;
	}
}
