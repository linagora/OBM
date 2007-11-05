#!/bin/bash

funambol_path=/usr/lib/funambol-6.5.1/funambol

ant pack && \
sudo cp ../output/obm-0.1.1/lib/obm-0.1.1.jar \
${funambol_path}/WEB-INF/lib/ && \
sudo /etc/init.d/obm-tomcat restart
