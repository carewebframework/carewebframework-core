#Getting Started

## CareWeb Framework
A modular, extensible framework for building clinical applications in a collaborative fashion, the CareWeb Framework is
built upon established open source technologies.

The framework serves as the foundation for the rest of the CareWeb Framework projects. Browse the
repositories under the [CareWebFramework organization][] on GitHub for a full list.

## Documentation
TODO artifact repository or continuous integration server published javadoc, etc?

## Getting support
TODO

## Issue Tracking
TODO

## Downloading artifacts
See [downloading CareWeb Framework artifacts][] for Maven repository information. Unable to
use Maven or other transitive dependency management tools? See [building a
distribution with dependencies][].
### Transitive Dependencies
The following dependencies are core to the CareWeb Framework
* ZK Framework - Please see the following [ZK][] page for references and guidelines for using and obtaining ZK artifacts.
* Spring Framework

## Building from source
The CareWeb Framework uses a Maven-based build system. In the instructions
below, pom.xml is invoked from the root of the source tree and serves as
a cross-platform, self-contained bootstrap mechanism for the build.

### Prerequisites

[Git][] and Java 6, Maven

Be sure that your `JAVA_HOME` environment variable points to the `jdk1.6.x` folder
extracted from the JDK download.

### Check out sources

`git clone https://github.com/carewebframework/carewebframework-core.git`

Note: Problem checking out?  Windows has a file path length limit of 260 characters.  If you run into errors suggesting that a file cannot be created, try cloning from a shorter root path (i.e. C:\workspace).

### Install all CareWeb Framework jars into your local Maven repository/cache
Make sure to cd into the carewebframework-core directory and then execute
`mvn clean install`

### Compile and test, build all jars, distribution zips and docs
From root dir, execute
`mvn clean package`

### Run TestHarness (Mock Webapp)
Pre-requisite is to install all CareWeb Framework jars into local Maven repository/cache.
From root dir, cd in the org.carewebframework.testharness.webapp module directory and execute the following.
`mvn tomcat:run-war`

Once tomcat has started, open browser and enter following URL
`http://localhost:8080/org.carewebframework.testharness.webapp-3.0.0-SNAPSHOT`

## Contributing
[Pull requests][] are welcome; see the [contributor guidelines][] for details.

## License
The CareWeb Framework is released under version 2.0 of the [Mozilla Public License][] as amended by the
[Health-Related Additional Disclaimer of Warranty and Limitation of Liability][].

Use of the Enterprise Edition of the ZK Framework requires a valid [ZK Open Source License Agreement][], available
on request from [ZK][] at no charge.

[CareWebFramework organization]: https://github.com/carewebframework
[downloading CareWeb Framework artifacts]: https://github.com/carewebframework/carewebframework-core/wiki/Downloading-CWF-artifacts
[Javadoc]: #
[Git]: http://help.github.com/set-up-git-redirect
[Pull requests]: http://help.github.com/send-pull-requests
[contributor guidelines]: #
[ZK]: #
[Mozilla Public License]: https://github.com/carewebframework/carewebframework-core/blob/master/LICENSE.md
[Health-Related Additional Disclaimer of Warranty and Limitation of Liability]: https://github.com/carewebframework/carewebframework-core/blob/master/DISCLAIMER.md
[ZK Open Source License Agreement]: http:/www.carewebframework.com/licensing/zk/zol.pdf
[ZK]: http://www.zkoss.org/license/#zol
[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/089df57f5244ce596d7d069013f85bdf "githalytics.com")](http://githalytics.com/carewebframework/carewebframework-core)
