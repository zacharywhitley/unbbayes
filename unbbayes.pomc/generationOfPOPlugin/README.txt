CONFIGURING MAVEN
------------------

The first thing needed is to install the maven project in your computer.

After that, it is necessary to install the maven plugin for your IDE, in this case, the Eclipse IDE.

Before using this new Maven project, it is necessary to install the following libraries in
the local repository. These jars were not found in common remote repositories.

In the near future, these files shall be in unbbayes's remote project.

<If you do not have UnBBayes in your local mvn repository, then execute the below command>
Go to \mvn_lib\unbbayes>
mvn install:install-file -DgroupId=guess -DartifactId=jpf -Dversion=1.5 -Dpackaging=jar -Dfile=jpf-1.5.jar
mvn install:install-file -DgroupId=br.unb.cic -DartifactId=unbbayes -Dversion=4.21.18 -Dpackaging=jar -Dfile=unbbayes-4.21.18.jar
mvn install:install-file -DgroupId=br.unb.cic -DartifactId=unbbayes.prs.mebn -Dversion=1.14.13 -Dpackaging=jar -Dfile=unbbayes.prs.mebn-1.14.13.jar

The following JAR is a dependency of the UnBBayes PR-OWL 2 format extension
Go to \mvn_lib\>
mvn install:install-file -DgroupId=br.unb.cic -DartifactId=unbbayes.gui.mebn.ontology.protege -Dversion=1.2.5 -Dpackaging=jar -Dfile=unbbayes.gui.mebn.ontology.protege-1.2.5.jar

Go to \mvn_lib\protege>
mvn install:install-file -DgroupId=guess -DartifactId=protege -Dversion=3.2 -Dpackaging=jar -Dfile=protege.jar

Go to \mvn_lib\protege\edu.stanford.smi.protegex.owl>
mvn install:install-file -DgroupId=guess -DartifactId=iri -Dversion=2.4 -Dpackaging=jar -Dfile=iri.jar
mvn install:install-file -DgroupId=guess -DartifactId=jena -Dversion=2.4 -Dpackaging=jar -Dfile=jena.jar
mvn install:install-file -DgroupId=guess -DartifactId=protege-owl -Dversion=3.2 -Dpackaging=jar -Dfile=protege-owl.jar



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