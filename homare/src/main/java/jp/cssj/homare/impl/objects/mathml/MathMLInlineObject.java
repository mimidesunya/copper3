package jp.cssj.homare.impl.objects.mathml;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import jp.cssj.homare.css.InlineObject;
import jp.cssj.homare.impl.ua.svg.SVGImageLoader;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.homare.xml.xerces.Xerces2Parser;
import jp.cssj.sakae.gc.image.Image;
import jp.cssj.sakae.gc.image.util.TransformedImage;
import net.sourceforge.jeuclid.MathMLParserSupport;
import net.sourceforge.jeuclid.context.LayoutContextImpl;
import net.sourceforge.jeuclid.layout.JEuclidView;

import org.apache.batik.dom.util.SAXDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;

public class MathMLInlineObject extends SAXDocumentFactory implements InlineObject {
	protected SVGImageLoader loader = null;

	public MathMLInlineObject() throws ParserConfigurationException {
		super(MathMLParserSupport.createDocumentBuilder().getDOMImplementation(),
				XMLResourceDescriptor.getXMLParserClassName());
		try {
			this.parser = Xerces2Parser.createXMLReader();
		} catch (Exception e) {
			// ignore
		}
		this.setValidating(false);
	}

	public Image getImage(UserAgent ua) throws IOException {
		final java.awt.Image tempimage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) tempimage.getGraphics();
		JEuclidView view = new JEuclidView(this.document, LayoutContextImpl.getDefaultLayoutContext(), g);

		Image image = new MathMLImage(view);
		double scale = ua.getFontMagnification();
		if (scale != 1.0) {
			AffineTransform at = AffineTransform.getScaleInstance(scale, scale);
			image = new TransformedImage(image, at);
		}
		return image;
	}

}
