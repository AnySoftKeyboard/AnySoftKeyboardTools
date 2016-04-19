import org.xml.sax.Attributes;


public class ThemeDeclarationSAXParserHandler extends BasicDeclarationSAXParserHandler {

    
    protected ThemeDeclarationSAXParserHandler() {
        super("themes.xml", "Theme");
    }

    @Override
    protected void verifyNode(Attributes attributes) {
    }

}
