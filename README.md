SCIFIO
======

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
* Quicktime
* MNG
* Micromanager
* NRRD
* TIFF
* OBF
* PCX
* PGM
* PIC
* Zipped images

Additionally,
[Bio-Formats](http://www.openmicroscopy.org/site/products/bio-formats) is
[available as a SCIFIO plugin](https://github.com/scifio/scifio-bf-compat) for
supporting hundreds of additional proprietary formats.


For users
---------

[ImageJ2](http://developer.imagej.net/) is a leading adopter of SCIFIO. Additionally,
any product that uses Bio-Formats will support SCIFIO plugins as well.


For developers
--------------

Several software libraries use SCIFIO for image I/O:
* SCIFIO has built-in support for opening and saving
  [ImgLib2](https://github.com/imagej/imglib) data structures
  (see the [io.scif.img](scifio/src/main/java/io/scif/img) package)
* We have [updated Bio-Formats](https://github.com/scifio/bioformats) to
  also support SCIFIO plugins, backwards compatibly with existing code.
* [ITK](https://github.com/Kitware/ITK) has an
  [ImageIO module](https://github.com/scifio/scifio-imageio)
  for reading and writing images using SCIFIO.

See the included [Tutorials module](tutorials) for a step-by-step introduction
to the SCIFIO API.


More information
----------------

For more information, see the [SCIFIO web site](http://scif.io/).


Mailing lists
-------------

* Please use [ome-users](ome-users@lists.openmicroscopy.org.uk) for any SCIFIO or
  Bio-Formats questions.


Contributing to SCIFIO
----------------------

SCIFIO is an open product and anyone is welcome to submit pull requests to the
[SCIFIO repository](https://github.com/scifio/scifio). Although, with SCIFIO's
dynamic extensibility, you don't need to make upstream changes to get your
formats into user's hands.

If you are interested in submitting a pull request though, that's great! All we ask
is that you check:

* mvn clean test

from the top level.

If you're adding a new feature, it would be fantastic if you
could write a unit test for it! You can add your test to:

* src/test/java/io/scif/utests/testng-template.xml

to have it run by the SCIFIO test suite.
