CONFIGURING MAVEN
------------------

The first thing needed is to install the maven project in your computer.

After that, it is necessary to install the maven plugin for your IDE, in this case, the Eclipse IDE.
The update site for this plugin is: http://q4e.googlecode.com/svn/trunk/updatesite/.

Before using this new Maven project, it is necessary to install the following libraries in
the local repository. These jars were not found in common remote repositories.

In the near future, these files shall be in unbbayes's remote project.

Go to \lib>
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

-> NEW RELEASE - ZIP FILE
1. Change the pom file so that it defines the right version (majorVersion.NumberOfFeatures.NumberOfFixedBugs).
2. Define the features implemented and bugs fixed in the RELEASE.txt file and in src/changes/changes.xml 
and the candidates features and bugs for the next version in README.txt. 
3. Change the unbbayes.properties file to the correct version
4. Create dist file. Go to the projects root folder command line and run*:
mvn assembly:assembly -Dmaven.test.skip=true
5. Make a few changes in the generated dist file. 
a) Unzip the distribution file somewhere. Remove unnecessary example files (until examples folder is cleaned up).
b) Rename the jar file to unbbayes.jar.
c) Zip the remain files again (without the '-dist' part).

-> NEW WEBSITE**
6. Make the necessary changes in the web site by changing the files in /src/site/.
7. Create the website. Go to the projects root folder command line and run**:
mvn site:site -Dmaven.test.skip=true
8. Verify that the created website is correct.
9. Deploy the new website. Go to the projects root folder command line and run**:
mvn site:deploy -Dmaven.test.skip=true

* For now we are skipping the tests because we have to fix some tests before doing that.
** To make sure you can deploy the website, please configure your machine to be able to connect 
to the sourceforge server. Read http://docs.codehaus.org/display/MAVENUSER/MavenAndSourceforge 
for details.


CANDIDATES FOR NEXT RELEASE
---------------------------

--add--
 2718989	Improve performance	 Open	 2009-03-28	rommelnc	rommelnc	 9
 2717798	Implement test case for exact Evaluation	 Open	 2009-03-27	rommelnc	rommelnc	 5
 2027179	Do not allow duplicate finding	 Open	 2008-07-24	laecio_lima	rommelnc	 5
 1999629	Pseudocode to reference Context Nodes	 Open	 2008-06-21	 nobody	cardialfly	 4
 1968929	"Metafor" interfaces	 Open	 2008-05-21	cardialfly	cardialfly	 5
 1968926	Make it easier to edit networks at lower display resolutions	 Open	 2008-05-21	laecio_lima	cardialfly	 1
 1962669	Likelyhood finding	 Open	 2008-05-12	 nobody	cardialfly	 5
 1962667	FOL finding	 Open	 2008-05-12	 nobody	cardialfly	 2
 1962651	Save current	 Open	 2008-05-12	 nobody	cardialfly	 3
 1949085	Saving findings on PR-OWL files	 Open	 2008-04-22	 nobody	cardialfly	 5
 1908032	MEBN element's description and PR-OWL comments	 Open	 2008-03-05	laecio_lima	cardialfly	 1
 1908029	Dynamic help	 Open	 2008-03-05	laecio_lima	cardialfly	 1
 1908023	Arrows at node's edge (outer line).	 Open	 2008-03-05	rommelnc	cardialfly	 1
 1908018	Self-resizing nodes.	 Open	 2008-03-05	rommelnc	cardialfly	 1
 1907930	Saving "recursive" (entity ordering) information in PR-OWL	 Open	 2008-03-05	 nobody	cardialfly	 5
 1907925	Recursive variable definition in PR-OWL	 Open	 2008-03-05	adelfi23	cardialfly	 8
--fix--
 1975377	Can not edit MTheory name	 Open	 2008-05-27	laecio_lima	rommelnc	 None	 1
 1955808	Problem with the names of the classes of Pr-OWL	 Open	 2008-05-01	 nobody	laecio_lima	 None	 5
 1954465	Resizing of nodes are not applicable anymore (ordinal BN)	 Open	 2008-04-29	laecio_lima	cardialfly	 None	 3
 1949115	Cannot instantiate categorical entities	 Open	 2008-04-22	 nobody	cardialfly	 None	 3
 1910479	Some variable/entity names causes KB to crush	 Open	 2008-03-09	 nobody	cardialfly	 Later	 6
 1907281	Zombie-nodes at query panel	 Open	 2008-03-04	laecio_lima	cardialfly	 None	 1

