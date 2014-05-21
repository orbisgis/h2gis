#!/bin/bash

# Capitalize SQL types
sql_type=(int double boolean varchar)
for f in $(find . -type f -name 'ST*.md'); do
    for name in ${sql_type[*]}; do
        upper_name=${name^^}
        sed -i "s/($name /($upper_name /g" $f
        sed -i "s/ $name / $upper_name /g" $f
        sed -i "s/$name ST/$upper_name ST/g" $f
    done
done
# Put spaces after commas
find . -type f -name 'ST*.md' -exec sed -i 's/\([^,]*\),\([^ ]\)/\1, \2/g' {} \;
# Remove trailing whitespace
find . -type f -name 'ST*.md' -exec sed -i 's/[[:space:]]*$//' {} \;
