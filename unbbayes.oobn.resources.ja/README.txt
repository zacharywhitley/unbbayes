CONFIGURING MAVEN
------------------

The first thing needed is to install the maven project in your computer.

After that, it is necessary to install the maven plugin for your IDE, in this case, the Eclipse IDE.
The update site for this plugin is: http://q4e.googlecode.com/svn/trunk/updatesite/.

Before using this new Maven project, it is necessary to install the following libraries in
the local repository. These jars were not found in common remote repositories.

In the near future, these files shall be in unbbayes's remote project.

<If you do not have UnBBayes in your local mvn repository, then execute the below command>
Go to \mvn_lib\unbbayes>
mvn install:install-file -DgroupId=guess -DartifactId=jpf -Dversion=1.5 -Dpackaging=jar -Dfile=jpf-1.5.jar
mvn install:install-file -DpomFile=pom.xml -Dpackaging=jar -Dfile=unbbayes-4.0.0.jar

<If you do not have MSBN module in your local mvn repository, then execute the below command>
Go to \mvn_lib\unbbayes>
mvn install:install-file -DgroupId=br.unb.cic -DartifactId=unbbayes.prs.msbn -Dversion=1.1.0 -Dpackaging=jar -Dfile=unbbayes.prs.msbn-1.1.0.jar


<If you do not have OOBN module in your local mvn repository, then execute the below command>
Go to \mvn_lib\unbbayes>
mvn install:install-file -DgroupId=br.unb.cic -DartifactId=unbbayes.prs.oobn -Dversion=1.1.0 -Dpackaging=jar -Dfile=unbbayes.prs.oobn-1.1.0.jar

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