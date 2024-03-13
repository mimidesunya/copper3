package jp.cssj.sakae.pdf;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;

import jp.cssj.sakae.pdf.annot.Annot;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: PdfPageOutput.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public abstract class PdfPageOutput extends PdfGraphicsOutput {
	/**
	 * 
	 * @param pdfWriter
	 * @param out
	 * @param width
	 * @param height
	 * @throws IOException
	 */
	protected PdfPageOutput(PdfWriter pdfWriter, OutputStream out, double width, double height) throws IOException {
		super(pdfWriter, out, width, height);
	}

	/**
	 * アノテーションを追加します。
	 * 
	 * @param annot
	 * @throws IOException
	 */
	public abstract void addAnnotation(Annot annot) throws IOException;

	/**
	 * ドキュメントフラグメントを追加します。
	 * 
	 * @param id
	 *            文書中でユニークな名前。
	 * @param location
	 *            場所。
	 * @throws IOException
	 */
	public abstract void addFragment(String id, Point2D location) throws IOException;

	/**
	 * ブックマークの階層を開始します。
	 * <p>
	 * startBookmarkに対するendBookmarkの数は合わせる必要はありません。 ドキュメント構築完了時に閉じてない階層は自動的に閉じられます。
	 * </p>
	 * 
	 * @param title
	 * @param location
	 * @throws IOException
	 */
	public abstract void startBookmark(String title, Point2D location) throws IOException;

	/**
	 * ブックマークの階層を終了します。
	 * 
	 * @throws IOException
	 */
	public abstract void endBookmark() throws IOException;

	public abstract void setMediaBox(Rectangle2D mediaBox);

	public abstract void setCropBox(Rectangle2D cropBox);

	public abstract void setBleedBox(Rectangle2D bleedBox);

	public abstract void setTrimBox(Rectangle2D trimBox);

	public abstract void setArtBox(Rectangle2D artBox);
}
