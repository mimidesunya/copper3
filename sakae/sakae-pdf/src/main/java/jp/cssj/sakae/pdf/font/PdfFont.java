package jp.cssj.sakae.pdf.font;

import java.io.IOException;
import java.io.Serializable;

import jp.cssj.sakae.font.Font;
import jp.cssj.sakae.pdf.PdfFragmentOutput;
import jp.cssj.sakae.pdf.XRef;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: PdfFont.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public interface PdfFont extends Font, Serializable {
	/**
	 * PDF文書内での識別に使われるフォント名を返します。
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * フォント情報をPDFオブジェクトとして出力します。
	 * 
	 * @param out
	 * @param xref
	 * @throws IOException
	 */
	public void writeTo(PdfFragmentOutput out, XRef xref) throws IOException;
}
