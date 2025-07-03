package jp.cssj.sakae.pdf.font.cid;

import jp.cssj.sakae.gc.font.Panose;
import jp.cssj.sakae.pdf.font.PdfFontSource;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: CIDFontSource.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public interface CIDFontSource extends PdfFontSource {
	/**
	 * PANOSE-1コードを返します。
	 * 
	 * @return PANOSE-1コード。
	 */
	public Panose getPanose();
}
