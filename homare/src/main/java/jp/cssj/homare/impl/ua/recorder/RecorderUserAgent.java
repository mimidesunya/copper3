package jp.cssj.homare.impl.ua.recorder;

import jp.cssj.homare.impl.ua.AbstractUserAgent;
import jp.cssj.homare.impl.ua.NopVisitor;
import jp.cssj.homare.style.visitor.Visitor;
import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.gc.RecorderGC;
import jp.cssj.sakae.gc.font.FontManager;
import jp.cssj.sakae.pdf.font.FontManagerImpl;

public class RecorderUserAgent extends AbstractUserAgent {
	private FontManagerImpl fontManager;

	public FontManager getFontManager() {
		if (this.fontManager == null) {
			this.fontManager = new FontManagerImpl(this.getUAContext().getFontSourceManager());
		}
		return this.fontManager;
	}

	public void meta(String name, String content) {
		// ignore
	}

	public GC nextPage() {
		final GC gc = new RecorderGC(this.fontManager);
		return gc;
	}

	public Visitor getVisitor(GC gc) {
		return new NopVisitor(this);
	}
}
