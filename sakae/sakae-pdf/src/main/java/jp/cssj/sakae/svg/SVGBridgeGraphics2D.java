package jp.cssj.sakae.svg;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.PatternPaint;

import jp.cssj.sakae.g2d.gc.BridgeGraphics2D;
import jp.cssj.sakae.g2d.util.G2dUtils;
import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.gc.GraphicsException;
import jp.cssj.sakae.gc.image.Image;
import jp.cssj.sakae.gc.paint.Paint;
import jp.cssj.sakae.gc.paint.Pattern;

public class SVGBridgeGraphics2D extends BridgeGraphics2D {
	public SVGBridgeGraphics2D(GC gc) throws GraphicsException {
		super(gc, SVGGraphicsConfiguration.SHARED_INSTANCE);
	}

	public void setPaint(java.awt.Paint paint) {
		if (paint == null) {
			return;
		}
		this.paint = paint;
		if (paint instanceof java.awt.Color) {
			this.foreground = (java.awt.Color) paint;
		}
		Paint spaint;
		if (paint instanceof PatternPaint) {
			PatternPaint pattern = (PatternPaint) paint;
			GraphicsNode node = pattern.getGraphicsNode();

			Rectangle2D rect = pattern.getPatternRect();
			AffineTransform nat = node.getTransform();
			nat.translate(-rect.getX(), -rect.getY());
			node.setTransform(nat);
			Image image = new SVGImage(node, rect.getWidth(), rect.getHeight());

			AffineTransform at = new AffineTransform(pattern.getPatternTransform());
			at.translate(rect.getX(), rect.getY());
			spaint = new Pattern(image, at);
		} else {
			spaint = G2dUtils.fromAwtPaint(paint);
		}
		if (spaint != null) {
			this.gc.setStrokePaint(spaint);
			this.gc.setFillPaint(spaint);
		}
	}

}
