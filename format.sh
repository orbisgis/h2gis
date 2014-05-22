#!/bin/bash

sql_type=(int double boolean varchar geometry geometrycollection)
for f in $(find . -not -path "./_site/*" -type f \( -name "*.html" -o -name "*.md" \) | grep -v "top.html" | grep -v "README.md"); do
    # Remove trailing whitespace
    sed -i 's/[[:space:]]*$//' $f
    # Put spaces after commas
    sed -i 's/\([^,]*\),\([^ ]\)/\1, \2/g' $f
    # Capitalize SQL types
    for name in ${sql_type[*]}; do
        upper_name=${name^^}
        # Return type
        sed -i "s/$name ST_/$upper_name ST_/gi" $f
        # First parameter
        sed -i "s/($name /($upper_name /gi" $f
        # Any other parameter
        sed -i "s/, $name /, $upper_name /gi" $f
    done
    # Remove spaces before left parentheses following capitalized words.
    # In tables, preserve | alignment by adding a space just before the |.
    sed -i "s/| \([A-Z]\+\) (\([^|]*\)/| \1(\2 /g" $f
    # In regular text, we don't have to worry about alignment.
    sed -i "s/\([A-Z]\+\) (\([^A-Z]*\)/\1(\2/g" $f
    # Remove multiple blank lines
    sed -i "/^$/N;/\n$/D" $f
done
