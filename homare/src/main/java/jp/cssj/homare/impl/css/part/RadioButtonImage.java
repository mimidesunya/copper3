package jp.cssj.homare.impl.css.part;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import jp.cssj.homare.css.util.ColorValueUtils;
import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.gc.GraphicsException;
import jp.cssj.sakae.gc.image.Image;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: RadioButtonImage.java 1632 2022-06-16 01:22:06Z miyabe $
 */
public class RadioButtonImage implements Image {
	private final boolean checked, disabled;

	public RadioButtonImage(boolean checked, boolean disabled) {
		this.checked = checked;
		this.disabled = disabled;
	}

	public double getWidth() {
		return 12.0;
	}

	public double getHeight() {
		return 12.0;
	}

	public String getAltString() {
		return "○";
	}

	public void drawTo(GC gc) throws GraphicsException {
		gc.begin();

		Shape frame = new Ellipse2D.Double(2, 2, 8, 8);
		Shape check = new Ellipse2D.Double(4, 4, 4, 4);

		gc.setFillPaint((this.disabled ? ColorValueUtils.LIGHTGRAY : ColorValueUtils.WHITE).getColor());
		gc.setStrokePaint((this.disabled ? ColorValueUtils.DIMGRAY : ColorValueUtils.BLACK).getColor());
		gc.setLineWidth(1.0);
		gc.setLinePattern(GC.STROKE_SOLID);
		gc.fillDraw(frame);

		if (this.checked) {
			gc.setFillPaint((this.disabled ? ColorValueUtils.DIMGRAY : ColorValueUtils.BLACK).getColor());
			gc.fill(check);
		}

		gc.end();
	}
}
