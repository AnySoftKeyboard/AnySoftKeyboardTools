package com.anysoftkeyboard.tools.generatewordslist;

import java.io.*;
import java.util.*;

class Parser {

    private final static int LOOKING_FOR_WORD_START = 1;
    private final static int LOOKING_FOR_WORD_END = 2;
    private final InputStreamReader mInput;
    private final OutputStreamWriter mOutput;
    private final HashSet<Character> mLangChars;
    private final HashSet<Character> mLangInnerChars;
    private final HashMap<String, WordWithCount> mWords;
    private final long mInputSize;
    private final int mMaxListSize;
    private final Locale mLocale;
    public Parser(File inputFile, File outputFile, char[] wordCharacters, Locale locale, char[] additionalInnerWordCharacters, int maxListSize) throws IOException {
        if (!inputFile.exists()) throw new IOException("Could not file input file " + inputFile);
        if (!inputFile.isFile()) throw new IOException("Input must be a file.");

        mLocale = locale;
        mMaxListSize = maxListSize;
        mInputSize = inputFile.length();
        mInput = new InputStreamReader(new FileInputStream(inputFile));
        mOutput = new OutputStreamWriter(new FileOutputStream(outputFile));

        mLangInnerChars = new HashSet<>(additionalInnerWordCharacters.length + wordCharacters.length);
        mLangChars = new HashSet<>(wordCharacters.length);
        for (char c : wordCharacters) {
            mLangChars.add(c);
            mLangInnerChars.add(c);
        }

        for (char c : additionalInnerWordCharacters) {
            mLangInnerChars.add(c);
        }

        mWords = new HashMap<>();

        System.out.println(String.format(Locale.US, "Parsing '%s' for maximum %d words, and writing into '%s'.", inputFile, mMaxListSize, outputFile));
    }

    public void parse() throws IOException {
        System.out.println("Reading input...");
        addWordsFromInputStream(mInput);

        mInput.close();

        System.out.println("Sorting list...");
        List<WordWithCount> sortedList = new ArrayList<>(Math.min(mWords.size(), mMaxListSize));
        sortedList.addAll(mWords.values());
        Collections.sort(sortedList);

        System.out.println("Creating output XML file...");
        createXml(sortedList, mOutput, mMaxListSize);

        mOutput.flush();
        mOutput.close();

        System.out.println("Done.");
    }

    public static void createXml(List<WordWithCount> sortedList, Writer outputWriter, int maxListSize) throws IOException {
        final int wordsCount = Math.min(maxListSize, sortedList.size());

        XmlWriter writer = new XmlWriter(outputWriter, false, 0, true);
        writer.writeEntity("wordlist");
        for (int wordIndex = 0; wordIndex < wordsCount; wordIndex++) {
            WordWithCount word = sortedList.get(wordIndex);
            writer.writeEntity("w").writeAttribute("f", Integer.toString(calcActualFreq(wordIndex, wordsCount))).writeText(word.getWord()).endEntity();
        }
        System.out.println("Wrote "+wordsCount+" words.");
        writer.endEntity();
    }

    private static int calcActualFreq(double wordIndex, double wordsCount) {
        return Math.min(255, 1 + (int) (255 * (wordsCount - wordIndex) / wordsCount));
    }

    private void addWordsFromInputStream(InputStreamReader input) throws IOException {
        StringBuilder word = new StringBuilder();
        int intChar;

        int state = LOOKING_FOR_WORD_START;
        int read = 0;
        while ((intChar = input.read()) > 0) {
            if ((read % 50000) == 0) {
                System.out.println("Read " + read + " out of " + mInputSize + " (" + ((100 * read) / mInputSize) + "%)...");
            }
            char currentChar = (char) intChar;
            read++;
            switch (state) {
                case LOOKING_FOR_WORD_START:
                    if (mLangChars.contains(currentChar)) {
                        word.append(currentChar);
                        state = LOOKING_FOR_WORD_END;
                    }
                    break;
                case LOOKING_FOR_WORD_END:
                    if (mLangInnerChars.contains(currentChar)) {
                        word.append(currentChar);
                    } else {
                        addWord(word);
                        word.setLength(0);
                        state = LOOKING_FOR_WORD_START;
                    }
            }
        }
        //last word?
        if (word.length() > 0)
            addWord(word);
    }

    private void addWord(StringBuilder word) {
        //removing all none chars from the end.
        String typedWord = word.toString();
        String wordKey = typedWord.toLowerCase(mLocale);
        if (mWords.containsKey(wordKey)) {
            mWords.get(wordKey).addFreq(typedWord);
        } else {
            mWords.put(wordKey, new WordWithCount(typedWord));
        }
    }

}
