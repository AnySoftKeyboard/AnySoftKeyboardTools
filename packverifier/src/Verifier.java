import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/*
 * Copyright (C) 2012 AnySoftKeyboard.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

public class Verifier {

    public static class PackDetails
    {
        public final String PackageName;
        public final String KeyboardSourceCodeFile;
        public final String DictionarySourceCodeFile;
        public final String ThemeSourceCodeFile;

        public PackDetails(String packName, String keyboardSourceFile, String dictionarySourceFile,
                String themeSourceFile) {
            PackageName = packName;
            KeyboardSourceCodeFile = keyboardSourceFile != null
                    && keyboardSourceFile.startsWith(".") ? PackageName
                    + keyboardSourceFile : keyboardSourceFile;
            DictionarySourceCodeFile = dictionarySourceFile != null
                    && dictionarySourceFile.startsWith(".") ? PackageName
                    + dictionarySourceFile : dictionarySourceFile;
            ThemeSourceCodeFile = themeSourceFile != null && themeSourceFile.startsWith(".") ? PackageName
                    + themeSourceFile
                    : themeSourceFile;
        }
    }

    public static void main(String[] args) throws ParserConfigurationException, SAXException,
            IOException {
        // will verify that the project at the current working folder is valid
        // tests:
        // 1) package name is valid
        // 1.1) in AndroidManifest.xml
        // 1.2) in any source file
        // 2) any declared pack XML is valid
        // 2.1) pack ID is unique and valid

        final File currentFolder = new File(System.getProperty("user.dir"));

        final PackDetails packDetails = verifyAndroidManifestAndGetPackageName(currentFolder);
        System.out.println("Package name is " + packDetails.PackageName);

        verifyAntBuildFile(currentFolder, packDetails);

        verifySourceCodeHasCorrectPackageName(currentFolder, packDetails);

        if (packDetails.KeyboardSourceCodeFile != null)
            verifyKeyboardDeclaration(currentFolder, packDetails);

        if (packDetails.DictionarySourceCodeFile != null)
            verifyDictionaryDeclaration(currentFolder, packDetails);

        if (packDetails.ThemeSourceCodeFile != null)
            verifyThemeDeclaration(currentFolder, packDetails);
    }

    private static void verifyAntBuildFile(File currentFolder, PackDetails packDetails)
            throws ParserConfigurationException, SAXException, IOException {
        File antFile = new File(currentFolder, "build.xml");
        System.out.println("Verifying ANT build file for validity...");
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(antFile, new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName, String qName,
                    Attributes attributes) throws SAXException {
                super.startElement(uri, localName, qName, attributes);
                if (qName.equals("project")) {
                    if (attributes.getValue("name").equals("LanguagePack")) {
                        throw new InvalidPackConfiguration("build.xml",
                                "The name of the ant should be changed");
                    }
                }
            }
        });
    }

    private static void verifyKeyboardDeclaration(File currentFolder, PackDetails packDetails)
            throws ParserConfigurationException, SAXException, IOException {
        File declarationFile = new File(currentFolder, "res/xml/keyboards.xml");
        System.out.println("Verifying plugins declaration file " + declarationFile
                + " for validity...");
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        BasicDeclarationSAXParserHandler handler = new KeyboardDeclarationSAXParserHandler();
        parser.parse(declarationFile, handler);
    }

    private static void verifyDictionaryDeclaration(File currentFolder, PackDetails packDetails)
            throws ParserConfigurationException, SAXException, IOException {
        File declarationFile = new File(currentFolder, "res/xml/dictionaries.xml");
        System.out.println("Verifying plugins declaration file " + declarationFile
                + " for validity...");
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        BasicDeclarationSAXParserHandler handler = new DictionaryDeclarationSAXParserHandler();
        parser.parse(declarationFile, handler);
    }

    private static void verifyThemeDeclaration(File currentFolder, PackDetails packDetails)
            throws ParserConfigurationException, SAXException, IOException {
        File declarationFile = new File(currentFolder, "res/xml/themes.xml");
        System.out.println("Verifying plugins declaration file " + declarationFile
                + " for validity...");
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        BasicDeclarationSAXParserHandler handler = new ThemeDeclarationSAXParserHandler();
        parser.parse(declarationFile, handler);
    }

    private static void verifySourceCodeHasCorrectPackageName(File currentFolder,
            PackDetails packDetails) throws IOException {
        final File srcFolder = new File(currentFolder, "src");
        if (packDetails.KeyboardSourceCodeFile != null)
            verifySourceCodeFileHasCorrectPackageName(new File(srcFolder,
                    packDetails.KeyboardSourceCodeFile.replace('.', '/') + ".java"), packDetails);
        if (packDetails.DictionarySourceCodeFile != null)
            verifySourceCodeFileHasCorrectPackageName(new File(srcFolder,
                    packDetails.DictionarySourceCodeFile.replace('.', '/') + ".java"), packDetails);
        if (packDetails.ThemeSourceCodeFile != null)
            verifySourceCodeFileHasCorrectPackageName(new File(srcFolder,
                    packDetails.ThemeSourceCodeFile.replace('.', '/') + ".java"), packDetails);
    }

    private static void verifySourceCodeFileHasCorrectPackageName(File source,
            PackDetails packDetails) throws IOException {
        final String requiredPackage = "package " + packDetails.PackageName + ";";
        System.out.println("Verifying source file " + source + " for package name validity ("
                + requiredPackage + ")...");
        if (!source.exists()) {
            throw new InvalidPackConfiguration(source.getAbsolutePath(),
                    "Source file does not exist!");
        }
        if (!source.isFile()) {
            throw new InvalidPackConfiguration(source.getAbsolutePath(),
                    "Specified file is not a file!");
        }

        BufferedReader in = new BufferedReader(new FileReader(source));
        String strLine = null;
        boolean found = false;
        try {
            while (!found && (strLine = in.readLine()) != null) {
                if (strLine.equals(requiredPackage))
                    found = true;
            }
        } finally {
            in.close();
        }

        if (!found) {
            throw new InvalidPackConfiguration(source.getAbsolutePath(),
                    "Package name is invalid, it should be " + packDetails.PackageName);
        }
    }

    private static PackDetails verifyAndroidManifestAndGetPackageName(File currentFolder)
            throws ParserConfigurationException, SAXException, IOException {
        final File inputFile = new File(currentFolder, "AndroidManifest.xml");
        System.out.println("Reading AndroidManifest from input " + inputFile.getAbsolutePath());

        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        AndroidManifestSAXParserHandler handler = new AndroidManifestSAXParserHandler();
        parser.parse(inputFile, handler);

        return handler.createPackDetails();
    }

}
