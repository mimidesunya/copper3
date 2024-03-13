package net.zamasoft.font.test;

import java.io.File;

import net.zamasoft.font.FontFile;
import net.zamasoft.font.Glyph;
import net.zamasoft.font.OpenTypeFont;

public class TrueTypeTest {
	public static void main(String[] args) throws Exception {
		File dir = new File("/home/miyabe/workspaces/copper3.2/CopperPDF.dev/conf/profiles/fonts/truetype");
		
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				for (File file2 : file.listFiles()) {
					if (!file2.getName().endsWith(".ttf") && !file2.getName().endsWith(".ttc")) {
						continue;
					}
					font(file2);
				}
			}
			if (!file.getName().endsWith(".ttf") && !file.getName().endsWith(".ttc")) {
				continue;
			}
			font(file);
		}
	}
	public static void font(File file) throws Exception {
		System.out.println(file);
		FontFile ff = new FontFile(file);
		for (int i = 0; i < ff.getNumFonts(); ++i) {
			OpenTypeFont font = ff.getFont(i);
			for (int j = 0; j < font.getNumGlyphs(); ++j) {
				Glyph g = font.getGlyph(j);
			}
		}
	}
}
