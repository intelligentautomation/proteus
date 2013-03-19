# Proteus 

This repository contains the source code for Proteus. Proteus is a
desktop client for sensor discovery and management developed by
[Intelligent Automation Inc.](http://www.i-a-i.com)

![proteus](https://raw.github.com/intelligentautomation/proteus/master/utilities/screenshots/proteus-1.2.0.beta.png)

## Software Dependencies 

Proteus is built using the
[Eclipse Rich Client Platform](http://wiki.eclipse.org/index.php/Rich_Client_Platform)
and has the following dependencies:

* [Proteus Common](https://github.com/intelligentautomation/proteus-common), Version 1.0, GNU Lesser General Public License (LGPL)

* [NASA World Wind](http://worldwind.arc.nasa.gov/java/), Version 1.3, [NOSA License, version 1.3](http://ti.arc.nasa.gov/opensource/nosa/ "NOSA License"). The NASA World Wind source code is redistributed, but unmodified. 

* [Eclipse Albireo](http://wiki.eclipse.org/Albireo_Project "Eclipse Albireo"), Version 0.0.3.v20081031, [Eclipse Public License, version 1.0](http://www.eclipse.org/legal/epl-v10.html "Eclipse Public License"). The Albireo code is distributed, but unmodified. 

* [JFreeChart](http://www.jfree.org/jfreechart/), Version 1.0.13, GNU Lesser General Public License (LGPL)

* [Java XT - RSS & GIS](http://www.javaxt.com/), Version 1.1, [MIT License](http://www.javaxt.com/downloads/javaxt-core/LICENSE.TXT "MIT License")

* [Apache Commons Validator](http://commons.apache.org/proper/commons-validator/ "Apache Commons Validator"), Version 1.4.0, Apache License, Version 2.0

* [Apache Log4j](http://logging.apache.org/log4j/1.2/ "Apache Log4j"), Version 1.2.17, Apache License, Version 2.0

## Building From Source

Proteus is built on the [Eclipse Rich Client Platform (RCP)](http://wiki.eclipse.org/index.php/Rich_Client_Platform "Eclipse RCP"). Eclipse plugins and features (bundles) can be compiled and built using the [PDE/Build component](http://www.eclipse.org/pde/pde-build/ "PDE/Build"). 

### Requirements 

Proteus has been tested with JDK6, JDK7 and Eclipse 3.7.x. (_Indigo_). To [build](#building) Proteus for multiple platforms (e.g. Mac OS X, Linux and Windows), the Eclipse installation needs to have the [Delta pack](#delta-pack) installed. The Delta pack contains platform-specific bundles and executables for multiple platforms. By default an Eclipse RCP installation only contains bundles for the specific platform it will run on. 

#### Delta pack

The Delta pack is specific to each Eclipse version. The Delta pack for Eclipse 3.7.2, for example, can be found [here](http://download.eclipse.org/eclipse/downloads/drops/R-3.7.2-201202080800/). 

Unpack the downloaded Delta pack into the Eclipse installation with the same version. This will make platform-specific bundles available so that builds for multiple platforms can be produced on a single platform.

### Building 

There are two simple steps to successfully build Proteus with PDE/Build. 

1. Configure the build
2. Execute the build

We will detail these two steps below. 

### Configure the build

The build is controlled by a _build.properties_ file. The _build.properties_ file can be found in the _utilities/builder_ folder and provides information needed by the PDE/Build scripts, as well as specifies exactly what should be built. It is a Java properties file, so it consists of key-value (_key=value_) pairs. Most default values can be left untouched, or be overridden.

For sake of simplicity we have provided a batch file where the values that need to be overridden can easily be changed. On Mac OS X/Linux the batch file template is _utilities/build\_template.sh_ and on Windows it is _utilities/build\_template.bat_. 

**Step 1: Copy the build batch file (Mac OS X/Linux)**

    cd utilities
    cp build_template.sh build.sh

*Note: This step is not strictly needed, but helps to avoid making changes to versioned controlled files during specific system build configurations.*

**Step 2: Edit the build batch file**

There are two values that need to be modified:

1. **PROTEUS\_SRC\_ROOT** The value should point to the location of the clonsed Proteus repository that should be built.
* **ECLIPSE\_TARGET\_ROOT** The value should point to the target Eclipse installation that should be used during the build. 

*Note: The target Eclipse installation needs to have the [Delta pack](#delta-pack) installed in order to successfully build for multiple platforms (this is the default). If you do not want to build for multiple platforms the utilities/builder/build.properties file needs to be modified. Which platforms to build for is controlled by the configs parameter*

### Execute the build 

When the build has been [configured](#configure-the-build), simple execute the modified build script (_build.sh_ for Mac OS X/Linux and _build.bat_ for Windows).

The build artifacts will end up in the _utilities/target/S.proteus_ folder. The build for each platform and architecture will be packaged in a separate ZIP file. Unpack the appropriate ZIP file and execute the appropriate Protues.* file (e.g. Proteus.exe on Windows, Proteus.app on Mac OS X). 

### Known Build Issues 

#### Eclipse 3.7.x and Java 7 on Mac OS X

There is an issue with the PDE/Build system on Eclipse 3.7.x with Java 7 on Mac OS X. Details can be found in [Bug 392434](https://bugs.eclipse.org/bugs/show_bug.cgi?id=392434 "Bug 392434"). A simple workaround is to do the following: 

    cd /Library/Java/JavaVirtualMachines/jdk1.7.0_*.jdk/Contents/Home
    sudo ln -s jre/lib Classes

You may need root access for the above.

## Running

You can run Proteus either from within Eclipse or by using the [built product](#execute-the-build). 

To run Proteus from Eclipse: 

1. Import all bundles (plugins and features into Eclipse).
2. Right-click on the _proteus.product_ file found in the _com.iai.proteus_ plugin. 
3. Select _Run As_ -> _Eclipse Application_

## License 

This software is released under the GNU Lesser General Public License (LGPL). See the file "LICENSE" for more details. 

## Acknowledgments

This software was initially developed by [Intelligent Automation, Inc.](http://www.i-a-i.com "IAI"), under NASA funding (contract no: NNX11CA19C). 

The icons used in Proteus are from the
[Fugue Icon set](http://p.yusukekamiyamane.com/icons/search/fugue/) and are   (C) 2012 Yusuke Kamiyamane, used under
[Creative Commons
Attribution 3.0 License](http://creativecommons.org/licenses/by/3.0/).

