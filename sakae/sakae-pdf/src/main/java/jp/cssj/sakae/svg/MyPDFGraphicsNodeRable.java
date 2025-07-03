package jp.cssj.sakae.svg;

import java.awt.Graphics2D;
import java.io.IOException;

import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.filter.GraphicsNodeRable8Bit;

import jp.cssj.sakae.g2d.gc.BridgeGraphics2D;
import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.pdf.PdfGraphicsOutput;
import jp.cssj.sakae.pdf.gc.PdfGC;
import jp.cssj.sakae.pdf.gc.PdfGroupImage;
import jp.cssj.sakae.pdf.params.PdfParams;

class MyPDFGraphicsNodeRable extends GraphicsNodeRable8Bit {
	private final boolean forceVector;

	MyPDFGraphicsNodeRable(GraphicsNode node, boolean forceVector) {
		super(node);
		this.forceVector = forceVector;
	}

	public boolean paintRable(Graphics2D g2d) {
		if (!(g2d instanceof BridgeGraphics2D)) {
			return super.paintRable(g2d);
		}
		GC gc = ((BridgeGraphics2D) g2d).getGC();
		if (!(gc instanceof PdfGC)) {
			return super.paintRable(g2d);
		}

		PdfGC pgc = (PdfGC) gc;
		try {
			PdfGraphicsOutput pdfgo = pgc.getPDFGraphicsOutput();
			if (!this.forceVector) {
				int pdfVersion = pdfgo.getPdfWriter().getParams().getVersion();
				if (pdfVersion < PdfParams.VERSION_1_4 || pdfVersion == PdfParams.VERSION_PDFA1B
						|| pdfVersion == PdfParams.VERSION_PDFX1A) {
					// 透明化処理がサポートされない場合。
					return super.paintRable(g2d);
				}
			}

			try (PdfGroupImage image2 = pdfgo.getPdfWriter().createGroupImage(pdfgo.getWidth(), pdfgo.getHeight())) {
				PdfGC gc2 = new PdfGC(image2);
				gc2.begin();
				Graphics2D g2d2 = new BridgeGraphics2D(gc2, g2d.getDeviceConfiguration());
				GraphicsNode gn = getGraphicsNode();
				if (getUsePrimitivePaint()) {
					gn.primitivePaint(g2d2);
				} else {
					gn.paint(g2d2);
				}
				gc2.end();
				gc.drawImage(image2);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return true;
	}
}
