#!/bin/bash
set -e

JAVA_HOME=$(readlink -f `which javac` | sed "s:/bin/javac::")

apt-get update
apt-get -y install autoconf build-essential nasm libtool ruby ruby-dev
gem install fpm
cd ./libjpeg-turbo
mkdir -p ./build
cd ./build
JNI_CFLAGS="-I$JAVA_HOME/include -I/usr/include -I$JAVA_HOME/include/linux" ../configure --with-java
JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF8 make
cp .libs/libturbojpeg.so.0.1.0 ../../libturbojpeg-jni.so
cd ../../
fpm -s dir -t deb -n libturbojpeg-jni -v 1.5.1 --prefix /usr/lib libturbojpeg-jni.so
rm -rf libturbojpeg-jni.so libjpeg-turbo/build
chmod a+rw *.deb
