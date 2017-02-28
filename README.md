[SCIFIO](http://scif.io/)
======

[![](https://travis-ci.org/scifio/scifio.svg?branch=master)](https://travis-ci.org/scifio/scifio)
[![Join the chat at https://gitter.im/scifio/scifio](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/scifio/scifio?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

SCIFIO (SCientific Image Format Input & Output) is an extensible Java framework
for reading and writing images, particularly N-dimensional scientific images.

This core package supports parsing pixels and metadata for a collection of open
formats. Additional formats can be supported simply by downloading SCIFIO
plugins and including them in your project.


Purpose
-------

SCIFIO's primary purpose is to provide a clear convention for supporting image
input and output. By lowering the barrier for adding new image formats, all
SCIFIO-backed software will grow more versatile and powerful.


Supported formats
-----------------

The SCIFIO core includes support for:
* APNG
* AVI
* BMP
* DICOM
* EPS
* FITS
* GIF
* ICS
* JPEG
* JPEG2000
* MNG
* Micro-Manager
* NRRD
* OBF
* PCX
* PGM
* QuickTime
* TIFF
* Zipped images

Additionally,
[Bio-Formats](http://www.openmicroscopy.org/site/products/bio-formats) is
[available as a SCIFIO plugin](https://github.com/scifio/scifio-bf-compat) for
supporting more than a hundred additional proprietary formats.


For users
---------

[ImageJ2](https://github.com/imagej/imagej) and
[Fiji](https://github.com/fiji/fiji) use SCIFIO for image I/O.


For developers
--------------

Several software libraries use SCIFIO for image I/O:
* SCIFIO has built-in support for opening and saving
  [ImgLib2](https://github.com/imagej/imglib) data structures
  (see the [io.scif.img](src/main/java/io/scif/img) package).
* We have [updated Bio-Formats](https://github.com/scifio/bioformats) to
  also support SCIFIO plugins, backwards compatibly with existing code.
* [ITK](https://github.com/Kitware/ITK) has an
  [ImageIO module](https://github.com/scifio/scifio-imageio)
  for reading and writing images using SCIFIO.

Developer documentation:
* See the [SCIFIO tutorials](https://github.com/scifio/scifio-tutorials) for a
  step-by-step introduction to the SCIFIO API.
* See also the
  [SCIFIO Javadocs](https://javadoc.imagej.net/SCIFIO/).


More information
----------------

For more information, see the [SCIFIO FAQ](https://github.com/scifio/scifio/wiki/FAQ)
and [SCIFIO web site](http://scif.io/).


Mailing list
-------------

* [scifio@scif.io](http://scif.io/mailman/listinfo/scifio):
  for SCIFIO questions.

Contributing to SCIFIO
----------------------

SCIFIO is an open project and anyone is very welcome to submit pull requests
to the [SCIFIO repository](https://github.com/scifio/scifio).

With SCIFIO's focus on extensibility, you typically will not need to make
upstream changes to get your formats into users' hands. However, if you are
interested in submitting a pull request, that's great!
All we ask is that you check:

    mvn clean test

from the top level.

If you're adding a new feature, it would be fantastic if you
could write a unit test for it! Simply base it on JUnit
to have it run by the SCIFIO test suite.
