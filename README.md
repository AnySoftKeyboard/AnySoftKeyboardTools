AnySoftKeyboardTools
====================

# Make-Dictionary (`makedictioanry`)
Builds a binary dictionary from word-list XML file.

## Input
Input should be an XML file in the following format:
```
<?xml version="1.0" encoding="UTF-8"?>
<wordlist>
    <w f="255">Hello</w>
    <w f="255">Goodbye</w>
    <w f="254">Bye</w>
```
`f` is the frequency of the word - a value between _255..1_ - where higher value
means more frequent (common) word.

## Output
Will produce the followings:
 1. A `resources-array` resource file `words_dict_array.xml`. Should be placed under `/res/values/`
 2. A set of binary files which are readable by [AnySoftKeyboard](https://github.com/AnySoftKeyboard/AnySoftKeyboard).
 These files should be placed under `/res/raw/` resources folder.

Some Android versions had issues with reading `raw` files which are larger than 1MB.
This utility will break your binary dictionary file into multiple files if
the size of the binary data is more than 1MB.

## How to use
This is a Gradle Plugin.
First, add this to the build-script class path:
```
buildscript {
    repositories {
        //any other repositories

        //AnySoftKeyboard-Tools' repository
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        //any other dependencies
        classpath 'com.github.AnySoftKeyboard.AnySoftKeyboardTools:makedictionary:-SNAPSHOT'
    }
}
```
Then add a `Task` to create the binary-dictionary:
```
task makeEnglishDictionary(type: com.anysoftkeyboard.tools.makedictionary.MakeDictionaryTask) {
    inputWordsListFile new File(project.getProjectDir(), "english_dictionary/words.xml")
}
```

optionally, if your `res` folder is non-standard, or you have multiple such folders, you may want to specify its location:
```
task makeEnglishDictionary(type: com.anysoftkeyboard.tools.makedictionary.MakeDictionaryTask) {
    inputWordsListFile new File(project.getProjectDir(), "english_dictionary/words.xml")
    resourcesFolder new File(project.getProjectDir(), "src/main/res/")
}
```

To create the dictionary, run the task from command-line:
```
./gradlew makeEnglishDictionary
```

See an actual, real world, usage at [AnySoftKeyboard](https://github.com/AnySoftKeyboard/AnySoftKeyboard/blob/master/build.gradle)