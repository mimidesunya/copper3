package jp.cssj.sakae.font.emoji;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;

import jp.cssj.sakae.font.BBox;
import jp.cssj.sakae.font.FontSource;
import jp.cssj.sakae.font.ImageFont;
import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.gc.GraphicsException;
import jp.cssj.sakae.gc.font.util.FontUtils;
import jp.cssj.sakae.gc.text.Text;
import jp.cssj.sakae.pdf.gc.PdfGC;
import jp.cssj.sakae.pdf.gc.PdfGroupImage;
import jp.cssj.sakae.pdf.params.PdfParams;
import jp.cssj.sakae.svg.Dimension2DImpl;
import jp.cssj.sakae.svg.GVTBuilderImpl;
import jp.cssj.sakae.svg.SVGBridgeGraphics2D;
import jp.cssj.sakae.svg.UserAgentImpl;

class EmojiFont implements ImageFont {
	private static final long serialVersionUID = 2L;
	protected final EmojiFontSource source;
	protected final Map<Integer, GraphicsNode> gidToNode = new HashMap<Integer, GraphicsNode>();
	protected final Map<Integer, PdfGroupImage> gidToImage = new HashMap<Integer, PdfGroupImage>();
	protected static final Dimension2D VIEWPORT = new Dimension2DImpl(128, 128);

	public EmojiFont(EmojiFontSource source) {
		this.source = source;
	}

	public int toGID(final int c) {
		String code = Integer.toHexString(c);
		Integer fgid = EmojiFontSource.codeToFgid.get(code);
		if (fgid == null) {
			return 0;
		}
		return fgid;
	}

	public short getAdvance(int gid) {
		return 1000;
	}

	public short getWidth(int gid) {
		return 1000;
	}

	public BBox getBBox() {
		EmojiFontSource source = (EmojiFontSource) this.source;
		return source.getBBox();
	}

	public FontSource getFontSource() {
		return this.source;
	}

	public short getKerning(int sgid, int gid) {
		return 0;
	}

	public int getLigature(int gid, int cid) {
		if (gid == -1) {
			return -1;
		}
		String scode = EmojiFontSource.fgidToCode.get(gid);
		String code = scode + "_" + Integer.toHexString(cid);
		Integer fgid = EmojiFontSource.codeToFgid.get(code);
		if (fgid == null) {
			return -1;
		}
		return fgid;
	}

	public void drawTo(GC gc, Text text) throws IOException, GraphicsException {
		FontUtils.drawText(gc, this, text);
	}

	public void drawGlyphForGid(GC gc, int gid, AffineTransform at) {
		GraphicsNode gvtRoot = null;
		PdfGroupImage image = this.gidToImage.get(gid);
		if (image == null) {
			gvtRoot = this.gidToNode.get(gid);
			if (gvtRoot == null) {
				String code = EmojiFontSource.fgidToCode.get(gid);
				if (code.endsWith("_200d")) {
					code = code.substring(0, code.length() - 5);
				}
				URL url = EmojiFontSource.class.getResource("emoji_u" + code + ".svg");
				if (url == null) {
					return;
				}
				try (InputStream in = url.openStream()) {
					SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(
							XMLResourceDescriptor.getXMLParserClassName());
					SVGOMDocument doc = (SVGOMDocument) factory.createDocument(null, in);
					UserAgent ua = new UserAgentImpl(VIEWPORT);
					DocumentLoader loader = new DocumentLoader(ua);
					BridgeContext ctx = new BridgeContext(ua, loader);
					ctx.setDynamic(false);
					GVTBuilder gvt = new GVTBuilderImpl();
					gvtRoot = gvt.build(ctx, doc);
					if (gc instanceof PdfGC && ((PdfGC) gc).getPDFGraphicsOutput().getPdfWriter().getParams().getVersion() >= PdfParams.VERSION_1_4) {
						image = ((PdfGC) gc).getPDFGraphicsOutput().getPdfWriter().createGroupImage(1000, 1000);
						PdfGC gc2 = new PdfGC(image);
						gc2.transform(AffineTransform.getScaleInstance(1000.0 / VIEWPORT.getWidth(), 1000.0 / VIEWPORT.getHeight()));
						gc2.begin();
						Graphics2D g2d = new SVGBridgeGraphics2D(gc2);
						gvtRoot.paint(g2d);
						g2d.dispose();
						gc2.end();
						image.close();
						this.gidToImage.put(gid, image);
					} else {
						this.gidToNode.put(gid, gvtRoot);
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}

		gc.begin();
		if (at != null) {
			gc.transform(at);
		}
		gc.transform(AffineTransform.getTranslateInstance(0, -this.source.getAscent()));
		if (image != null) {
			gc.begin();
			gc.drawImage(image);
		} else {
			gc.transform(AffineTransform.getScaleInstance(1000.0 / VIEWPORT.getWidth(), 1000.0 / VIEWPORT.getHeight()));
			gc.begin();
			Graphics2D g2d = new SVGBridgeGraphics2D(gc);
			gvtRoot.paint(g2d);
			g2d.dispose();
		}
		gc.end();
		gc.end();
	}
}
