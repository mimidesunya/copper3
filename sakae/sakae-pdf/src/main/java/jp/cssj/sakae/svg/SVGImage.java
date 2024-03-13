package jp.cssj.sakae.svg;

import java.awt.Graphics2D;

import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.svg.SVGPreserveAspectRatio;

import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.gc.GraphicsException;
import jp.cssj.sakae.gc.image.Image;

/**
 * SVG画像です。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: SVGImage.java 1635 2023-04-03 08:16:41Z miyabe $
 */
public class SVGImage implements Image {
	protected final GraphicsNode gvtRoot;
	
	protected final SVGPreserveAspectRatio preserveAspectRatio;

	protected final double width, height;

	public SVGImage(GraphicsNode gvtRoot, double width, double height, SVGPreserveAspectRatio preserveAspectRatio) {
		this.gvtRoot = gvtRoot;
		this.width = width;
		this.height = height;
		this.preserveAspectRatio = preserveAspectRatio;
	}

	public SVGImage(GraphicsNode gvtRoot, double width, double height) {
		this(gvtRoot, width, height, null);
	}

	public GraphicsNode getNode() {
		return this.gvtRoot;
	}
	
	public SVGPreserveAspectRatio getPreserveAspectRatio() {
		return this.preserveAspectRatio;
	}

	public double getWidth() {
		return this.width;
	}

	public double getHeight() {
		return this.height;
	}

	public void drawTo(GC gc) throws GraphicsException {
		 gc.begin();
		 Graphics2D g2d = new SVGBridgeGraphics2D(gc);
		 this.gvtRoot.paint(g2d);
		 g2d.dispose();
		 gc.end();
	}

	public String getAltString() {
		return null;
	}
	
	public boolean isRasterImage() {
		return false;
	}
}
