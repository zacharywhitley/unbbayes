CONFIGURING MAVEN
------------------

The first thing needed is to install the maven project in your computer.
The newest version should work, but UnBBayes was built using maven 2, so you may want to download version 2 for compatibility.


You will also need to install a maven plugin for your Eclipse, so that
dependencies are automatically solved for projects in your workspace.
Newer versions of eclipse already comes with a maven plugin. Please, make sure you have
downloaded an eclipse version which comes with the "m2e" plugin.

Sometimes, your maven plugin may not understand that UnBBayes is a maven project.
In such case, please right click the project's root folder, and
choose "Configure > Convert to Maven Project".



CREATING A RELEASE
-------------------

-> NEW RELEASE - ZIP FILE
1. Change the pom file so that it defines the right version (majorVersion.NumberOfFeatures.NumberOfFixedBugs).
2. Create dist file. Go to the projects root folder command line and run*:

mvn assembly:assembly

 

