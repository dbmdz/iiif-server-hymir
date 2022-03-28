# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [5.1.4] - 2022-03-28
### Fixed
- Fix bug in metrics handling that caused variouus metrics to be silently dropped

## [5.1.3] - 2022-03-15
### Fixed
- Fix off-by-one errors when decoding scaled down full-area image (#280, #281)
- Bump imageio-jnr version to fix bug in cropping of JPEG2000 images

## [5.1.2] - 2022-02-14
### Fixed
- Fix error handling for image requests with size or region of 0 (#277)
- Ensure a HTTP 500 is returned in case of low-level I/O errors instead of a 404 (#276)

## [5.1.1] - 2021-10-18
### Fixed
- Fixed ACL check in `ImageServiceImpl` that did not pass the HTTP request to the security backend

## [5.1.0] - 2021-10-15
### Added
- New API in [`ImageSecurityService`][api-image] and [`PresentationSecurityService`][api-presentation]
  for determining access permissions based on the associated `HttpServletRequest`.
  This can be used to to e.g. verify security tokens or the source IP. The old API that
  is based on the identifier only still works, and the new method on the interface comes
  with a default implementation that delegates to the existing API, so existing
  implementations do not have to be updated if they have no use for the HTTP request.

[api-image]: https://github.com/dbmdz/iiif-server-hymir/blob/main/src/main/java/de/digitalcollections/iiif/hymir/image/business/api/ImageSecurityService.java
[api-presentation]: https://github.com/dbmdz/iiif-server-hymir/blob/main/src/main/java/de/digitalcollections/iiif/hymir/presentation/business/api/PresentationSecurityService.java

### Fixed
- Fix an internal server error that occurred when specifying a bad image quality parameter,
  this now returns a HTTP 400 error.
