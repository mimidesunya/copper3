package jp.cssj.homare.css.value.ext;

import jp.cssj.sakae.gc.font.FontPolicyList;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: CSSJFontPolicyValue.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class CSSJFontPolicyValue extends FontPolicyList implements ExtValue {
	private static final long serialVersionUID = 1L;

	public static final CSSJFontPolicyValue CORE_CID_KEYED_VALUE = new CSSJFontPolicyValue(
			new byte[] { FontPolicyList.FONT_POLICY_CORE, FontPolicyList.FONT_POLICY_CID_KEYED });

	public static final CSSJFontPolicyValue CORE_CID_IDENTITY_VALUE = new CSSJFontPolicyValue(
			new byte[] { FontPolicyList.FONT_POLICY_CORE, FontPolicyList.FONT_POLICY_CID_IDENTITY });

	public static final CSSJFontPolicyValue CORE_EMBEDDED_VALUE = new CSSJFontPolicyValue(
			new byte[] { FontPolicyList.FONT_POLICY_CORE, FontPolicyList.FONT_POLICY_EMBEDDED });

	public static final CSSJFontPolicyValue OUTLINES_VALUE = new CSSJFontPolicyValue(
			new byte[] { FontPolicyList.FONT_POLICY_OUTLINES, FontPolicyList.FONT_POLICY_EMBEDDED });

	public static final CSSJFontPolicyValue PDFA1_VALUE = new CSSJFontPolicyValue(
			new byte[] { FontPolicyList.FONT_POLICY_EMBEDDED });

	public CSSJFontPolicyValue(byte[] fontPolicy) {
		super(fontPolicy);
	}

	public short getValueType() {
		return TYPE_CSSJ_FONT_POLICY;
	}
}