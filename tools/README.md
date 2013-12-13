SCIFIO-Tools
============

An extensible application for using SCIFIO components from the command line.

Installation
------------

Bash script and Windows .bat packages are available. The first step is to download one of the archives. Either:

* [scripts.zip](http://jenkins.imagej.net/view/SCIFIO/job/SCIFIO/lastSuccessfulBuild/artifact/tools/target/scifio-tools-0.7.4-SNAPSHOT-scripts.zip)

or

* [scripts.bz2](http://jenkins.imagej.net/view/SCIFIO/job/SCIFIO/lastSuccessfulBuild/artifact/tools/target/scifio-tools-0.7.4-SNAPSHOT-scripts.tar.bz2)

and extract the downloaded archive to a directory of your choice.

Two subdirectories will be extracted:

* ```/bin``` - contains the scifio scripts. If you add this directory to your ```PATH``` environment variable, you can run these scripts from any directory. See the [usage](#usage) section for specifics on running these scripts.
* ```/repo``` - contains a flattened local repository of all the dependencies required to run the SCIFIO command-line tools. Everything in this directory will be added to the script classpath, so feel free to add more plugins to this directory. For example, adding [scifio-lifesci](https://github.com/scifio/scifio-lifesci) will allow any commands you run to operate on supported [Bio-Formats](http://www.openmicroscopy.org/site/products/bio-formats) datasets. New commands can also be added by implementing [SCIFIOToolCommand](https://github.com/scifio/scifio/blob/master/tools/src/main/java/io/scif/tools/SCIFIOToolCommand.java).

Note that the relative location of ```/bin``` and ```/repo``` is important. However, if these directories are separated, you can still set a ```REPO``` environment variable that points to the script dependencies. In addition to this directory, the ```CLASSPATH``` environment variable will always be included.

Building from source
--------------------

If you would like to build the SCIFIO command-line tools from scratch, simply run:

  ```mvn clean install```

from the top-level SCIFIO directory. You should see three projects completed: "SCIFIO projects", "SCIFIO Core" and "SCIFIO Tools." After installation, the tools will be installed to ```tools/target/appassembler/```. Here you will find the directory structure detailed in the [installation](#installation) section.

Note that there is one caveat in using these tools on *nix operating systems: by default, they will not be created witih execute permissions (per [this issue](http://jira.codehaus.org/browse/MAPPASM-54). So, instead of running ```scifio ...``` commands as in the [usage](#usage) section, you will have two options:

* run ```bash scifio ...``` instead

or

* Add execution permissions to the scifio script, using ```chmod a+x tools/target/appassembler/bin/scifio```

Note also that the zipped scripts (which will also be created as part of the install, in ```tools/target```) will unzip with execute permission already set.

Usage
-----

The SCIFIO command-line tools were designed to be syntactically similar to [git](http://git-scm.com/docs/gittutorial). So if you already know git, this should feel familiar.

Assuming you have the scripts on your ```PATH``` (per [installation](#installation) instructions) executing a command will always take the form of:

  ```scifio <command> [options] <parameters>```

NB: in all these examples, use ```scifio.bat``` if running in a Windows environment.

The ```<command>``` option is the simple lowercase name of the command you want to run. For example, if you wanted to view (using the ```Show.java``` command) a picture of a [kraken](http://en.wikipedia.org/wiki/Kraken), you would use:

  ```scifio show kraken.tiff```

If you want to see a list of all available commands, just run the script with no arguments.

Commands may have a set of flags or options available to modify their behavior. All command options are designed to work like [unix flags or switches](http://www.cs.bu.edu/teaching/unix/reference/vocab.html#flag), and typically will have a short (```-x```) and explicit (```--exterminate-kraken```) version. You can combine as many of these options as you want. For example, to print an ascii version of the top left 128x128 square of your kraken picture, you could use:

  ```scifio show -A --crop 0,128,0,128 kraken.tiff```

If you ever need to see the list of options a command has, and parameters a command requires, each command has a help flag:

  ```scifio show -h``` or ```scifio show --help```

You can also run the ```help``` meta-command:

  ```scifio help show```

Don't worry about making mistakes with the command invocation - commands will always print their usage on failure.

Mailing List
------------

If you run into any problems or have questions about the commands, or adding new commands, please use the mailing list:

* [scifio@scif.io](http://scif.io/mailman/listinfo/scifio)

Thank you for using the SCIFIO command-line tools!
