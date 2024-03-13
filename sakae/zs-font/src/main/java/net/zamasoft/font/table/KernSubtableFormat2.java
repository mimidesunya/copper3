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
 * 
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 * @version $Id: KernSubtableFormat2.java 1034 2013-10-23 05:51:57Z miyabe $
 */
public class KernSubtableFormat2 extends KernSubtable {

	/** Creates new KernSubtableFormat2 */
	protected KernSubtableFormat2(RandomAccessFile raf) throws IOException {
		raf.readUnsignedShort();// rowWidth
		raf.readUnsignedShort();// leftClassTable
		raf.readUnsignedShort();// rightClassTable
		raf.readUnsignedShort();// array
	}

	public int getKerningPairCount() {
		return 0;
	}

	public KerningPair getKerningPair(int i) {
		return null;
	}

}
