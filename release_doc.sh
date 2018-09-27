#!/bin/bash

if [[ -z "$1" ]]; then
	echo "Please enter a version."
	exit
fi

cd "$(dirname "$0")/docs"
oldVersion=""
while IFS='' read -r line || [[ -n "$line" ]]; do
	if [[ $line == *"last release" ]] && [[ $line != "* [$1] : last release" ]]; then
		oldVersion=$(echo $line| cut -d'[' -f 2| cut -d']' -f 1)
		echo "* [$1] : last release" >> "Home.md.new"
		echo "${line/last release/former release}" >> "Home.md.new"
	elif [[ $line == "[dev]: ../dev/home" ]]; then
		echo "$line" >> "Home.md.new"
		echo "[$1]: ../$1/home" >> "Home.md.new"
	elif [[ $line != "[$1]: ../$1/home" ]]; then
		echo "$line" >> "Home.md.new"
	fi
done < "Home.md"

rm "Home.md"
mv "Home.md.new" "Home.md"

cp -r "dev" "$1" 
cd "$1" 

oldStr="permalink: /docs/dev/"
newStr="permalink: /docs/$1/"
sed -i -e 's%'"$oldStr"'%'"$newStr"'%g' *.md
sed -i -e 's%'"$oldStr"'%'"$newStr"'%g' */*.md

cd "../.."

sed -i -e 's%'"$oldVersion"'%'"$1"'%g' index.html
