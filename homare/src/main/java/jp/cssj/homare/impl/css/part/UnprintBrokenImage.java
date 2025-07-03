package jp.cssj.homare.impl.css.part;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import jp.cssj.homare.ua.UserAgent;
import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.gc.GraphicsException;
import jp.cssj.sakae.pdf.PdfOutput;
import jp.cssj.sakae.pdf.PdfPageOutput;
import jp.cssj.sakae.pdf.annot.SquareAnnot;
import jp.cssj.sakae.pdf.gc.PdfGC;
import jp.cssj.sakae.pdf.gc.PdfGroupImage;

/**
 * 印刷には現われない、バッテン画像です。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: UnprintBrokenImage.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class UnprintBrokenImage extends BrokenImage {
	public UnprintBrokenImage(UserAgent ua, String alt) {
		super(ua, alt);
	}

	public void drawTo(GC gc) throws GraphicsException {
		if (!(gc instanceof PdfGC)) {
			return;
		}
		Rectangle2D rect = new Rectangle2D.Double(0, 0, WIDTH, HEIGHT);
		final AffineTransform at = gc.getTransform();
		if (at != null) {
			rect = at.createTransformedShape(rect).getBounds2D();
		}
		PdfPageOutput out = (PdfPageOutput) ((PdfGC) gc).getPDFGraphicsOutput();
		SquareAnnot annot = new SquareAnnot() {
			public void writeTo(PdfOutput out, PdfPageOutput pageOut) throws IOException {
				super.writeTo(out, pageOut);

				Rectangle2D rect = this.getShape().getBounds2D();
				PdfGroupImage group = pageOut.getPdfWriter().createGroupImage(rect.getWidth(), rect.getHeight());
				PdfGC gc = new PdfGC(group);
				if (at != null) {
					AffineTransform atd = new AffineTransform();
					atd.scale(at.getScaleX(), at.getScaleY());
					gc.transform(atd);
				}
				UnprintBrokenImage.super.drawTo(gc);
				group.close();

				// もし、印刷時だけ表示したいならこうする
				// out.writeName("F");
				// out.writeInt(0x24);
				// out.breakBefore();

				out.writeName("AP");
				out.startHash();
				out.writeName("N");
				out.writeObjectRef(group.getObjectRef());
				out.endHash();
				out.breakBefore();
			}
		};
		annot.setShape(rect);
		annot.setContents(this.alt);
		try {
			out.addAnnotation(annot);
		} catch (IOException e) {
			throw new GraphicsException(e);
		}
	}
}
