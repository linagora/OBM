#!/bin/sh
#
# Clear all temporary files

# files ending with ~
rm `find . -name '*~'`

# files starting with .#
rm `find . -name '.#*'`


rm `find . -name '.*.s*'`
