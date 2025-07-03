package jp.cssj.homare.css.property;

import java.net.URI;

import jp.cssj.homare.css.CSSStyle;
import jp.cssj.homare.css.value.Value;

/**
 * 複数のプロパティの組み合わせです。
 * 
 * <p>
 * shorthandプロパティを実装するために用います。
 * </p>
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: CompositeProperty.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class CompositeProperty implements Property {
	private final String name;
	private final URI uri;
	private final boolean important;
	private final Entry[] entries;

	public static class Entry {
		private final PrimitivePropertyInfo info;
		private final Value value;

		public Entry(PrimitivePropertyInfo info, Value value) {
			this.info = info;
			this.value = value;
		}

		public PrimitivePropertyInfo getPrimitivePropertyInfo() {
			return this.info;
		}

		public Value getValue() {
			return this.value;
		}

		public String toString() {
			return this.info + "=" + this.value;
		}
	}

	protected CompositeProperty(String name, Entry[] entries, URI uri, boolean important) {
		this.name = name;
		this.entries = entries;
		this.uri = uri;
		this.important = important;
	}

	public String getName() {
		return this.name;
	}

	public URI getURI() {
		return this.uri;
	}

	public boolean isImportant() {
		return this.important;
	}

	/**
	 * プロパティを適用します。
	 */
	public void applyProperty(CSSStyle style) {
		for (int i = 0; i < this.entries.length; ++i) {
			Entry entry = this.entries[i];
			PrimitivePropertyInfo info = entry.getPrimitivePropertyInfo();
			style.set(info.getEffectiveInfo(style), entry.getValue(),
					this.important ? CSSStyle.MODE_IMPORTANT : CSSStyle.MODE_NORMAL);
		}
	}

	public String toString() {
		StringBuffer buff = new StringBuffer(this.name + ":");
		for (int i = 0; i < this.entries.length; ++i) {
			Entry entry = this.entries[i];
			buff.append(' ');
			buff.append(entry.getValue());
		}
		buff.append((this.important ? " !" : "") + " [uri=" + this.uri + "]");
		return buff.toString();
	}
}