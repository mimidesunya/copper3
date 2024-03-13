package jp.cssj.homare.impl.ua.svg;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.URIResolver;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;

import jp.cssj.homare.ua.UserAgent;
import jp.cssj.resolver.Source;
import jp.cssj.resolver.helpers.URIHelper;

public class MyURIResolver extends URIResolver {
	protected final UserAgent ua;
	protected final SVGImageLoader loader;

	public MyURIResolver(SVGDocument doc, DocumentLoader dl, UserAgent ua, SVGImageLoader loader) {
		super(doc, dl);
		this.ua = ua;
		this.loader = loader;
	}

	public Node getNode(String uri, Element ref) throws MalformedURLException, IOException, SecurityException {
		try {
			String baseURI = getRefererBaseURI(ref);
			if (baseURI != null && baseURI.length() == 0) {
				baseURI = null;
			}
			if (baseURI == null && uri.charAt(0) == '#') {
				return getNodeByFragment(uri.substring(1), ref);
			}

			ParsedURL pURL;
			if (baseURI != null && !uri.startsWith(baseURI)) {
				pURL = new ParsedURL(baseURI, uri);
			} else {
				pURL = new ParsedURL(uri);
			}
			if (this.documentURI == null) {
				this.documentURI = this.document.getURL();
			}

			String frag = pURL.getRef();
			if ((frag != null) && (this.documentURI != null)) {
				ParsedURL pDocURL = new ParsedURL(this.documentURI);
				if (pDocURL.sameFile(pURL)) {
					return this.document.getElementById(frag);
				}
			}

			String purlStr = pURL.toString();
			if (frag != null) {
				purlStr = purlStr.substring(0, purlStr.length() - (frag.length() + 1));
			}

			try {
				Source source = this.ua.resolve(URIHelper.create("UTF-8", purlStr));
				try {
					Document doc = this.loader.loadDocument(source);
					if (frag != null) {
						return doc.getElementById(frag);
					}
					return doc;
				} finally {
					this.ua.release(source);
				}
			} catch (URISyntaxException e) {
				throw new MalformedURLException(purlStr);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}
}
