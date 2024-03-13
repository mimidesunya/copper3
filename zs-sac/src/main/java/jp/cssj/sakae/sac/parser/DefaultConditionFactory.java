/*

 ============================================================================
 The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
 include  the following  acknowledgment:  "This product includes  software
 developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 Alternately, this  acknowledgment may  appear in the software itself,  if
 and wherever such third-party acknowledgments normally appear.

 4. The names "Batik" and  "Apache Software Foundation" must  not  be
 used to  endorse or promote  products derived from  this software without
 prior written permission. For written permission, please contact
 apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
 "Apache" appear  in their name,  without prior written permission  of the
 Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation. For more  information on the
 Apache Software Foundation, please see <http://www.apache.org/>.

 */

package jp.cssj.sakae.sac.parser;

import jp.cssj.sakae.sac.css.AttributeCondition;
import jp.cssj.sakae.sac.css.CSSException;
import jp.cssj.sakae.sac.css.CombinatorCondition;
import jp.cssj.sakae.sac.css.Condition;
import jp.cssj.sakae.sac.css.ConditionFactory;
import jp.cssj.sakae.sac.css.ContentCondition;
import jp.cssj.sakae.sac.css.LangCondition;
import jp.cssj.sakae.sac.css.NegativeCondition;
import jp.cssj.sakae.sac.css.PositionalCondition;

/**
 * This class provides an implementation of the
 * {@link jp.cssj.sakae.sac.css.ConditionFactory}interface.
 * 
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion </a>
 * @version $Id: DefaultConditionFactory.java,v 1.3 2003/08/08 11:38:57 vhardy
 *          Exp $
 */

public class DefaultConditionFactory implements ConditionFactory {

	/**
	 * The instance of this class.
	 */
	public final static ConditionFactory INSTANCE = new DefaultConditionFactory();

	/**
	 * This class does not need to be instantiated.
	 */
	protected DefaultConditionFactory() {
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link ConditionFactory#createAndCondition(Condition,Condition)}.
	 */
	public CombinatorCondition createAndCondition(Condition first, Condition second) throws CSSException {
		return new DefaultAndCondition(first, second);
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link ConditionFactory#createOrCondition(Condition,Condition)}.
	 */
	public CombinatorCondition createOrCondition(Condition first, Condition second) throws CSSException {
		throw new CSSException("Not implemented in CSS2");
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link jp.cssj.sakae.sac.css.ConditionFactory#createNegativeCondition(Condition)}
	 * .
	 */
	public NegativeCondition createNegativeCondition(Condition condition) throws CSSException {
		throw new CSSException("Not implemented in CSS2");
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link ConditionFactory#createPositionalCondition(int,boolean,boolean)}.
	 */
	public PositionalCondition createPositionalCondition(int position, boolean typeNode, boolean type)
			throws CSSException {
		throw new CSSException("Not implemented in CSS2");
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link ConditionFactory#createAttributeCondition(String,String,boolean,String)}
	 * .
	 */
	public AttributeCondition createAttributeCondition(String localName, String namespaceURI, boolean specified,
			String value) throws CSSException {
		return new DefaultAttributeCondition(localName, namespaceURI, specified, value);
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link jp.cssj.sakae.sac.css.ConditionFactory#createIdCondition(String)}.
	 */
	public AttributeCondition createIdCondition(String value) throws CSSException {
		return new DefaultIdCondition(value);
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link jp.cssj.sakae.sac.css.ConditionFactory#createLangCondition(String)}.
	 */
	public LangCondition createLangCondition(String lang) throws CSSException {
		return new DefaultLangCondition(lang);
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link ConditionFactory#createOneOfAttributeCondition(String,String,boolean,String)}
	 * .
	 */
	public AttributeCondition createOneOfAttributeCondition(String localName, String nsURI, boolean specified,
			String value) throws CSSException {
		return new DefaultOneOfAttributeCondition(localName, nsURI, specified, value);
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link ConditionFactory#createBeginHyphenAttributeCondition(String,String,boolean,String)}
	 * .
	 */
	public AttributeCondition createBeginHyphenAttributeCondition(String localName, String namespaceURI,
			boolean specified, String value) throws CSSException {
		return new DefaultBeginHyphenAttributeCondition(localName, namespaceURI, specified, value);
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link jp.cssj.sakae.sac.css.ConditionFactory#createClassCondition(String,String)}
	 * .
	 */
	public AttributeCondition createClassCondition(String namespaceURI, String value) throws CSSException {
		return new DefaultClassCondition(namespaceURI, value);
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link ConditionFactory#createPseudoClassCondition(String,String)}.
	 */
	public AttributeCondition createPseudoClassCondition(String namespaceURI, String value) throws CSSException {
		return new DefaultPseudoClassCondition(namespaceURI, value);
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link jp.cssj.sakae.sac.css.ConditionFactory#createOnlyChildCondition()}.
	 */
	public Condition createOnlyChildCondition() throws CSSException {
		throw new CSSException("Not implemented in CSS2");
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link jp.cssj.sakae.sac.css.ConditionFactory#createOnlyTypeCondition()}.
	 */
	public Condition createOnlyTypeCondition() throws CSSException {
		throw new CSSException("Not implemented in CSS2");
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link jp.cssj.sakae.sac.css.ConditionFactory#createContentCondition(String)}.
	 */
	public ContentCondition createContentCondition(String data) throws CSSException {
		throw new CSSException("Not implemented in CSS2");
	}
}