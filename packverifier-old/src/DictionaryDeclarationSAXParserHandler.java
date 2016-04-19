import org.xml.sax.Attributes;

public class DictionaryDeclarationSAXParserHandler extends BasicDeclarationSAXParserHandler {

    public DictionaryDeclarationSAXParserHandler() {
        super("dictionaries.xml", "Dictionary");
    }

    @Override
    protected void verifyNode(Attributes attributes) {
        /*
         * locale="iw" type="binary_resource"
         * dictionaryResourceId="@raw/he_main"
         * autoTextResourceId="@xml/he_autotext" -- optional
         */

        verifyValidAttribute(attributes, "locale");
        verifyValidAttribute(attributes, "type", new String[] {
                "binary_resource", "assets_file"
        });
        if (attributes.getValue("type").equals("binary_resource"))
            verifyValidArrayResId(attributes, "dictionaryResourceId");
    }

}
