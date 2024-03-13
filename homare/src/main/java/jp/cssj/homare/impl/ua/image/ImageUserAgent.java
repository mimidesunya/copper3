package jp.cssj.homare.impl.ua.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileCacheImageOutputStream;

import jp.cssj.cti2.CTISession;
import jp.cssj.cti2.results.NopResults;
import jp.cssj.cti2.results.Results;
import jp.cssj.homare.impl.ua.AbstractUserAgent;
import jp.cssj.homare.impl.ua.NopVisitor;
import jp.cssj.homare.style.visitor.Visitor;
import jp.cssj.homare.ua.AbortException;
import jp.cssj.homare.ua.BrokenResultException;
import jp.cssj.homare.ua.RandomResultUserAgent;
import jp.cssj.homare.ua.props.UAProps;
import jp.cssj.resolver.MetaSource;
import jp.cssj.resolver.helpers.MetaSourceImpl;
import jp.cssj.rsr.RandomBuilder;
import jp.cssj.rsr.Sequential;
import jp.cssj.rsr.helpers.RandomBuilderOutputStream;
import jp.cssj.rsr.helpers.SequentialOutputStream;
import jp.cssj.sakae.g2d.gc.G2dGC;
import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.gc.GraphicsException;
import jp.cssj.sakae.gc.font.FontManager;
import jp.cssj.sakae.pdf.font.FontManagerImpl;

public class ImageUserAgent extends AbstractUserAgent implements RandomResultUserAgent {
	private Results results, xresults;

	protected FontManagerImpl fontManager;

	protected BufferedImage image;

	protected int page = 0;

	public void setResults(Results results) {
		this.results = results;
	}

	public void prepare(byte mode) {
		super.prepare(mode);
		switch (mode) {
		case PREPARE_MIDDLE_PASS:
			if (this.results != NopResults.SHARED_INSTANCE) {
				this.xresults = this.results;
				this.results = NopResults.SHARED_INSTANCE;
			}
			this.reset();
			break;
		case PREPARE_LAST_PASS:
			this.results = this.xresults;
			this.reset();
			break;
		}
	}

	private void reset() {
		this.image = null;
		this.fontManager = null;
		this.page = 0;
	}

	public FontManager getFontManager() {
		if (this.fontManager == null) {
			this.fontManager = new FontManagerImpl(this.getUAContext().getFontSourceManager());
		}
		return this.fontManager;
	}

	public void meta(String name, String content) {
		// ignore
	}

	public GC nextPage() {
		this.checkAbort(CTISession.ABORT_FORCE);
		final Point2D size = new Point2D.Double(this.pageWidth, this.pageHeight);
		final double ppi = UAProps.OUTPUT_IMAGE_RESOLUTION.getDouble(this);
		final double pxPerPt = ppi / 72;
		final AffineTransform at = AffineTransform.getScaleInstance(pxPerPt, pxPerPt);
		at.transform(size, size);
		final int w = (int) size.getX();
		final int h = (int) size.getY();
		this.image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		final Graphics2D g2d = (Graphics2D) this.image.getGraphics();

		// 背景クリア
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, w, h);
		g2d.setColor(Color.BLACK);
		g2d.setTransform(at);

		// オブジェクトとテキストのアンチエイリアス
		if (UAProps.OUTPUT_IMAGE_ANTIALIAS.getBoolean(this)) {
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		} else {
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		}
		return new G2dGC(g2d, this.getFontManager());
	}

	public void closePage(GC gc) throws IOException {
		super.closePage(gc);
		String mimeType = UAProps.OUTPUT_TYPE.getString(this);
		MetaSource metaSource = new MetaSourceImpl(URI.create("#" + (++this.page)), mimeType);
		RandomBuilder builder = this.results.nextBuilder(metaSource);
		try {
			OutputStream out;
			if (builder instanceof Sequential) {
				out = new SequentialOutputStream((Sequential) builder);
			} else {
				builder.addBlock();
				out = new RandomBuilderOutputStream(builder, 0);
			}
			try (FileCacheImageOutputStream iout = new FileCacheImageOutputStream(out, null)) {
				Iterator<ImageWriter> i = ImageIO.getImageWritersByMIMEType(mimeType);
				ImageWriter writer = (ImageWriter) i.next();
				try {
					writer.setOutput(iout);
					writer.write(this.image);
				} finally {
					writer.dispose();
				}
			}
			builder.finish();
		} catch (IOException e) {
			throw new GraphicsException(e);
		} finally {
			builder.dispose();
		}
		if (!this.results.hasNext()) {
			throw new AbortException(CTISession.ABORT_NORMAL);
		}
		this.checkAbort(CTISession.ABORT_NORMAL);
	}

	public void finish() throws BrokenResultException, IOException {
		super.finish();
		this.results.end();
	}

	public Visitor getVisitor(GC gc) {
		return new NopVisitor(this);
	}
}
