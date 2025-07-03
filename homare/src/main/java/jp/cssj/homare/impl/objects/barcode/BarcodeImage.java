package jp.cssj.homare.impl.objects.barcode;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import jp.cssj.homare.css.util.LengthUtils;
import jp.cssj.homare.css.value.LengthValue;
import jp.cssj.homare.message.MessageCodes;
import jp.cssj.homare.style.box.AbstractReplacedBox;
import jp.cssj.homare.style.box.content.ReplacedBoxImage;
import jp.cssj.homare.style.util.StyleUtils;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.gc.font.FontManager;
import jp.cssj.sakae.gc.font.FontMetrics;
import jp.cssj.sakae.gc.font.FontStyle;
import jp.cssj.sakae.gc.font.FontStyleImpl;
import jp.cssj.sakae.gc.image.Image;
import jp.cssj.sakae.gc.paint.Color;
import jp.cssj.sakae.gc.text.Element;
import jp.cssj.sakae.gc.text.GlyphHandler;
import jp.cssj.sakae.gc.text.Glypher;
import jp.cssj.sakae.gc.text.Quad;
import jp.cssj.sakae.gc.text.Text;
import jp.cssj.sakae.gc.text.TextImpl;

import org.krysalis.barcode4j.BarcodeDimension;
import org.krysalis.barcode4j.BarcodeGenerator;
import org.krysalis.barcode4j.TextAlignment;
import org.krysalis.barcode4j.output.AbstractCanvasProvider;
import org.krysalis.barcode4j.output.CanvasProvider;

/**
 * バーコードを描画します。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id$
 */
public class BarcodeImage implements Image, ReplacedBoxImage {
	protected final UserAgent ua;

	protected final BarcodeGenerator bg;

	protected final String message;

	protected final double upm, width, height;

	protected FontStyle fontStyle;

	protected Color color;

	public BarcodeImage(UserAgent ua, BarcodeGenerator bg, String message) {
		this.ua = ua;
		this.bg = bg;
		this.message = message;
		this.upm = LengthUtils.convert(ua, 1.0, LengthValue.UNIT_MM, LengthValue.UNIT_PT);
		double width, height;
		try {
			BarcodeDimension dim = this.bg.calcDimensions(message);
			width = dim.getWidthPlusQuiet() * this.upm;
			height = dim.getHeightPlusQuiet() * this.upm;
		} catch (Exception e) {
			width = 40;
			height = 40;
		}
		this.width = width;
		this.height = height;
	}

	public void setReplacedBox(AbstractReplacedBox box, double width, double height) {
		this.fontStyle = box.getReplacedParams().fontStyle;
		this.color = box.getReplacedParams().color;
	}

	public double getWidth() {
		return this.width;
	}

	public double getHeight() {
		return this.height;
	}

	public String getAltString() {
		return this.message;
	}

	public void drawTo(GC gc) {
		gc.begin();
		gc.transform(AffineTransform.getScaleInstance(this.upm, this.upm));
		gc.setFillPaint(this.color);
		gc.setStrokePaint(this.color);
		CanvasProvider cv = new MyCanvadProvider(gc, 0);
		try {
			this.bg.generateBarcode(cv, this.message);
		} catch (Exception e) {
			this.ua.message(MessageCodes.WARN_PLUGIN, "jp.cssj.homare.impl.objects.barcode", e.getLocalizedMessage());
			StyleUtils.drawText(gc, ua.getDefaultFontPolicy(), 5, e.getLocalizedMessage(), 3, 3, this.width - 6);
		}
		gc.end();
	}

	private class MyCanvadProvider extends AbstractCanvasProvider {
		private final GC gc;

		public MyCanvadProvider(GC gc, int orientation) {
			super(orientation);
			this.gc = gc;
		}

		public void deviceText(String text, double x, double xx, double y, String fontName, double fontSize,
				TextAlignment textAlign) {
			FontManager fm = ua.getFontManager();
			FontStyle font = new FontStyleImpl(fontStyle.getFamily(), fontSize, fontStyle.getStyle(),
					fontStyle.getWeight(), FontStyle.DIRECTION_LTR, ua.getDefaultFontPolicy());
			this.gc.begin();
			this.gc.transform(AffineTransform.getTranslateInstance(x, y));

			Glypher glypher = fm.getGlypher();
			MyGlyphHandler gh = new MyGlyphHandler();
			glypher.setGlyphHander(gh);
			glypher.fontStyle(font);
			char[] ch = text.toCharArray();
			glypher.characters(-1, ch, 0, ch.length);
			glypher.flush();

			double width = xx - x;
			double a = 0, xs = 0;
			List<Element> list = gh.buffer;
			if (textAlign == TextAlignment.TA_RIGHT) {
				a = width - gh.advance;
			} else if (textAlign == TextAlignment.TA_CENTER) {
				a = (width - gh.advance) / 2;
			} else if (textAlign == TextAlignment.TA_JUSTIFY) {
				int count = -1;
				for (int i = 0; i < list.size(); ++i) {
					Element e = (Element) list.get(i);
					switch (e.getElementType()) {
					case Element.TEXT:
						Text t = (Text) e;
						count += t.getGLen();
						break;
					case Element.QUAD:
						break;
					default:
						throw new IllegalStateException();
					}
				}
				if (count >= 2) {
					xs = (width - gh.advance) / count;
				}
			}
			for (int i = 0; i < list.size(); ++i) {
				Element e = (Element) list.get(i);
				switch (e.getElementType()) {
				case Element.TEXT:
					TextImpl t = (TextImpl) e;
					t.setLetterSpacing(xs);
					this.gc.drawText(t, a, 0);
					break;
				case Element.QUAD:
					break;
				default:
					throw new IllegalStateException();
				}
				a += e.getAdvance();
			}

			this.gc.end();
		}

		public void deviceFillRect(double x, double y, double w, double h) {
			Rectangle2D rect = new Rectangle2D.Double(x, y, w, h);
			this.gc.fill(rect);
		}
	}
}

class MyGlyphHandler implements GlyphHandler {
	List<Element> buffer = new ArrayList<Element>();
	double advance = 0;
	private TextImpl text;

	public void startTextRun(int charOffset, FontStyle fontStyle, FontMetrics fontMetrics) {
		this.text = new TextImpl(charOffset, fontStyle, fontMetrics);
	}

	public void glyph(int charOffset, char[] ch, int coff, byte clen, int gid) {
		this.advance += this.text.appendGlyph(ch, coff, clen, gid);
	}

	public void quad(Quad quad) {
		if (this.text.glen > 0) {
			this.buffer.add(this.text);
			this.text = new TextImpl(-1, this.text.fontStyle, this.text.fontMetrics);
		}
		this.buffer.add(quad);
		this.advance += quad.getAdvance();
	}

	public void endTextRun() {
		if (this.text.glen > 0) {
			this.buffer.add(this.text);
			this.text = new TextImpl(-1, this.text.fontStyle, this.text.fontMetrics);
		}
	}

	public void flush() {
		if (this.text.glen > 0) {
			this.buffer.add(this.text);
		}
	}

	public void finish() {
		this.flush();
	}
}
