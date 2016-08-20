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
        if (resourcesFolder == null) resourcesFolder = new File(project.projectDir, "/src/main/res/")
        MainClass.buildDictionary(inputWordsListFile, resourcesFolder)
    }
}