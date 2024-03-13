package jp.cssj.homare.impl.ua.svg;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.text.AttributedCharacterIterator;
import java.util.List;

import org.apache.batik.bridge.StrokingTextPainter;
import org.apache.batik.bridge.TextSpanLayout;
import org.apache.batik.gvt.text.TextPaintInfo;

import jp.cssj.homare.ua.UserAgent;
import jp.cssj.sakae.g2d.gc.BridgeGraphics2D;
import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.gc.font.FontManager;
import jp.cssj.sakae.gc.font.FontStyle;
import jp.cssj.sakae.gc.text.TextLayoutHandler;
import jp.cssj.sakae.gc.text.hyphenation.Hyphenation;
import jp.cssj.sakae.gc.text.hyphenation.HyphenationBundle;
import jp.cssj.sakae.gc.text.layout.PageLayoutGlyphHandler;

class MyTextPainter extends StrokingTextPainter {
	protected final FontManager fm;
	protected final Hyphenation hyphenation;

	public MyTextPainter(UserAgent ua) {
		this.fm = ua.getFontManager();
		this.hyphenation = HyphenationBundle.getHyphenation(null);
	}

	protected void paintTextRuns(@SuppressWarnings("rawtypes") List textRuns, Graphics2D g2d) {
		// TODO 輪郭だけの描画
		// TODO SVG埋め込みフォント(Batik側の制限がある模様)
		GC gc = ((BridgeGraphics2D) g2d).getGC();
		for (int i = 0; i < textRuns.size(); i++) {
			TextRun textRun = (TextRun) textRuns.get(i);
			AttributedCharacterIterator aci = textRun.getACI();

			// 塗りの設定
			TextPaintInfo tpi = (TextPaintInfo) aci.getAttribute(StrokingTextPainter.PAINT_INFO);
			if (tpi != null) {
				if (tpi.composite != null) {
					g2d.setComposite(tpi.composite);
				}
				if (tpi.fillPaint != null) {
					g2d.setPaint(tpi.fillPaint);
				}
			}

			// フォント情報取得
			MyGVTFont font = (MyGVTFont) aci.getAttribute(GVT_FONT);
			FontStyle fontStyle = font.fontStyle;

			// 文字列抽出
			char[] ch = new char[aci.getEndIndex() - aci.getBeginIndex()];
			aci.first();
			for (int j = 0; aci.getIndex() < aci.getEndIndex(); ++j) {
				ch[j] = aci.current();
				aci.next();
			}

			// 描画
			TextSpanLayout layout = textRun.getLayout();
			Point2D position = layout.getOffset();
			gc.begin();
			double x = position.getX();
			double y = position.getY();
			AffineTransform at = AffineTransform.getTranslateInstance(x, y - fontStyle.getSize());
			gc.transform(at);
			PageLayoutGlyphHandler lineHandler = new PageLayoutGlyphHandler();
			lineHandler.setDirection(fontStyle.getDirection());
			lineHandler.setGC(gc);
			lineHandler.setLineAdvance(Double.MAX_VALUE);
			TextLayoutHandler tlf = new TextLayoutHandler(gc, this.hyphenation, lineHandler);
			tlf.setDirection(fontStyle.getDirection());
			tlf.fontStyle(fontStyle);
			tlf.characters(-1, ch, 0, ch.length);
			tlf.flush();
			lineHandler.finish();
			gc.end();
		}
	}
}
