/*
 * (c) COPYRIGHT 1999 World Wide Web Consortium
 * (Massachusetts Institute of Technology, Institut National de Recherche
 *  en Informatique et en Automatique, Keio University).
 * All Rights Reserved. http://www.w3.org/Consortium/Legal/
 *
 * $Id: DescendantSelector.java,v 1.1 2000/07/15 22:08:32 plehegar Exp $
 */
package jp.cssj.sakae.sac.css;

import jp.cssj.sakae.sac.css.Selector;
import jp.cssj.sakae.sac.css.SimpleSelector;

/**
 * @version $Revision: 1.1 $
 * @author Philippe Le Hegaret
 * @see Selector#SAC_DESCENDANT_SELECTOR
 * @see Selector#SAC_CHILD_SELECTOR
 */
public interface DescendantSelector extends Selector {

	/**
	 * Returns the parent selector.
	 */
	public Selector getAncestorSelector();

	/*
	 * Returns the simple selector.
	 */
	public SimpleSelector getSimpleSelector();
}
