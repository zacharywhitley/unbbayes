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

RUNNING AS A LIBRARY
-------------------

1. Generate a distribution by following the step 3 in Section "CREATING A RELEASE", 
2. Extract the content of the generated ZIP file, 
3. Include all jar files in the "lib" folder and in the root folder 
(you will supposedly find only the "unbbayes.prs.bn.inference.extension.MinProductJunctionTree-<version>.jar" file in the root)
in your classpath.

Note: some useful classes/interfaces in this plug-in (when used as library) are:

Class - edu.gmu.ace.scicast.MarkovEngineImpl (main class of the Markov Engine backend in DAGGRE project <http://daggre.org/>)
Interface - edu.gmu.ace.scicast.MarkovEngineInterface (interface of MarkovEngineImpl)

Class - unbbayes.prs.bn.inference.extension.JunctionTreeMPEAlgorithm (UnBBayes' inference algorithm class for most probable explanation algorithm)
Class - unbbayes.prs.bn.inference.extension.JunctionTreeLPEAlgorithm (UnBBayes' inference algorithm class for "least" probable explanation algorithm)
Class - unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm (UnBBayes' inference algorithm class for asset's update algorithm - the core of Markov Engine backend).
Test suite - unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithmTest (Sample usage of AssetAwareInferenceAlgorithm and soft evidence - can also be used as sample for the other algorithms if adapted).
