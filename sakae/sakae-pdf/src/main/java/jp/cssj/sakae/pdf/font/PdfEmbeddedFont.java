package jp.cssj.sakae.pdf.font;

import java.awt.Shape;

import jp.cssj.sakae.font.BBox;

public interface PdfEmbeddedFont extends PdfFont {
	public String getPSName();

	public BBox getBBox();

	public String getRegistry();

	public String getOrdering();

	public int getSupplement();

	public Shape getShape(int i);

	public byte[] getCharString(int i);

	public int getGlyphCount();

	public int getCharCount();
}
