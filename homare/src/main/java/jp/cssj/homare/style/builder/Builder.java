package jp.cssj.homare.style.builder;

import jp.cssj.homare.style.box.AbstractBlockBox;
import jp.cssj.homare.style.box.IBox;
import jp.cssj.homare.style.box.impl.FlowBlockBox;
import jp.cssj.sakae.gc.text.GlyphHandler;

/**
 * レイアウトコンテキストです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: Builder.java 1554 2018-04-26 03:34:02Z miyabe $
 */
public interface Builder extends GlyphHandler, LayoutStack {
	public boolean isMain();

	/**
	 * shrink-to-fitを実行中のコンテキストはtrueを返します。
	 * 
	 * @return
	 */
	public boolean isTwoPass();

	/**
	 * 通常のフローのブロックを開始します。
	 * 
	 * @param flowContainer
	 */
	public void startFlowBlock(FlowBlockBox flowContainer);

	/**
	 * 通常のフローのブロックを終了します。
	 */
	public void endFlowBlock();

	/**
	 * 構築済みのボックスを追加します。
	 * 
	 * @param box
	 */
	public void addBound(IBox box);

	/**
	 * 構築済みのテーブルを追加します。
	 * 
	 * @param tableBuilder
	 */
	public void addTable(TableBuilder tableBuilder);

	/**
	 * 新しいレイアウトコンテキストを返します。
	 * 
	 * @param stfBox
	 * @return
	 */
	public Builder newBuilder(AbstractBlockBox stfBox);

	/**
	 * テキストボックスがあれば終了します。
	 */
	public void endTextBlock();
}
