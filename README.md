# Hymir IIIF Server

[![Javadocs](https://javadoc.io/badge/de.digitalcollections/iiif-server-hymir.svg)](https://javadoc.io/doc/de.digitalcollections/iiif-server-hymir)
[![Build Status](https://img.shields.io/travis/dbmdz/iiif-server-hymir/master.svg)](https://travis-ci.org/dbmdz/iiif-server-hymir)
[![Codecov](https://img.shields.io/codecov/c/github/dbmdz/iiif-server-hymir/master.svg)](https://codecov.io/gh/dbmdz/iiif-server-hymir)
[![License](https://img.shields.io/github/license/dbmdz/iiif-server-hymir.svg)](LICENSE)
[![GitHub release](https://img.shields.io/github/release/dbmdz/iiif-server-hymir.svg)](https://github.com/dbmdz/iiif-server-hymir/releases)
[![Maven Central](https://img.shields.io/maven-central/v/de.digitalcollections/iiif-server-hymir.svg)](https://search.maven.org/search?q=a:iiif-server-hymir)

Hymir is a Java based IIIF Server. It is based on our [IIIF API Java Libraries](https://github.com/dbmdz/iiif-apis) (Java implementations of the [IIIF specifications](http://iiif.io/technical-details/)). It can be used to serve images, presentation manifests, presentation collections and presentation annotation lists.

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
- Embedded IIIF Presentation Viewer: Mirador 2.7.0 (see "Usage" below)
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

Run the downloaded application:

- locally for testing (default active configuration profile is "local"):

```sh
$ java -jar hymir-<version>-exec.jar
```
Logging: to console

Image, manifest, collection and annotation list file resolving: see [here](src/main/resources/application.yml#L49) (using directories under `/var/local/iiif`)

Application configuration: see [here](src/main/resources/application.yml) (`local` profile section at beginning of file)

- in production

```sh
$ java -jar hymir-4.0.0-exec.jar --spring.profiles.active=PROD
```

Logging: to file `./hymir.log` in [Logstash](https://www.elastic.co/de/products/logstash)-JSON format

Image, manifest, collection and annotation list file resolving: see [here](src/main/resources/application.yml#L49) (using directories under `/var/local/iiif`)

Application configuration: see [here](src/main/resources/application.yml) (`PROD` profile section overriding some values at bottom of file)

- in production with custom logging configuration file:

```sh
$ java -Dlogging.config=file:/etc/hymir/logback-spring.xml -jar hymir-4.0.0-exec.jar --spring.profiles.active=PROD
```

Read <https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-logging.html> and <https://logback.qos.ch/manual/configuration.html>.

- in production with custom file resolving rules (to specify images, manifest and collection locations):

```sh
$ java -jar hymir-4.0.0-exec.jar --spring.profiles.active=PROD --rules=file:/etc/hymir/rules.yml
```

- in production with custom configuration file `application.yml`:

(Custom `application.yml` placed beside jar-file. No explicit command line option needed.)

```sh
$ java -jar hymir-4.0.0-exec.jar --spring.profiles.active=PROD
```

- in production with a custom server port (e.g. port 8080):

```sh
$ java -Dserver.port=8080 -jar hymir-4.0.0-exec.jar --spring.profiles.active=PROD
```

Complete parametrized example:

```sh
$ java -Dserver.port=8080 -Dlogging.config=file:/etc/hymir/logback-spring.xml -jar hymir-4.0.0-exec.jar --spring.profiles.active=PROD --rules=file:/etc/hymir/rules.yml
```

(and `application.yml` beside jar file).

Access Hymir GUI (e.g. http://localhost:9000/).

## Configuration

### Image and presentation manifest resolving

Based on unique resource identifiers the server tries to resolve identifiers to a `file:` or `http:` path.
The resolving rules (one rule per line) are configurable with regular expressions in YML-files.

You can pass the path to your custom resolving rules with the `--rules=/path/to/rules.yml` option.

Example file `rules.yml`:

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

An example for an entry in the `rules.yml`  (including matching just for identifiers and numbered images with digits) then just could look like this:

```yaml
- pattern: ^(\d{8})_(\d{5})$
  substitutions:
    - 'file:/var/local/iiif/images/$1/image_$1_$2.jpg'
```

An IIIF Image API url example for this pattern: `http://localhost:9000/image/v2/00000005_00012/full/full/0/default.jpg`

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

An example for an entry in the `rules.yml` (including matching just for identifiers with digits) then could look like this:

```yaml
- pattern: ^(\d{8})$
  substitutions:
    - 'file:/var/local/iiif/presentation/manifests/manifest_$1.json'
```

An IIIF Presentation API url for a manifest example: `http://localhost:9000/presentation/v2/00000002/manifest`

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

An example for an entry in the `rules.yml` then just could look like this:

```yaml
- pattern: ^collection-(.*)$
  substitutions:
    - 'file:/var/local/iiif/presentation/collections/$1.json'
```

An IIIF Presentation API url for a collection example: `http://localhost:9000/presentation/v2/collection/newspapers`

Implementation background: To get a regex resolvable pattern that can be differentiated from patterns for manifest json-files (same mimetype), Hymir adds the static prefix `collection-` to the given identifier for collections. (This does not appear in the identifier in the url, just in the rules.yml regex)

### Logging

Default logging configuration is specified in the file `logback-spring.xml` packaged in the exectable Hymir JAR-file. The default logging file is configured as `./hymir.log` in Logstash-JSON-format.

If you want human readable logging to console use `--spring.profiles.active=local` on start command line or define a custom `logback-spring.xml` config location (see "Usage" section above).

### Using the TurboJPEG backend

By default, a Java-based image processing backend is used. If you want better
performance, it is recommended to use the native image processing backend
that is based on TurboJPEG. For this, you will have to install the TurboJPEG
native library, on Ubuntu `libturbojpeg`.

### Custom configuration file `application.yml`

The default configuration of the server comes packaged in the executable JAR-file of Hymir.
To customize (override) the default configuration parameters, simply put your custom `application.yml` file beside (in the same directory of) the Hymir JAR-file.

Your custom `application.yml` does not have to replace all default properties. It can contain only the properties you want to change.

To get the default configuration file, you should download the `hymir-<release-version>.jar` file (NOT containing `-exec` in filename) from <https://github.com/dbmdz/iiif-server-hymir/releases> and unpack the contained `application.yml` with:

```sh
$ java xfv hymir-4.0.0.jar application.yml
```

Now put the file beside the executable Hymir jar and edit it according to your requirements.

#### Configure custom HTTP-Response-Header

If you already put your custom `application.yml` file in place (see above), it is possible to set custom HTTP response headers in responses for

- all requests to Image and Presentation API urls
- Image API: image requests
- Image API: info.json requests
- Presentation API: manifest requests (includes canvas and range requests)
- Presentation API: collection requests
- Presentation API: Annotation list requests

Customized response headers are placed in the `custom.iiif.headers`-section of your `application.yml` configuration file, e.g.:

```yml
custom:
  iiif:
    headers:
      all:
        - name: 'served by'
          value: 'hymir'
      image:
        image:
          - name: 'cache-control'
            value: 'max-age=86400'
        info:
          - name: 'header1'
            value: 'value1'
      presentation:
        manifest:
          - name: 'mani1'
            value: 'mani-value1'
          - name: 'mani2'
            value: 'mani-value2'
        collection: null
        annotationList: null
```

If you want to override a header that is set by default (e.g. `Access-Control-Allow-Origin=*`), you just have to configure it with another value, e.g.:

```yml
custom:
  iiif:
    headers:
      image:
        info:
          - name: 'Access-Control-Allow-Origin'
            value: 'https://yourdomain.org'
```

(Given example is a bad practice in the IIIF context, as it contradicts the "interoperability" idea of IIIF...)

## Administration

Monitoring endpoints under http://localhost:9001/monitoring (HAL-Browser-GUI), authentication by default: admin/secret (configurable in application.yml)

## Users

- Bavarian State Library, Project "bavarikon": <a href="http://www.bavarikon.de/">https://www.bavarikon.de/</a>
- Bavarian State Library, Project "Digital East Asian Collections": <a href="https://ostasien.digitale-sammlungen.de/">https://ostasien.digitale-sammlungen.de/</a>
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
