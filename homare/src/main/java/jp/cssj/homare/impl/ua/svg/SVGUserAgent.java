package jp.cssj.homare.impl.ua.svg;

import java.awt.Dimension;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

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

public class SVGUserAgent extends AbstractUserAgent implements RandomResultUserAgent {
	private Results results, xresults;

	private FontManagerImpl fontManager;

	private SVGGraphics2D svgGen;

	private int page = 0;

	protected SVGUserAgent() {
		// ignore
	}

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
		this.svgGen = null;
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
		Dimension dim = new Dimension((int) this.pageWidth, (int) this.pageHeight);

		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
		Document doc = domImpl.createDocument(null, "svg", null);
		this.svgGen = new SVGGraphics2D(doc);
		this.svgGen.setSVGCanvasSize(dim);
		G2dGC gc = new G2dGC(this.svgGen, this.fontManager);
		return gc;
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
			try (Writer writer = new OutputStreamWriter(out, "UTF-8")) {
				this.svgGen.stream(writer, true);
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
