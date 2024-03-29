package jp.cssj.sakae.pdf;

import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.OutputStream;

import jp.cssj.sakae.gc.paint.CMYKColor;
import jp.cssj.sakae.gc.paint.Color;
import jp.cssj.sakae.gc.paint.RGBColor;
import jp.cssj.sakae.pdf.params.PdfParams;
import jp.cssj.sakae.util.ColorUtils;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: PdfGraphicsOutput.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public abstract class PdfGraphicsOutput extends PdfOutput {
	protected final double width, height;

	protected final PdfWriter pdfWriter;

	public PdfGraphicsOutput(PdfWriter pdfWriter, OutputStream out, double width, double height) throws IOException {
		super(out, pdfWriter.getParams().getPlatformEncoding());
		this.width = width;
		this.height = height;
		this.pdfWriter = pdfWriter;
	}

	/**
	 * 作成元のPDFWriterを返します。
	 * 
	 * @return
	 */
	public PdfWriter getPdfWriter() {
		return this.pdfWriter;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	public abstract void useResource(String type, String name) throws IOException;

	/**
	 * 座標を出力します。
	 * 
	 * @param x
	 * @param y
	 * @throws IOException
	 */
	public void writePosition(double x, double y) throws IOException {
		this.writeReal(x);
		this.writeReal(this.height - y);
	}

	/**
	 * Rectangle型を出力します。
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @throws IOException
	 */
	public void writeRectangle(double x1, double y1, double x2, double y2) throws IOException {
		this.startArray();
		this.writePosition(x1, y2);
		this.writePosition(x2, y1);
		this.endArray();
	}

	/**
	 * x, y, width, height形式の矩形を出力します。 <strong>これはRectangle型とは違い、配列でくくられません。
	 * </strong>
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @throws IOException
	 */
	public void writeRect(double x, double y, double width, double height) throws IOException {
		this.writePosition(x, y + height);
		this.writeReal(width);
		this.writeReal(height);
	}

	/**
	 * アフィン変換行列を出力します。
	 * 
	 * @param at
	 * @throws IOException
	 */
	public void writeTransform(AffineTransform at) throws IOException {
		AffineTransform tpdf = new AffineTransform(1, 0, 0, -1, 0, this.height);
		AffineTransform iat = new AffineTransform(at);
		iat.preConcatenate(tpdf);
		iat.concatenate(tpdf);
		this.writeReal(iat.getScaleX());
		this.writeReal(iat.getShearY());
		this.writeReal(iat.getShearX());
		this.writeReal(iat.getScaleY());
		this.writeReal(iat.getTranslateX());
		this.writeReal(iat.getTranslateY());
	}

	/**
	 * 塗りつぶし色を出力します。
	 * 
	 * @param color
	 * @throws IOException
	 */
	public void writeFillColor(Color color) throws IOException {
		if (this.getPdfWriter().getParams().getColorMode() == PdfParams.COLOR_MODE_GRAY) {
			if (color.getColorType() != Color.GRAY) {
				color = ColorUtils.toGray(color);
			}
		} else if (this.getPdfWriter().getParams().getColorMode() == PdfParams.COLOR_MODE_CMYK) {
			if (color.getColorType() != Color.CMYK) {
				color = ColorUtils.toCMYK(color);
			}
		}
		switch (color.getColorType()) {
		case Color.GRAY:
			this.writeReal(color.getComponent(0));
			this.writeOperator("g");
			break;
		case Color.RGB:
		case Color.RGBA:
			this.writeReal(color.getComponent(RGBColor.R));
			this.writeReal(color.getComponent(RGBColor.G));
			this.writeReal(color.getComponent(RGBColor.B));
			this.writeOperator("rg");
			break;
		case Color.CMYK:
			float c = color.getComponent(CMYKColor.C);
			float m = color.getComponent(CMYKColor.M);
			float y = color.getComponent(CMYKColor.Y);
			float k = color.getComponent(CMYKColor.K);
			this.writeReal(c);
			this.writeReal(m);
			this.writeReal(y);
			this.writeReal(k);
			this.writeOperator("k");
			break;
		default:
			throw new IllegalStateException();
		}
	}

	/**
	 * ストロークの色を出力します。
	 * 
	 * @param color
	 * @throws IOException
	 */
	public void writeStrokeColor(Color color) throws IOException {
		if (this.getPdfWriter().getParams().getColorMode() == PdfParams.COLOR_MODE_GRAY) {
			// グレイカラーモード
			if (color.getColorType() != Color.GRAY) {
				color = ColorUtils.toGray(color);
			}
		} else if (this.getPdfWriter().getParams().getColorMode() == PdfParams.COLOR_MODE_CMYK) {
			// CMYKカラーモード
			if (color.getColorType() != Color.CMYK) {
				color = ColorUtils.toCMYK(color);
			}
		}
		switch (color.getColorType()) {
		case Color.GRAY:
			this.writeReal(color.getComponent(0));
			this.writeOperator("G");
			break;
		case Color.RGB:
		case Color.RGBA:
			this.writeReal(color.getComponent(RGBColor.R));
			this.writeReal(color.getComponent(RGBColor.G));
			this.writeReal(color.getComponent(RGBColor.B));
			this.writeOperator("RG");
			break;
		case Color.CMYK:
			float c = color.getComponent(CMYKColor.C);
			float m = color.getComponent(CMYKColor.M);
			float y = color.getComponent(CMYKColor.Y);
			float k = color.getComponent(CMYKColor.K);
			this.writeReal(c);
			this.writeReal(m);
			this.writeReal(y);
			this.writeReal(k);
			this.writeOperator("K");
			break;
		default:
			throw new IllegalStateException();
		}
	}
}
