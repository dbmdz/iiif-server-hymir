jpegtran
========
JNI wrapper for libjpeg

Build:
------
$ mvn clean install

Troubleshooting:
----------------
Problem:  jconfig.h not found
Solution: $ sudo apt-get install libjpeg8-dev

Problem:  jni.h not found
Solution: JAVA_HOME wird anhand javac location errechnet (s. Makefile)

Problem:  jni_md.h not found
Solution: add ..../include/linux to CFLAGS
CFLAGS=-g -c -fPIC -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux -I./src/main/native -ljpeg

Problem:  mvn clean install -> Tests schlagen fehl: 
java.lang.UnsatisfiedLinkError: /tmp/libjpegtran-jni1514378406830677596.so: /tmp/libjpegtran-jni1514378406830677596.so: undefined symbol: jpeg_resync_to_restart
    a  t  java.lang.ClassLoader$ NativeLibrary.load(Native Method)
    a  t  java.lang.ClassLoader. loadLibrary1(ClassLoader.java:1965)
    a  t  java.lang.ClassLoader. loadLibrary0(ClassLoader.java:1890)
    a  t  java.lang.ClassLoader. loadLibrary(ClassLoader.java:1851)
    a  t  java.lang.Runtime.load 0(Runtime.java:795)
    a  t  java.lang.System.load( System.java:1062)
    a  t  org.mdz.jpegtran.Libra ryLoader.loadLibrary(LibraryLoader.java:46)
    a  t  org.mdz.jpegtran.Trans formation.<clinit>(Transformation.java:16)
    a  t  org.mdz.jpegtran.JpegI mage.transpose(JpegImage.java:91)
    a  t  org.mdz.jpegtran.TestJ pegImage.testTranspose(TestJpegImage.java:85)
Solution:
Auf SuSE-Server kompilieren und installieren mit: make install (kopiert target/classes/libjpegtran-jni.so nach /usr/lib64)
(Ubuntu verwendet andere/nicht-kompatible jpeg-Implementierung/Library (libjpeg-turbo8-dev))

Problem: mvn clean install -> Tests schlagen unter Ubuntu fehl und deswegen wird JAR nicht gebaut
Solution: mvn clean install -DskipTests=true