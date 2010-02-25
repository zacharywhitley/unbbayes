CONFIGURING MAVEN
------------------

The first thing needed is to install the maven project in your computer.

After that, it is necessary to install the maven plugin for your IDE, in this case, the Eclipse IDE.
The update site for this plugin is: http://q4e.googlecode.com/svn/trunk/updatesite/.

Before using this new Maven project, it is necessary to install the following libraries in
the local repository. These jars were not found in common remote repositories.

In the near future, these files shall be in unbbayes's remote project.

<If you wish to build unbbayes.datamining plugin (you do not need to do this if you wish to build only the core)>
Go to \mvn_lib>
mvn install:install-file -DgroupId=guess -DartifactId=plot -Dversion=1.0 -Dpackaging=jar -Dfile=plot.jar
mvn install:install-file -DgroupId=guess -DartifactId=miglayout15-swing -Dversion=1.0 -Dpackaging=jar -Dfile=miglayout15-swing.jar

<If you do not have UnBBayes in your local repository, then execute the below command>
Go to \mvn_lib\unbbayes>
mvn install:install-file -DpomFile=pom.xml -Dpackaging=jar -Dfile=unbbayes.jar

<If you do not have learning module in your local repository, then execute the below command>
Go to \plugins>
mvn install:install-file -DgroupId=br.unb.cic -DartifactId=unbbayes.learning -Dversion=0.0.1 -Dpackaging=jar -Dfile=unbbayes.learning.jar

CONFIGURING IDE'S BUILDPATH
-------------------
-> This is only necessary if you want to run UnBBayes and its plugin directly on your IDE (e.g. Eclipse).
   Maven does not use IDE's buildpath to build a project.
-> Every files must be placed into "plugins/<YOUR_PLUGIN_ID>" folder (this is where UnBBayes,
   on execution, will look for plugins).
1. Point all your output folder to <YOUR_PROJECT_FOLDER>/plugins/<YOUR_PLUGIN_ID> 
(e.g. UnBMiner/plugins/unbbayes.datamining in this case), so that all
classes (and optionally resources) will be exported to plugin folder. 
2. Configure disable inclusion/exclusion filter (include everything and exclude nothing).


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