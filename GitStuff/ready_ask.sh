#!/bin/bash

function do_clone {
	if [  !  -d $2 ]; then
		echo "Cloning $2..."
		git clone git@github.com:$1/$2.git
		cd $2
		git remote add upstream git://github.com/AnySoftKeyboard/$2.git
		git pull origin master
		git fetch upstream
		cd ..
	else
		echo "Not cloning $2 since folder already here."
	fi
}

USERNAME=$1
if [ -z $USERNAME ]; then
	echo "I need your GitHub username, provide it as the first parameter to the script."
	exit
fi

do_clone $USERNAME AnySoftKeyboard
do_clone $USERNAME AnySoftKeyboard-API
do_clone $USERNAME AnySoftKeyboardTools

echo "Creating links to push-all,pull-all and fetch-all scripts..."
ln -s AnySoftKeyboardTools/GitStuff/push-all.sh push-all.sh
ln -s AnySoftKeyboardTools/GitStuff/pull-all.sh pull-all.sh
ln -s AnySoftKeyboardTools/GitStuff/fetch-all.sh fetch-all.sh

echo "Thanks for cloning AnySoftKeyboard!"