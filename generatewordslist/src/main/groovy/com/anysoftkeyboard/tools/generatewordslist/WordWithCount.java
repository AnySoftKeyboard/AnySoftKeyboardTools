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

class WordWithCount implements Comparable<WordWithCount> {
    private final String mWord;
    private int mFreq;
    private int mCapitalFreq;

    public WordWithCount(String word) {
        mWord = word.toLowerCase();
        mFreq = 0;
        mCapitalFreq = 0;
        addFreq(word);
    }

    public WordWithCount(String word, int frequency) {
        mWord = word.toLowerCase();
        mFreq = frequency;
        mCapitalFreq = Character.isUpperCase(word.charAt(0))? frequency : 0;
    }

    public String getKey() {
        return mWord;
    }

    public String getWord() {
        //if more than 90% of the word occurrences are capital,
        //then use capital style
        if ((mFreq * 0.90) < mCapitalFreq)
            return Character.toUpperCase(mWord.charAt(0)) + mWord.substring(1);
        else
            return mWord;
    }

    public int getFreq() {
        return mFreq;
    }

    public void addFreq(String word) {
        if (mFreq < Integer.MAX_VALUE) mFreq++;
        if (Character.isUpperCase(word.charAt(0))) mCapitalFreq++;
    }

    public void addOtherWord(WordWithCount wordWithCount) {
        mFreq += wordWithCount.mFreq;
        mCapitalFreq += wordWithCount.mCapitalFreq;
    }

    @Override
    public int compareTo(WordWithCount o) {
        return o.mFreq - mFreq;
    }
}
