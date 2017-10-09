# IIIF Image API Java Libraries

[![Build Status](https://travis-ci.org/dbmdz/iiif-image-api.svg?branch=master)](https://travis-ci.org/dbmdz/iiif-image-api)
[![MIT License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![GitHub release](https://img.shields.io/github/release/dbmdz/iiif-image-api.svg?maxAge=2592000)](https://github.com/dbmdz/iiif-image-api/releases)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/4791195661d84028945d5b384ce5324f)](https://www.codacy.com/app/ralf-eichinger/iiif-image-api?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=dbmdz/iiif-image-api&amp;utm_campaign=Badge_Grade)
[![codecov](https://codecov.io/gh/dbmdz/iiif-image-api/branch/master/graph/badge.svg)](https://codecov.io/gh/dbmdz/iiif-image-api)
[![Maven Central](https://img.shields.io/maven-central/v/de.digitalcollections/iiif-image-api.svg?maxAge=2592000)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22iiif-image-api%22)

These Java libraries implement the IIIF Image API (see <a href="http://iiif.io/api/image/2.0/">http://iiif.io/api/image/2.0/</a>):

"The IIIF Image API specifies a web service that returns an image in response to a standard HTTP or HTTPS request. The URI can specify the region, size, rotation, quality characteristics and format of the requested image. A URI can also be constructed to request basic technical information about the image to support client applications. This API was conceived of to facilitate systematic reuse of image resources in digital image repositories maintained by cultural heritage organizations. It could be adopted by any image repository or service, and can be used to retrieve static images in response to a properly constructed URI."

## Features

- IIIF Image API 2.0.0 conform
- Embeddable Spring components: Spring MVC Controller, Spring Services
- Access to images over project specific Resolver-plugin mechanism.
- Image processing engines:
    - Java Image I/O API (javax.imageio)<br/>
(see http://docs.oracle.com/javase/8/docs/api/javax/imageio/package-summary.html#package.description)
    - TurboJPEG from libjpeg-turbo "<br/>
(see http://libjpeg-turbo.org/)

## Usage

### Maven dependencies

Depending on what library you want use, these are the dependency definitions for all modules:

```xml
<dependency>
  <groupId>de.digitalcollections</groupId>
  <artifactId>iiif-image-backend-api</artifactId>
  <version>${version.iiif-image}</version>
</dependency>

<dependency>
  <groupId>de.digitalcollections</groupId>
  <artifactId>iiif-image-backend-impl</artifactId>
  <version>${version.iiif-image}</version>
</dependency>

<dependency>
  <groupId>de.digitalcollections</groupId>
  <artifactId>iiif-image-business-api</artifactId>
  <version>${version.iiif-image}</version>
</dependency>

<dependency>
  <groupId>de.digitalcollections</groupId>
  <artifactId>iiif-image-business-impl</artifactId>
  <version>${version.iiif-image}</version>
</dependency>

<dependency>
  <groupId>de.digitalcollections</groupId>
  <artifactId>iiif-image-model-api</artifactId>
  <version>${version.iiif-image}</version>
</dependency>

<dependency>
  <groupId>de.digitalcollections</groupId>
  <artifactId>iiif-image-model-impl</artifactId>
  <version>${version.iiif-image}</version>
</dependency>

<dependency>
  <groupId>de.digitalcollections</groupId>
  <artifactId>iiif-image-frontend-impl-springmvc</artifactId>
  <version>${version.iiif-image}</version>
</dependency>
```
### Configuration

#### File resolving of backend

The default implementation (see <https://github.com/dbmdz/digitalcollections-core/tree/master/digitalcollections-core-backend/digitalcollections-core-backend-impl-file>) for the backend repository is file based and can be configured by using regular expressions.

### Embed IIIF-Controller into your Spring MVC webapplication</h3>

- For IIIF Image API support add Spring MVC-library as dependency to your pom.xml:

```xml
<dependency>
  <groupId>de.digitalcollections</groupId>
  <artifactId>iiif-image-frontend-impl-springmvc</artifactId>
  <version>2.1.2</version>
</dependency>
```

- Import library's root configuration class into the Spring configuration of your webapp. Example:

```java
@Configuration
@ComponentScan(basePackages = {
  "de.digitalcollections.iiif.image.config"
}) // scans all frontend, business and backend configs of Image API
...
public class SpringConfig implements EnvironmentAware {
  ...
}
```

- Add Listener "IIOProviderContextListener" (for supporting additional ImageIO image formats) in your WebappInitializer

```java
import com.twelvemonkeys.servlet.image.IIOProviderContextListener;
...

public class WebappInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
  ...
  @Override
  public void onStartup(ServletContext servletContext) throws ServletException {
    super.onStartup(servletContext);
    servletContext.addListener(new IIOProviderContextListener());
  }
}
```

- Implement project specific Resolvers for accessing images. Place them into package "de.digitalcollections.core.backend.impl.file.repository.resource.resolver" or a subpackage and implement interface "de.digitalcollections.core.backend.impl.file.repository.resource.resolver.FileNameResolver". They will be found automatically by ComponentScan.

- Start your Spring MVC webapp. You should see mappings for IIIF-Image-API-URLs in your log:

```
...
[2016-07-13 15:41:29,868 INFO ] [...] RequestMappingHandlerMapping (main    ) > Mapped "{[/image/v2/{identifier}/info.json],methods=[GET]}" onto public org.springframework.http.ResponseEntity<java.lang.String> de.digitalcollections.iiif.image.frontend.impl.springmvc.controller.v2.IIIFImageApiController.getInfo(java.lang.String,javax.servlet.http.HttpServletRequest) throws de.digitalcollections.iiif.image.frontend.impl.springmvc.exception.ResolvingException,de.digitalcollections.iiif.image.frontend.impl.springmvc.exception.UnsupportedFormatException,java.lang.UnsupportedOperationException,java.io.IOException
[2016-07-13 15:41:29,868 INFO ] [...] RequestMappingHandlerMapping (main    ) > Mapped "{[/image/v2/{identifier}],methods=[GET]}" onto public java.lang.String de.digitalcollections.iiif.image.frontend.impl.springmvc.controller.v2.IIIFImageApiController.getInfoRedirect(java.lang.String)
[2016-07-13 15:41:29,868 INFO ] [...] RequestMappingHandlerMapping (main    ) > Mapped "{[/image/v2/{identifier}/{region}/{size}/{rotation}/{quality}.{format}]}" onto public org.springframework.http.ResponseEntity<byte[]> de.digitalcollections.iiif.image.frontend.impl.springmvc.controller.v2.IIIFImageApiController.getImageRepresentation(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,javax.servlet.http.HttpServletRequest) throws de.digitalcollections.iiif.image.frontend.impl.springmvc.exception.ResolvingException,de.digitalcollections.iiif.image.frontend.impl.springmvc.exception.UnsupportedFormatException,java.lang.UnsupportedOperationException,java.io.IOException,java.net.URISyntaxException,de.digitalcollections.iiif.image.frontend.impl.springmvc.exception.InvalidParametersException
...
```

### Local build

Clone project and build it:

```shell
$ mvn clean install
```


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
