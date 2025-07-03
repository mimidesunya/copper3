import java.io.File;

import jp.cssj.sakae.font.otf.OpenTypeFontSource;
import jp.cssj.sakae.gc.font.FontStyle;

public class OpenTypeFontSourceTest {
    public static void main(String[] args) throws Exception {
        File file = new File("Y:\\workspaces\\copper3.2\\proprietary\\CopperPDF.dev\\conf\\profiles\\fonts\\truetype\\ipa\\ipaexg.ttf");
        OpenTypeFontSource fontSource = new OpenTypeFontSource(file, 0, FontStyle.DIRECTION_LTR);
        System.out.println("Font Name: " + fontSource.getFontName());
        System.out.println("Can display: " + fontSource.canDisplay(0x91d1));
        System.out.println("Can display: " + fontSource.canDisplay(0x8F03));
    }
}
