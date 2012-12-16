/*
 * Copyright (C) 2009 The Android Open Source Project
 * Copyright (C) 2012 AnySoftKeyboard
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Compresses a list of words and frequencies into a tree structured binary dictionary.
 */
public class MakeBinaryDictionary {
	private static final int DICT_FILE_CHUNK_SIZE = 1000*1000;
	
    public static final int ALPHA_SIZE = 256;

    public static final String TAG_WORD = "w";
    public static final String ATTR_FREQ = "f";

    private static final int FLAG_ADDRESS_MASK  = 0x400000;
    private static final int FLAG_TERMINAL_MASK = 0x800000;
    private static final int ADDRESS_MASK = 0x3FFFFF;
    
    public static final CharNode EMPTY_NODE = new CharNode();

    List<CharNode> roots;
    Map<String, Integer> mDictionary;
    int mWordCount;
    
    static class CharNode {
        char data;
        int freq;
        boolean terminal;
        List<CharNode> children;
        static int sNodes;

        public CharNode() {
            sNodes++;
        }
    }
    
    public static void main(String[] args) throws IOException {
    	final File currentFolder = new File(System.getProperty("user.dir"));
    	final File inputFile = new File(currentFolder, "dict/words.xml");
    	final File tempOutputFile = new File(currentFolder, "dict/words.dict");
    	final File outputFolder = new File(currentFolder, "res/raw/");
    	final File dict_id_array = new File(currentFolder, "/res/values/words_dict_array.xml");
    	
    	System.out.println("Reading words from input "+inputFile.getAbsolutePath());
    	System.out.println("Will store output files under "+outputFolder.getAbsolutePath());
    	System.out.println("Deleting previous versions...");
    	//deleting current files
    	tempOutputFile.delete();
    	File[] dictFiles = outputFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File arg0, String arg1) {
				return arg1.contains(".dict");
			}
		});
    	if (dictFiles != null && dictFiles.length > 0)
    	{
    		for (File file : dictFiles) {
    			file.delete();
			} 
    	}
    	dict_id_array.delete();
    	
    	if (!inputFile.exists()) {
    		System.out.println("No input file found. Quiting.");
    		return;
    	}
        new MakeBinaryDictionary(inputFile.getAbsolutePath(), tempOutputFile.getAbsolutePath());
        //now, if the file is larger than 1MB, I'll need to split it to 1MB chunks and rename them.
        if (tempOutputFile.exists()) {
        	final int file_postfix = splitOutputFile(tempOutputFile, outputFolder);
        	//creating the dict array XML resource
        	XmlWriter xml = new XmlWriter(dict_id_array);
        	xml.writeEntity("resources");
        	xml.writeEntity("array").writeAttribute("name", "words_dict_array");
        	for(int i=1;i<=file_postfix;i++)
        	{
        		xml.writeEntity("item").writeText("@raw/words_"+i).endEntity();
        	}
        	xml.endEntity();
        	xml.endEntity();
        	xml.close();
        	//no need for that temp file
        	tempOutputFile.deleteOnExit();
        }
        
    }

	public static int splitOutputFile(final File tempOutputFile,
			final File outputFolder) throws FileNotFoundException, IOException {
		//output should be words_1.dict....words_n.dict
		InputStream inputStream = new FileInputStream(tempOutputFile);
		int file_postfix = 0;
		int current_output_file_size = 0;
		byte[] buffer = new byte[4*1024];
		OutputStream outputStream = null;
		int read = 0;
		while((read = inputStream.read(buffer)) > 0) {
			if (outputStream != null && current_output_file_size >= DICT_FILE_CHUNK_SIZE) {
				outputStream.flush();
				outputStream.close();
				outputStream = null;
			}
			
			if (outputStream == null) {
				file_postfix++;
				current_output_file_size = 0;
				File chunkFile = new File(outputFolder, "words_"+file_postfix+".dict");
				outputStream = new FileOutputStream(chunkFile);
				System.out.println("Writing to dict file "+chunkFile.getAbsolutePath());
			}
			
			outputStream.write(buffer, 0, read);
			current_output_file_size += read;
		}
		
		inputStream.close();
		if (outputStream != null) {
			outputStream.flush();
			outputStream.close();
			outputStream = null;
		}
		System.out.println("Done. Wrote "+file_postfix+" files.");
		
		return file_postfix;
	}

    public MakeBinaryDictionary(String srcFilename, String destFilename) {
        populateDictionary(srcFilename);
        writeToDict(destFilename);
        // Enable the code below to verify that the generated tree is traversable.
        //traverseDict(0, new char[32], 0);
    }
    
    private void populateDictionary(String filename) {
        roots = new ArrayList<CharNode>();
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(new File(filename), new DefaultHandler() {
                boolean inWord;
                int freq;
                StringBuilder wordBuilder = new StringBuilder(48);

                @Override
                public void startElement(String uri, String localName,
                        String qName, Attributes attributes) {
                    if (qName.equals("w")) {
                        inWord = true;
                        freq = Integer.parseInt(attributes.getValue(0));
                        wordBuilder.setLength(0);
                    }
                }

                @Override
                public void characters(char[] data, int offset, int length) {
                    // Ignore other whitespace
                    if (!inWord) return;
                    wordBuilder.append(data, offset, length);
                }

                @Override
                public void endElement(String uri, String localName,
                        String qName) {
                    if (qName.equals("w")) {
                        if (wordBuilder.length() > 1) {
                            addWordTop(wordBuilder.toString(), freq);
                            mWordCount++;
                        }
                        inWord = false;
                    }
                }
            });
        } catch (Exception ioe) {
            System.err.println("Exception in parsing\n" + ioe);
            ioe.printStackTrace();
        }
        System.out.println("Nodes = " + CharNode.sNodes);
    }

    private int indexOf(List<CharNode> children, char c) {
        if (children == null) {
            return -1;
        }
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).data == c) {
                return i;
            }
        }
        return -1;
    }

    private void addWordTop(String word, int occur) {
        if (occur > 255) occur = 255;
        char firstChar = word.charAt(0);
        int index = indexOf(roots, firstChar);
        if (index == -1) {
            CharNode newNode = new CharNode();
            newNode.data = firstChar;
            newNode.freq = occur;
            index = roots.size();
            roots.add(newNode);
        } else {
            roots.get(index).freq += occur;
        }
        if (word.length() > 1) {
            addWordRec(roots.get(index), word, 1, occur);
        } else {
            roots.get(index).terminal = true;
        }
    }

    private void addWordRec(CharNode parent, String word, int charAt, int occur) {
        CharNode child = null;
        char data = word.charAt(charAt);
        if (parent.children == null) {
            parent.children = new ArrayList<CharNode>();
        } else {
            for (int i = 0; i < parent.children.size(); i++) {
                CharNode node = parent.children.get(i);
                if (node.data == data) {
                    child = node;
                    break;
                }
            }
        }
        if (child == null) {
            child = new CharNode();
            parent.children.add(child);
        }
        child.data = data;
        if (child.freq == 0) child.freq = occur;
        if (word.length() > charAt + 1) {
            addWordRec(child, word, charAt + 1, occur);
        } else {
            child.terminal = true;
            child.freq = occur;
        }
    }

    byte[] dict;
    int dictSize;
    static final int CHAR_WIDTH = 8;
    static final int FLAGS_WIDTH = 1; // Terminal flag (word end)
    static final int ADDR_WIDTH = 23; // Offset to children
    static final int FREQ_WIDTH_BYTES = 1;
    static final int COUNT_WIDTH_BYTES = 1;

    private void addCount(int count) {
        dict[dictSize++] = (byte) (0xFF & count);
    }
    
    private void addNode(CharNode node) {
        int charData = 0xFFFF & node.data;
        if (charData > 254) {
            dict[dictSize++] = (byte) 255;
            dict[dictSize++] = (byte) ((node.data >> 8) & 0xFF);
            dict[dictSize++] = (byte) (node.data & 0xFF);
        } else {
            dict[dictSize++] = (byte) (0xFF & node.data);
        }
        if (node.children != null) {
            dictSize += 3; // Space for children address
        } else {
            dictSize += 1; // Space for just the terminal/address flags
        }
        if ((0xFFFFFF & node.freq) > 255) {
            node.freq = 255;
        }
        if (node.terminal) {
            byte freq = (byte) (0xFF & node.freq);
            dict[dictSize++] = freq;
        }
    }

    int nullChildrenCount = 0;
    int notTerminalCount = 0;

    private void updateNodeAddress(int nodeAddress, CharNode node,
            int childrenAddress) {
        if ((dict[nodeAddress] & 0xFF) == 0xFF) { // 3 byte character
            nodeAddress += 2;
        }
        childrenAddress = ADDRESS_MASK & childrenAddress;
        if (childrenAddress == 0) {
            nullChildrenCount++;
        } else {
            childrenAddress |= FLAG_ADDRESS_MASK;
        }
        if (node.terminal) {
            childrenAddress |= FLAG_TERMINAL_MASK;
        } else {
            notTerminalCount++;
        }
        dict[nodeAddress + 1] = (byte) (childrenAddress >> 16);
        if ((childrenAddress & FLAG_ADDRESS_MASK) != 0) {
            dict[nodeAddress + 2] = (byte) ((childrenAddress & 0xFF00) >> 8);
            dict[nodeAddress + 3] = (byte) ((childrenAddress & 0xFF));
        }
    }

    void writeWordsRec(List<CharNode> children) {
        if (children == null || children.size() == 0) {
            return;
        }
        final int childCount = children.size();
        addCount(childCount);
        //int childrenStart = dictSize;
        int[] childrenAddresses = new int[childCount];
        for (int j = 0; j < childCount; j++) {
            CharNode node = children.get(j);
            childrenAddresses[j] = dictSize;
            addNode(node);
        }
        for (int j = 0; j < childCount; j++) {
            CharNode node = children.get(j);
            int nodeAddress = childrenAddresses[j];
            int cacheDictSize = dictSize;
            writeWordsRec(node.children);
            updateNodeAddress(nodeAddress, node, node.children != null
                    ? cacheDictSize : 0);
        }
    }

    void writeToDict(String dictFilename) {
        // 4MB max, 22-bit offsets
        dict = new byte[4 * 1024 * 1024]; // 4MB upper limit. Actual is probably
                                          // < 1MB in most cases, as there is a limit in the
                                          // resource size in apks.
        dictSize = 0;
        writeWordsRec(roots);
        System.out.println("Dict Size = " + dictSize);
        try {
            FileOutputStream fos = new FileOutputStream(dictFilename);
            fos.write(dict, 0, dictSize);
            fos.close();
        } catch (IOException ioe) {
            System.err.println("Error writing dict file:" + ioe);
        }
    }

    void traverseDict(int pos, char[] word, int depth) {
        int count = dict[pos++] & 0xFF;
        for (int i = 0; i < count; i++) {
            char c = (char) (dict[pos++] & 0xFF);
            if (c == 0xFF) {
                c = (char) (((dict[pos] & 0xFF) << 8) | (dict[pos+1] & 0xFF));
                pos += 2;
            }
            word[depth] = c;
            boolean terminal = (dict[pos] & 0x80) > 0;
            int address = 0;
            if ((dict[pos] & (FLAG_ADDRESS_MASK >> 16)) > 0) {
                address = 
                    ((dict[pos + 0] & (FLAG_ADDRESS_MASK >> 16)) << 16)
                    | ((dict[pos + 1] & 0xFF) << 8)
                    | ((dict[pos + 2] & 0xFF));
                pos += 2;
            }
            pos++;
            if (terminal) {
                showWord(word, depth + 1, dict[pos] & 0xFF);
                pos++;
            }
            if (address != 0) {
                traverseDict(address, word, depth + 1);
            }
        }
    }

    void showWord(char[] word, int size, int freq) {
        System.out.print(new String(word, 0, size) + " " + freq + "\n");
    }
}