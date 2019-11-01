package io.github.ladycailin.qrterminal;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class QRTerminal {

	public static void main(String[] args) {
		System.out.println(new QRTerminal().generate(args[0],
				new QRCodeOptions().setIsSmall(false)));
	}

	private QRErrorCorrectLevel errorLevel;

	public QRTerminal() {
		this(QRErrorCorrectLevel.L);
	}

	private static final String BLACK = "\033[40m  \033[0m";
	private static final String WHITE = "\033[47m  \033[0m";

	private static String toCell(boolean isBlack) {
		return isBlack ? BLACK : WHITE;
	}

	private static String repeat(CharSequence color, int count) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < count; i++) {
			b.append(color);
		}
		return b.toString();
	}

	private static String repeat(char color, int count) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < count; i++) {
			b.append(color);
		}
		return b.toString();
	}

	private static List<Boolean> fill(int length, boolean value) {
		List<Boolean> arr = new ArrayList<>(length);
		for (int i = 0; i < length; i++) {
			arr.add(i, value);
		}
		return arr;
	}

	public QRTerminal(QRErrorCorrectLevel errorLevel) {
		this.errorLevel = errorLevel;
	}

	public static class QRCodeOptions {

		boolean isSmall = false;

		public boolean isSmall() {
			return this.isSmall;
		}

		public QRCodeOptions setIsSmall(boolean isSmall) {
			this.isSmall = isSmall;
			return this;
		}
	}

	private static final char WHITE_ALL = '\u2588';
	private static final char WHITE_BLACK = '\u2580';
	private static final char BLACK_WHITE = '\u2584';
	private static final char BLACK_ALL = ' ';

	/**
	 * Generates a QR code as a terminal-displayable string with the default settings.
	 *
	 * @param input
	 *
	 * @return
	 */
	public String generate(String input) {
		return generate(input, new QRCodeOptions());
	}

	/**
	 * Generates a QR code as a terminal-displayable string.
	 *
	 * @param input
	 * @param opts
	 * @return
	 */
	public String generate(String input, QRCodeOptions opts) {

		QRCode qrcode = new QRCode(-1, this.errorLevel);
		qrcode.addData(input);
		qrcode.make();

		StringBuilder output = new StringBuilder();
		if (opts.isSmall()) {
			boolean black = true;
			boolean white = false;
			int moduleCount = qrcode.getModuleCount();
			Boolean[][] md = QRUtil.copyBooleanArray(qrcode.getModules());
			List<List<Boolean>> moduleData = new ArrayList<>();
			for(int i = 0; i < md.length; i++) {
				List<Boolean> B = new ArrayList<>();
				Boolean[] b = md[i];
				for(int j = 0; j < b.length; j++) {
					Boolean r = b[j];
					B.add(r);
				}
				moduleData.add(B);
			}

			boolean oddRow = (moduleCount % 2) == 1;
			if (oddRow) {
				moduleData.add(fill(moduleCount, white));
			}

			String borderTop = repeat(BLACK_WHITE, moduleCount + 2);
			String borderBottom = repeat(WHITE_BLACK, moduleCount + 2);
			output.append(borderTop).append('\n');

			for (int row = 0; row < moduleCount; row += 2) {
				output.append(WHITE_ALL);

				for (int col = 0; col < moduleCount; col++) {
					if (moduleData.get(row).get(col) == white && moduleData.get(row + 1).get(col) == white) {
						output.append(WHITE_ALL);
					} else if (moduleData.get(row).get(col) == white && moduleData.get(row + 1).get(col) == black) {
						output.append(WHITE_BLACK);
					} else if (moduleData.get(row).get(col) == black && moduleData.get(row + 1).get(col) == white) {
						output.append(BLACK_WHITE);
					} else {
						output.append(BLACK_ALL);
					}
				}

				output.append(WHITE_ALL).append('\n');
			}

			if (!oddRow) {
				output.append(borderBottom);
			}
		} else {
			String border = repeat(WHITE, qrcode.getModuleCount() + 2);

			output.append(border).append('\n');
			Boolean[][] modules = qrcode.getModules();
			for(int i = 0; i < modules.length; i++) {
				output.append(WHITE);
				Boolean[] b = modules[i];
				for(int j = 0; j < b.length; j++) {
					Boolean bb = b[j];
					output.append(toCell(bb));
				}
				output.append(WHITE).append('\n');
			}
			output.append(border);
		}

		return output.toString();
	}
}
