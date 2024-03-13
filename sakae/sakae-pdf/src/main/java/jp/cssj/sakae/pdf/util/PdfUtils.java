package jp.cssj.sakae.pdf.util;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: PdfUtils.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public final class PdfUtils {
	private PdfUtils() {
		// unused
	}

	private static final byte[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
			'F' };

	public static byte[] encodeName(String s, String encoding) throws UnsupportedEncodingException {
		boolean encode = false;
		byte[] b = s.getBytes(encoding);
		for (int i = 0; i < b.length; ++i) {
			byte c = b[i];
			if (c >= '!' && c <= '~') {
				switch (c) {
				case '#':
				case '(':
				case ')':
				case '[':
				case ']':
				case '{':
				case '}':
				case '<':
				case '>':
				case '/':
				case '%':
					break;
				default:
					continue;
				}
			}
			encode = true;
			break;
		}
		if (!encode) {
			return b;
		}
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		for (int i = 0; i < b.length; ++i) {
			byte c = b[i];
			if (c >= '!' && c <= '~') {
				switch (c) {
				case '#':
				case '(':
				case ')':
				case '[':
				case ']':
				case '{':
				case '}':
				case '<':
				case '>':
				case '/':
				case '%': {
					buff.write('#');
					short h = (short) ((c >> 4) & 0x0F);
					short l = (short) (c & 0x0F);
					buff.write(HEX[h]);
					buff.write(HEX[l]);
				}
					break;
				default:
					buff.write(c);
					break;
				}
			} else {
				buff.write('#');
				short h = (short) ((c >> 4) & 0x0F);
				short l = (short) (c & 0x0F);
				buff.write(HEX[h]);
				buff.write(HEX[l]);
			}
		}
		return buff.toByteArray();
	}

	public static String decodeName(String s, String encoding) throws UnsupportedEncodingException {
		char[] ch = s.toCharArray();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (int i = 0; i < ch.length; ++i) {
			char c = ch[i];
			if (c != '#') {
				out.write(c);
			} else {
				char h = s.charAt(++i);
				char l = s.charAt(++i);
				out.write(Integer.parseInt("" + h + l, 16));
			}
		}
		return new String(out.toByteArray(), encoding);
	}
}