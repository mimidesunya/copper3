package jp.cssj.homare.impl.ua.pdf;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.cssj.cti2.CTISession;
import jp.cssj.cti2.TranscoderException;
import jp.cssj.cti2.results.NopResults;
import jp.cssj.cti2.results.Results;
import jp.cssj.homare.HomareVersion;
import jp.cssj.homare.impl.ua.AbstractUserAgent;
import jp.cssj.homare.impl.ua.NopVisitor;
import jp.cssj.homare.message.MessageCodeUtils;
import jp.cssj.homare.message.MessageCodes;
import jp.cssj.homare.style.visitor.Visitor;
import jp.cssj.homare.ua.BrokenResultException;
import jp.cssj.homare.ua.RandomResultUserAgent;
import jp.cssj.homare.ua.props.OutputColor;
import jp.cssj.homare.ua.props.OutputPdfCompression;
import jp.cssj.homare.ua.props.OutputPdfEncryption;
import jp.cssj.homare.ua.props.OutputPdfEncryptionV4CFM;
import jp.cssj.homare.ua.props.OutputPdfImageCompression;
import jp.cssj.homare.ua.props.OutputPdfJpegImage;
import jp.cssj.homare.ua.props.OutputPdfVersion;
import jp.cssj.homare.ua.props.OutputPdfWatermarkMode;
import jp.cssj.homare.ua.props.UAProps;
import jp.cssj.resolver.MetaSource;
import jp.cssj.resolver.Source;
import jp.cssj.resolver.helpers.MetaSourceImpl;
import jp.cssj.resolver.helpers.URIHelper;
import jp.cssj.rsr.RandomBuilder;
import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.gc.GraphicsException;
import jp.cssj.sakae.gc.font.FontManager;
import jp.cssj.sakae.gc.image.Image;
import jp.cssj.sakae.gc.image.util.TransformedImage;
import jp.cssj.sakae.gc.paint.Pattern;
import jp.cssj.sakae.pdf.Attachment;
import jp.cssj.sakae.pdf.PdfGraphicsOutput;
import jp.cssj.sakae.pdf.PdfMetaInfo;
import jp.cssj.sakae.pdf.PdfOutput;
import jp.cssj.sakae.pdf.PdfPageOutput;
import jp.cssj.sakae.pdf.PdfWriter;
import jp.cssj.sakae.pdf.action.JavaScriptAction;
import jp.cssj.sakae.pdf.annot.SquareAnnot;
import jp.cssj.sakae.pdf.gc.PdfGC;
import jp.cssj.sakae.pdf.gc.PdfGroupImage;
import jp.cssj.sakae.pdf.impl.PdfWriterImpl;
import jp.cssj.sakae.pdf.params.EncryptionParams;
import jp.cssj.sakae.pdf.params.PdfParams;
import jp.cssj.sakae.pdf.params.R2Permissions;
import jp.cssj.sakae.pdf.params.R3Permissions;
import jp.cssj.sakae.pdf.params.V1EncryptionParams;
import jp.cssj.sakae.pdf.params.V2EncryptionParams;
import jp.cssj.sakae.pdf.params.V4EncryptionParams;
import jp.cssj.sakae.pdf.params.ViewerPreferences;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

public class PDFUserAgent extends AbstractUserAgent implements RandomResultUserAgent {
	private static final Logger LOG = Logger.getLogger(PDFUserAgent.class.getName());

	private Results results, xresults;
	private RandomBuilder builder, xbuilder;
	private PdfWriter pdfWriter = null, xpdfWriter = null;
	private final PdfMetaInfo metaInfo;
	private Pattern watermark = null;
	private PdfGroupImage watermarkGroup = null;

	protected PDFVisitor visitor = null;

	private boolean pageGenerated = false;

	protected PDFUserAgent() {
		this.metaInfo = new PdfMetaInfo();
		this.metaInfo.setProducer(HomareVersion.INSTANCE.longVersion);
	}

	public void setResults(Results results) {
		this.results = results;
	}

	public void prepare(byte mode) {
		super.prepare(mode);
		switch (mode) {
		case PREPARE_DOCUMENT:
			break;
		case PREPARE_MIDDLE_PASS:
			if (this.results != NopResults.SHARED_INSTANCE) {
				this.xresults = this.results;
				this.results = NopResults.SHARED_INSTANCE;
				this.xpdfWriter = this.pdfWriter;
				this.xbuilder = this.builder;
			}
			this.reset();
			break;
		case PREPARE_LAST_PASS:
			this.results = this.xresults;
			this.xresults = null;
			if (this.xpdfWriter != null) {
				this.builder = null;
			}
			this.reset();
			if (this.xpdfWriter != null) {
				this.pdfWriter = this.xpdfWriter;
				this.xpdfWriter = null;
				this.builder = this.xbuilder;
			}
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	private void reset() {
		if (this.builder != null) {
			this.builder.dispose();
			this.builder = null;
		}
		this.visitor = null;
		this.pageGenerated = false;
		this.pdfWriter = null;
	}

	public void setBoundSide(byte boundSide) {
		super.setBoundSide(boundSide);

		// 綴じ方向
		if (this.getBoundSide() != BOUND_SIDE_SINGLE && this.pdfWriter != null) {
			ViewerPreferences vp = this.pdfWriter.getParams().getViewerPreferences();
			switch (this.getBoundSide()) {
			case BOUND_SIDE_LEFT:
				vp.setDirection(ViewerPreferences.DIRECTION_L2R);
				break;
			case BOUND_SIDE_RIGHT:
				vp.setDirection(ViewerPreferences.DIRECTION_R2L);
				break;
			default:
				throw new IllegalStateException();
			}
		}
	}

	private void preparePdfWriter() throws IOException {
		if (this.pdfWriter != null) {
			return;
		}
		// PDFセットアップ
		final PdfParams params = new PdfParams();
		params.setFontSourceManager(this.getUAContext().getFontSourceManager());

		// バージョン
		switch (UAProps.OUTPUT_PDF_VERSION.getCode(this)) {
		case OutputPdfVersion.V1_2:
			params.setVersion(PdfParams.VERSION_1_2);
			break;
		case OutputPdfVersion.V1_3:
			params.setVersion(PdfParams.VERSION_1_3);
			break;
		case OutputPdfVersion.V1_4:
			params.setVersion(PdfParams.VERSION_1_4);
			break;
		case OutputPdfVersion.V1_4A1:
			params.setVersion(PdfParams.VERSION_PDFA1B);
			break;
		case OutputPdfVersion.V1_4X1:
			params.setVersion(PdfParams.VERSION_PDFX1A);
			break;
		case OutputPdfVersion.V1_5:
			params.setVersion(PdfParams.VERSION_1_5);
			break;
		case OutputPdfVersion.V1_6:
			params.setVersion(PdfParams.VERSION_1_6);
			break;
		case OutputPdfVersion.V1_7:
			params.setVersion(PdfParams.VERSION_1_7);
			break;
		default:
			throw new IllegalStateException();
		}

		// ファイルID
		String fileId = UAProps.OUTPUT_PDF_FILE_ID.getString(this);
		if (fileId != null) {
			if (fileId.length() == 32) {
				byte[] id = new byte[16];
				try {
					for (int i = 0; i < fileId.length(); i += 2) {
						String hex = fileId.substring(i, i + 2);
						id[i / 2] = (byte) (Integer.parseInt(hex, 16) & 0xFF);
					}
					params.setFileId(id);
				} catch (NumberFormatException e) {
					this.message(MessageCodes.WARN_BAD_IO_PROPERTY,
							new String[] { UAProps.OUTPUT_PDF_FILE_ID.name, fileId });
				}
			} else {
				this.message(MessageCodes.WARN_BAD_IO_PROPERTY,
						new String[] { UAProps.OUTPUT_PDF_FILE_ID.name, fileId });
			}
		}

		// 日付
		String creationDate = UAProps.OUTPUT_PDF_META_CREATION_DATE.getString(this);
		String modDate = UAProps.OUTPUT_PDF_META_MOD_DATE.getString(this);
		if (creationDate != null || modDate != null) {
			DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
			DateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			format1.setLenient(true);
			format2.setLenient(true);
			if (creationDate != null) {
				try {
					long time;
					try {
						time = format1.parse(creationDate).getTime();
					} catch (ParseException e) {
						try {
							int colon = creationDate.lastIndexOf(':');
							String s = creationDate.substring(0, colon) + creationDate.substring(colon + 1);
							time = format1.parse(s).getTime();
						} catch (Exception e2) {
							time = format2.parse(creationDate).getTime();
						}
					}
					this.metaInfo.setCreationDate(time);
				} catch (ParseException e) {
					this.message(MessageCodes.WARN_BAD_IO_PROPERTY,
							new String[] { UAProps.OUTPUT_PDF_META_CREATION_DATE.name, creationDate });
				}
			}
			if (modDate != null) {
				try {
					long time;
					try {
						time = format1.parse(modDate).getTime();
					} catch (ParseException e) {
						try {
							int colon = modDate.lastIndexOf(':');
							String s = modDate.substring(0, colon) + modDate.substring(colon + 1);
							time = format1.parse(s).getTime();
						} catch (Exception e2) {
							time = format2.parse(modDate).getTime();
						}
					}
					this.metaInfo.setModDate(time);
				} catch (ParseException e) {
					this.message(MessageCodes.WARN_BAD_IO_PROPERTY, UAProps.OUTPUT_PDF_META_MOD_DATE.name, modDate);
				}
			}
		}

		// カラー
		int color = UAProps.OUTPUT_COLOR.getCode(this);
		if (params.getVersion() == PdfParams.VERSION_PDFX1A && color == OutputColor.RGB) {
			this.message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY, UAProps.OUTPUT_COLOR.name, "rgb", "PDF/X-1a");
			color = OutputColor.CMYK;
		}
		switch (color) {
		case OutputColor.RGB:
			params.setColorMode(PdfParams.COLOR_MODE_PRESERVE);
			break;
		case OutputColor.GRAY:
			params.setColorMode(PdfParams.COLOR_MODE_GRAY);
			break;
		case OutputColor.CMYK:
			params.setColorMode(PdfParams.COLOR_MODE_CMYK);
			break;
		default:
			throw new IllegalStateException();
		}

		// 圧縮
		switch (UAProps.OUTPUT_PDF_COMPRESSION.getCode(this)) {
		case OutputPdfCompression.NONE:
			params.setCompression(PdfParams.COMPRESSION_NONE);
			break;
		case OutputPdfCompression.ASCII:
			params.setCompression(PdfParams.COMPRESSION_ASCII);
			break;
		case OutputPdfCompression.BINARY:
			params.setCompression(PdfParams.COMPRESSION_BINARY);
			break;
		default:
			throw new IllegalStateException();
		}

		// ブックマーク
		if (UAProps.OUTPUT_PDF_BOOKMARKS.getBoolean(this)) {
			params.setBookmarks(true);
		}

		// JPEG画像
		switch (UAProps.OUTPUT_PDF_JPEG_IMAGE.getCode(this)) {
		case OutputPdfJpegImage.RAW:
			params.setJpegImage(PdfParams.JPEG_IMAGE_RAW);
			break;
		case OutputPdfJpegImage.TO_FLATE:
		case OutputPdfJpegImage.TO_RECOMPRESS:
			params.setJpegImage(PdfParams.JPEG_IMAGE_RECOMPRESS);
			break;
		default:
			throw new IllegalStateException();
		}

		// JPEG圧縮
		switch (UAProps.OUTPUT_PDF_IMAGE_COMPRESSION.getCode(this)) {
		case OutputPdfImageCompression.FLATE:
			params.setImageCompression(PdfParams.IMAGE_COMPRESSION_FLATE);
			break;
		case OutputPdfImageCompression.JPEG:
			params.setImageCompression(PdfParams.IMAGE_COMPRESSION_JPEG);
			break;
		case OutputPdfImageCompression.JPEG2000:
			params.setImageCompression(PdfParams.IMAGE_COMPRESSION_JPEG2000);
			break;
		default:
			throw new IllegalStateException();
		}

		// ロスレス圧縮
		params.setImageCompressionLossless(UAProps.OUTPUT_PDF_IMAGE_COMPRESSION_LOSSLESS.getInteger(this));

		// 最大画像サイズ
		params.setMaxImageWidth(UAProps.OUTPUT_PDF_IMAGE_MAX_WIDTH.getInteger(this));
		params.setMaxImageHeight(UAProps.OUTPUT_PDF_IMAGE_MAX_HEIGHT.getInteger(this));

		// プラットフォームエンコーディング
		params.setPlatformEncoding(UAProps.OUTPUT_PDF_PLATFORM_ENCODING.getString(this));

		// 暗号化
		switch (UAProps.OUTPUT_PDF_ENCRYPTION.getCode(this)) {
		case OutputPdfEncryption.NONE:
			break;

		case OutputPdfEncryption.V1:
			// v1暗号化
			if (params.getVersion() == PdfParams.VERSION_PDFA1B) {
				this.message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY, UAProps.OUTPUT_PDF_ENCRYPTION.name, "v1",
						"PDF/A-1");
			} else if (params.getVersion() == PdfParams.VERSION_PDFX1A) {
				this.message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY, UAProps.OUTPUT_PDF_ENCRYPTION.name, "v1",
						"PDF/X-1a");
			} else {
				V1EncryptionParams v1Params = new V1EncryptionParams();
				this.applyEncryptionParams(v1Params);
				R2Permissions r2p = v1Params.getPermissions();
				this.applyR2Permissions(r2p);
				params.setEncription(v1Params);
			}
			break;

		case OutputPdfEncryption.V2:
			// v2暗号化
			if (params.getVersion() == PdfParams.VERSION_PDFA1B) {
				this.message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY, UAProps.OUTPUT_PDF_ENCRYPTION.name, "v2",
						"PDF/A-1");
			} else if (params.getVersion() == PdfParams.VERSION_PDFX1A) {
				this.message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY, UAProps.OUTPUT_PDF_ENCRYPTION.name, "v2",
						"PDF/X-1a");
			} else if (params.getVersion() >= PdfParams.VERSION_1_3) {
				V2EncryptionParams v2Params = new V2EncryptionParams();
				this.applyEncryptionParams(v2Params);
				int length = UAProps.OUTPUT_PDF_ENCRYPTION_LENGTH.getInteger(this);
				try {
					v2Params.setLength(length);
				} catch (IllegalArgumentException e) {
					this.message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY,
							UAProps.OUTPUT_PDF_ENCRYPTION_LENGTH.name, String.valueOf(length), "V2 Encryption");
				}
				R3Permissions r3p = v2Params.getPermissions();
				this.applyR2Permissions(r3p);
				this.applyR3Permissions(r3p);
				params.setEncription(v2Params);
			} else {
				this.message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY, UAProps.OUTPUT_PDF_ENCRYPTION.name, "v2",
						"1.2");
			}
			break;

		case OutputPdfEncryption.V4:
			// v4暗号化
			if (params.getVersion() == PdfParams.VERSION_PDFA1B) {
				this.message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY, UAProps.OUTPUT_PDF_ENCRYPTION.name, "v4",
						"PDF/A-1");
			} else if (params.getVersion() == PdfParams.VERSION_PDFX1A) {
				this.message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY, UAProps.OUTPUT_PDF_ENCRYPTION.name, "v4",
						"PDF/X-1a");
			} else if (params.getVersion() >= PdfParams.VERSION_1_5) {
				V4EncryptionParams v4Params = new V4EncryptionParams();
				this.applyEncryptionParams(v4Params);
				switch (UAProps.OUTPUT_PDF_ENCRYPTION_V4_CFM.getCode(this)) {
				case OutputPdfEncryptionV4CFM.V2:
					v4Params.setCfm(V4EncryptionParams.CFM_V2);
					break;
				case OutputPdfEncryptionV4CFM.AESV2:
					if (params.getVersion() >= PdfParams.VERSION_1_6) {
						v4Params.setCfm(V4EncryptionParams.CFM_AESV2);
					} else {
						this.message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY,
								UAProps.OUTPUT_PDF_ENCRYPTION_V4_CFM.name, "AESV2", "1.5");
					}
					break;
				default:
					throw new IllegalStateException();
				}
				int length = UAProps.OUTPUT_PDF_ENCRYPTION_LENGTH.getInteger(this);
				try {
					v4Params.setLength(length);
				} catch (IllegalArgumentException e) {
					this.message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY,
							UAProps.OUTPUT_PDF_ENCRYPTION_LENGTH.name, String.valueOf(length), "V4 Encryption");
				}
				R3Permissions r3p = v4Params.getPermissions();
				this.applyR2Permissions(r3p);
				this.applyR3Permissions(r3p);
				params.setEncription(v4Params);
			} else {
				this.message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY, UAProps.OUTPUT_PDF_ENCRYPTION.name, "v4",
						"1.4");
			}
			break;

		default:
			throw new IllegalStateException();
		}

		ViewerPreferences vp = params.getViewerPreferences();
		vp.setHideToolbar(UAProps.OUTPUT_PDF_VIEWER_PREFERENCES_HIDE_TOOLBAR.getBoolean(this));
		vp.setHideMenubar(UAProps.OUTPUT_PDF_VIEWER_PREFERENCES_HIDE_MENUBAR.getBoolean(this));
		vp.setHideWindowUI(UAProps.OUTPUT_PDF_VIEWER_PREFERENCES_HIDE_WINDOWUI.getBoolean(this));
		vp.setFitWindow(UAProps.OUTPUT_PDF_VIEWER_PREFERENCES_FIT_WINDOW.getBoolean(this));
		vp.setCenterWindow(UAProps.OUTPUT_PDF_VIEWER_PREFERENCES_CENTER_WINDOW.getBoolean(this));
		if (UAProps.OUTPUT_PDF_VIEWER_PREFERENCES_DISPLAY_DOC_TITLE.getBoolean(this)) {
			if (params.getVersion() >= PdfParams.VERSION_1_4) {
				vp.setDisplayDocTitle(true);
			} else {
				this.message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY,
						UAProps.OUTPUT_PDF_VIEWER_PREFERENCES_DISPLAY_DOC_TITLE.name, "true", "1.3");
			}
		}

		vp.setNonFullScreenPageMode(
				(byte) UAProps.OUTPUT_PDF_VIEWER_PREFERENCES_NON_FULL_SCREEN_PAGE_MODE.getCode(this));

		byte printScaling = (byte) UAProps.OUTPUT_PDF_VIEWER_PREFERENCES_PRINT_SCALING.getCode(this);
		if (printScaling != ViewerPreferences.PRINT_SCALING_APP_DEFAULT) {
			if (params.getVersion() >= PdfParams.VERSION_1_6) {
				vp.setPrintScaling(printScaling);
			} else {
				this.message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY,
						UAProps.OUTPUT_PDF_VIEWER_PREFERENCES_PRINT_SCALING.name,
						this.getProperty(UAProps.OUTPUT_PDF_VIEWER_PREFERENCES_PRINT_SCALING.name), "1.5");
			}
		}

		byte duplex = (byte) UAProps.OUTPUT_PDF_VIEWER_PREFERENCES_DUPLEX.getCode(this);
		if (duplex != ViewerPreferences.DUPLEX_NONE) {
			if (params.getVersion() >= PdfParams.VERSION_1_7) {
				vp.setDuplex(duplex);
			} else {
				this.message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY,
						UAProps.OUTPUT_PDF_VIEWER_PREFERENCES_DUPLEX.name,
						this.getProperty(UAProps.OUTPUT_PDF_VIEWER_PREFERENCES_DUPLEX.name), "1.6");
			}
		}

		if (UAProps.OUTPUT_PDF_VIEWER_PREFERENCES_PICK_TRAY_BY_PDF_SIZE.getBoolean(this)) {
			if (params.getVersion() >= PdfParams.VERSION_1_7) {
				vp.setPickTrayByPDFSize(true);
			} else {
				this.message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY,
						UAProps.OUTPUT_PDF_VIEWER_PREFERENCES_PICK_TRAY_BY_PDF_SIZE.name, "true", "1.6");
			}
		}

		String pageRange = UAProps.OUTPUT_PDF_VIEWER_PREFERENCES_PRINT_PAGE_RANGE.getString(this);
		if (pageRange != null) {
			if (params.getVersion() >= PdfParams.VERSION_1_7) {
				IntList ranges = new ArrayIntList();
				try {
					for (StringTokenizer st = new StringTokenizer(pageRange, ", "); st.hasMoreTokens();) {
						String token = st.nextToken();
						int hyphen = token.indexOf('-');
						if (hyphen == -1) {
							int page = Integer.parseInt(token);
							ranges.add(page);
							ranges.add(page);
						} else {
							int a = Integer.parseInt(token.substring(0, hyphen));
							int b = Integer.parseInt(token.substring(hyphen + 1));
							ranges.add(a);
							ranges.add(b);
						}
					}
					vp.setPrintPageRange(ranges.toArray());
				} catch (NumberFormatException e) {
					this.message(MessageCodes.WARN_BAD_IO_PROPERTY,
							UAProps.OUTPUT_PDF_VIEWER_PREFERENCES_PRINT_PAGE_RANGE.name, pageRange);
				}
			} else {
				this.message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY,
						UAProps.OUTPUT_PDF_VIEWER_PREFERENCES_PRINT_PAGE_RANGE.name, pageRange, "1.6");
			}
		}

		int numCopies = UAProps.OUTPUT_PDF_VIEWER_PREFERENCES_NUM_COPIES.getInteger(this);
		if (numCopies != 0) {
			if (params.getVersion() >= PdfParams.VERSION_1_7) {
				try {
					vp.setNumCopies(numCopies);
				} catch (IllegalArgumentException e) {
					this.message(MessageCodes.WARN_BAD_IO_PROPERTY,
							UAProps.OUTPUT_PDF_VIEWER_PREFERENCES_NUM_COPIES.name, String.valueOf(numCopies));
				}
			} else {
				this.message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY,
						UAProps.OUTPUT_PDF_VIEWER_PREFERENCES_NUM_COPIES.name, String.valueOf(numCopies), "1.6");
			}
		}

		String javaScript = UAProps.OUTPUT_PDF_OPEN_ACTION_JAVA_SCRIPT.getString(this);
		if (javaScript != null) {
			if (params.getVersion() >= PdfParams.VERSION_1_3) {
				params.setOpenAction(new JavaScriptAction(javaScript));
			} else {
				this.message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY,
						UAProps.OUTPUT_PDF_OPEN_ACTION_JAVA_SCRIPT.name, String.valueOf(numCopies), "1.2");
			}
		}

		MetaSource metaSource = new MetaSourceImpl(URIHelper.CURRENT_URI, "application/pdf");
		this.builder = this.results.nextBuilder(metaSource);
		this.pdfWriter = new PdfWriterImpl(this.builder, params);
		this.setBoundSide(this.getBoundSide());
	}

	private void applyEncryptionParams(EncryptionParams params) {
		params.setUserPassword(UAProps.OUTPUT_PDF_ENCRYPTION_USER_PASSWORD.getString(this));
		params.setOwnerPassword(UAProps.OUTPUT_PDF_ENCRYPTION_OWNER_PASSWORD.getString(this));
	}

	private void applyR2Permissions(R2Permissions r2p) {
		r2p.setPrint(UAProps.OUTPUT_PDF_ENCRYPTION_PERMISSIONS_PRINT.getBoolean(this));
		r2p.setModify(UAProps.OUTPUT_PDF_ENCRYPTION_PERMISSIONS_MODIFY.getBoolean(this));
		r2p.setCopy(UAProps.OUTPUT_PDF_ENCRYPTION_PERMISSIONS_COPY.getBoolean(this));
		r2p.setAdd(UAProps.OUTPUT_PDF_ENCRYPTION_PERMISSIONS_ADD.getBoolean(this));
	}

	private void applyR3Permissions(R3Permissions r3p) {
		r3p.setFill(UAProps.OUTPUT_PDF_ENCRYPTION_PERMISSIONS_FILL.getBoolean(this));
		r3p.setExtract(UAProps.OUTPUT_PDF_ENCRYPTION_PERMISSIONS_EXTRACT.getBoolean(this));
		r3p.setAssemble(UAProps.OUTPUT_PDF_ENCRYPTION_PERMISSIONS_ASSEMBLE.getBoolean(this));
		r3p.setPrintHigh(UAProps.OUTPUT_PDF_ENCRYPTION_PERMISSIONS_PRINT_HIGH.getBoolean(this));
	}

	public FontManager getFontManager() {
		try {
			this.preparePdfWriter();
		} catch (IOException e) {
			throw new GraphicsException(e);
		}
		return this.pdfWriter.getFontManager();
	}

	public void meta(String name, String content) {
		if (name.equalsIgnoreCase("author")) {
			this.metaInfo.setAuthor(content);
		} else if (name.equalsIgnoreCase("creator") || name.equalsIgnoreCase("generator")) {
			this.metaInfo.setCreator(content);
		} else if (name.equalsIgnoreCase("keywords")) {
			this.metaInfo.setKeywords(content);
		} else if (name.equalsIgnoreCase("producer")) {
			this.metaInfo.setProducer(content);
		} else if (name.equalsIgnoreCase("subject") || name.equalsIgnoreCase("description")) {
			this.metaInfo.setSubject(content);
		} else if (name.equalsIgnoreCase("title")) {
			this.message(MessageCodes.INFO_TITLE, content);
			this.metaInfo.setTitle(content);
			this.getPassContext().getSectionState().title = content;
		}
	}

	public Image getImage(Source source) throws IOException {
		this.preparePdfWriter();
		Image image;
		try {
			image = this.pdfWriter.loadImage(source);
		} catch (IOException e) {
			image = this.loadImage(source);
		}
		AffineTransform pixelToUnit = this.getPixelToUnit();
		if (!pixelToUnit.isIdentity()) {
			image = new TransformedImage(image, pixelToUnit);
		}
		return image;
	}

	public boolean isMeasurePass() {
		return this.results == NopResults.SHARED_INSTANCE;
	}

	public GC nextPage() {
		this.checkAbort(CTISession.ABORT_FORCE);
		try {
			this.preparePdfWriter();
			if (this.isMeasurePass()) {
				this.pageGenerated = true;
				return null;
			}
			double w = this.pageWidth;
			double h = this.pageHeight;
			if (w < PdfWriter.MIN_PAGE_WIDTH) {
				this.message(MessageCodes.ERROR_BAD_PAGE_SIZE, PdfWriter.MIN_PAGE_WIDTH + "(width)>",
						String.valueOf(w));
				w = PdfWriter.MIN_PAGE_WIDTH;
			}
			if (h < PdfWriter.MIN_PAGE_HEIGHT) {
				this.message(MessageCodes.ERROR_BAD_PAGE_SIZE, PdfWriter.MIN_PAGE_HEIGHT + "(height)>",
						String.valueOf(h));
				h = PdfWriter.MIN_PAGE_HEIGHT;
			}
			if (w > PdfWriter.MAX_PAGE_WIDTH) {
				this.message(MessageCodes.ERROR_BAD_PAGE_SIZE, PdfWriter.MAX_PAGE_WIDTH + "(width)>",
						String.valueOf(w));
				w = PdfWriter.MAX_PAGE_WIDTH;
			}
			if (h > PdfWriter.MAX_PAGE_HEIGHT) {
				this.message(MessageCodes.ERROR_BAD_PAGE_SIZE, PdfWriter.MAX_PAGE_HEIGHT + "(height)>",
						String.valueOf(h));
				h = PdfWriter.MAX_PAGE_HEIGHT;
			}

			// すかし
			if (this.watermark == null) {
				String uri = UAProps.OUTPUT_PDF_WATERMARK_URI.getString(this);
				if (uri != null) {
					if (this.pdfWriter.getParams().getVersion() >= PdfParams.VERSION_1_4) {
						try {
							Source source = this.resolve(URIHelper.create("UTF-8", uri));
							try {
								Image image = this.getImage(source);
								this.watermark = new Pattern(image, null);
							} finally {
								this.release(source);
							}
						} catch (Exception e) {
							LOG.log(Level.FINE, "Missing image", e);
							this.message(MessageCodes.WARN_MISSING_IMAGE, uri);
						}
					} else {
						this.message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY,
								UAProps.OUTPUT_PDF_WATERMARK_URI.name, uri, "1.3");
					}
				}
			}
			PdfGraphicsOutput page = this.pdfWriter.nextPage(w, h);
			PdfGC gc = new PdfGC(page);
			this.pageGenerated = true;
			if (this.watermark != null) {
				// 背面
				short mode = UAProps.OUTPUT_PDF_WATERMARK_MODE.getCode(this);
				if (mode == OutputPdfWatermarkMode.BACK) {
					if (this.watermarkGroup == null) {
						PdfPageOutput out = (PdfPageOutput) gc.getPDFGraphicsOutput();
						this.watermarkGroup = out.getPdfWriter().createGroupImage(this.pageWidth, this.pageHeight);
						int flags = 0;
						if (!UAProps.OUTPUT_PDF_WATERMARK_VIEW.getBoolean(this)) {
							if (this.pdfWriter.getParams().getVersion() >= PdfParams.VERSION_1_5) {
								flags |= PdfGroupImage.VIEW_OFF;
							} else {
								this.message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY,
										UAProps.OUTPUT_PDF_WATERMARK_VIEW.name, "false", "1.4");
							}
						}
						if (!UAProps.OUTPUT_PDF_WATERMARK_PRINT.getBoolean(this)) {
							if (this.pdfWriter.getParams().getVersion() >= PdfParams.VERSION_1_5) {
								flags |= PdfGroupImage.PRINT_OFF;
							} else {
								this.message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY,
										UAProps.OUTPUT_PDF_WATERMARK_PRINT.name, "false", "1.4");
							}
						}
						if (flags != 0) {
							this.watermarkGroup.setOCG(flags);
						}
						PdfGC ggc = new PdfGC(this.watermarkGroup);
						ggc.setFillPaint(this.watermark);
						double opacity = UAProps.OUTPUT_PDF_WATERMARK_OPACITY.getDouble(PDFUserAgent.this);
						if (opacity != 1) {
							if (this.pdfWriter.getParams().getVersion() == PdfParams.VERSION_PDFA1B) {
								this.message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY,
										UAProps.OUTPUT_PDF_WATERMARK_OPACITY.name, String.valueOf(opacity), "PDF/A-1");
							} else if (this.pdfWriter.getParams().getVersion() == PdfParams.VERSION_PDFX1A) {
								this.message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY,
										UAProps.OUTPUT_PDF_WATERMARK_OPACITY.name, String.valueOf(opacity), "PDF/X-1a");
							} else {
								ggc.setFillAlpha((float) opacity);
							}
						}
						Rectangle2D mask = new Rectangle2D.Double(0, 0, w, h);
						ggc.fill(mask);
						this.watermarkGroup.close();
					}
					gc.drawImage(this.watermarkGroup);
				}
			}
			return gc;
		} catch (IOException e) {
			throw new GraphicsException(e);
		}
	}

	public void closePage(final GC gc) throws IOException {
		super.closePage(gc);
		if (gc == null) {
			return;
		}
		final PdfGC pdfGc = (PdfGC) gc;
		try (final PdfPageOutput out = (PdfPageOutput) pdfGc.getPDFGraphicsOutput()) {
			if (this.watermark != null) {
				final short mode = UAProps.OUTPUT_PDF_WATERMARK_MODE.getCode(this);
				if (mode == OutputPdfWatermarkMode.FRONT) {
					// 前面
					Rectangle2D rect = new Rectangle2D.Double(0, 0, this.pageWidth, this.pageHeight);
					final AffineTransform at = gc.getTransform();
					if (at != null) {
						rect = at.createTransformedShape(rect).getBounds2D();
					}
					final SquareAnnot annot = new SquareAnnot() {
						public void writeTo(PdfOutput out, PdfPageOutput pageOut) throws IOException {
							super.writeTo(out, pageOut);

							Rectangle2D rect = this.getShape().getBounds2D();
							final PdfGroupImage group = pageOut.getPdfWriter().createGroupImage(rect.getWidth(),
									rect.getHeight());
							final PdfGC gc = new PdfGC(group);
							if (at != null) {
								AffineTransform atd = new AffineTransform();
								atd.scale(at.getScaleX(), at.getScaleY());
								gc.transform(atd);
							}

							gc.setFillPaint(PDFUserAgent.this.watermark);
							final double opacity = UAProps.OUTPUT_PDF_WATERMARK_OPACITY.getDouble(PDFUserAgent.this);
							if (opacity != 1) {
								PdfParams params = pageOut.getPdfWriter().getParams();
								if (params.getVersion() == PdfParams.VERSION_PDFA1B) {
									message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY,
											UAProps.OUTPUT_PDF_WATERMARK_OPACITY.name, String.valueOf(opacity),
											"PDF/A-1");
								} else if (params.getVersion() == PdfParams.VERSION_PDFX1A) {
									message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY,
											UAProps.OUTPUT_PDF_WATERMARK_OPACITY.name, String.valueOf(opacity),
											"PDF/X-1a");
								} else {
									gc.setFillAlpha((float) opacity);
								}
							}
							final Rectangle2D mask = new Rectangle2D.Double(0, 0, PDFUserAgent.this.pageWidth,
									PDFUserAgent.this.pageHeight);
							gc.fill(mask);
							group.close();

							// 印刷時だけ表示するフラグ
							out.writeName("F");
							int flags = 0;
							if (!UAProps.OUTPUT_PDF_WATERMARK_VIEW.getBoolean(PDFUserAgent.this)) {
								flags |= 0x20;
							}
							if (UAProps.OUTPUT_PDF_WATERMARK_PRINT.getBoolean(PDFUserAgent.this)) {
								flags |= 0x4;
							}
							out.writeInt(flags);
							out.breakBefore();

							out.writeName("AP");
							out.startHash();
							out.writeName("N");
							out.writeObjectRef(group.getObjectRef());
							out.endHash();
							out.breakBefore();
						}
					};
					annot.setShape(rect);
					try {
						out.addAnnotation(annot);
					} catch (IOException e) {
						throw new GraphicsException(e);
					}
				}
			}
		}

		// 中断チェック
		this.checkAbort(CTISession.ABORT_NORMAL);
	}

	public Visitor getVisitor(GC gc) {
		if (gc == null) {
			return new NopVisitor(this);
		}
		if (this.visitor == null) {
			this.visitor = new PDFVisitor(this);
		}
		this.visitor.nextPage((PdfGC) gc);
		return this.visitor;
	}

	public void finish() throws BrokenResultException, IOException {
		super.finish();
		if (!this.pageGenerated) {
			final short code = MessageCodes.ERROR_NO_CONTENT;
			String mes = MessageCodeUtils.toString(code, null);
			this.message(code, mes);
			throw new TranscoderException(TranscoderException.STATE_BROKEN, code, null, mes);
		}
		try {
			// メタ情報
			this.pdfWriter.getParams().setMetaInfo(this.metaInfo);

			// PDF後処理
			// ファイルの添付
			byte[] buff = new byte[8192];
			for (int i = 0;; ++i) {
				String prefix = UAProps.OUTPUT_PDF_ATTACHMENTS + i + ".";
				String uriStr = this.getProperty(prefix + "uri");
				if (uriStr == null) {
					break;
				}
				if (this.pdfWriter.getParams().getVersion() < PdfParams.VERSION_1_4) {
					this.message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY, prefix + "uri", uriStr, "1.3");
					break;
				}
				if (this.pdfWriter.getParams().getVersion() == PdfParams.VERSION_PDFA1B) {
					this.message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY, prefix + "uri", uriStr, "PDF/A-1");
					break;
				}
				if (this.pdfWriter.getParams().getVersion() == PdfParams.VERSION_PDFX1A) {
					this.message(MessageCodes.WARN_UNSUPPORTED_PDF_CAPABILITY, prefix + "uri", uriStr, "PDF/X-1a");
					break;
				}
				URI uri;
				try {
					uri = URIHelper.create(this.getDocumentContext().getEncoding(), uriStr);
				} catch (URISyntaxException e1) {
					this.message(MessageCodes.WARN_MISSING_ATTACHMENT, uriStr);
					continue;
				}
				String name = this.getProperty(prefix + "name");
				Attachment att = new Attachment();
				att.description = this.getProperty(prefix + "description");
				att.mimeType = this.getProperty(prefix + "mime-type");
				if (name == null) {
					uriStr = uri.getPath();
					int slash = uriStr.lastIndexOf('/');
					if (slash == -1) {
						name = uriStr;
					} else {
						name = uriStr.substring(slash + 1);
					}
				}
				Source attachmetSource = null;
				try {
					attachmetSource = this.resolve(uri);
				} catch (Exception e) {
					this.message(MessageCodes.WARN_MISSING_ATTACHMENT, uri.toString());
					continue;
				}
				try {
					if (att.mimeType == null) {
						att.mimeType = attachmetSource.getMimeType();
					}
					try (OutputStream out = this.pdfWriter.addAttachment(name, att);
							InputStream in = attachmetSource.getInputStream()) {
						for (int len = in.read(buff); len != -1; len = in.read(buff)) {
							out.write(buff, 0, len);
						}
					}
				} finally {
					this.release(attachmetSource);
				}
			}
			this.pdfWriter.finish();
			this.builder.finish();
		} finally {
			this.builder.dispose();
			this.builder = null;
			this.pdfWriter = null;
			this.watermark = null;
			this.watermarkGroup = null;
		}
		this.results.end();
	}

	public void dispose() {
		super.dispose();
		if (this.builder != null) {
			this.builder.dispose();
			this.builder = null;
		}
		this.pdfWriter = null;
		this.watermark = null;
		this.watermarkGroup = null;
	}
}
