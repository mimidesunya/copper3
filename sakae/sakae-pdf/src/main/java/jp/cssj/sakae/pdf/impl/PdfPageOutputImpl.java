package jp.cssj.sakae.pdf.impl;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Map;

import jp.cssj.sakae.pdf.ObjectRef;
import jp.cssj.sakae.pdf.PdfFragmentOutput;
import jp.cssj.sakae.pdf.PdfPageOutput;
import jp.cssj.sakae.pdf.PdfWriter;
import jp.cssj.sakae.pdf.annot.Annot;
import jp.cssj.sakae.pdf.params.PdfParams;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: PdfPageOutputImpl.java 1565 2018-07-04 11:51:25Z miyabe $
 */
class PdfPageOutputImpl extends PdfPageOutput {
	private final PdfFragmentOutputImpl pageFlow;

	/** 現在のページオブジェクト。 */
	private final ObjectRef pageRef;

	/** 現在のページのパラメータフロー。 */
	private final PdfFragmentOutputImpl paramsFlow;

	/** 現在のページの注釈フロー。 */
	private final PdfFragmentOutputImpl annotsFlow;

	/** 現在のページに注釈があるか？ */
	private boolean hasAnnots = false;

	private Rectangle2D mediaBox, cropBox, bleedBox, trimBox, artBox;

	public PdfPageOutputImpl(PdfWriterImpl pdfWriter, ObjectRef rootPageRef, PdfFragmentOutputImpl pagesKidsFlow,
			double width, double height) throws IOException {
		super(pdfWriter, null, width, height);
		if (width < PdfWriter.MIN_PAGE_WIDTH || height < PdfWriter.MIN_PAGE_HEIGHT) {
			throw new IllegalArgumentException("ページサイズが3pt未満です: width=" + width + ",height=" + height);
		}
		if (width > PdfWriter.MAX_PAGE_WIDTH || height > PdfWriter.MAX_PAGE_HEIGHT) {
			throw new IllegalArgumentException("ページサイズが14400ptを超えています: width=" + width + ",height=" + height);
		}
		if (pdfWriter.getParams().getVersion() == PdfParams.VERSION_PDFX1A) {
			this.artBox = new Rectangle2D.Double(0, 0, width, height);
		}

		PdfFragmentOutputImpl mainFlow = pdfWriter.mainFlow;

		this.pageRef = pdfWriter.xref.nextObjectRef();
		mainFlow.startObject(this.pageRef);
		pagesKidsFlow.writeObjectRef(this.pageRef);
		mainFlow.startHash();

		mainFlow.writeName("Type");
		mainFlow.writeName("Page");
		mainFlow.lineBreak();

		this.paramsFlow = mainFlow.forkFragment();
		this.mediaBox = new Rectangle2D.Double(0, 0, width, height);

		mainFlow.writeName("Parent");
		mainFlow.writeObjectRef(rootPageRef);
		mainFlow.lineBreak();

		mainFlow.writeName("Resources");
		mainFlow.writeObjectRef(pdfWriter.pageResourceRef);
		mainFlow.lineBreak();

		mainFlow.writeName("Contents");
		ObjectRef contentsRef = pdfWriter.xref.nextObjectRef();
		mainFlow.writeObjectRef(contentsRef);
		mainFlow.lineBreak();

		this.annotsFlow = mainFlow.forkFragment();
		mainFlow.lineBreak();

		mainFlow.endHash();
		mainFlow.endObject();

		this.pageFlow = mainFlow.forkFragment();
		this.pageFlow.startObject(contentsRef);

		this.out = this.pageFlow.startStream(PdfFragmentOutput.STREAM_ASCII);
	}

	private PdfWriterImpl getPDFWriterImpl() {
		return (PdfWriterImpl) this.pdfWriter;
	}

	public PdfPageOutput getPDFPageOutput() {
		return this;
	}

	public void useResource(String type, String name) throws IOException {
		PdfWriterImpl pdfWriter = this.getPDFWriterImpl();
		ResourceFlow resourceFlow = pdfWriter.pageResourceFlow;
		if (resourceFlow.contains(name)) {
			return;
		}
		Map<String, ObjectRef> nameToResourceRef = pdfWriter.nameToResourceRef;

		ObjectRef objectRef = (ObjectRef) nameToResourceRef.get(name);
		resourceFlow.put(type, name, objectRef);
	}

	/**
	 * アノテーションを追加します。
	 */
	public void addAnnotation(Annot annot) throws IOException {
		if (this.pdfWriter.getParams().getVersion() == PdfParams.VERSION_PDFX1A) {
			throw new UnsupportedOperationException("アノテーションは PDF/X では利用できません。");
		}

		if (!this.hasAnnots) {
			this.annotsFlow.writeName("Annots");
			this.annotsFlow.startArray();
			this.hasAnnots = true;
		}
		ObjectRef annotRef = this.getPDFWriterImpl().xref.nextObjectRef();
		this.annotsFlow.writeObjectRef(annotRef);

		try (PdfFragmentOutput objectsFlow = this.getPDFWriterImpl().objectsFlow.forkFragment()) {
			objectsFlow.startObject(annotRef);
			objectsFlow.startHash();

			// 詳細出力
			annot.writeTo(objectsFlow, this);

			if (this.pdfWriter.getParams().getVersion() == PdfParams.VERSION_PDFA1B
					|| this.pdfWriter.getParams().getVersion() == PdfParams.VERSION_PDFX1A) {
				// フラグ
				objectsFlow.writeName("F");
				objectsFlow.writeInt(0x04);
				objectsFlow.lineBreak();
			}

			objectsFlow.endHash();
			objectsFlow.endObject();
		}
	}

	/**
	 * フラグメントを追加します。
	 */
	public void addFragment(String id, Point2D location) throws IOException {
		Destination dest = new Destination(this.pageRef, location.getX(), this.height - location.getY(), 0);
		this.getPDFWriterImpl().fragments.addEntry(id, dest);
	}

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
	public void startBookmark(String title, Point2D location) throws IOException {
		if (this.getPDFWriterImpl().outline != null) {
			this.getPDFWriterImpl().outline.startBookmark(this.pageRef, title, this.height, location.getX(),
					location.getY());
		}
	}

	/**
	 * ブックマークの階層を終了します。
	 * 
	 * @throws IOException
	 */
	public void endBookmark() throws IOException {
		if (this.getPDFWriterImpl().outline != null) {
			this.getPDFWriterImpl().outline.endBookmark();
		}
	}

	private void paramRect(Rectangle2D r) throws IOException {
		this.paramsFlow.startArray();
		this.paramsFlow.writeReal(r.getMinX());
		this.paramsFlow.writeReal(this.height - r.getMaxY());
		this.paramsFlow.writeReal(r.getMaxX());
		this.paramsFlow.writeReal(this.height - r.getMinY());
		this.paramsFlow.endArray();
		this.paramsFlow.lineBreak();
	}

	public void setMediaBox(Rectangle2D mediaBox) {
		if (mediaBox == null) {
			throw new NullPointerException();
		}
		this.mediaBox = mediaBox;
	}

	public void setCropBox(Rectangle2D cropBox) {
		this.cropBox = cropBox;
	}

	public void setBleedBox(Rectangle2D bleedBox) {
		if (bleedBox != null && this.pdfWriter.getParams().getVersion() < PdfParams.VERSION_1_3) {
			throw new UnsupportedOperationException("BleedBoxはPDF 1.4 以降で使用できます。");
		}
		this.bleedBox = bleedBox;
	}

	public void setTrimBox(Rectangle2D trimBox) {
		if (trimBox != null && this.pdfWriter.getParams().getVersion() < PdfParams.VERSION_1_3) {
			throw new UnsupportedOperationException("TrimBoxはPDF 1.4 以降で使用できます。");
		}
		this.trimBox = trimBox;
	}

	public void setArtBox(Rectangle2D artBox) {
		if (artBox != null && this.pdfWriter.getParams().getVersion() < PdfParams.VERSION_1_3) {
			throw new UnsupportedOperationException("ArtBoxはPDF 1.4 以降で使用できます。");
		}
		this.artBox = artBox;
	}

	public void close() throws IOException {
		super.close();

		if (this.mediaBox == null) {
			throw new IllegalStateException();
		}
		this.paramsFlow.writeName("MediaBox");
		this.paramRect(this.mediaBox);

		if (this.cropBox != null) {
			this.paramsFlow.writeName("CropBox");
			this.paramRect(this.cropBox);
		}

		if (this.bleedBox != null) {
			this.paramsFlow.writeName("BleedBox");
			this.paramRect(this.bleedBox);
		}

		if (this.trimBox != null) {
			this.paramsFlow.writeName("TrimBox");
			this.paramRect(this.trimBox);
		}

		if (this.artBox != null) {
			this.paramsFlow.writeName("ArtBox");
			this.paramRect(this.artBox);
		}

		this.paramsFlow.close();

		if (this.hasAnnots) {
			this.annotsFlow.endArray();
			this.hasAnnots = false;
		}
		this.annotsFlow.close();
		this.pageFlow.endObject();
		this.pageFlow.close();
	}
}
