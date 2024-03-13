/*

 Copyright 2001  The Apache Software Foundation 

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */
package net.zamasoft.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @version $Id: CmapFormat.java 1263 2014-06-10 14:40:37Z miyabe $
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public abstract class CmapFormat {
	public static CmapFormat createCmapFormat(int format, int numGlyphs, RandomAccessFile data) throws IOException {
		if (format == 14) {
			return new UvsCmapFormat(format, numGlyphs, data);
		}
		return new GenericCmapFormat(format, numGlyphs, data);
	}
}
