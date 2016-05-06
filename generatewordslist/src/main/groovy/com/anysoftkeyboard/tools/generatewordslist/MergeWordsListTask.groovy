/*
 * Copyright (C) 2016 AnySoftKeyboard
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
package com.anysoftkeyboard.tools.generatewordslist;

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler

import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory

/**
 * Task to merge several word-list files into one
 */
public class MergeWordsListTask extends DefaultTask {

    File[] inputWordsListFiles;
    File outputWordsListFile;

    String[] wordsToDiscard = []

    int maxWordsInList = Integer.MAX_VALUE

    @TaskAction
    def mergeWordsLists() {
        if (inputWordsListFiles == null || inputWordsListFiles.length == 0) throw new IllegalArgumentException("Must specify at least one inputWordsListFiles")
        if (outputWordsListFile == null) throw new IllegalArgumentException("Must supply outputWordsListFile")

        println "Merging ${inputWordsListFiles.length} files for maximum ${maxWordsInList} words, and writing into '${outputWordsListFile}'. Discarding ${wordsToDiscard.length} words."
        HashMap<String, WordWithCount> allWords = new HashMap<>()

        for (File inputFile : inputWordsListFiles) {
            println "Reading ${inputFile}..."
            if (!inputFile.exists()) throw new FileNotFoundException(inputFile.absolutePath);
            SAXParserFactory parserFactor = SAXParserFactory.newInstance();
            SAXParser parser = parserFactor.newSAXParser();
            parser.parse(inputFile, new MySaxHandler(allWords))
        }

        //discarding unwanted words
        if (wordsToDiscard.length > 0) {
            print 'Discarding words...'
            wordsToDiscard.findAll({ word -> if (allWords.remove(word) != null) print '.' })
            println ''
        }

        println 'Sorting list...'
        List<WordWithCount> sortedList = new ArrayList<>(Math.min(maxWordsInList, allWords.size()))
        sortedList.addAll(allWords.values());
        Collections.sort(sortedList);

        println 'Creating output XML file...'
        Writer output = new OutputStreamWriter(new FileOutputStream(outputWordsListFile))
        Parser.createXml(sortedList, output, maxWordsInList);

        output.flush();
        output.close();

        println 'Done.'
    }

    private static class MySaxHandler extends DefaultHandler {
        private HashMap<String, WordWithCount> allWords;
        boolean inWord
        StringBuilder word = new StringBuilder()
        int freq=0

        public MySaxHandler(HashMap<String, WordWithCount> allWords) {
            this.allWords = allWords
        }

        @Override
        void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes)
            if (qName.equals("w")) {
                inWord = true
                freq = Integer.parseInt(attributes.getValue("f"))
                word.setLength(0)
            } else {
                inWord = false
            }
        }

        @Override
        void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length)
            if (inWord) {
                word.append(ch, start, length)
            }
        }

        @Override
        void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName)
            if (qName.equals("w") && inWord) {
                WordWithCount wordWithCount = new WordWithCount(word.toString())
                if (allWords.containsKey(wordWithCount.getKey())) {
                    allWords.get(wordWithCount.getKey()).addOtherWord(wordWithCount)
                } else {
                    allWords.put(wordWithCount.getKey(), wordWithCount);
                }
            }
            inWord = false
        }
    }
}