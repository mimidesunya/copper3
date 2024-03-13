package jp.cssj.homare.ua;

import java.io.IOException;

import jp.cssj.plugin.Plugin;
import jp.cssj.resolver.Source;
import jp.cssj.sakae.gc.image.Image;

public interface ImageLoader extends Plugin<Source> {
	public Image loadImage(UserAgent ua, Source source) throws IOException;
}
