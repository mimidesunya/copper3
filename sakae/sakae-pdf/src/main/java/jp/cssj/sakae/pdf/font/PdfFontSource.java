package jp.cssj.sakae.pdf.font;

import jp.cssj.sakae.font.FontSource;
import jp.cssj.sakae.pdf.ObjectRef;

public interface PdfFontSource extends FontSource {
	/**
	 * 不明なフォントです。
	 */
	public static final byte TYPE_MISSING = 0;

	/**
	 * コアフォントです。
	 */
	public static final byte TYPE_CORE = 1;

	/**
	 * 埋め込みフォントです。
	 */
	public static final byte TYPE_EMBEDDED = 2;

	/**
	 * 外部フォントです。
	 */
	public static final byte TYPE_CID_IDENTITY = 3;

	/**
	 * CID-Keyedフォントです。
	 */
	public static final byte TYPE_CID_KEYED = 4;

	/**
	 * フォントの種類を返します。
	 * 
	 * @return
	 */
	public byte getType();

	public PdfFont createFont(String name, ObjectRef fontRef);
}
