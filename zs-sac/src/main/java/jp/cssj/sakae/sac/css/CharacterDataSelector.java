/*
 * (c) COPYRIGHT 1999 World Wide Web Consortium
 * (Massachusetts Institute of Technology, Institut National de Recherche
 *  en Informatique et en Automatique, Keio University).
 * All Rights Reserved. http://www.w3.org/Consortium/Legal/
 *
 * $Id: CharacterDataSelector.java,v 1.3 1999/09/26 10:05:32 plehegar Exp $
 */
package jp.cssj.sakae.sac.css;

import jp.cssj.sakae.sac.css.Selector;
import jp.cssj.sakae.sac.css.SimpleSelector;

/**
 * @version $Revision: 1.3 $
 * @author Philippe Le Hegaret
 * @see Selector#SAC_TEXT_NODE_SELECTOR
 * @see Selector#SAC_CDATA_SECTION_NODE_SELECTOR
 * @see Selector#SAC_COMMENT_NODE_SELECTOR
 */
public interface CharacterDataSelector extends SimpleSelector {

	/**
	 * Returns the character data.
	 */
	public String getData();
}
