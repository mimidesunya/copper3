package jp.cssj.cti2.examples;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;

import jp.cssj.cti2.CTIDriverManager;
import jp.cssj.cti2.CTISession;
import jp.cssj.cti2.helpers.CTISessionHelper;
import jp.cssj.resolver.helpers.MetaSourceImpl;

/**
 * クライアントから送ったデータを変換します。
 */
public class ClientResource {
	/** 接続先。 */
	private static final URI SERVER_URI = URI.create("ctip://127.0.0.1:8099/");

	/** ユーザー。 */
	private static final String USER = "user";

	/** パスワード。 */
	private static final String PASSWORD = "kappa";

	public static void main(String[] args) throws Exception {
		// 接続する
		try (CTISession session = CTIDriverManager.getSession(SERVER_URI, USER, PASSWORD)) {
			// test.pdfに結果を出力する
			File file = new File("test.pdf");
			CTISessionHelper.setResultFile(session, file);

			// リソースの送信
			try (PrintWriter out = new PrintWriter(new OutputStreamWriter(
					session.resource(new MetaSourceImpl(URI.create("style.css"), "text/html")), "UTF-8"))) {
				// CSSを出力
				out.println("p {color: Red;}");
			}

			// 出力先ストリームを取得
			try (OutputStreamWriter out = new OutputStreamWriter(
					session.transcode(new MetaSourceImpl(URI.create("."), "text/html", "MS932")), "MS932")) {
				// 文書を出力
				out.write("<html>");
				out.write("<head>");
				out.write("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>");
				out.write("<link rel='StyleSheet' type='text/css' href='style.css'>");
				out.write("<title>サンプル</title>");
				out.write("</head>");
				out.write("<body>");
				out.write("<p>Hello World! サンプル</p>");
				out.write("</body>");
				out.write("</html>");
			}
		}
	}
}