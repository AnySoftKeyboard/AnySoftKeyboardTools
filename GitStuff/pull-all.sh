#!/bin/bash
f=`dirname "$0"`
. $f/misc.sh

echo "Pulling from your repositories..."
exec_at_folder "AnySoftKeyboard" "git pull origin master"
exec_at_folder "AnySoftKeyboard-API" "git pull origin master"
exec_at_folder "AnySoftKeyboardTools" "git pull origin master"