#!/bin/bash
f=`dirname "$0"`
. $f/misc.sh

echo "Pushing local commits to your remote respositories..."

exec_at_folder "AnySoftKeyboard" "git push origin master"
exec_at_folder "AnySoftKeyboard-API" "git push origin master"
exec_at_folder "AnySoftKeyboardTools" "git push origin master"
