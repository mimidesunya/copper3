package jp.cssj.cti2.results;

import java.io.IOException;

import jp.cssj.resolver.MetaSource;
import jp.cssj.rsr.RandomBuilder;

/**
 * 処理結果です。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: Results.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public interface Results {
	/**
	 * 次の結果を出力可能であればtrueを返します。
	 * 
	 * @return 次の結果を出力可能であればtrueそうでなければfalse。
	 */
	public boolean hasNext();

	/**
	 * 次の処理結果を構築するためのビルダを返します。
	 * 
	 * @param metaSource
	 *            出力データのメタ情報。
	 * @return データ構築オブジェクト。
	 * @throws IOException
	 */
	public RandomBuilder nextBuilder(MetaSource metaSource) throws IOException;

	/**
	 * 一連のデータ出力を完了します。
	 * 
	 * @throws IOException
	 */
	public void end() throws IOException;
}
