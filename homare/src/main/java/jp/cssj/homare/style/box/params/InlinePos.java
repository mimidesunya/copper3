package jp.cssj.homare.style.box.params;

import jp.cssj.homare.style.box.content.CSSVerticalAlignPolicy;
import jp.cssj.homare.style.box.content.VerticalAlignPolicy;

/**
 * インラインレベルの配置パラメータです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: InlinePos.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class InlinePos extends AbstractStaticPos implements Pos {
	/**
	 * 垂直方向アラインメントです。
	 */
	public VerticalAlignPolicy verticalAlign = CSSVerticalAlignPolicy.BASELINE_POLICY;

	public double lineHeight = 1.0;

	public byte getType() {
		return TYPE_INLINE;
	}

	public String toString() {
		return super.toString() + "[verticalAlign=" + this.verticalAlign + ",lineHeight=" + this.lineHeight + "]";
	}
}
