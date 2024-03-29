package jp.cssj.sakae.pdf.font.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jp.cssj.sakae.font.FontSource;

/**
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: MultimapUtils.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public final class MultimapUtils {
	private MultimapUtils() {
		// unused
	}

	public static void put(Map<String, Object> map, String key, FontSource e) {
		@SuppressWarnings("unchecked")
		List<FontSource> list = (List<FontSource>) map.get(key);
		if (list == null) {
			list = new ArrayList<FontSource>();
			map.put(key, list);
		}
		list.add(e);
	}

	public static void putDirect(Map<String, Object> map, String key, FontSource e) {
		FontSource[] fonts = (FontSource[]) map.get(key);
		if (fonts == null) {
			fonts = new FontSource[] { e };
		} else {
			FontSource[] dest = new FontSource[fonts.length + 1];
			System.arraycopy(fonts, 0, dest, 0, fonts.length);
			dest[fonts.length] = e;
			fonts = dest;
		}
		map.put(key, fonts);
	}

	public static FontSource[] get(Map<String, Object> map, String key) {
		return (FontSource[]) map.get(key);
	}

	public static Map<String, Object> unmodifiableMap(Map<String, Object> map) {
		for (Iterator<Map.Entry<String, Object>> i = map.entrySet().iterator(); i.hasNext();) {
			Map.Entry<String, Object> entry = i.next();
			@SuppressWarnings("unchecked")
			List<FontSource> list = (List<FontSource>) entry.getValue();
			FontSource[] fonts = (FontSource[]) list.toArray(new FontSource[list.size()]);
			entry.setValue(fonts);
		}
		return Collections.unmodifiableMap(map);
	}
}