# Hymir IIIF Server

[![Build Status](https://travis-ci.org/dbmdz/iiif-server-hymir.svg?branch=master)](https://travis-ci.org/dbmdz/iiif-server-hymir)
[![MIT License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![GitHub release](https://img.shields.io/github/release/dbmdz/iiif-server-hymir.svg?maxAge=2592000)](https://github.com/dbmdz/iiif-server-hymir/releases)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/54890c0e2bce4489ad0793658b2a4d0c)](https://www.codacy.com/app/ralf-eichinger/iiif-server-hymir?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=dbmdz/iiif-server-hymir&amp;utm_campaign=Badge_Grade)
[![codecov](https://codecov.io/gh/dbmdz/iiif-server-hymir/branch/master/graph/badge.svg)](https://codecov.io/gh/dbmdz/iiif-server-hymir)
[![Maven Central](https://img.shields.io/maven-central/v/de.digitalcollections/iiif-server-hymir.svg?maxAge=2592000)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22iiif-server-hymir%22)

Hymir is a Java based IIIF Server. It is based on our [IIIF API Java Libraries](https://github.com/dbmdz/iiif-apis) (Java implementations of the [IIIF specifications](http://iiif.io/technical-details/)). It can be used to serve images, presentation manifests and presentation collections.

## Features

- IIIF Image API 2.1 compliant (see <a href="http://iiif.io/api/image/2.1/">http://iiif.io/api/image/2.1/</a>).
- IIIF Presentation API 2.1 compliant (see <a href="http://iiif.io/api/presentation/2.1/">http://iiif.io/api/presentation/2.1/</a>).
- On the fly image processing. No additional pregenerated (pyramid zoom) images are needed. No additional storage consumption.
- Can simply be run as a standalone IIIF server from the JAR, no application server necessary
- Spring based modular, extendable, easy to maintain enterprise architecture.
- Highly customizable image storage and identifier resolving: Access to images over project specific Resolver-plugin mechanism.
- Support for Filesystem- and HTTP-Image-Repositories (own protocols can be added by providing specific resolver)
- Pluggable Manifest generation: implement your own mapping from project specific structure metadata to a standard Manifest object.
- Embedded IIIF Image Viewer (for out of the box viewing of served images): OpenSeadragon 2.3.1 (see "Usage" below)
- Embedded IIIF Presentation Viewer: Mirador 2.6.0 (see "Usage" below)
- Direct Manifest access (see "Usage" below)

### Supported image formats

| Format    | Reading  | Writing | Dependencies                               |
| --------- | -------- | ------- | ------------------------------------------ |
|   JPEG    |   [x]    |   [x]   |   libturbojpeg (optional, but recommended) |
|  JPEG2000 |   [x]    |   [ ]   |   libopenjp2 (>= 2.3 recommended)          |
|    TIFF   |   [x]    |   [x]   |                                            |
|    PNG    |   [x]    |   [x]   |                                            |
|    BMP    |   [x]    |   [x]   |                                            |
|    GIF    |   [x]    |   [x]   |                                            |


## Prerequisites

- Server with minimum 4GB RAM.
- Java 8

## Installation

Download `hymir-<version>-exec.jar` from the GitHub [releases](https://github.com/dbmdz/iiif-server-hymir/releases) page.

## Usage

Run the downloaded application with `java -jar hymir-<version>-exec.jar`

- Example with Logstash-JSON-logging to ./hymir.log:

```sh
$ java -jar hymir-4.0.0-exec.jar
```

- Example with logging to console:

```sh
$ java -jar hymir-4.0.0-exec.jar --spring.profiles.active=local
```

- Example with logging to console and custom file resolving rules:

```sh
$ java -jar hymir-4.0.0-exec.jar --spring.profiles.active=local --rules=file:/etc/hymir/rules.yml
```

Access Hymir GUI (e.g. http://localhost:9000/).

## Configuration

### Using the TurboJPEG backend
By default, a Java-based image processing backend is used. If you want better
performance, it is recommended to use the native image processing backend
that is based on TurboJPEG. For this, you will have to install the TurboJPEG
native library, on Ubuntu `libturbojpeg`.

### Logging

(Configured in logback-spring.xml)

The default logging file is configured as `./hymir.log` in Logstash-JSON-format.

If you want human readable logging to console use `--spring.profiles.active=local` on start command line.


### Image and presentation manifest resolving

Based on unique resource identifiers the server tries to resolve identifiers to a "file:" or "http:" path.
The resolving rules (one rule per line) are configurable with regular expressions in YML-files.

You can pass the path to your custom resolving rules with the `--rules=/path/to/rules.yml` option.

Example file "rules.yml":

```yaml
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
# An image pattern resolving example:
- pattern: ^(\d{8})_(\d{5})$
  substitutions:
    - 'file:/var/local/iiif/images/$1/original/image_$1_$2.tif'
    - 'file:/var/local/iiif/images/$1/300/image_$1_$2.jpg'
    - 'file:/var/local/iiif/images/$1/150/image_$1_$2.jpg'

# An manifest pattern resolving example:
- pattern: ^(\d{8})$
  substitutions:
    - 'file:/var/local/iiif/presentation/manifests/manifest_$1.json'

# For the official IIIF Image API Validator
- pattern: 67352ccc-d1b0-11e1-89ae-279075081939
  substitutions:
    - 'classpath:validation.jp2'
    - 'classpath:validation.png'

# Collection manifests ('collection-' pattern-prefix is statically added to requested collection name to disambigued from other patterns)
- pattern: ^collection-(.*)$
  substitutions:
    - 'file:/var/local/iiif/presentation/collections/$1.json'
```

### Serve images

See <https://iiif.io/api/image/2.1/>

In the simplest case you just want to serve images of a directory.

Example: 

Let's assume you have a bunch of jpg-files residing in the directory "/var/local/iiif/images" for objects with identifiers `00000001`, `00000002` and so on. The `images`-directory contains a directory for each object in which in turn all images of an object reside.
The files are named "image_00000001_00001.jpg", "image_00000001_00002.jpg", ... thus containing the object id and the image number in the filename.

An example for a rules.yml entry (including matching just for identifiers and numbered images with digits) then just could look like this:

```yaml
- pattern: ^(\d{8})_(\d{5})$
  substitutions:
    - 'file:/var/local/iiif/images/$1/image_$1_$2.jpg'
```

An IIIF Image API url example for this pattern:

```
http://localhost:9000/image/v2/00000005_00012/full/full/0/default.jpg
```

#### Change Image API URL prefix

By default the url prefix of the IIIF Image API endpoint is `/image/v2/`.
You can configure another url prefix on server startup using system property `custom.iiif.image.urlPrefix`.

Example:

```sh
$ java -Dcustom.iiif.image.urlPrefix='/iiifImage/' -jar target/hymir-4.0.0-exec.jar --rules=file:/etc/hymir/rules.yml --spring.profiles.active=local
```

Resulting URL: `http://localhost:9000/iiifImage/00113391_00001/full/300,/0/default.jpg`

### Serve IIIF Presentation manifests

See <https://iiif.io/api/presentation/2.1/#manifest>

In the simplest case you just want to serve static (pregenerated) IIIF Presentation manifest json-files of a directory.

Example:

Let's assume you have a bunch of json-files residing in the directory "/var/local/iiif/presentation/manifests" for the objects with identifiers `00000001`, `00000002` and so on.
The files are named "manifest_00000001.json", "manifest_00000002.json", ... containing the object id in the filename.

An example for a rules.yml entry (including matching just for identifiers with digits) then could look like this:

```yaml
- pattern: ^(\d{8})$
  substitutions:
    - 'file:/var/local/iiif/presentation/manifests/manifest_$1.json'
```

An IIIF Presentation API url for a manifest example:

```
http://localhost:9000/presentation/v2/00000002/manifest
```

#### Change Presentation API URL prefix

By default the url prefix of the IIIF Presentation API endpoint is `/presentation/v2/`.
You can configure another url prefix on server startup using system property `custom.iiif.presentation.urlPrefix`.

Example:

```sh
$ java -Dcustom.iiif.presentation.urlPrefix='/iiifPresentation/' -jar target/hymir-4.0.0-exec.jar --rules=file:/etc/hymir/rules.yml --spring.profiles.active=local
```

Resulting URL: `http://localhost:9000/iiifPresentation/00113391/manifest`

### Serve IIIF Presentation collections

See <https://iiif.io/api/presentation/2.1/#collection>

In the simplest case you just want to serve static (pregenerated) IIIF Presentation collection json-files of a directory.

Example:

Let's assume you have a bunch of json-files residing in the directory "/var/local/iiif/presentation/collections" for collections with a specific name, e.g. `newspapers`.
The files are named e.g. `newspapers.json`, `medieval_manuscripts.json`, ... containing the collection name in the filename.

An example for a rules.yml entry then just could look like this:

```yaml
- pattern: ^collection-(.*)$
  substitutions:
    - 'file:/var/local/iiif/presentation/collections/$1.json'
```

An IIIF Presentation API url for a collection example:

```
http://localhost:9000/presentation/v2/collection/newspapers
```

Implementation background: To get a regex resolvable pattern that can be differentiated from patterns for manifest json-files (same mimetype), Hymir adds the static prefix `collection-` to the given identifier for collections. (This does not appear in the identifier in the url, just in the rules.yml regex)

## Administration

Monitoring endpoints under http://localhost:9001/monitoring (HAL-Browser-GUI), authentication by default: admin/secret (configurable in application.yml)

## Users

- Bavarian State Library, Project "bavarikon": <a href="http://www.bavarikon.de/">https://www.bavarikon.de/</a>
- Bavarian State Library, Project "Digital East Asian Collections": <a href="https://eastasia.digital-collections.de/">https://eastasia.digital-collections.de/</a>
- Bavarian State Library, Project digiPress: <a href="https://digipress.digitale-sammlungen.de/">https://digipress.digitale-sammlungen.de/</a>
- Bavarian State Library, iiif-Bookshelf: <a href="https://iiif.digitale-sammlungen.de/">https://iiif.digitale-sammlungen.de/</a>

## Development

* Install git client
* Install Java JDK 1.8 or above
* Install Apache Maven buildttool

```sh
$ cd ~/development
$ git clone git@github.com:dbmdz/iiif-server-hymir.git
$ cd iiif-server-hymir
$ mvn clean install
```

Executable JAR-file is in `target` directory.

On systems without installed `libturbojpeg` test fail.
To build without tests, execute:

```sh
$ mvn clean install -DskipTests=true
```

To install `libturbojpeg` on Debian based systems:

````bash
$ sudo apt install libturbojpeg
```