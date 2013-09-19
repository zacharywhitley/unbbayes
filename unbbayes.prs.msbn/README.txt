CONFIGURING MAVEN
------------------

The first thing needed is to install the maven project in your computer.

After that, it is necessary to install the maven plugin for your IDE, in this case, the Eclipse IDE.
Current distributions comes with m2e plugin already, but please, make sure your Eclipse distribution comes with maven plugin.

This project depends on UnBBayes, so you should also checkout UnBBayes folder from trunk and
enable m2e workspace resolution, so that this project automatically points to classes in the same workspace.
Or else, you'll need to obtain UnBBayes' jar file and either include it in your local maven repository, or include it in your classpath.


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