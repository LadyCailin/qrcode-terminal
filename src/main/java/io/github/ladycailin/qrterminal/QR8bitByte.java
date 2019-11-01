package io.github.ladycailin.qrterminal;

/**
 *
 */
public class QR8bitByte {

	private final String data;
	private final QRMode mode;

	public QR8bitByte(String data) {
		this.mode = QRMode.MODE_8BIT_BYTE;
		this.data = data;
	}

	public int getLength() {
		return this.data.length();
	}

	public QRMode getMode() {
		return this.mode;
	}

	public void write(QRBitBuffer buffer) {
		for (int i = 0; i < this.data.length(); i++) {
			// not JIS ...
			buffer.put(this.data.charAt(i), 8);
		}
	}
}
