# Hymir IIIF Server

[![Javadocs](https://javadoc.io/badge/de.digitalcollections/iiif-server-hymir.svg)](https://javadoc.io/doc/de.digitalcollections/iiif-server-hymir)
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
- Embedded IIIF Image Viewer (for out of the box viewing of served images): OpenSeadragon 2.4.0 (see "Usage" below)
- Embedded IIIF Presentation Viewer: Mirador 2.7.0 (see "Usage" below)
- Direct Manifest access (see "Usage" below)

### Supported image formats

| Format    | Reading  | Writing | Dependencies                               | Comment    |
| --------- | -------- | ------- | ------------------------------------------ | ---------- |
|   JPEG    |   [x]    |   [x]   |   libturbojpeg (optional, but recommended) |            |
|  JPEG2000 |   [x]    |   [ ]   |   libopenjp2 (>= 2.3 recommended)          |            |
|    TIFF   |   [x]    |   [x]   |                                            |            |
|    PNG    |   [x]    |   [x]   |                                            | Due to possible transparency (alpha channel) in PNG it is not possible to use a PNG source file and deliver it as JPG. PNG delivered as PNG is possible. |
|    BMP    |   [x]    |   [x]   |                                            |            |
|    GIF    |   [x]    |   [x]   |                                            |            |


## Prerequisites

- Server with minimum 4GB RAM.
- Java 11

## Installation

Download `hymir-<version>-exec.jar` from the GitHub [releases](https://github.com/dbmdz/iiif-server-hymir/releases) page.

### Using the TurboJPEG backend for JPEG files

By default, a Java-based image processing backend is used. If you want better
performance, it is recommended to use the native image processing backend
that is based on TurboJPEG. For this, you will have to install the TurboJPEG
native library, on Ubuntu `libturbojpeg`.

Debian:

```sh
$ sudo apt-get install libturbojpeg
```

### Adding JPEG2000 support

By default, a Java-based image processing backend is used which has no support for JPEG2000.
For adding JPEG2000 support, you will have to install the libopenjp2 native library.

Debian:

```sh
$ sudo apt-cache search libopenjp2
libopenjp2-7 - Kompressions-/Dekompressions-Bibliothek für das Bildformat JPEG 2000
libopenjp2-tools - Kommandozeilenwerkzeuge zur Verwendung der JPEG 2000-Bibliothek
libopenjp2-7-dev - development files for OpenJPEG, a JPEG 2000 image library
$ sudo apt-get install libopenjp2-7
```

### Creating logging directories

Create directories for

- hymir application logging (configured in logback-spring.xml), e.g. `/var/log/hymir`
- hymir access logs (default: `/var/log/digitalcollections`)

Example (use more restricted access rights than in this example):

```sh
$ sudo mkdir /var/log/hymir
$ sudo chmod 777 /var/log/hymir
$ sudo mkdir /var/log/digitalcollections
$ sudo chmod 777 /var/log/digitalcollections
```

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
$ java -jar hymir-<version>-exec.jar --spring.profiles.active=PROD
```

Logging: to file `./hymir.log` in [Logstash](https://www.elastic.co/de/products/logstash)-JSON format

Image, manifest, collection and annotation list file resolving: see [here](src/main/resources/application.yml#L49) (using directories under `/var/local/iiif`)

Application configuration: see [here](src/main/resources/application.yml) (`PROD` profile section overriding some values at bottom of file)

- in production with custom logging configuration file:

```sh
$ java -jar hymir-<version>-exec.jar --logging.config=file:/etc/hymir/logback-spring.xml  --spring.profiles.active=PROD
```

Read <https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-logging.html> and <https://logback.qos.ch/manual/configuration.html>.

- in production with custom configuration file `application.yml`:

(Custom `application.yml` placed beside jar-file. No explicit command line option needed.)

```sh
$ java -jar hymir-<version>-exec.jar --spring.profiles.active=PROD
```

- in production with a custom server port (e.g. port 8080):

```sh
$ java -jar hymir-<version>-exec.jar --server.port=8080 --spring.profiles.active=PROD
```

Complete parametrized example:

```sh
$ java -jar hymir-<version>-exec.jar --logging.config=file:/etc/hymir/logback-spring.xml --server.port=8080 --spring.profiles.active=PROD
```

(and `application.yml` beside jar file).

Access Hymir GUI (e.g. http://localhost:9000/).

## Configuration

### Running Hymir behind a proxy server

If you are running Hymir behind a proxy server, it is important to configure the proxy server to set the `X-Forwarded-For` and `X-Forwarded-Proto` headers, because they are used e.g. in rendering absolute URLs in `info.json` response content.

For NGinx this can be configured like this:

```
proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
proxy_set_header X-Forwarded-Proto $scheme;
```

### Image and presentation manifest resolving

Based on unique resource identifiers the server tries to resolve identifiers to a `file:` or `http:` path.
The resolving rules (one rule per line) are configurable with regular expressions in YML-files.

You can pass the path to your custom resolving rules with the `--spring.config.additional-location=/path/to/rules.yml` option.

Example:

```sh
$ java -jar hymir-<version>-exec.jar --spring.config.additional-location=file:/etc/hymir/rules.yml
```

Example file `/etc/hymir/rules.yml`:

```yaml
resourceRepository:
  resolved:
    patterns:
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
$ java -jar target/hymir-<version>-exec.jar --custom.iiif.image.urlPrefix='/iiifImage/' --spring.config.additional-location=file:/etc/hymir/rules.yml --spring.profiles.active=local
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
$ java -jar hymir-<version>-exec.jar --custom.iiif.presentation.urlPrefix='/iiifPresentation/' --spring.config.additional-location=file:/etc/hymir/rules.yml --spring.profiles.active=local
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

Example: Custom config file with human readable logging

```sh
$ java -jar hymir-<version>-exec.jar --logging.config=file:/etc/hymir/logback-spring.xml
```

Example file `/etc/hymir/logback-spring.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <!-- see https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-logging.html#_profile_specific_configuration -->
  <springProfile name="PROD">
    <appender name="default" class="ch.qos.logback.core.rolling.RollingFileAppender">
      <file>/var/log/hymir/hymir.log</file>
      <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>/var/log/hymir/hymir.%d{yyyy-MM-dd}.log</fileNamePattern>
      </rollingPolicy>
      <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
        <fileNamePattern>/var/log/hymir/hymir.%d{yyyy-MM}.%i.log.gz</fileNamePattern>
        <maxFileSize>100MB</maxFileSize>
        <maxHistory>90</maxHistory>
        <totalSizeCap>5GB</totalSizeCap>
      </rollingPolicy>
      <encoder>
        <pattern>[%d{ISO8601} %5p] %40.40c:%4L [%-8t] - %m%n</pattern>
      </encoder>
      <!--
      <encoder class="net.logstash.logback.encoder.LogstashEncoder">
        <customFields>{"service":"hymir-server", "group":"rest", "instance":"${instance.name:-default}"}</customFields>
      </encoder>
      -->
    </appender>
  </springProfile>

  <springProfile name="local">
    <appender name="default" class="ch.qos.logback.core.ConsoleAppender">
      <encoder>
        <pattern>[%d{ISO8601} %5p] %40.40c:%4L [%-8t] - %m%n</pattern>
      </encoder>
    </appender>
  </springProfile>

  <logger name="de.digitalcollections.iiif.hymir.presentation.backend" level="error" />
  <logger name="de.digitalcollections.iiif.hymir.presentation.business" level="error" />
  <logger name="de.digitalcollections.commons" level="error" />

  <root level="info">
    <appender-ref ref="default" />
  </root>

</configuration>
```

### Custom configuration file `application.yml`

The default configuration of the server comes packaged in the executable JAR-file of Hymir.
To customize (override) the default configuration parameters, simply put your custom `application.yml` file beside (in the same directory of) the Hymir JAR-file.

Your custom `application.yml` does not have to replace all default properties. It can contain only the properties you want to change.

To get the default configuration file, you should download the `hymir-<release-version>.jar` file (NOT containing `-exec` in filename) from <https://github.com/dbmdz/iiif-server-hymir/releases> and unpack the contained `application.yml` with:

```sh
$ jar xfv hymir-<version>.jar application.yml
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

### Monitoring

Monitoring endpoints under http://localhost:9001/monitoring, authentication by default: `admin/secret` (configurable in `application.yml`)

To change monitoring port, e.g. to `8081` use `management.server.port` option:

```sh
$ java -jar hymir-<version>-exec.jar --management.server.port=8081
```

### Out Of Memory handling

In case the IIIF server runs out of memory it should quit - use java options for this. (To be restarted automatically install it as systemd service, see below.)

```sh
$ java -jar hymir-<version>-exec.jar -XX:+ExitOnOutOfMemoryError -XX:+CrashOnOutOfMemoryError
```

### Configure IIIF service as systemd service

In Linux production environments it is a best practice to install the IIIF server as a systemd service / daemon.

Therefore create a user `iiif`, a service file, add it as service and enable it:

```sh
$ sudo nano /etc/systemd/system/iiif-hymir.service
[Unit]
Description=IIIF Hymir Server
After=syslog.target

[Service]
User=iiif
ExecStart=/usr/bin/java -jar /opt/hymir-<version>-exec.jar \
    -XX:+ExitOnOutOfMemoryError -XX:+CrashOnOutOfMemoryError \
    --spring.config.additional-location=file:/etc/hymir/rules.yml \
    --spring.profiles.active=PROD \
    --logging.config=file:/etc/hymir/logback-spring.xml \
    --server.port=8080 \
    --management.server.port=8081
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target

$ sudo systemctl daemon-reload
$ sudo systemctl enable iiif-hymir.service
$ sudo systemctl start iiif-hymir.service
```

## Users

- Bavarian State Library, Project "bavarikon": <a href="http://www.bavarikon.de/">https://www.bavarikon.de/</a>
- Bavarian State Library, Project "Digital East Asian Collections": <a href="https://ostasien.digitale-sammlungen.de/">https://ostasien.digitale-sammlungen.de/</a>
- Bavarian State Library, Project digiPress: <a href="https://digipress.digitale-sammlungen.de/">https://digipress.digitale-sammlungen.de/</a>
- Bavarian State Library, iiif-Bookshelf: <a href="https://iiif.digitale-sammlungen.de/">https://iiif.digitale-sammlungen.de/</a>

## Development

* Install git client
* Install Java JDK 11 or above
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

On newer Java versions `fmt-maven-plugin` will fail:

```sh
[ERROR] Failed to execute goal com.coveo:fmt-maven-plugin:2.13:format (default) on project iiif-apis: Execution default of goal com.coveo:fmt-maven-plugin:2.13:format failed: An API incompatibility was encountered while executing com.coveo:fmt-maven-plugin:2.13:format: java.lang.IllegalAccessError: null
```

If you get the error message above (on newer Java versions), consider skipping the `fmt-maven-plugin` by passing `-Dfmt.skip`, like in `mvn package -Dfmt.skip`. Note that additional Maven arguments (`-D`) an be chained, to ignore multiple problems at once (like in `mvn package -Dfmt.skip -DskipTests`).

Sometimes the tests won't pass, especially when you're using a development version, just pass `-DskipTests` to Maven, like in `mvn package -DskipTests`.

To install `libturbojpeg` on Debian based systems:

```sh
$ sudo apt install libturbojpeg
```
