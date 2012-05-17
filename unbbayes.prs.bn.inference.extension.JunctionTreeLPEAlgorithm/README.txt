CONFIGURING MAVEN
------------------

The first thing needed is to install the maven project in your computer.

After that, it is necessary to install the maven plugin for your IDE, in this case, the Eclipse IDE.
The update site for this plugin is: http://q4e.googlecode.com/svn/trunk/updatesite/.

Before using this new Maven project, it is necessary to install the following libraries in
the local repository. These jars were not found in common remote repositories.

In the near future, these files shall be in unbbayes's remote project.

<If you do not have UnBBayes in your local mvn repository, then execute the below command>

(Recommended procedure)
-Checkout UnBBayes project from Subversion (<https://unbbayes.svn.sourceforge.net/svnroot/unbbayes/trunk/UnBBayes>)
to the current eclipse workspace, and then configure m2e plugin to "enable workspace resolution"

(alternative procedure)
Get the newest UnBBayes' JAR file and run the following command.
Substitute <UnBBayes Jar File> to the actual file name you have.
mvn install:install-file -DgroupId=br.unb.cic -DartifactId=unbbayes -Dversion=4.11.5 -Dpackaging=jar -Dfile=<UnBBayes Jar File>

Go to \mvn_lib\unbbayes>
mvn install:install-file -DgroupId=guess -DartifactId=jpf -Dversion=1.5 -Dpackaging=jar -Dfile=jpf-1.5.jar



Go to \mvn_lib\opencsv>
mvn install:install-file -DpomFile=pom.xml -Dpackaging=jar -Dfile=opencsv-2.3.jar

CREATING A RELEASE
-------------------

-> NEW RELEASE - ZIP FILE
1. Change the pom file so that it defines the right version (majorVersion.NumberOfFeatures.NumberOfFixedBugs).
2. Define the features implemented and bugs fixed in the RELEASE.txt file
3. Create dist file. Go to the projects root folder command line and run*:
mvn assembly:assembly -Dmaven.test.skip=true

RUNNING ON UNBBAYES
-------------------
Just place the generated ZIP file into "plugins" folder.