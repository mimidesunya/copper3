package jp.cssj.cti2.helpers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.BitSet;

/**
 * URI解析の補助クラスです。 JavaのURIクラスよりも柔軟に解析します。
 * 
 * @deprecated jp.cssj.resolver.helpers.URIHelperを使ってください。
 * @author MIYABE Tatsuhiko
 * @version $Id: URIHelper.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public final class URIHelper {
	private static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
			'F' };
	private static BitSet SAFE_CHARS;
	static {
		SAFE_CHARS = new BitSet(256);
		int i;
		// 'lowalpha' rule
		for (i = 'a'; i <= 'z'; i++) {
			SAFE_CHARS.set(i);
		}
		// 'hialpha' rule
		for (i = 'A'; i <= 'Z'; i++) {
			SAFE_CHARS.set(i);
		}
		// 'digit' rule
		for (i = '0'; i <= '9'; i++) {
			SAFE_CHARS.set(i);
		}

		// 'safe' rule
		SAFE_CHARS.set('$');
		SAFE_CHARS.set('-');
		SAFE_CHARS.set('_');
		SAFE_CHARS.set('.');
		SAFE_CHARS.set('+');
		SAFE_CHARS.set('%');

		// 'extra' rule
		SAFE_CHARS.set('!');
		SAFE_CHARS.set('*');
		SAFE_CHARS.set('\'');
		SAFE_CHARS.set('(');
		SAFE_CHARS.set(')');
		SAFE_CHARS.set(',');

		// special characters common to http: file: and ftp: URLs ('fsegment'
		// and 'hsegment' rules)
		SAFE_CHARS.set('/');
		SAFE_CHARS.set(':');
		SAFE_CHARS.set(';');
		SAFE_CHARS.set('@');
		SAFE_CHARS.set('&');
		SAFE_CHARS.set('=');
		SAFE_CHARS.set('?');
		SAFE_CHARS.set('#');
	}
	private static final boolean WINDOWS = System.getProperty("os.name").startsWith("Windows");

	public static final URI CURRENT_URI = URI.create(".");

	private URIHelper() {
		// unused
	}

	private static String filter(String encoding, String uri) {
		uri = uri.trim();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			try (Writer writer = new OutputStreamWriter(out, encoding)) {
				for (int i = 0; i < uri.length(); ++i) {
					char c = uri.charAt(i);
					if (!Character.isISOControl(c)) {
						writer.write(c);
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		byte[] bytes = out.toByteArray();
		StringBuffer buff = new StringBuffer(bytes.length);
		for (int i = 0; i < bytes.length; ++i) {
			int b = bytes[i] & 0xFF;
			if (SAFE_CHARS.get(b)) {
				buff.append((char) b);
			} else {
				buff.append('%');
				buff.append(HEX[b >> 4]);
				buff.append(HEX[b & 0x0F]);
			}
		}
		uri = buff.toString();
		return uri;
	}

	private static URI filter(URI uri) throws URISyntaxException {
		String path = uri.getPath();
		if (path == null) {
			return uri;
		}
		while (path.startsWith("/../")) {
			path = path.substring(3);
			uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), path, uri.getQuery(),
					uri.getFragment());
		}
		String schema = uri.getScheme();
		if (schema != null && schema.equals("file")) {
			String ssp = uri.getSchemeSpecificPart();
			if (ssp != null && ssp.length() >= 2 && ssp.charAt(0) == '/' && ssp.charAt(1) != '/') {
				uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), "//" + path,
						uri.getQuery(), uri.getFragment());
			}
		}
		return uri;
	}

	/**
	 * URIを生成します。
	 * 
	 * @param encoding
	 *            マルチバイト文字のエンコーディング。
	 * @param href
	 *            URI文字列。
	 * @return 生成したURI。
	 * @throws URISyntaxException
	 */
	public static URI create(String encoding, String href) throws URISyntaxException {
		href = filter(encoding, href);
		URI uri = new URI(href);
		return filter(uri);
	}

	/**
	 * 相対URIを解決します。
	 * 
	 * @param encoding
	 *            マルチバイト文字のエンコーディング。
	 * @param baseURI
	 *            基底URI。
	 * @param href
	 *            相対URI文字列。
	 * @return 生成したURI。
	 * @throws URISyntaxException
	 */
	public static URI resolve(String encoding, URI baseURI, String href) throws URISyntaxException {
		href = filter(encoding, href);
		if (isWindowsDrive(href)) {
			href = "file:///" + href;
		}
		URI hrefURI = new URI(href);
		URI uri = baseURI.resolve(hrefURI);
		if (uri.getScheme() == null && baseURI.getScheme() != null) {
			uri = new URI(baseURI.getScheme(), uri.getSchemeSpecificPart(), uri.getFragment());
		}
		if ((hasWindowsDrive(baseURI) && !hrefURI.isAbsolute()) || hasWindowsDrive(hrefURI)) {
			// Windowsのファイルパスでrelativeをすると、ドライブ名のコロンが消えるバグがあるための対策。
			String path = uri.getSchemeSpecificPart();
			int start = 0;
			for (; start < path.length() && path.charAt(start) == '/'; ++start)
				;
			++start;
			if (start <= path.length() && path.charAt(start) != ':') {
				uri = new URI(uri.getScheme(), path.substring(0, start) + ':' + path.substring(start),
						uri.getFragment());
			}
		}

		uri = filter(uri);
		return uri;
	}

	public static URI resolve(String encoding, String baseURI, String href) throws URISyntaxException {
		return resolve(encoding, create(encoding, baseURI), href);
	}

	/**
	 * Windowsのドライブかどうかチェックします。
	 * 
	 * @param uri
	 * @return Windowsのドライブであればtrue
	 */
	private static boolean hasWindowsDrive(URI uri) {
		if (!WINDOWS) {
			return false;
		}
		if (!"file".equalsIgnoreCase(uri.getScheme())) {
			return false;
		}
		String path = uri.getSchemeSpecificPart();
		int start = 0;
		for (; start < path.length() && path.charAt(start) == '/'; ++start)
			;
		return start > 0 && path.length() >= start + 2 && path.charAt(start + 1) == ':'
				&& ((path.charAt(start) >= 'A' && path.charAt(start) <= 'Z')
						|| (path.charAt(start) >= 'a' && path.charAt(start) <= 'z'));
	}

	private static boolean isWindowsDrive(String href) {
		if (!WINDOWS) {
			return false;
		}
		return href.length() >= 2 && ((href.charAt(0) >= 'A' && href.charAt(0) <= 'Z')
				|| (href.charAt(0) >= 'a' && href.charAt(0) <= 'z')) && href.charAt(1) == ':';
	}
}