import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class AndroidManifestSAXParserHandler extends DefaultHandler {

    private static final String FILENAME = "AndroidManifest.xml";
    private static final String CHANGE_ME = "change_me";
    private String mPackageName;
    
    private boolean mInReceiver = false;
    private boolean mInIntentFilter = false;
    private String mCurrentRecieverClass;
    //com.menny.android.anysoftkeyboard.KEYBOARD
    private String mKeyboardClass;
    //com.menny.android.anysoftkeyboard.DICTIONARY
    private String mDictionaryClass;
    //com.menny.android.anysoftkeyboard.THEME
    private String mThemeClass;
    
    //com.menny.android.anysoftkeyboard.keyboards
    private boolean mSawKeyboardsMetadata = false;
    //com.menny.android.anysoftkeyboard.dictionaries
    private boolean mSawDictionariesMetadata = false;
    //com.menny.android.anysoftkeyboard.themes
    private boolean mSawThemesMetadata = false;
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (qName.equals("manifest")) {
            mPackageName = attributes.getValue("package");
            if (mPackageName == null || mPackageName.length() == 0 || mPackageName.contains(CHANGE_ME))
                throw new InvalidPackConfiguration(FILENAME, "Package name is invalid!");
        }
        
        if (qName.equals("receiver")) {
            mInReceiver = true;
            mCurrentRecieverClass = attributes.getValue("android:name");
        }
        
        if (mInReceiver && qName.equals("intent-filter")) {
            mInIntentFilter = true;
        }
        
        if (mInReceiver && mInIntentFilter && qName.equals("action")) {
            String actionName = attributes.getValue("android:name");
            if (actionName.equals("com.menny.android.anysoftkeyboard.KEYBOARD")) {
                mKeyboardClass = mCurrentRecieverClass;
            } else if (actionName.equals("com.menny.android.anysoftkeyboard.DICTIONARY")) {
                mDictionaryClass = mCurrentRecieverClass;
            } else if (actionName.equals("com.menny.android.anysoftkeyboard.THEME")) {
                mThemeClass = mCurrentRecieverClass;
            } else {
                System.out.println("Unknown reciever type found in AndroidManifest! Action: "+actionName);
            }
        }
        
        if (mInReceiver && qName.equals("meta-data")) {
            final String type = attributes.getValue("android:name");
            if (type.equals("com.menny.android.anysoftkeyboard.keyboards"))
                mSawKeyboardsMetadata = true;
            else if (type.equals("com.menny.android.anysoftkeyboard.dictionaries"))
                mSawDictionariesMetadata = true;
            else if (type.equals("com.menny.android.anysoftkeyboard.themes"))
                mSawThemesMetadata = true;
            else 
                System.out.println("Unknown reciever meta-data found in AndroidManifest! Name: "+type);
        }
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        
        if (qName.equals("receiver")) {
            mInReceiver = false;
            mCurrentRecieverClass = null;
        }
        
        if (mInReceiver && qName.equals("intent-filter")) {
            mInIntentFilter = false;
        }
    }
    
    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        if (mPackageName == null || mPackageName.length() == 0)
            throw new InvalidPackConfiguration(FILENAME, "No package name defined!");
        
        if (!mSawKeyboardsMetadata && !mSawDictionariesMetadata && !mSawThemesMetadata)
            throw new InvalidPackConfiguration(FILENAME, "Pack defines no plugins!");
        //last verifications
        verifyMetaDataAndReciever(mKeyboardClass, mSawKeyboardsMetadata, "Keyboard");
        verifyMetaDataAndReciever(mDictionaryClass, mSawDictionariesMetadata, "Dictionary");
        verifyMetaDataAndReciever(mThemeClass, mSawThemesMetadata, "Theme");
    }
    
    private void verifyMetaDataAndReciever(String className, boolean sawMetadata, String humanName) {
        if ((className == null || className.length() == 0) && sawMetadata)
            throw new InvalidPackConfiguration(FILENAME, humanName+" metadata has been seen, but no receiver intent-filter found!");
        else if ((className != null && className.length() > 0) && !sawMetadata)
            throw new InvalidPackConfiguration(FILENAME, humanName+" receiver intent-filter has been seen, but no meta-data found!");
    }

    public Verifier.PackDetails createPackDetails() {
        return new Verifier.PackDetails(mPackageName, mKeyboardClass, mDictionaryClass, mThemeClass);
    }

}
