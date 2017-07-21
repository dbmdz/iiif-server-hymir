# Hymir IIIF Server

[![Build Status](https://travis-ci.org/dbmdz/iiif-server-hymir.svg?branch=master)](https://travis-ci.org/dbmdz/iiif-server-hymir)
[![MIT License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![GitHub release](https://img.shields.io/github/release/dbmdz/iiif-server-hymir.svg?maxAge=2592000)](https://github.com/dbmdz/iiif-server-hymir/releases)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/54890c0e2bce4489ad0793658b2a4d0c)](https://www.codacy.com/app/ralf-eichinger/iiif-server-hymir?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=dbmdz/iiif-server-hymir&amp;utm_campaign=Badge_Grade)
[![codecov](https://codecov.io/gh/dbmdz/iiif-server-hymir/branch/master/graph/badge.svg)](https://codecov.io/gh/dbmdz/iiif-server-hymir)
[![Maven Central](https://img.shields.io/maven-central/v/de.digitalcollections/iiif-server-hymir.svg?maxAge=2592000)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22iiif-server-hymir%22)

Hymir is a Java based IIIF Server. It is based on [IIIF Image API Java Libraries](https://github.com/dbmdz/iiif-image-api "IIIF Image API Java Libraries") and [IIIF Presentation API Java Libraries](https://github.com/dbmdz/iiif-presentation-api "IIIF Presentation API Java Libraries") (Java implementations of the [IIIF specifications](http://iiif.io/technical-details/ "IIIF specifications")).

## Features

- IIIF Image API 2.0.0 compliant (see <a href="http://iiif.io/api/image/2.0/">http://iiif.io/api/image/2.0/</a>).
- IIIF Presentation API 2.0.0 compliant (see <a href="http://iiif.io/api/presentation/2.0/">http://iiif.io/api/presentation/2.0/</a>).
- Based on IIIF Image API Java Library and IIIF Presentation API Java Library.
- On the fly image processing. No additional pregenerated (pyramid zoom) images are needed. No additional storage consumption.
- Can simply be run as a standalone IIIF server from the JAR, no application server necessary
- Spring based modular, extendable, easy to maintain enterprise architecture.
- Highly customizable image storage and identifier resolving: Access to images over project specific Resolver-plugin mechanism.
- Support for Filesystem- and HTTP-Image-Repositories (own protocols can be added by providing specific resolver)
- Highly customizable manifest generation: implement your own mapping from project specific structure metadata to standard Manifest object.
- Configurable, pluggable Image processing engines: Choose one of
    - Java Image I/O API (javax.imageio) (see <a href="http://docs.oracle.com/javase/8/docs/api/javax/imageio/package-summary.html#package.description">Image IO package description</a>)
    - Independent JPEG Group library "libjpeg8" (see <a href="http://ijg.org/">http://ijg.org/</a>)
    - ... more to come (or implement your own as pluggable library)
- Embedded IIIF Image Viewer (for out of the box viewing of served images): OpenSeadragon 2.2.1 (see "Usage" below)
- Embedded IIIF Presentation Viewer: Mirador 2.4.0 (see "Usage" below)
- Direct Manifest access (see "Usage" below)

### Supported image formats

<table border="1">
  <tr>
    <th>Format</th>
    <th>supported</th>
    <th>Image processing engine</th>
    <th>tested</th>
  </tr>
  <tr>
    <td><a href="http://www.jpeg.org/">JPEG</a></td>
    <td>yes</td>
    <td>Java Image I/O</a>
    <td>yes</td>
  </tr>
  <tr>
    <td></td>
    <td>yes</td>
    <td>turbojpeg</a>
    <td>yes</td>
  </tr>
  <tr>
    <td><a href="http://www.libpng.org/pub/png/spec/">PNG</a></td>
    <td>yes</td>
    <td>Java Image I/O</a>
    <td>no</td>
  </tr>
  <tr>
    <td><a href="http://www.jpeg.org/">BMP</a></td>
    <td>yes</td>
    <td>Java Image I/O</a>
    <td>no</td>
  </tr>
  <tr>
    <td><a href="http://www.wapforum.org/what/technical/SPEC-WAESpec-19990524.pdf">WBMP</a></td>
    <td>yes</td>
    <td>Java Image I/O</a>
    <td>no</td>
  </tr>
  <tr>
    <td><a href="http://www.w3.org/Graphics/GIF/spec-gif89a.txt">GIF</a></td>
    <td>yes</td>
    <td>Java Image I/O</a>
    <td>no</td>
  </tr>
</table>

More formats (including TIFF) will be supported soon by using Java Image I/O format plugins from <a href="http://haraldk.github.io/TwelveMonkeys/">TwelveMonkeys ImageIO</a>.

## Prerequisites

- Server with minimum 4GB RAM.
- Java 8

## Installation

Download from GitHub-project page under "releases" and run the JAR with `java -jar`

### Using the TurboJPEG backend
By default, a Java-based image processing backend is used. If you want better
performance, it is recommended to use the native image processing backend
that is based on TurboJPEG. For this, you will have to install a shared library
into `/usr/lib` that the Java code can then load.

If you are running Debian Jessie, you can use the Debian packages provided
on the [Releases](https://github.com/dbmdz/iiif-image-api/releases) page.

For other distributions, you can use the `install_turbojpeg_jni.sh` script in
the repository root. Note that you will need a recent (>=1.8) JDK, a C compiler
and  `libtool` and `nasm` installed. Just run the script as root on the target
machine that runs the application and your image requests should be
significantly faster.

## Configuration

### Logging

The default logging file is configured as `/local/iiif/log/iiif/iiifServer.log`.
To configure another file path you can set it using the environment variable IIIF_SERVER_LOGFILE (e.g. in Tomcat using JAVA_OPTS).

Example Tomcat config:

```sh
$ cd $TOMCAT_HOME/bin
$ vi setenv.sh
#!/bin/sh
...
export JAVA_OPTS="$JAVA_OPTS -DIIIF_SERVER_LOGFILE=/var/log/iiif/iiifServer.log"
...
```

### Image and presentation manifest resolving

Based on unique resource identifiers the server tries to resolve identifiers to a "file:" or "http:" path.
The resolving rules (one rule per line) are configurable with regular expressions in YML-files.

You can pass the path to your custom resolving rules with the `--multiPatternResolvingFile=/path/to/rules.yml` option.

Example file "multiPatternResolving-PROD.yml":

```
...
# This configuration file defines a list of patterns with one ore more substitutions.
# These are used for resolving IDs to a concrete URI, e.g. on the file system, the
# classpath or even a remote HTTP endpoint.
# You can specify multiple substitutions, the resolver will try to match them against
# the desired MIME type and return all that matches
# The repository will then verify which of these URIs are actually readable and return
# the first matching substitution.
# In the example below, we have two MIME types (tiff/jpeg) and for JPEG two resolutions
# in decreasing order of quality, so that the higher-resolution image will be chosen
# if it is available.
- pattern: bsb(\d{8})_(\d{5})
  substitutions:
    - 'file:/var/local/bsb$1/images/original/bsb$1_$2.tif'
    - 'file:/var/local/bsb$1/images/300/bsb$1_$2.jpg'
    - 'file:/var/local/bsb$1/images/150/bsb$1_$2.jpg'

# A simpler example with just a single substitution pattern (for resolving IIIF-manifests)
- pattern: bsb(\d{8})
  substitutions:
    - 'file:/var/local/bsb$1/manifest/bsb$1.json'
...
```

## Usage

Run the application with `java -jar hymir-<version>-exec.jar`

Example:

```sh
$ java -jar hymir-3.0.0-SNAPSHOT-exec.jar --spring.profiles.active=local
```

Access Hymir GUI (e.g. http://localhost:9000/).

## Administration

Monitoring endpoints under http://localhost:9001/actuator (HAL-Browser-GUI), authentication by default: admin/secret (configurable in application.yml)

## Users

- Bavarian State Library, Project Bavarikon: <a href="http://www.bavarikon.de/">http://www.bavarikon.de/</a>
- Bavarian State Library, Project digiPress: <a href="https://digipress.digitale-sammlungen.de/">https://digipress.digitale-sammlungen.de/</a>
- Bavarian State Library, iiif-Bookshelf: <a href="https://iiif.digitale-sammlungen.de/">https://iiif.digitale-sammlungen.de/</a>
