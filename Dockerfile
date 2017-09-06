## The goal of this machine is to build packages and expose them into DEB and RPM repositories

## Usage - build and expose packages:
##	cd OBM_SOURCES_ROOT_DIRECTORY
##	docker build --tag build-obm .
##	docker run --name expose-packages --rm --publish 8888:80 build-obm

## Usage - APT configuration example
## 	 deb http://HOST_RUNNING_DOCKER:8888/deb obm obm
##	 deb http://deb.obm.org/30/contrib wheezy obm

## Usage - YUM configuration example
##	 [obm-stable]
##	 name=obm-stable
##	 baseurl=http://HOST_RUNNING_DOCKER:8888/rpm
##	 enabled=1
##	 gpgcheck=0

FROM debian:7.3

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && apt-get install -y \
     python locales locales-all git \
     devscripts ant ant-optional openjdk-7-jre-headless cdbs \
     reprepro maven openjdk-7-jdk wget webfs rpm createrepo

RUN mkdir /root/build-{deb,el6} && \
    mkdir -p /repo/deb/conf && \
    mkdir -p /repo/rpm

## COPY OBM SOURCES
ADD . obm

## BUILD OBM PACKAGES
RUN cd obm/java/sync && \
    mvn dependency:go-offline || true && \
    mvn package -T1C

RUN cd obm/build-system && \
     ./build.py /root/build-deb --nocompile --osversion wheezy all && \
     ./build.py /root/build-el6 --nocompile --osversion el6 all

## BUILD DEB REPOSITORY
COPY docker-resources/distributions /repo/deb/conf/
RUN cd /repo/deb && find /root/build-deb -name \*.deb -exec reprepro -V includedeb obm {} \;

## BUILD RPM REPOSITORY
RUN cp /root/build-el6/*.rpm /repo/rpm/ ; createrepo /repo/rpm

EXPOSE 80

CMD webfsd -F -p 80 -r /repo/


