package jp.cssj.homare.impl.ua.image;

import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.FileCacheImageInputStream;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import com.twelvemonkeys.imageio.plugins.jpeg.JPEGImageReader;

import jp.cssj.homare.ua.ImageLoader;
import jp.cssj.homare.ua.UserAgent;
import jp.cssj.resolver.Source;
import jp.cssj.sakae.g2d.image.RasterImageImpl;
import jp.cssj.sakae.g2d.util.G2dUtils;
import jp.cssj.sakae.gc.image.Image;

public class RasterImageLoader implements ImageLoader {
	public boolean match(Source key) {
		return true;
	}

	@SuppressWarnings("resource")
	public boolean available(Source source) throws IOException {
		ImageInputStream imageIn;
		if (source.isFile()) {
			imageIn = new FileImageInputStream(source.getFile());
		} else {
			imageIn = ImageIO.createImageInputStream(source.getInputStream());
		}
		try { // ImageIOによるラスタ画像の取得
			Iterator<ImageReader> iri = ImageIO.getImageReaders(imageIn);
			if (iri != null && iri.hasNext()) {
				return true;
			}
			return false;
		} finally {
			imageIn.close();
		}
	}

	public Image loadImage(UserAgent ua, Source source) throws IOException {
		ImageInputStream imageIn;
		if (source.isFile()) {
			imageIn = new FileImageInputStream(source.getFile()) {
				public void flushBefore(long pos) throws IOException {
					// 再読み込み不可能になることを防止するため、flushを無視する
				}
			};
		} else {
			imageIn = new FileCacheImageInputStream(source.getInputStream(), null) {
				public void flushBefore(long pos) throws IOException {
					// 再読み込み不可能になることを防止するため、flushを無視する
				}
			};
		}
		try { // ImageIOによるラスタ画像の取得
			JPEGImageReader cir = null;
			Iterator<ImageReader> iri = ImageIO.getImageReaders(imageIn);
			ImageReader ir = null;
			while (iri != null && iri.hasNext()) {
				ir = iri.next();
				ir.setInput(imageIn);
				try {
					Iterator<ImageTypeSpecifier> iti = ir.getImageTypes(0);
					if (iti != null && iti.hasNext()) {
						imageIn.seek(0);
						if (ir instanceof JPEGImageReader) {
							cir = (JPEGImageReader)ir;
							ir = null;
							continue;
						}
						break;
					}
				} catch (IOException e) {
					// ignore
				}
				ir.dispose();
				ir = null;
				imageIn.seek(0);
			}
			if (ir == null) {
				if (cir != null) {
					ir = cir;
				}
				else {
					throw new IOException("ImageIOがサポートしない画像形式です");
				}
			}
			else {
				if (cir != null) {
					cir.dispose();
				}
			}
			imageIn.seek(0);
			return new RasterImageImpl(G2dUtils.loadImage(ir, imageIn));
		} finally {
			imageIn.close();
		}
	}
}
