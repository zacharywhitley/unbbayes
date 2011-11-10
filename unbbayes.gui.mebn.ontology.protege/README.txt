CONFIGURING MAVEN
------------------

The first thing needed is to install the maven project in your computer.

After that, it is necessary to install the maven plugin for your IDE, in this case, the Eclipse IDE.
The update site for this plugin is: http://q4e.googlecode.com/svn/trunk/updatesite/.

Before using this new Maven project, it is necessary to install the following libraries in
the local repository. These jars were not found in common remote repositories.

In the near future, these files shall be in unbbayes's remote project.

The following JARs are dependencies of UnBBayes MEBN plugin

Go to \mvn_lib\powerloom>
mvn install:install-file -DgroupId=guess -DartifactId=powerloom -Dversion=1.0 -Dpackaging=jar -Dfile=powerloom.jar
mvn install:install-file -DgroupId=guess -DartifactId=stella -Dversion=1.0 -Dpackaging=jar -Dfile=stella.jar

Go to \mvn_lib\protege>
mvn install:install-file -DgroupId=guess -DartifactId=protege -Dversion=3.2 -Dpackaging=jar -Dfile=protege.jar

Go to \mvn_lib\protege\edu.stanford.smi.protegex.owl>
mvn install:install-file -DgroupId=guess -DartifactId=iri -Dversion=2.4 -Dpackaging=jar -Dfile=iri.jar
mvn install:install-file -DgroupId=guess -DartifactId=jena -Dversion=2.4 -Dpackaging=jar -Dfile=jena.jar
mvn install:install-file -DgroupId=guess -DartifactId=protege-owl -Dversion=3.2 -Dpackaging=jar -Dfile=protege-owl.jar

The following JARs are dependencies of protege 4.1

Go to \src\main\resources\protege\bin>
mvn install:install-file -DgroupId=org.apache.felix -DartifactId=org.apache.felix.main -Dversion=2.0.4 -Dpackaging=jar -Dfile=felix.jar

Go to \src\main\resources\protege\bundles>
mvn install:install-file -DgroupId=org.apache.felix -DartifactId=org.apache.felix.bundlerepository -Dversion=1.4.2 -Dpackaging=jar -Dfile=org.apache.felix.bundlerepository-1.4.2.jar
mvn install:install-file -DgroupId=guess -DartifactId=org.eclipse.equinox.common -Dversion=3.5.0.v20090520-1800 -Dpackaging=jar -Dfile=org.eclipse.equinox.common.jar
mvn install:install-file -DgroupId=guess -DartifactId=org.eclipse.equinox.registry -Dversion=3.4.100.v20090520-1800 -Dpackaging=jar -Dfile=org.eclipse.equinox.registry.jar
mvn install:install-file -DgroupId=guess -DartifactId=org.eclipse.equinox.supplement -Dversion=1.2.0.v20090518 -Dpackaging=jar -Dfile=org.eclipse.equinox.supplement.jar
mvn install:install-file -DgroupId=guess -DartifactId=org.protege.common -Dversion=4.1 -Dpackaging=jar -Dfile=org.protege.common.jar
mvn install:install-file -DgroupId=guess -DartifactId=org.protege.editor.core.application -Dversion=4.1 -Dpackaging=jar -Dfile=org.protege.editor.core.application.jar
mvn install:install-file -DgroupId=guess -DartifactId=org.protege.jaxb -Dversion=1.0.0.2010_10_26_0330 -Dpackaging=jar -Dfile=org.protege.jaxb.jar

Go to \src\main\resources\protege\plugins>
mvn install:install-file -DgroupId=guess -DartifactId=org.coode.dlquery -Dversion=1.1.0.2010_09_03_0304 -Dpackaging=jar -Dfile=org.coode.dlquery.jar
mvn install:install-file -DgroupId=guess -DartifactId=org.protege.editor.owl -Dversion=4.1.0.b209_2010_09_03_0303 -Dpackaging=jar -Dfile=org.protege.editor.owl.jar
mvn install:install-file -DgroupId=guess -DartifactId=org.semanticweb.HermiT -Dversion=1.2.5.927 -Dpackaging=jar -Dfile=org.semanticweb.HermiT.jar
mvn install:install-file -DgroupId=guess -DartifactId=org.semanticweb.owl.owlapi -Dversion=3.1.0.1602 -Dpackaging=jar -Dfile=org.semanticweb.owl.owlapi.jar

The following dependencies are jars within the protege plugins, but because they cannot be read by non-OSGI applications, they must be explicitly included to classpath
Go to \mvn_lib\Protege_4.1_beta\lib>
mvn install:install-file -DgroupId=com.jgoodies -DartifactId=looks -Dversion=2.2.1 -Dpackaging=jar -Dfile=looks.jar
mvn install:install-file -DgroupId=guess -DartifactId=org.protege.owlapi.extensions -Dversion=1.0.0 -Dpackaging=jar -Dfile=protege-owlapi-extensions.jar
mvn install:install-file -DgroupId=guess -DartifactId=org.protege.xmlcatalog -Dversion=1.0.0 -Dpackaging=jar -Dfile=xmlcatalog.jar
mvn install:install-file -DgroupId=guess -DartifactId=org.onemind.jxp -Dversion=1.2.0 -Dpackaging=jar -Dfile=jxp.jar
mvn install:install-file -DgroupId=guess -DartifactId=org.coode.mdock -Dversion=1.0.0 -Dpackaging=jar -Dfile=mdock.jar
mvn install:install-file -DgroupId=guess -DartifactId=org.jdesktop.swingworker -Dversion=1.0.0 -Dpackaging=jar -Dfile=swing-worker.jar

...And just add the other jars in mvn_lib to maven local repository.


The following JAR is a dependency of the UnBBayes core
Go to \mvn_lib\jpf>
mvn install:install-file -DgroupId=guess -DartifactId=jpf -Dversion=1.5 -Dpackaging=jar -Dfile=jpf-1.5.jar

<If you do not have UnBBayes in your local mvn repository, then execute the below command>
Go to \mvn_lib\unbbayes>
mvn install:install-file -DpomFile=pom.xml -Dpackaging=jar -Dfile=unbbayes-4.2.3.jar

Go to \plugins\unbbayes.prs.mebn>
mvn install:install-file -DpomFile=pom.xml -Dpackaging=jar -Dfile=unbbayes.prs.mebn-1.8.7.jar

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