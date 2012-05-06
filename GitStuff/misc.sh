#!/bin/bash

function no_repositories {
	echo "Could not find AnySoftKeyboard repositories at this folder. Have you clone them? Are you running this script from their parent folder?"
	exit
}

function exec_at_folder {
	cd $1
	$2
	cd ..
}

function verify_repositories {
	if [  ! -d "AnySoftKeyboard" ]; then
		no_repositories
	fi
	if [  ! -d "AnySoftKeyboard-API" ]; then
		no_repositories
	fi
	if [  ! -d "AnySoftKeyboardTools" ]; then
		no_repositories
	fi
}

verify_repositories
