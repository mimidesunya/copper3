/*
 * (c) COPYRIGHT 1999 World Wide Web Consortium
 * (Massachusetts Institute of Technology, Institut National de Recherche
 *  en Informatique et en Automatique, Keio University).
 * All Rights Reserved. http://www.w3.org/Consortium/Legal/
 *
 * $Id: NegativeSelector.java,v 1.2 1999/09/25 12:32:36 plehegar Exp $
 */
package jp.cssj.sakae.sac.css;

import jp.cssj.sakae.sac.css.Selector;
import jp.cssj.sakae.sac.css.SimpleSelector;

/**
 * @version $Revision: 1.2 $
 * @author Philippe Le Hegaret
 * @see Selector#SAC_NEGATIVE_SELECTOR
 */
public interface NegativeSelector extends SimpleSelector {

	/**
	 * Returns the simple selector.
	 */
	public SimpleSelector getSimpleSelector();
}
