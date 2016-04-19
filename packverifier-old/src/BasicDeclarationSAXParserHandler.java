import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.UUID;

public abstract class BasicDeclarationSAXParserHandler extends DefaultHandler {
    private final String mNodeName;
    private final String mFileName;

    protected BasicDeclarationSAXParserHandler(String fileName, String nodeName) {
        mFileName = fileName;
        mNodeName = nodeName;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (qName.equals(mNodeName)) {
            verifyNodeBasics(attributes);
            verifyNode(attributes);
        }
    }

    protected abstract void verifyNode(Attributes attributes);

    private void verifyNodeBasics(Attributes attributes) {
        /* id="7263630a-3284-4f46-8137-eef38adb5649" 
         * nameResId="@string/keyboard" 
         * description="Created by Menny Even Danan" 
         * index="1"
         */

        
        String id = attributes.getValue("id");
        if (    id == null || 
                id.length() == 0 || 
                id.contains("change_me")) {
            try {
                UUID uuid = UUID.fromString(id);
                if (uuid == null) throw new RuntimeException();//so I'll catch it.
            } catch(Exception e) {
                throw new InvalidPackConfiguration(mFileName, "Node "+mNodeName+" has an invalid ID!");
            }
        }
        
        verifyValidStringResId(attributes, "nameResId");
        verifyValidAttribute(attributes, "description");
    }
    
    protected void verifyValidAttribute(Attributes attributes, String attributeName) {
        verifyValidAttribute(attributes, attributeName, null);
    }
    
    protected void verifyValidAttribute(Attributes attributes, String attributeName, String[] possibleValues) {
        String value = attributes.getValue(attributeName);
        if (value == null || value.length() == 0)
            throw new InvalidPackConfiguration(mFileName, "Node "+mNodeName+" has an invalid "+attributeName+" value!");
        
        if (possibleValues == null || possibleValues.length == 0)
            return;
        
        for (String aPossibleValue : possibleValues) {
            if (value.equals(aPossibleValue))
                return;
        }
        
        throw new InvalidPackConfiguration(mFileName, "Node "+mNodeName+" has an invalid "+attributeName+" value! It is not one of the possible values!");
    }
    
    protected void verifyValidStringResId(Attributes attributes, String attributeName) {
        String nameRes = attributes.getValue(attributeName);
        verifyResource(attributeName, nameRes, "string");
    }
    
    protected void verifyValidXmlResId(Attributes attributes, String attributeName) {
        String nameRes = attributes.getValue(attributeName);
        verifyResource(attributeName, nameRes, "xml");
    }
    
    protected void verifyValidRawResId(Attributes attributes, String attributeName) {
        String nameRes = attributes.getValue(attributeName);
        verifyResource(attributeName, nameRes, "raw");
    }
    
    protected void verifyValidDrawableResId(Attributes attributes, String attributeName) {
        String nameRes = attributes.getValue(attributeName);
        verifyResource(attributeName, nameRes, "drawable");
    }
    
    protected void verifyValidArrayResId(Attributes attributes, String attributeName) {
        String nameRes = attributes.getValue(attributeName);
        verifyResource(attributeName, nameRes, "array");
    }
    
    private void verifyResource(String attributeName, String nameRes, String resType) {
        if (nameRes == null || !nameRes.startsWith("@"+resType+"/"))
            throw new InvalidPackConfiguration(mFileName, "Node "+mNodeName+" has an invalid "+attributeName+" "+resType+" resource !");
    }
}
