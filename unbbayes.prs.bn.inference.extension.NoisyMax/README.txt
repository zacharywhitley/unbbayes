CONFIGURING MAVEN
------------------

The first thing needed is to install the maven project in your computer.

After that, it is necessary to install the maven plugin for your IDE, in this case, the Eclipse IDE.
The update site for this plugin is: http://q4e.googlecode.com/svn/trunk/updatesite/.

Before using this new Maven project, it is necessary to install the following libraries in
the local repository. These jars were not found in common remote repositories.

In the near future, these files shall be in unbbayes's remote project.

The classes in this project depends on UnBBayes core to compile.
Please, also checkout UnBBayes core (https://svn.code.sf.net/p/unbbayes/code/trunk/UnBBayes/) in your workspace and enable workspace resolution
in your Eclipse's maven plug-in in order to compile the classes.
Please, make sure that the "dependencies" section of your pom.xml is pointing to correct version number.

			<dependency>
				<groupId>br.unb.cic</groupId>
				<artifactId>unbbayes</artifactId>
				<version>[THE VERSION NUMBER HERE MUST MATCH THE VERSION OF UNBBAYES CORE IN YOUR WORKSPACE]</version>
			</dependency>

Just changing the version number in the pom.xml is an quick/dirty workaround if version numbers don't match.
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