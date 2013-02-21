This project contains example code for working with [SCIFIO][1].


GETTING STARTED
---------------

These tutorials are heavily commented to explain the SCIFIO API.
Simply running a given tutorial may not be meaningful; it is
highly recommended to open the source files and follow the code
step by step.

You can import these projects into your favorite IDE:

  * Eclipse: File > Import > Existing Maven Projects
  * NetBeans: File > Open Project
  * IDEA: File > Open Project... (select pom.xml)

Or build and run from the command line...

... via Maven:

    mvn
    mvn exec:java -Dexec.mainClass=ome.scifio.tutorials.T1aIntroToSCIFIO

... via ant (from the Bio-Formats top-level)

    ant jar-scifio-tutorials
    java -cp jar/*:artifacts/* ome.scifio.tutorials.T1aIntroToSCIFIO

TUTORIAL ORDER
-------------

The tutorials are named in a suggested order for execution:
  * Tutorials 1a-d focus on basic image IO using convenience methods
  * Tutorials 2a-b cover individual component use
  * Tutorials 3a-c explain the discovery mechanism and defining
    your own SCIFIO Formats and components.

LICENSING
---------

To the extent possible under law, the SCIFIO developers have waived
all copyright and related or neighboring rights to this tutorial code.

See the [CC0 1.0 Universal license][2] for details.

[1]: http://loci.wisc.edu/software/scifio
[2]: http://creativecommons.org/publicdomain/zero/1.0/

