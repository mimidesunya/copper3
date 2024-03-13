package jp.cssj.homare.driver;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import jp.cssj.cti2.CTISession;
import jp.cssj.cti2.TranscoderException;
import jp.cssj.cti2.helpers.AbstractCTISession;
import jp.cssj.cti2.helpers.CTIMessageCodes;
import jp.cssj.cti2.helpers.CTIMessageHelper;
import jp.cssj.cti2.message.MessageHandler;
import jp.cssj.cti2.progress.ProgressListener;
import jp.cssj.cti2.results.Results;
import jp.cssj.homare.HomareVersion;
import jp.cssj.homare.formatter.Formatter;
import jp.cssj.homare.message.MessageCodeUtils;
import jp.cssj.homare.message.MessageCodes;
import jp.cssj.homare.ua.AbortException;
import jp.cssj.homare.ua.BrokenResultException;
import jp.cssj.homare.ua.RandomResultUserAgent;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.homare.ua.UserAgentFactory;
import jp.cssj.homare.ua.UserAgentFactory.Type;
import jp.cssj.homare.ua.props.UAProps;
import jp.cssj.plugin.PluginLoader;
import jp.cssj.resolver.MetaSource;
import jp.cssj.resolver.Source;
import jp.cssj.resolver.SourceResolver;
import jp.cssj.resolver.file.FileSource;
import jp.cssj.resolver.helpers.URIHelper;
import jp.cssj.resolver.stream.StreamSource;
import jp.cssj.sakae.font.FontSource;
import jp.cssj.sakae.font.FontSourceManager;
import jp.cssj.sakae.gc.font.FontStyle;
import jp.cssj.sakae.pdf.font.ConfigurablePdfFontSourceManager;
import jp.cssj.sakae.pdf.font.PdfFontSource;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.input.TeeInputStream;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: DirectSession.java 1577 2018-12-21 02:07:20Z miyabe $
 */
public class DirectSession extends AbstractCTISession
		implements CTISession, MessageHandler, jp.cssj.homare.message.MessageHandler {
	private static final Logger LOG = Logger.getLogger(DirectSession.class.getName());

	private static final Set<String> SPECIAL_PROPERTIES = new HashSet<String>(
			Arrays.asList(new String[] { UAProps.INPUT_INCLUDE, UAProps.INPUT_EXCLUDE }));

	/** バージョン情報のURIです。 */
	private static final URI VERSION_INFO_URI = URI.create("http://www.cssj.jp/ns/ctip/version");

	/** 出力データ形式情報のURIです。 */
	private static final URI OUTPUT_TYPES_INFO_URI = URI.create("http://www.cssj.jp/ns/ctip/output-types");

	/** 利用可能なフォント情報のURIです。 */
	private static final URI FONTS_INFO_URI = URI.create("http://www.cssj.jp/ns/ctip/fonts");

	private Results results = null;

	private ProgressListener progressListener = null;

	private MessageHandler messageHandler = CTIMessageHelper.NULL;

	private Map<String, String> props = new HashMap<String, String>();

	private final MySourceResolver resolver = new MySourceResolver();

	private class PipeThread extends Thread {
		IOException exception;
		final PipedOutputStream out;

		PipeThread(PipedOutputStream out) {
			this.out = out;
		}
	}

	private PipeThread th = null;

	private File profileFile;

	@SuppressWarnings("unchecked")
	private static final Map<File, FontSourceManager> FONT_CACHE = Collections.synchronizedMap(new LRUMap());

	private UserAgent ua;

	private boolean continuous = false;

	private boolean aborted = false;

	private boolean decodeMessage = true;

	private boolean middlePath = false;

	public DirectSession() {
		// ignore
	}

	public InputStream getServerInfo(URI uri) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			TransformerHandler handler = (TransformerHandler) SAXTransformerFactory.newInstance().newTransformer();
			handler.setResult(new StreamResult(out));
			Transformer tr = handler.getTransformer();
			tr.setOutputProperty(OutputKeys.METHOD, "xml");
			tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tr.setOutputProperty(OutputKeys.INDENT, "yes");
			AttributesImpl atts = new AttributesImpl();
			if (uri.equals(VERSION_INFO_URI)) {
				// バージョン情報
				handler.startDocument();
				handler.startElement("", "version", "version", atts);
				{
					handler.startElement("", "long-version", "long-version", atts);
					String data = HomareVersion.INSTANCE.longVersion;
					handler.characters(data.toCharArray(), 0, data.length());
					handler.endElement("", "long-version", "long-version");
				}
				{
					handler.startElement("", "name", "name", atts);
					String data = HomareVersion.INSTANCE.name;
					handler.characters(data.toCharArray(), 0, data.length());
					handler.endElement("", "name", "name");
				}
				{
					handler.startElement("", "number", "number", atts);
					String data = HomareVersion.INSTANCE.version;
					handler.characters(data.toCharArray(), 0, data.length());
					handler.endElement("", "number", "number");
				}
				{
					handler.startElement("", "build", "build", atts);
					String data = HomareVersion.INSTANCE.build;
					handler.characters(data.toCharArray(), 0, data.length());
					handler.endElement("", "build", "build");
				}
				{
					handler.startElement("", "copyrights", "copyrights", atts);
					String data = HomareVersion.INSTANCE.copyrights;
					handler.characters(data.toCharArray(), 0, data.length());
					handler.endElement("", "copyrights", "copyrights");
				}
				{
					handler.startElement("", "credits", "credits", atts);
					String data = HomareVersion.INSTANCE.credits;
					handler.characters(data.toCharArray(), 0, data.length());
					handler.endElement("", "credits", "credits");
				}
				handler.endElement("", "version", "version");
				handler.endDocument();
			} else if (uri.equals(OUTPUT_TYPES_INFO_URI)) {
				// 出力形式
				handler.startDocument();
				handler.startElement("", "output-types", "output-types", atts);
				for (Iterator<?> i = PluginLoader.getPluginLoader().plugins(UserAgentFactory.class); i.hasNext();) {
					UserAgentFactory uaf = (UserAgentFactory) i.next();
					for (Iterator<?> j = uaf.types(); j.hasNext();) {
						Type type = (Type) j.next();
						atts.addAttribute("", "name", "name", "CDATA", type.name);
						atts.addAttribute("", "mimeType", "mimeType", "CDATA", type.mimeType);
						atts.addAttribute("", "suffix", "suffix", "CDATA", type.suffix);
						handler.startElement("", "type", "type", atts);
						atts.clear();
						handler.endElement("", "type", "type");
					}
				}
				handler.endElement("", "output-types", "output-types");
				handler.endDocument();
			} else if (uri.equals(FONTS_INFO_URI)) {
				// フォント
				final FontSourceManager fsm = this.getFontSourceManager();
				FontSource[] fonts = fsm.lookup(null);

				handler.startDocument();
				handler.startElement("", "fonts", "fonts", atts);
				for (int i = 0; i < fonts.length; ++i) {
					FontSource font = fonts[i];
					atts.addAttribute("", "name", "name", "CDATA", font.getFontName());
					if (font.isItalic()) {
						atts.addAttribute("", "italic", "italic", "CDATA", "true");
					}
					atts.addAttribute("", "weight", "weight", "CDATA", String.valueOf(font.getWeight()));
					if (font instanceof PdfFontSource) {
						String typeStr;
						byte type = ((PdfFontSource) font).getType();
						switch (type) {
						case PdfFontSource.TYPE_MISSING:
							typeStr = "missing";
							break;
						case PdfFontSource.TYPE_EMBEDDED:
							typeStr = "embedded";
							break;
						case PdfFontSource.TYPE_CORE:
							typeStr = "core";
							break;
						case PdfFontSource.TYPE_CID_KEYED:
							typeStr = "cid-keyed";
							break;
						case PdfFontSource.TYPE_CID_IDENTITY:
							typeStr = "cid-identity";
							break;
						default:
							throw new IllegalStateException();
						}
						atts.addAttribute("", "type", "type", "CDATA", String.valueOf(typeStr));
					}

					String directionStr;
					byte direction = font.getDirection();
					switch (direction) {
					case FontStyle.DIRECTION_LTR:
						directionStr = "ltr";
						break;
					case FontStyle.DIRECTION_RTL:
						directionStr = "rtl";
						break;
					case FontStyle.DIRECTION_TB:
						directionStr = "tb";
						break;
					default:
						throw new IllegalStateException();
					}
					atts.addAttribute("", "direction", "direction", "CDATA", String.valueOf(directionStr));

					handler.startElement("", "font", "font", atts);
					atts.clear();

					String[] aliases = font.getAliases();
					for (int j = 0; j < aliases.length; ++j) {
						atts.addAttribute("", "name", "name", "CDATA", aliases[j]);
						handler.startElement("", "alias", "alias", atts);
						atts.clear();
						handler.endElement("", "alias", "alias");
					}

					handler.endElement("", "font", "font");
				}
				handler.endElement("", "fonts", "fonts");
				handler.endDocument();
			}
		} catch (TransformerConfigurationException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		}
		return new ByteArrayInputStream(out.toByteArray());
	}

	public File getProfileFile() {
		if (this.profileFile == null) {
			this.profileFile = DirectDriver.getProfileFile(null);
		}
		return this.profileFile;
	}

	public void setProfileFile(File profileFile) {
		this.profileFile = profileFile;
	}

	public void message(short code, String[] args) {
		this.message(code, args, null);
	}

	public void message(short code, String[] args, String mes) {
		if (this.messageHandler != null) {
			if (this.decodeMessage && mes == null) {
				mes = MessageCodeUtils.toString(code, args);
			}
			this.messageHandler.message(code, args, mes);
		}
	}

	public boolean isDecodeMessage() {
		return this.decodeMessage;
	}

	public void setDecodeMessage(boolean decodeMessage) {
		this.decodeMessage = decodeMessage;
	}

	public void setResults(Results results) {
		assert results != null;
		this.results = results;
	}

	public void setUserAgent(UserAgent ua) {
		assert ua != null;
		this.ua = ua;
	}

	public void setProgressListener(ProgressListener progressListener) {
		this.progressListener = progressListener;
	}

	public void setMessageHandler(MessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}

	public void property(String name, String value) throws IOException {
		if (SPECIAL_PROPERTIES.contains(name)) {
			this.specialProperty(name, value);
		} else {
			if (value == null || value.length() == 0) {
				this.props.remove(name);
			} else {
				this.props.put(name, value);
			}
		}
	}

	public void setContinuous(boolean continuous) {
		this.continuous = continuous;
	}

	private void specialProperty(String name, String value) {
		if (name.equals(UAProps.INPUT_INCLUDE)) {
			// URIのエンコーディングはUTF-8で固定
			try {
				URI uri = URIHelper.create("UTF-8", value);
				this.resolver.include(uri);
			} catch (URISyntaxException e) {
				this.message(MessageCodes.WARN_BAD_URI_PATTERN, new String[] { value });
			}
		} else if (name.equals(UAProps.INPUT_EXCLUDE)) {
			try {
				URI uri = URIHelper.create("UTF-8", value);
				this.resolver.exclude(uri);
			} catch (URISyntaxException e) {
				this.message(MessageCodes.WARN_BAD_URI_PATTERN, new String[] { value });
			}
		}
	}

	public OutputStream resource(final MetaSource metaSource) throws IOException {
		File file = this.resolver.putFile(metaSource);
		OutputStream out = new FileOutputStream(file);
		return out;
	}

	public void resource(final Source source) throws IOException {
		try (OutputStream out = this.resource((MetaSource) source); InputStream in = source.getInputStream()) {
			IOUtils.copy(in, out);
		}
	}

	public void setSourceResolver(SourceResolver resolver) {
		this.resolver.setUserResolver(resolver);
	}

	public void transcode(URI uri) throws IOException, TranscoderException {
		this.prepareTranscode(uri);
		final Source source = this.resolver.resolve(uri, true);
		try {
			Source xsource = source;
			if (this.progressListener != null) {
				try {
					long srcLength = source.getLength();
					if (srcLength != -1) {
						this.progressListener.sourceLength(srcLength);
					}
					InputStream in = source.getInputStream();
					in = new BufferedInputStream(new ProgressInputStream(in, this.progressListener));
					xsource = new StreamSource(uri, in, source.getMimeType(), source.getEncoding());
				} catch (IOException e) {
					throw new FileNotFoundException();
				}
			}
			this.transcode(xsource);
		} catch (FileNotFoundException e) {
			final short code = MessageCodes.ERROR_MISSING_SERVERSIDE_DOCUMENT;
			final String[] args = new String[] { uri.toString() };
			this.message(code, args);
			throw new TranscoderException(TranscoderException.STATE_BROKEN, code, args,
					MessageCodeUtils.toString(code, args));
		} finally {
			this.resolver.release(source);
		}
	}

	public OutputStream transcode(final MetaSource metaSource) throws IOException {
		this.prepareTranscode(metaSource.getURI());
		PipedOutputStream out = new PipedOutputStream() {
			public void close() throws IOException {
				super.close();
				DirectSession.this.flush();
			}
		};
		final String outputType = UAProps.OUTPUT_TYPE.getString(this.props);
		final PipedInputStream in = new PipedInputStream(out);
		this.th = new PipeThread(out) {
			public void run() {
				try {
					InputStream xin = in;
					if (DirectSession.this.progressListener != null) {
						if (metaSource.getLength() != -1L) {
							DirectSession.this.progressListener.sourceLength(metaSource.getLength());
						}
						xin = new BufferedInputStream(new ProgressInputStream(in, DirectSession.this.progressListener));
					}
					Source source = new StreamSource(metaSource.getURI(), xin, outputType, metaSource.getEncoding());
					DirectSession.this.transcode(source);
				} catch (IOException e) {
					this.exception = e;
				}
			}
		};
		this.th.start();
		return out;
	}

	protected FontSourceManager getFontSourceManager() throws IOException {
		final File dir = this.getProfileFile().getParentFile();
		String systemFonts = (String) this.props.get("system.fonts");
		if (systemFonts == null) {
			systemFonts = "fonts/fonts.xml";
		}
		final File fontSource = new File(dir, systemFonts).getCanonicalFile();
		FontSourceManager fsm;
		synchronized (FONT_CACHE) {
			fsm = FONT_CACHE.get(fontSource);
			if (fsm == null) {
				File fontDb = new File(dir, systemFonts + ".db");
				fsm = new ConfigurablePdfFontSourceManager(new FileSource(fontSource), fontDb);
				FONT_CACHE.put(fontSource, fsm);
			}
		}
		return fsm;
	}

	public void transcode(Source source) throws IOException, TranscoderException {
		URI uri = source.getURI();
		this.prepareTranscode(uri);

		// UAのセットアップ
		this.ua.setSourceResolver(this.resolver);
		this.ua.setMessageHandler(this);
		this.ua.setProperties(this.props);

		final FontSourceManager fsm = this.getFontSourceManager();
		this.ua.getUAContext().setFontSourceManager(fsm);

		// 変換を実行
		try {
			this.format(source);
			if (!this.continuous) {
				this.ua.finish();
			}
		} catch (AbortException e) {
			// 中断
			this.continuous = false;
			short code = CTIMessageCodes.INFO_ABORT;
			String mes = MessageCodeUtils.toString(code, null);
			if (e.getState() == ABORT_NORMAL) {
				try {
					this.ua.finish();
				} catch (BrokenResultException e1) {
					throw new TranscoderException(TranscoderException.STATE_BROKEN, code, null, mes);
				}
			} else {
				throw new TranscoderException(TranscoderException.STATE_BROKEN, code, null, mes);
			}
		} catch (TranscoderException e) {
			this.continuous = false;
			// 中断
			if (e.getState() == TranscoderException.STATE_READABLE) {
				try {
					this.ua.finish();
				} catch (BrokenResultException e1) {
					throw new TranscoderException(TranscoderException.STATE_BROKEN, e.getCode(), e.getArgs(),
							e.getMessage());
				}
				return;
			}
			throw e;
		} catch (FileNotFoundException e) {
			this.continuous = false;
			short code = MessageCodes.ERROR_MISSING_SERVERSIDE_DOCUMENT;
			String[] args = new String[] { uri.toString() };
			this.message(code, args);
			throw new TranscoderException(TranscoderException.STATE_BROKEN, code, args,
					MessageCodeUtils.toString(code, args));
		} catch (Throwable t) {
			this.continuous = false;
			this.ua.message(CTIMessageCodes.FATAL_UNEXPECTED, t.getMessage());
			LOG.log(Level.SEVERE, "予期しないエラー", t);
			short code = CTIMessageCodes.FATAL_UNEXPECTED;
			String mes = MessageCodeUtils.toString(code, new String[] { t.getMessage() });
			if (!UAProps.PROCESSING_FAIL_ON_FATAL_ERROR.getBoolean(this.ua)) {
				try {
					this.ua.finish();
				} catch (BrokenResultException e1) {
					throw new TranscoderException(TranscoderException.STATE_BROKEN, code, null, mes);
				}
				return;
			}
			throw new TranscoderException(TranscoderException.STATE_BROKEN, code, null, mes);
		} finally {
			if (!this.continuous) {
				this.ua.dispose();
				this.ua = null;
			}
		}
	}

	private void prepareDefaultProperties() throws IOException {
		File profileFile = this.getProfileFile();
		Properties defaultProperties = new Properties();
		try (InputStream in = new FileInputStream(profileFile)) {
			defaultProperties.load(in);
		} catch (IOException e) {
			this.message(MessageCodes.WARN_MISSING_PROFILE, new String[] { String.valueOf(profileFile) });
		}
		for (Iterator<?> i = defaultProperties.entrySet().iterator(); i.hasNext();) {
			Entry<?, ?> e = (Entry<?, ?>) i.next();
			String name = (String) e.getKey();
			String value = (String) e.getValue();
			this.property(name, value);
		}
	}

	private void prepareTranscode(URI uri) throws IOException, TranscoderException {
		if (this.ua != null) {
			return;
		}
		if (this.results == null) {
			throw new IllegalStateException("Resultsが設定されていません。");
		}
		this.prepareDefaultProperties();

		// include/exclude
		for (int i = 0;; ++i) {
			String exName = UAProps.INPUT_EXCLUDE + "." + i;
			String inName = UAProps.INPUT_INCLUDE + "." + i;
			String exValue = (String) this.props.get(exName);
			String inValue = (String) this.props.get(inName);
			if (exValue == null && inValue == null) {
				break;
			}
			if (exValue != null) {
				this.specialProperty(UAProps.INPUT_EXCLUDE, exValue);
			}
			if (inValue != null) {
				this.specialProperty(UAProps.INPUT_INCLUDE, inValue);
			}
		}

		this.resolver.setup(uri, this.props, this);
		this.aborted = false;

		final String outputType = UAProps.OUTPUT_TYPE.getString(this.props);
		UserAgentFactory factory = (UserAgentFactory) PluginLoader.getPluginLoader().search(UserAgentFactory.class,
				outputType);
		if (factory != null) {
			this.ua = factory.createUserAgent();
		} else {
			throw new IllegalStateException("UnsupportedType: " + outputType);
		}
	}

	protected void flush() throws IOException, TranscoderException {
		if (this.th != null) {
			try {
				this.th.join();
			} catch (InterruptedException e) {
				// ignore
			}
			try {
				if (this.th.exception != null) {
					throw this.th.exception;
				}
			} finally {
				this.th = null;
			}
		}
	}

	public void join() throws IOException {
		this.middlePath = false;
		if (this.ua == null) {
			return;
		}
		try {
			this.ua.finish();
		} catch (BrokenResultException e) {
			short code = CTIMessageCodes.FATAL_UNEXPECTED;
			String mes = MessageCodeUtils.toString(code, new String[] { e.getMessage() });
			throw new TranscoderException(TranscoderException.STATE_BROKEN, code, null, mes);
		} finally {
			this.ua.dispose();
			this.ua = null;
		}
	}

	public void abort(byte mode) throws IOException {
		if (!this.aborted && this.ua != null) {
			this.ua.abort(mode);
			this.aborted = true;
		}
	}

	public void setup() throws IOException {
		this.prepareDefaultProperties();
	}

	public void reset() throws IOException {
		try {
			if (this.ua != null) {
				this.abort(ABORT_FORCE);
				if (this.th != null) {
					this.th.out.close();
				}
			}
		} finally {
			this.ua = null;
			this.middlePath = false;
			this.props.clear();
			this.prepareDefaultProperties();
			this.resolver.reset();
		}
	}

	public void close() throws IOException {
		this.reset();
	}

	/**
	 * 文書を変換します。
	 * 
	 * @param source
	 * @throws AbortException
	 * @throws TranscoderException
	 */
	protected void format(Source source) throws AbortException, TranscoderException {
		// @format
		final Formatter formatter = (Formatter) PluginLoader.getPluginLoader().search(Formatter.class, source);
		Results results = this.results;
		long limit = UAProps.OUTPUT_SIZE_LIMIT.getLong(this.ua);
		if (limit != -1L) {
			results = new LimitedResults(results, limit, this.ua);
		}
		int passCount = UAProps.PROCESSING_PASS_COUNT.getInteger(this.ua);
		try {
			if (passCount == 1) {
				// 1パス
				byte mode = UserAgent.PREPARE_DOCUMENT;
				boolean middlePath = UAProps.PROCESSING_MIDDLE_PASS.getBoolean(this.ua);
				if (this.middlePath != middlePath) {
					mode = middlePath ? UserAgent.PREPARE_MIDDLE_PASS : UserAgent.PREPARE_LAST_PASS;
					if (middlePath) {
						((RandomResultUserAgent) this.ua).setResults(results);
					}
				}
				if (!middlePath) {
					((RandomResultUserAgent) this.ua).setResults(results);
				}
				this.middlePath = middlePath;
				this.ua.prepare(mode);
				this.ua.getDocumentContext().setBaseURI(source.getURI());
				this.ua.getUAContext().setPassCount(passCount);
				this.ua.message(MessageCodes.INFO_PASS_REMAINDER, String.valueOf(passCount));
				formatter.format(source, this.ua);
			} else {
				// 複数パス
				((RandomResultUserAgent) this.ua).setResults(results);
				File tmpFile = null;
				try {
					tmpFile = File.createTempFile("copper", ".tmp");
					// 保存
					this.ua.prepare(UserAgent.PREPARE_MIDDLE_PASS);
					this.ua.getDocumentContext().setBaseURI(source.getURI());
					this.ua.getUAContext().setPassCount(passCount);
					this.ua.message(MessageCodes.INFO_PASS_REMAINDER, String.valueOf(passCount));
					try (final FileOutputStream out = new FileOutputStream(tmpFile);
							final TeeInputStream in = new TeeInputStream(source.getInputStream(), out)) {
						final Source fileSource = new StreamSource(source.getURI(), in, source.getMimeType(),
								source.getEncoding());
						formatter.format(fileSource, this.ua);
					}
					// 中間処理
					for (--passCount; passCount > 1; --passCount) {
						this.ua.prepare(UserAgent.PREPARE_MIDDLE_PASS);
						this.ua.getDocumentContext().setBaseURI(source.getURI());
						this.ua.getUAContext().setPassCount(passCount);
						this.ua.message(MessageCodes.INFO_PASS_REMAINDER, String.valueOf(passCount));
						try (final InputStream in = new FileInputStream(tmpFile)) {
							final Source fileSource = new StreamSource(source.getURI(), in, source.getMimeType(),
									source.getEncoding());
							formatter.format(fileSource, this.ua);
						}
					}
					// 目的物生成
					this.ua.prepare(UserAgent.PREPARE_LAST_PASS);
					this.ua.getDocumentContext().setBaseURI(source.getURI());
					try (final InputStream in = new FileInputStream(tmpFile)) {
						final Source fileSource = new StreamSource(source.getURI(), in, source.getMimeType(),
								source.getEncoding());
						this.ua.getUAContext().setPassCount(passCount);
						this.ua.message(MessageCodes.INFO_PASS_REMAINDER, String.valueOf(passCount));
						formatter.format(fileSource, this.ua);
					}
				} finally {
					if (tmpFile != null) {
						tmpFile.delete();
					}
				}
			}
		} catch (IOException e) {
			short code = CTIMessageCodes.ERROR_IO;
			String[] args = new String[] { e.getMessage() };
			String mes = MessageCodeUtils.toString(code, args);
			this.ua.message(code, args);
			LOG.log(Level.WARNING, mes, e);
			throw new TranscoderException(code, args, mes);
		}
	}
}

class ProgressInputStream extends CountingInputStream {
	final ProgressListener progressListener;

	ProgressInputStream(InputStream in, ProgressListener progressListener) {
		super(in);
		assert in != null;
		assert progressListener != null;
		this.progressListener = progressListener;
	}

	public int read(byte[] b, int off, int len) throws IOException {
		len = super.read(b, off, len);
		if (len != -1) {
			this.progressListener.progress(this.getByteCount());
		}
		return len;
	}

	public int read(byte[] b) throws IOException {
		int len = super.read(b);
		if (len != -1) {
			this.progressListener.progress(this.getByteCount());
		}
		return len;
	}

	public int read() throws IOException {
		int b = super.read();
		if (b != -1) {
			this.progressListener.progress(this.getByteCount());
		}
		return b;
	}
}
