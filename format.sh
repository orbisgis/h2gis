#!/bin/bash

sql_type=(INT DOUBLE BOOLEAN VARCHAR GEOMETRY GEOMETRYCOLLECTION NULL TRUE FALSE)
# Place the files you want to format here
# files=()

# Use the following for a global format:
for f in $(find . -not -path "./_site/*" -type f \( -name "*.html" -o -name "*.md" \) | grep -v "analytics.html" | grep -v "top.html" | grep -v "README.md"); do
# Local format:
# for f in ${files[*]}; do
    # Remove multiple blank lines
    sed -i "/^$/N;/\n$/D" $f
    # Remove trailing whitespace
    sed -i 's/[[:space:]]*$//' $f
    # Put spaces after commas unless followed by a | (in tables)
    sed -i 's/\([^,]*\),\([^ |]\)/\1, \2/g' $f
    # Remove spaces before ":" ";" and ","
    sed -i 's/ \([:;,]\)/\1/g' $f
    # Remove spaces before left parentheses following capitalized words.
    # In tables, preserve | alignment by adding a space just before the |.
    sed -i "s/| \([A-Z]\+\) (\([^|]*\)/| \1(\2 /g" $f
    # In regular text, we don't have to worry about alignment.
    sed -i "s/\([A-Z]\+\) (\([^A-Z]*\)/\1(\2/g" $f
    # Capitalize SQL types
    for name in ${sql_type[*]}; do
        # Return type
        sed -i "s/$name ST_/$name ST_/gi" $f
        # First parameter
        sed -i "s/($name /($name /gi" $f
        # First parameter on split line
        sed -i "s/^\( *\)$name /\1$name /gi" $f
        # Any other parameter
        sed -i "s/, $name /, $name /gi" $f
        # After Answer:
        sed -i "s/\(Answer: *\)$name/\1$name/gi" $f
    done
    # Replace `GEOMETRY` with Geometry
    sed -i "s/\`GEOMETRY\`/Geometry/gi" $f
    # Put space back between 'VALUES' and '('
    sed -i "s/VALUES(/VALUES (/gi" $f
done
