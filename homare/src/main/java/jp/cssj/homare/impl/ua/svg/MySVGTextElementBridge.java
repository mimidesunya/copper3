package jp.cssj.homare.impl.ua.svg;

import java.awt.font.TextAttribute;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.CSSUtilities;
import org.apache.batik.bridge.SVGTextElementBridge;
import org.apache.batik.bridge.TextUtilities;
import org.apache.batik.css.engine.SVGCSSEngine;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.gvt.font.GVTFont;
import org.w3c.dom.Element;

import jp.cssj.homare.css.value.FontFamilyValue;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.sakae.font.FontMetricsImpl;
import jp.cssj.sakae.gc.font.FontFamily;
import jp.cssj.sakae.gc.font.FontFamilyList;
import jp.cssj.sakae.gc.font.FontListMetrics;
import jp.cssj.sakae.gc.font.FontManager;
import jp.cssj.sakae.gc.font.FontPolicyList;
import jp.cssj.sakae.gc.font.FontStyle;
import jp.cssj.sakae.gc.font.FontStyleImpl;

public class MySVGTextElementBridge extends SVGTextElementBridge {
	protected final UserAgent ua;
	protected final FontManager fm;
	protected final FontPolicyList defaultPolicy;
	protected final FontFamilyValue defaultFamilies;

	public MySVGTextElementBridge(UserAgent ua) {
		this.ua = ua;
		this.fm = ua.getFontManager();
		this.defaultPolicy = ua.getDefaultFontPolicy();
		this.defaultFamilies = ua.getDefaultFontFamily();
	}

	public Bridge getInstance() {
		return new MySVGTextElementBridge(this.ua);
	}

	/**
	 * This method adds all the font related properties to <tt>result</tt> It also
	 * builds a List of the GVTFonts and returns it.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected List getFontList(BridgeContext ctx, Element element, Map result) {

		// Unique value for text element - used for run identification.
		result.put(TEXT_COMPOUND_ID, new WeakReference(element));

		// Font size.
		Float fontSizeFloat = TextUtilities.convertFontSize(element);
		float fontSize = fontSizeFloat.floatValue();
		result.put(TextAttribute.SIZE, fontSizeFloat);

		// Font stretch
		result.put(TextAttribute.WIDTH, TextUtilities.convertFontStretch(element));

		// Font style
		Float postureFloat = TextUtilities.convertFontStyle(element);
		result.put(TextAttribute.POSTURE, postureFloat);

		// Font weight
		Float weightFloat = TextUtilities.convertFontWeight(element);
		result.put(TextAttribute.WEIGHT, weightFloat);

		byte direction = FontStyle.DIRECTION_LTR;
		Value v = CSSUtilities.getComputedStyle(element, SVGCSSEngine.WRITING_MODE_INDEX);
		String s = v.getStringValue();
		switch (s.charAt(0)) {
		case 'r':
			direction = FontStyle.DIRECTION_RTL;
			break;
		case 't':
			direction = FontStyle.DIRECTION_TB;
			break;
		}

		// Needed for SVG fonts (also for dynamic documents).
		result.put(TEXT_COMPOUND_DELIMITER, element);

		// make a list of GVTFont objects
		Value val = CSSUtilities.getComputedStyle(element, SVGCSSEngine.FONT_FAMILY_INDEX);
		int len = val.getLength();
		FontFamily[] families = new FontFamily[len + this.defaultFamilies.getLength()];
		for (int i = 0; i < len; i++) {
			Value it = val.item(i);
			String fontFamilyName = it.getStringValue();
			families[i] = FontFamily.create(fontFamilyName);
		}
		for (int i = 0; i < this.defaultFamilies.getLength(); ++i) {
			families[i + len] = this.defaultFamilies.get(i);
		}

		byte style;
		if (postureFloat.equals(TextAttribute.POSTURE_OBLIQUE)) {
			style = FontStyle.FONT_STYLE_ITALIC;
		} else {
			style = FontStyle.FONT_STYLE_NORMAL;
		}
		short weight;
		if (weightFloat.equals(TextAttribute.WEIGHT_EXTRA_LIGHT)) {
			weight = FontStyle.FONT_WEIGHT_100;
		} else if (weightFloat.equals(TextAttribute.WEIGHT_LIGHT)) {
			weight = FontStyle.FONT_WEIGHT_200;
		} else if (weightFloat.equals(TextAttribute.WEIGHT_DEMILIGHT)) {
			weight = FontStyle.FONT_WEIGHT_300;
		} else if (weightFloat.equals(TextAttribute.WEIGHT_REGULAR)) {
			weight = FontStyle.FONT_WEIGHT_400;
		} else if (weightFloat.equals(TextAttribute.WEIGHT_SEMIBOLD)) {
			weight = FontStyle.FONT_WEIGHT_500;
		} else if (weightFloat.equals(TextAttribute.WEIGHT_BOLD)) {
			weight = FontStyle.FONT_WEIGHT_600;
		} else if (weightFloat.equals(TextAttribute.WEIGHT_HEAVY)) {
			weight = FontStyle.FONT_WEIGHT_700;
		} else if (weightFloat.equals(TextAttribute.WEIGHT_EXTRABOLD)) {
			weight = FontStyle.FONT_WEIGHT_800;
		} else if (weightFloat.equals(TextAttribute.WEIGHT_BOLD)) {
			weight = FontStyle.FONT_WEIGHT_900;
		} else {
			weight = FontStyle.FONT_WEIGHT_400;
		}

		FontStyle fontStyle = new FontStyleImpl(new FontFamilyList(families), fontSize, style, weight, direction,
				this.defaultPolicy);
		List fontList = new ArrayList();

		FontListMetrics ms = this.fm.getFontListMetrics(fontStyle);
		for (int i = 0; i < ms.getLength(); ++i) {
			FontMetricsImpl m = (FontMetricsImpl) ms.getFontMetrics(i);
			GVTFont font = new MyGVTFont(m, fontStyle);
			fontList.add(font);
		}

		if (!ctx.isDynamic()) {
			// Only leave this in the map for dynamic documents.
			// Otherwise it will cause the whole DOM to stay when
			// we don't really need it.
			result.remove(TEXT_COMPOUND_DELIMITER);
		}
		return fontList;
	}
}
