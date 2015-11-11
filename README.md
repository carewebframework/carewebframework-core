#Getting Started

## CareWeb Framework
A modular, extensible, composable framework for building clinical applications in a collaborative fashion, the CareWeb Framework is
built upon established open source technologies.

The <b>carewebframework-core</b> project serves as the foundation for all other CareWeb Framework projects. Browse the
repositories under the [CareWeb Framework main page](https://github.com/carewebframework) on GitHub for a full list.

## Documentation
Documentation and presentation materials may be downloaded 
[here](https://github.com/carewebframework/carewebframework.github.io/tree/master/documentation).

## Getting support
Support questions may be directed to [support@carewebframework.org](mailto:support@carewebframework.org).

## Issue tracking
Each project has its own issue tracker.  For example, issue tracking for the <b>carewebframework-core</b> project may be found
[here](https://github.com/carewebframework/carewebframework-core/issues).  If in doubt as to where a particular issue should
be logged, you may log it at that location.

## Downloading artifacts
CareWeb Framework artifacts for release versions are available from [Maven Central][] and do not require any special configuration.  Both snapshot and release versions are available from the Sonatype open source [snapshot](https://oss.sonatype.org/content/repositories/snapshots) and [release](https://oss.sonatype.org/content/repositories/releases) repositories, respectively.  To use either of these sources, you must configure your Maven [settings.xml](https://maven.apache.org/ref/3.3.3/maven-settings/settings.html) file.  A sample settings.xml file configured to access the Sonatype snapshot repository can be downloaded [here](http://www.carewebframework.org/downloads/sample-settings.xml).

### Transitive dependencies
The CareWeb Framework leverages many open source technologies.  Except where otherwise indicated, the vast majority of these requisite
dependencies may be retrieved from [Maven Central][] without any special configuration.  One major exception is the ZK Framework itself.
ZK artifacts are not reliably deployed to the [Maven Central][] repository (and then, only the Community Edition components are).  ZK
does maintain its own Maven repositories and ZK artifacts are most reliably retrieved from one of these. 
For information on configuring your Maven environment to retrieve these artifacts from one of ZK's repositories, please see the following 
[ZK page](http://books.zkoss.org/wiki/ZK_Installation_Guide/Setting_up_IDE/Maven/Resolving_ZK_Framework_Artifacts_via_Maven) 
for configuration guidelines. 

**Contrary to the recommendation that the repository information be placed directly into the <i>pom.xml</i> file, we strongly recommend instead that it be placed in your Maven <i>settings.xml</i> file.  This makes your project more resilient to possible changes in the Maven repositories upon which it depends.**

## Building from source
The CareWeb Framework uses a Maven-based build system. In the instructions that follow, Maven commands must be invoked from the root of the project directory tree where the project's Maven configuration file (<i>pom.xml</i>) is located.

### Prerequisites

<li>[Git](http://help.github.com/set-up-git-redirect)</li>
<li>[Java 7](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)</li>
<li>[Maven](https://maven.apache.org/download.cgi)</li>

**Note:** Be sure that your `JAVA_HOME` environment variable points to the `jdk1.7.x` folder extracted from the JDK download.

### Check out sources

`git clone https://github.com/carewebframework/carewebframework-core.git`

**Note:** Problem checking out?  Windows (yes, sadly, even Windows 10) has a file path length limit of 260 characters.  As Maven uses an often deeply nested directory structure with long directory names, this can become a problem.  

If you run into errors suggesting that a file cannot be created, try cloning from a shorter root path (e.g., `C:\git`).

### Install all CareWeb Framework jars into your local Maven repository/cache
Make sure to make the carewebframework-core directory your default and then execute

`mvn clean install`

This will build all core artifacts and deploy them to your local Maven repository.

### Run TestHarness (Mock Webapp)
Pre-requisite is to install all CareWeb Framework jars into local Maven repository/cache as directed above.
Then, change to the org.carewebframework.webapp.testharness module directory and execute the following:

`mvn tomcat:run-war`

Once tomcat has started, open your browser and enter following URL

`http://localhost:8080/org.carewebframework.webapp.testharness-x.y.z`

**Note:**  Replace x.y.z with the version of the CareWeb Framework that you have checked out.

## Contributing
[Pull requests](http://help.github.com/send-pull-requests) are welcome.

## License
The CareWeb Framework is released under version 2.0 of the 
[Mozilla Public License](https://github.com/carewebframework/carewebframework-core/blob/master/LICENSE.md) 
as amended by the
[Health-Related Additional Disclaimer of Warranty and Limitation of Liability](https://github.com/carewebframework/carewebframework-core/blob/master/DISCLAIMER.md).

While the core framework requires only the Community Edition of the ZK Framework, many add-ons will use the
more advanced features of ZK.  Inclusion of artifacts from the Enterprise Edition of the ZK Framework is, 
therefore, highly recommended and requires a valid 
[ZK Open Source License](http:/www.carewebframework.com/licensing/zk/zol.pdf), 
available on request from [ZK](http://www.zkoss.org/license/#zol) at no charge.

[Maven Central]: http://search.maven.org/#search%7Cga%7C1%7Ccarewebframework

