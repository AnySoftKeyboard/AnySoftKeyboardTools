import org.xml.sax.Attributes;


public class KeyboardDeclarationSAXParserHandler extends BasicDeclarationSAXParserHandler {

    protected KeyboardDeclarationSAXParserHandler() {
        super("keyboards.xml", "Keyboard");
    }

    @Override
    protected void verifyNode(Attributes attributes) {
        /* iconResId="@drawable/ic_stat_he" 
         * layoutResId="@xml/heb_qwerty" 
         * defaultDictionaryLocale="iw"
         */
        verifyValidDrawableResId(attributes, "iconResId");
        verifyValidXmlResId(attributes, "layoutResId");
        verifyValidAttribute(attributes, "defaultDictionaryLocale");
    }

}
