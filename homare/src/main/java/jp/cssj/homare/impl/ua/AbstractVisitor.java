package jp.cssj.homare.impl.ua;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.net.URI;
import java.net.URISyntaxException;

import jp.cssj.homare.css.CSSElement;
import jp.cssj.homare.css.util.LengthUtils;
import jp.cssj.homare.css.value.LengthValue;
import jp.cssj.homare.message.MessageCodes;
import jp.cssj.homare.style.box.IBox;
import jp.cssj.homare.style.box.params.ReplacedParams;
import jp.cssj.homare.style.visitor.Visitor;
import jp.cssj.homare.ua.Counter;
import jp.cssj.homare.ua.CounterScope;
import jp.cssj.homare.ua.DocumentContext;
import jp.cssj.homare.ua.ImageMap;
import jp.cssj.homare.ua.PageRef;
import jp.cssj.homare.ua.SectionState;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.homare.ua.props.UAProps;
import jp.cssj.homare.xml.Constants;
import jp.cssj.homare.xml.ext.CSSJML;
import jp.cssj.homare.xml.xhtml.XHTML;
import jp.cssj.resolver.helpers.URIHelper;

public abstract class AbstractVisitor implements Visitor {
	private static boolean isHyperlinkBox(short type) {
		switch (type) {
		case IBox.TYPE_LINE:
		case IBox.TYPE_REPLACED:
		case IBox.TYPE_INLINE:
			return true;
		}
		return false;
	}

	private static boolean isMarkupBox(short type) {
		switch (type) {
		case IBox.TYPE_PAGE:
		case IBox.TYPE_TEXT_BLOCK:
		case IBox.TYPE_LINE:
		case IBox.TYPE_TABLE:
		case IBox.TYPE_TABLE_COLUMN_GROUP:
		case IBox.TYPE_TABLE_COLUMN:
			return false;
		}
		return true;
	}

	protected final UserAgent ua;
	private Counter[] counters = null;
	private boolean processPageReference;
	private boolean hyperlinks;

	private boolean fragments;

	private boolean bookmarks;

	protected AbstractVisitor(UserAgent ua) {
		this.ua = ua;
		this.setProcessPageReference(UAProps.PROCESSING_PAGE_REFERENCES.getBoolean(this.ua));
	}

	protected abstract void addFragment(String id, Point2D location);

	protected abstract void addLink(Shape s, URI uri, CSSElement ce);

	protected abstract void endBookmark();

	private Counter[] getCounters() {
		if (this.counters == null) {
			CounterScope counter = this.ua.getPassContext().getCounterScope(0, false);
			Counter[] counters;
			if (counter == null) {
				counters = null;
			} else {
				counters = counter.copyCounters();
			}
			this.counters = counters;
		}
		return this.counters;
	}

	public boolean isBookmarks() {
		return bookmarks;
	}

	public boolean isFragments() {
		return fragments;
	}

	public boolean isHyperlinks() {
		return hyperlinks;
	}

	public boolean isProcessPageReference() {
		return processPageReference;
	}

	public void nextPage() {
		this.counters = null;
	}

	public void setBookmarks(boolean bookmarks) {
		this.bookmarks = bookmarks;
	}

	public void setFragments(boolean fragments) {
		this.fragments = fragments;
	}

	public void setHyperlinks(boolean hyperlinks) {
		this.hyperlinks = hyperlinks;
	}

	public void setProcessPageReference(boolean processPageReference) {
		this.processPageReference = processPageReference;
	}

	protected abstract void startBookmark(String title, Point2D location);

	public void startPage() {
		// ignore
	}

	public void endPage() {
		if (this.bookmarks || this.processPageReference) {
			SectionState state = this.ua.getPassContext().getSectionState();
			for (int i = 0; i < state.firstChangedSections.length; ++i) {
				state.firstChangedSections[i] = false;
				state.firstSections[i] = state.lastSections[i];
			}
		}
	}

	public void visitBox(AffineTransform transform, IBox box, double x, double y) {
		final CSSElement ce = (CSSElement) box.getParams().element;
		if (ce == null || ce.atts == null) {
			return;
		}

		final PageRef pageRef;
		if (this.processPageReference) {
			pageRef = this.ua.getUAContext().getPageRef();
		} else {
			pageRef = null;
		}

		final short type = box.getType();
		// ハイパーリンク
		if (this.hyperlinks && isHyperlinkBox(type)) {
			// Anchor tag
			String href = null;
			URI uri = null;
			try {
				href = Constants.XLINK_HREF_ATTR.getValue(ce.atts);
				if (href != null) {
					if (href.length() > 4096) {
						throw new URISyntaxException(href, "URI too long: >4096");
					}
					DocumentContext context = this.ua.getDocumentContext();
					uri = URIHelper.create(context.getEncoding(), href);
				}
			} catch (URISyntaxException e) {
				this.ua.message(MessageCodes.WARN_BAD_LINK_URI, e.getMessage());
			}
			if (uri != null) {
				double width = box.getWidth();
				double height = box.getHeight();
				Shape s = new Rectangle2D.Double(x, y, width, height);
				if (!transform.isIdentity()) {
					s = transform.createTransformedShape(s);
				}
				this.addLink(s, uri, ce);
			}
			
			if (type == IBox.TYPE_REPLACED) {
				// Image map
				String usemap = XHTML.USEMAP_ATTR.getValue(ce.atts);
				if (usemap != null && usemap.startsWith("#")) {
					usemap = usemap.substring(1);
					ImageMap imageMap = this.ua.getUAContext().getImageMaps().get(usemap);
					if (imageMap != null) {
						double f = LengthUtils.convert(this.ua, 1.0, LengthValue.UNIT_PX, LengthValue.UNIT_PT);
						AffineTransform t2 = AffineTransform.getScaleInstance(f, f);
						t2.translate(x, y);
						for (ImageMap.Area area : imageMap) {
							Shape s = area.shape;
							if (!t2.isIdentity()) {
								s = t2.createTransformedShape(s);
							}
							if (!transform.isIdentity()) {
								s = transform.createTransformedShape(s);
							}
							this.addLink(s, area.href, null);
						}
					}
				}
				
				// SVG Links
				ReplacedParams params = (ReplacedParams)box.getParams();
				ImageMap imageMap = this.ua.getUAContext().getImageMaps().remove(params.image);
				if (imageMap != null) {
					AffineTransform t2 = AffineTransform.getTranslateInstance(x, y);
					t2.scale(box.getInnerWidth() / params.image.getWidth(), box.getInnerHeight() / params.image.getHeight());
					for(ImageMap.Area link : imageMap) {
						Shape s = link.shape;
						if (!t2.isIdentity()) {
							s = t2.createTransformedShape(s);
						}
						if (!transform.isIdentity()) {
							s = transform.createTransformedShape(s);
						}
						this.addLink(s, link.href, null);
					}
				}
			}
		}

		// フラグメント
		if ((this.fragments || pageRef != null) && isMarkupBox(type)) {
			String id = XHTML.ID_ATTR.getValue(ce.atts);
			if (id != null) {
				// ページ参照を使う場合はいずれにしてもフラグメントを出す
				Point2D location = new Point2D.Double(x, y);
				if (!transform.isIdentity()) {
					location = transform.transform(location, location);
				}
				this.addFragment(id, location);
				if (pageRef != null) {
					// ページ参照
					try {
						URI uri = URIHelper.resolve(this.ua.getDocumentContext().getEncoding(),
								this.ua.getDocumentContext().getBaseURI(), "#" + id);
						pageRef.addFragment(uri, this.getCounters());
					} catch (URISyntaxException e) {
						this.ua.message(MessageCodes.WARN_BAD_LINK_URI, e.getMessage());
					}
				}
			}
		}

		// ブックマーク
		if ((this.bookmarks || pageRef != null) && isMarkupBox(type)) {
			String header = CSSJML.HEADER_ATTR.getValue(ce.atts);
			if (header != null) {
				// 見出しの処理
				try {
					int level = Integer.parseInt(header);
					SectionState state = this.ua.getPassContext().getSectionState();

					StringBuffer textBuff = new StringBuffer();
					box.getText(textBuff);
					String title;
					if (textBuff.length() == 0) {
						title = null;
					} else {
						title = textBuff.toString();
					}
					this.ua.message(MessageCodes.INFO_HEADING_TITLE, title == null ? "" : title);

					// System.out.println(level+"/"+state.sectionLevel
					// +"/"+state.sectionDepth);
					for (int j = state.sectionLevel - level; j >= 0 && state.sectionDepth > 0; --j) {
						// 見出し終了
						if (this.bookmarks) {
							this.endBookmark();
						}
						if (pageRef != null) {
							pageRef.endSection();
						}
						--state.sectionDepth;
						--state.sectionLevel;
						state.lastSections[state.sectionLevel] = null;
					}

					String ref = "cssj-header-" + (++state.sectionCount);

					Point2D location = null;
					if (this.bookmarks || pageRef != null) {
						location = new Point2D.Double(x, y);
						if (!transform.isIdentity()) {
							location = transform.transform(location, location);
						}
					}

					// 見出し開始
					if (this.bookmarks) {
						// ブックマーク
						this.startBookmark(title, location);
					}
					if (pageRef != null) {
						// ページ参照
						try {
							URI uri = URIHelper.resolve(this.ua.getDocumentContext().getEncoding(),
									this.ua.getDocumentContext().getBaseURI(), "#" + ref);
							pageRef.startSection(uri, title, this.getCounters());
							this.addFragment(ref, location);
						} catch (URISyntaxException e) {
							this.ua.message(MessageCodes.WARN_BAD_LINK_URI, e.getMessage());
						}
					}

					++state.sectionDepth;
					state.sectionLevel = level;
					if (!state.firstChangedSections[level - 1]) {
						state.firstSections[level - 1] = title;
						state.firstChangedSections[level - 1] = true;
					}
					state.lastSections[level - 1] = title;
				} catch (NumberFormatException e) {
					this.ua.message(MessageCodes.WARN_BAD_HEADER, header);
				}
			}

			if (ce.atts != null) {
				// アノテーション
				String annot = CSSJML.ANNOT_ATTR.getValue(ce.atts);
				if (annot != null) {
					this.ua.message(MessageCodes.INFO_ANNOTATION, annot);
				}
			}
		}
	}
};
