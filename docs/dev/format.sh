#!/bin/bash

# Remove trailing whitespace
find . -type f -name 'ST*.md' -exec sed -i 's/[[:space:]]*$//' {} \;
# Put spaces after commas
find . -type f -name 'ST*.md' -exec sed -i 's/\([^,]*\),\([^ ]\)/\1, \2/g' {} \;
# Capitalize SQL types
sql_type=(int double boolean varchar geometry)
for f in $(find . -type f -name 'ST*.md'); do
    for name in ${sql_type[*]}; do
        upper_name=${name^^}
        # Return type
        sed -i "s/$name ST_/$upper_name ST_/gi" $f
        # First parameter
        sed -i "s/($name /($upper_name /gi" $f
        # Any other parameter
        sed -i "s/, $name /, $upper_name /gi" $f
    done
done
