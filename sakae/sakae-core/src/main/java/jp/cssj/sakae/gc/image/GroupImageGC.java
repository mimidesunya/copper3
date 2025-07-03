package jp.cssj.sakae.gc.image;

import jp.cssj.sakae.gc.GC;
import jp.cssj.sakae.gc.GraphicsException;

public interface GroupImageGC extends GC {
	public Image finish() throws GraphicsException;
}
