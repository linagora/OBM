#!/bin/bash
# TODO: make it work for someone else

ant dist

scp -r dist/* root@10.92.0.15:/usr/lib/obm-autoconf/
