package com.anysoftkeyboard.tools.makedictionary

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.xml.sax.SAXException

import javax.xml.parsers.ParserConfigurationException

/**
 * Task to create a binary-dictionary readable by AnySoftKeyboard
 */
public class MakeDictionaryTask extends DefaultTask {

    File inputWordsListFile;
    File resourcesFolder

    @TaskAction
    def makeDictionary() {
        def androidConfiguration = project.android;
        println "androidConfiguration is "+androidConfiguration
        MainClass.buildDictionary(inputWordsListFile, resourcesFolder)
    }
}