#!/bin/bash
f=`dirname "$0"`
. $f/misc.sh

echo "Fetching from upstream to local respositories..."

exec_at_folder "AnySoftKeyboard" "git fetch upstream"
exec_at_folder "AnySoftKeyboard-API" "git fetch upstream"
exec_at_folder "AnySoftKeyboardTools" "git fetch upstream"
