package jp.cssj.homare.style.draw;

import java.awt.Shape;
import java.awt.geom.AffineTransform;

import jp.cssj.homare.style.box.impl.PageBox;
import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.gc.GraphicsException;
import jp.cssj.sakae.gc.image.GroupImageGC;
import jp.cssj.sakae.gc.image.Image;

public abstract class AbstractDrawable implements Drawable {
	protected final Shape clip;
	protected final PageBox pageBox;
	protected final float opacity;
	protected final AffineTransform transform;

	public AbstractDrawable(final PageBox pageBox, final Shape clip, final float opacity,
			final AffineTransform transform) {
		this.pageBox = pageBox;
		this.clip = clip;
		this.opacity = opacity;
		this.transform = transform;
	}

	public final void draw(GC gc, double x, double y) throws GraphicsException {
		if (this.clip != null || !this.transform.isIdentity()) {
			gc.begin();
			if (this.clip != null) {
				gc.clip(this.clip);
			}
			if (!this.transform.isIdentity()) {
				gc.transform(this.transform);
			}
		}

		/* NoAndroid begin */
		final GC xgc;
		final GroupImageGC ggc;
		float alpha = gc.getFillAlpha();
		if (this.opacity != 1f) {
			// 透明化開始
			xgc = gc;
			ggc = gc.createGroupImage(this.pageBox.getWidth(), this.pageBox.getHeight());
			gc = ggc;
		} else {
			xgc = ggc = null;
			gc.setFillAlpha(this.opacity);
		}
		/* NoAndroid end */

		this.innerDraw(gc, x, y);

		/* NoAndroid begin */
		if (this.opacity != 1f) {
			// 透明化終了
			Image gi = ggc.finish();
			xgc.setFillAlpha(this.opacity);
			xgc.drawImage(gi);
			xgc.setFillAlpha(alpha);
			gc = xgc;
		} else {
			gc.setFillAlpha(alpha);
		}
		/* NoAndroid end */

		if (this.clip != null || !this.transform.isIdentity()) {
			gc.end();
		}
	}

	public abstract void innerDraw(GC gc, double x, double y) throws GraphicsException;
}
