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

/**
 * This interface defines the constants that represent CSS lexical units.
 * 
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion </a>
 * @version $Id: LexicalUnits.java 1555 2018-04-26 04:15:29Z miyabe $
 */
public interface LexicalUnits {

	/**
	 * Represents the EOF lexical unit.
	 */
	int EOF = 0;

	/**
	 * Represents the '{' lexical unit.
	 */
	int LEFT_CURLY_BRACE = 1;

	/**
	 * Represents the '}' lexical unit.
	 */
	int RIGHT_CURLY_BRACE = 2;

	/**
	 * Represents the '=' lexical unit.
	 */
	int EQUAL = 3;

	/**
	 * Represents the '+' lexical unit.
	 */
	int PLUS = 4;

	/**
	 * Represents the '-' lexical unit.
	 */
	int MINUS = 5;

	/**
	 * Represents the ',' lexical unit.
	 */
	int COMMA = 6;

	/**
	 * Represents the '.' lexical unit.
	 */
	int DOT = 7;

	/**
	 * Represents the ';' lexical unit.
	 */
	int SEMI_COLON = 8;

	/**
	 * Represents the '&gt;' lexical unit.
	 */
	int PRECEDE = 9;

	/**
	 * Represents the '/' lexical unit.
	 */
	int DIVIDE = 10;

	/**
	 * Represents the '[' lexical unit.
	 */
	int LEFT_BRACKET = 11;

	/**
	 * Represents the ']' lexical unit.
	 */
	int RIGHT_BRACKET = 12;

	/**
	 * Represents the '*' lexical unit.
	 */
	int ANY = 13;

	/**
	 * Represents the '(' lexical unit.
	 */
	int LEFT_BRACE = 14;

	/**
	 * Represents the ')' lexical unit.
	 */
	int RIGHT_BRACE = 15;

	/**
	 * Represents the ':' lexical unit.
	 */
	int COLON = 16;

	/**
	 * Represents the '|' lexical unit.
	 */
	int DASH = 17;

	/**
	 * Represents the white space lexical unit.
	 */
	int SPACE = 18;

	/**
	 * Represents the comment lexical unit.
	 */
	int COMMENT = 19;

	/**
	 * Represents the string lexical unit.
	 */
	int STRING = 20;

	/**
	 * Represents the identifier lexical unit.
	 */
	int IDENTIFIER = 21;

	/**
	 * Represents the ' &lt;!--' lexical unit.
	 */
	int CDO = 22;

	/**
	 * Represents the '--&gt;' lexical unit.
	 */
	int CDC = 23;

	/**
	 * Represents the '!important' lexical unit.
	 */
	int IMPORTANT_SYMBOL = 24;

	/**
	 * Represents an integer.
	 */
	int INTEGER = 25;

	/**
	 * Represents the '|=' lexical unit.
	 */
	int DASHMATCH = 26;

	/**
	 * Represents the '~=' lexical unit.
	 */
	int INCLUDES = 27;

	/**
	 * Represents the '#name' lexical unit.
	 */
	int HASH = 28;

	/**
	 * Represents the '@import' lexical unit.
	 */
	int IMPORT_SYMBOL = 29;

	/**
	 * Represents the '@ident' lexical unit.
	 */
	int AT_KEYWORD = 30;

	/**
	 * Represents the '@charset' lexical unit.
	 */
	int CHARSET_SYMBOL = 31;

	/**
	 * Represents the '@font-face' lexical unit.
	 */
	int FONT_FACE_SYMBOL = 32;

	/**
	 * Represents the '@media' lexical unit.
	 */
	int MEDIA_SYMBOL = 33;

	/**
	 * Represents the '@page' lexical unit.
	 */
	int PAGE_SYMBOL = 34;

	/**
	 * Represents the '@namespace' lexical unit.
	 */
	int NAMESPACE_SYMBOL = 35;

	/**
	 * Represents a dimension lexical unit.
	 */
	int DIMENSION = 36;

	/**
	 * Represents a ex lexical unit.
	 */
	int EX = 37;

	/**
	 * Represents a em lexical unit.
	 */
	int EM = 38;

	/**
	 * Represents a cm lexical unit.
	 */
	int CM = 39;

	/**
	 * Represents a mm lexical unit.
	 */
	int MM = 40;

	/**
	 * Represents a in lexical unit.
	 */
	int IN = 41;

	/**
	 * Represents a ms lexical unit.
	 */
	int MS = 42;

	/**
	 * Represents a hz lexical unit.
	 */
	int HZ = 43;

	/**
	 * Represents a % lexical unit.
	 */
	int PERCENTAGE = 44;

	/**
	 * Represents a s lexical unit.
	 */
	int S = 45;

	/**
	 * Represents a pc lexical unit.
	 */
	int PC = 46;

	/**
	 * Represents a pt lexical unit.
	 */
	int PT = 47;

	/**
	 * Represents a px lexical unit.
	 */
	int PX = 48;

	/**
	 * Represents a deg lexical unit.
	 */
	int DEG = 49;

	/**
	 * Represents a rad lexical unit.
	 */
	int RAD = 50;

	/**
	 * Represents a grad lexical unit.
	 */
	int GRAD = 51;

	/**
	 * Represents a khz lexical unit.
	 */
	int KHZ = 52;

	/**
	 * Represents a 'url(URI)' lexical unit.
	 */
	int URI = 53;

	/**
	 * Represents a 'ident(' lexical unit.
	 */
	int FUNCTION = 54;

	/**
	 * Represents a unicode range lexical unit.
	 */
	int UNICODE_RANGE = 55;

	/**
	 * represents a real number.
	 */
	int REAL = 56;

	/**
	 * Represents the '@-cssj-page-conent' lexical unit.
	 */
	int PAGE_CONTENT_SYMBOL = 57;

	/**
	 * Represents a rem lexical unit.
	 */
	int REM = 58;

	/**
	 * Represents a ch lexical unit.
	 */
	int CH = 59;
}