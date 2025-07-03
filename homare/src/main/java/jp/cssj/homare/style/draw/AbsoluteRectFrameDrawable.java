package jp.cssj.homare.style.draw;

import java.awt.Shape;
import java.awt.geom.AffineTransform;

import jp.cssj.homare.style.box.impl.PageBox;
import jp.cssj.homare.style.part.AbsoluteRectFrame;
import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.gc.GraphicsException;

public class AbsoluteRectFrameDrawable extends AbstractDrawable {
	protected final AbsoluteRectFrame frame;
	protected final double width, height;
	protected final Shape textClip;

	public AbsoluteRectFrameDrawable(PageBox pageBox, Shape clip, float opacity, AffineTransform transform,
			AbsoluteRectFrame frame, double width, double height, Shape textClip) {
		super(pageBox, clip, opacity, transform);
		this.frame = frame;
		this.width = width;
		this.height = height;
		this.textClip = textClip;
	}

	public void innerDraw(GC gc, double x, double y) throws GraphicsException {
		this.frame.draw(gc, x, y, this.width, this.height, this.textClip);
	}
}
