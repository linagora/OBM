#!/bin/bash
# TODO: make it work for someone else

ant dist

scp -r dist/* root@10.0.0.5:/usr/lib/obm-autoconf/
