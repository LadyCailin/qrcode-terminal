package io.github.ladycailin.qrterminal;

/**
 *
 */
public class QRPolynomial {

	private final int[] num;

	public QRPolynomial(int[] num, int shift) {
		if (num.length == 0) {
			throw new Error(num.length + "/" + shift);
		}

		int offset = 0;

		while (offset < num.length && num[offset] == 0) {
			offset++;
		}

		this.num = new int[num.length - offset + shift];
		for (int i = 0; i < num.length - offset; i++) {
			this.num[i] = num[i + offset];
		}
	}

	public int get(int index) {
		return this.num[index];
	}

	public int getLength() {
		return this.num.length;
	}

	public QRPolynomial multiply(QRPolynomial e) {
		int[] num = new int[this.getLength() + e.getLength() - 1];

		for (int i = 0; i < this.getLength(); i++) {
			for (int j = 0; j < e.getLength(); j++) {
				num[i + j] ^= QRMath.gexp(QRMath.glog(this.get(i)) + QRMath.glog(e.get(j)));
			}
		}

		return new QRPolynomial(num, 0);
	}

	public QRPolynomial mod(QRPolynomial e) {
		if (this.getLength() - e.getLength() < 0) {
			return this;
		}

		int ratio = QRMath.glog(this.get(0)) - QRMath.glog(e.get(0));

		int[] num = new int[this.getLength()];

		for (int i = 0; i < this.getLength(); i++) {
			num[i] = this.get(i);
		}

		for (int x = 0; x < e.getLength(); x++) {
			num[x] ^= QRMath.gexp(QRMath.glog(e.get(x)) + ratio);
		}

		// recursive call
		return new QRPolynomial(num, 0).mod(e);
	}
}
