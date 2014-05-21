#!/bin/bash

find . -type f -name 'ST*.md' -exec sed -i 's/(int /(INT /g' {} \;
find . -type f -name 'ST*.md' -exec sed -i 's/ int / INT /g' {} \;
find . -type f -name 'ST*.md' -exec sed -i 's/int ST/INT ST/g' {} \;
find . -type f -name 'ST*.md' -exec sed -i 's/(double /(DOUBLE /g' {} \;
find . -type f -name 'ST*.md' -exec sed -i 's/ double / DOUBLE /g' {} \;
find . -type f -name 'ST*.md' -exec sed -i 's/double ST/DOUBLE ST/g' {} \;
find . -type f -name 'ST*.md' -exec sed -i 's/(varchar /(VARCHAR /g' {} \;
find . -type f -name 'ST*.md' -exec sed -i 's/ varchar / VARCHAR /g' {} \;
find . -type f -name 'ST*.md' -exec sed -i 's/varchar ST/VARCHAR ST/g' {} \;
find . -type f -name 'ST*.md' -exec sed -i 's/(boolean /(BOOLEAN /g' {} \;
find . -type f -name 'ST*.md' -exec sed -i 's/ boolean / BOOLEAN /g' {} \;
find . -type f -name 'ST*.md' -exec sed -i 's/boolean ST/BOOLEAN ST/g' {} \;
find . -type f -name 'ST*.md' -exec sed -i 's/\([^,]*\),\([^ ]\)/\1, \2/g' {} \;
