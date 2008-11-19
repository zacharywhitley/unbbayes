The first thing needed is to install the maven project in your computer.

After that, it is necessary to install the maven plugin for your IDE, in this case, the Eclipse IDE.
The update site for this plugin is: http://q4e.googlecode.com/svn/trunk/updatesite/.

Before using this new Maven project, it is necessary to install the following libraries in
the local repository. These jars were not found in common remote repositories.

In the near future, these files shall be in unbbayes's remote project.

Go to \lib>
mvn install:install-file -DgroupId=guess -DartifactId=GIFOutput -Dversion=1.0 -Dpackaging=jar -Dfile=GIFOutput.jar
mvn install:install-file -DgroupId=guess -DartifactId=plot -Dversion=1.0 -Dpackaging=jar -Dfile=plot.jar
mvn install:install-file -DgroupId=guess -DartifactId=miglayout15-swing -Dversion=1.0 -Dpackaging=jar -Dfile=miglayout15-swing.jar

Go to \lib\powerloom>
mvn install:install-file -DgroupId=guess -DartifactId=powerloom -Dversion=1.0 -Dpackaging=jar -Dfile=powerloom.jar
mvn install:install-file -DgroupId=guess -DartifactId=stella -Dversion=1.0 -Dpackaging=jar -Dfile=stella.jar

Go to \lib\protege>
mvn install:install-file -DgroupId=guess -DartifactId=protege -Dversion=3.2 -Dpackaging=jar -Dfile=protege.jar

Go to \lib\protege\edu.stanford.smi.protegex.owl>
mvn install:install-file -DgroupId=guess -DartifactId=iri -Dversion=2.4 -Dpackaging=jar -Dfile=iri.jar
mvn install:install-file -DgroupId=guess -DartifactId=jena -Dversion=2.4 -Dpackaging=jar -Dfile=jena.jar
mvn install:install-file -DgroupId=guess -DartifactId=protege-owl -Dversion=3.2 -Dpackaging=jar -Dfile=protege-owl.jar

CREATING A RELEASE
-------------------

To create a release, change the pom file so that it defines the right version (majorVersion.NumberOfFeatures.NumberOfFixedBugs).
Define the features implemented and bugs fixed in the RELEASE.txt file. Change the unbbayes.gui.AboutPane to show the 
correct version.

Go to the projects root folder command line and run:
mvn assembly:assembly -Dmaven.test.skip=true

