package com.anysoftkeyboard.tools.makedictionary

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Task to create a binary-dictionary readable by AnySoftKeyboard
 */
public class MakeDictionaryTask extends DefaultTask {

    File inputWordsListFile;
    File resourcesFolder

    @TaskAction
    def makeDictionary() {
        com.android.build.gradle.AppExtension androidConfiguration = project.android;
        if (resourcesFolder == null) resourcesFolder = androidConfiguration.sourceSets["main"].res.srcDirs[0]
        MainClass.buildDictionary(inputWordsListFile, resourcesFolder)
    }
}