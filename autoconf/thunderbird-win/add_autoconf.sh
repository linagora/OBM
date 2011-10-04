#!/bin/bash

#Ajoute l'autoconf à un installeur thunderbird
if [ "$1" == "" ]; then
  echo Error : missing parameter 1
  echo Usage : add_autoconf.bat thunderbird-installer.exe
  exit 1
fi
7z x -otb-extracted "$1"
cd tb-extracted
#Ajouter l'autoconf
cp ../config.jsc nonlocalized
cat ../all.js.add >> nonlocalized/greprefs/all.js
#recompresser
7z a -t7z -r ../custom.7z * -mx -m0=BCJ2 -m1=LZMA:d24 -m2=LZMA:d19 -m3=LZMA:d19 -mb0:1 -mb0s1:2 -mb0s2:3
cd ..
#transformer l'archive en setup
cat mozilla/7zSD.sfx mozilla/app.tag custom.7z > thunderbird_setup_autoconf.exe
#rm custom.7z
rm -rf tb-extracted
