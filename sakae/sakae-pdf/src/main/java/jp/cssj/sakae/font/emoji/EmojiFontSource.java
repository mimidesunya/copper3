package jp.cssj.sakae.font.emoji;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jp.cssj.sakae.font.AbstractFontSource;
import jp.cssj.sakae.font.BBox;
import jp.cssj.sakae.font.Font;
import jp.cssj.sakae.font.FontSource;
import jp.cssj.sakae.gc.font.FontStyle;
import jp.cssj.sakae.gc.font.Panose;

/**
 * 
 * @author MIYABE Tatsuhiko
 */
public class EmojiFontSource extends AbstractFontSource {
	private static final long serialVersionUID = 1L;

	private static final BBox BBOX = new BBox((short) 0, DEFAULT_DESCENT, (short) 1000, DEFAULT_ASCENT);

	protected static final Map<String, Integer> codeToFgid;
	protected static final Map<Integer, String> fgidToCode;
	static {
		Map<String, Integer> ctog = new HashMap<String, Integer>();
		Map<Integer, String> gtoc = new HashMap<Integer, String>();
		try (BufferedReader in = new BufferedReader(
				new InputStreamReader(EmojiFontSource.class.getResourceAsStream("INDEX"), "ISO8859-1"))) {
			int gid = 0;
			for (String code = in.readLine(); code != null; code = in.readLine()) {
				++gid;
				ctog.put(code, gid);
				gtoc.put(gid, code);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		codeToFgid = Collections.unmodifiableMap(ctog);
		fgidToCode = Collections.unmodifiableMap(gtoc);
	}

	public static final EmojiFontSource INSTANCES_LTR = new EmojiFontSource(FontStyle.DIRECTION_LTR);
	public static final EmojiFontSource INSTANCES_TB = new EmojiFontSource(FontStyle.DIRECTION_TB);

	private final byte direction;

	private EmojiFontSource(byte direction) {
		this.direction = direction;
	}

	public byte getDirection() {
		return this.direction;
	}

	public String getFontName() {
		return "emoji";
	}

	public BBox getBBox() {
		return BBOX;
	}

	public short getAscent() {
		return DEFAULT_ASCENT;
	}

	public short getDescent() {
		return DEFAULT_DESCENT;
	}

	public short getCapHeight() {
		return DEFAULT_CAP_HEIGHT;
	}

	public short getXHeight() {
		return DEFAULT_X_HEIGHT;
	}

	public short getSpaceAdvance() {
		return FontSource.DEFAULT_UNITS_PER_EM / 2;
	}

	public short getStemH() {
		return 0;
	}

	public short getStemV() {
		return 0;
	}

	public boolean canDisplay(int c) {
		return codeToFgid.containsKey(Integer.toHexString(c));
	}

	public Panose getPanose() {
		return null;
	}

	public Font createFont() {
		return new EmojiFont(this);
	}
}
