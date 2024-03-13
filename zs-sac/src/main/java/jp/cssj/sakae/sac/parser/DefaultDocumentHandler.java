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

import jp.cssj.sakae.sac.css.CSSException;
import jp.cssj.sakae.sac.css.DocumentHandler;
import jp.cssj.sakae.sac.css.InputSource;
import jp.cssj.sakae.sac.css.LexicalUnit;
import jp.cssj.sakae.sac.css.SACMediaList;
import jp.cssj.sakae.sac.css.SelectorList;

/**
 * This class provides a default implementation of the SAC DocumentHandler.
 * 
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion </a>
 * @version $Id: DefaultDocumentHandler.java,v 1.3 2003/08/08 11:38:57 vhardy
 *          Exp $
 */
public class DefaultDocumentHandler implements DocumentHandler {
	/**
	 * The instance of this class.
	 */
	public final static DocumentHandler INSTANCE = new DefaultDocumentHandler();

	/**
	 * Creates a new DefaultDocumentHandler.
	 */
	protected DefaultDocumentHandler() {
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link jp.cssj.sakae.sac.css.DocumentHandler#startDocument(InputSource)}.
	 */
	public void startDocument(InputSource source) throws CSSException {
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link jp.cssj.sakae.sac.css.DocumentHandler#endDocument(InputSource)}.
	 */
	public void endDocument(InputSource source) throws CSSException {
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link jp.cssj.sakae.sac.css.DocumentHandler#comment(String)}.
	 */
	public void comment(String text) throws CSSException {
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link jp.cssj.sakae.sac.css.DocumentHandler#ignorableAtRule(String)}.
	 */
	public void ignorableAtRule(String atRule) throws CSSException {
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link jp.cssj.sakae.sac.css.DocumentHandler#namespaceDeclaration(String,String)}
	 * .
	 */
	public void namespaceDeclaration(String prefix, String uri) throws CSSException {
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link DocumentHandler#importStyle(String,SACMediaList,String)}.
	 */
	public void importStyle(String uri, SACMediaList media, String defaultNamespaceURI) throws CSSException {
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link jp.cssj.sakae.sac.css.DocumentHandler#startMedia(SACMediaList)}.
	 */
	public void startMedia(SACMediaList media) throws CSSException {
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link jp.cssj.sakae.sac.css.DocumentHandler#endMedia(SACMediaList)}.
	 */
	public void endMedia(SACMediaList media) throws CSSException {
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link jp.cssj.sakae.sac.css.DocumentHandler#startPage(String,String)}.
	 */
	public void startPage(String name, String pseudo_page) throws CSSException {
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link jp.cssj.sakae.sac.css.DocumentHandler#endPage(String,String)}.
	 */
	public void endPage(String name, String pseudo_page) throws CSSException {
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link jp.cssj.sakae.sac.css.DocumentHandler#startFontFace()}.
	 */
	public void startFontFace() throws CSSException {
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link jp.cssj.sakae.sac.css.DocumentHandler#endFontFace()}.
	 */
	public void endFontFace() throws CSSException {
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link jp.cssj.sakae.sac.css.DocumentHandler#startSelector(SelectorList)}.
	 */
	public void startSelector(SelectorList selectors) throws CSSException {
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link jp.cssj.sakae.sac.css.DocumentHandler#endSelector(SelectorList)}.
	 */
	public void endSelector(SelectorList selectors) throws CSSException {
	}

	/**
	 * <b>SAC </b>: Implements
	 * {@link jp.cssj.sakae.sac.css.DocumentHandler#property(String,LexicalUnit,boolean)}
	 * .
	 */
	public void property(String name, LexicalUnit value, boolean important) throws CSSException {
	}
}