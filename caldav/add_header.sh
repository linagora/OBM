#!/bin/bash

files=`find . -name '*.java'`

add_header_to_file() {
    file_to_process=$1
    first_line=`head -n1 ${file_to_process} > ./test_fl.tmp`
    grep -q "^package" ./test_fl.tmp
    should_add=$?

    test 0 -eq $should_add && {
	echo "headers should be added to $1"
	cat header.txt > ./f_with_headers.tmp
	cat ${file_to_process} >> ./f_with_headers.tmp
	cp ./f_with_headers.tmp ${file_to_process}
	rm -f ./f_with_headers.tmp
	echo "HEADERS ADDED."
    }
    test 0 -eq $should_add || {
	echo "headers should NOT be added to $1"
    }
    rm -f test_fl.tmp
}


for i in ${files}; do
    add_header_to_file $i
done
