package jp.cssj.homare.style.box.params;

/**
 * 配置パラメータです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: PagePos.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class PagePos implements Pos {
	public static final PagePos POS = new PagePos();

	private PagePos() {
		// private
	}

	public byte getType() {
		return TYPE_PAGE;
	}
}
