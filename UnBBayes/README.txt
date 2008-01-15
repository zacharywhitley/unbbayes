Before using this new Maven project, it is necessary to install the following libraries in
the local repository. These jars were not found in commom remote repositories.

In the near future, these files shall be in unbbayes's remote project.

Go to \lib>
mvn install:install-file -DgroupId=guess -DartifactId=GIFOutput -Dversion=1.0 -Dpackaging=jar -Dfile=GIFOutput.jar
mvn install:install-file -DgroupId=guess -DartifactId=plot -Dversion=1.0 -Dpackaging=jar -Dfile=plot.jar

Go to \lib\protege\edu.stanford.smi.protegex.owl>
>mvn install:install-file -DgroupId=guess -DartifactId=iri -Dversion=2.4 -Dpackaging=jar -Dfile=iri.jar
>mvn install:install-file -DgroupId=guess -DartifactId=jena -Dversion=2.4 -Dpackaging=jar -Dfile=jena.jar
mvn install:install-file -DgroupId=guess -DartifactId=protege-owl -Dversion=3.2 -Dpackaging=jar -Dfile=protege-owl.jar

Go to \lib\protege>
mvn install:install-file -DgroupId=guess -DartifactId=protege -Dversion=3.2 -Dpackaging=jar -Dfile=protege.jar

Go to \lib\powerloom>
mvn install:install-file -DgroupId=guess -DartifactId=powerloom -Dversion=1.0 -Dpackaging=jar -Dfile=powerloom.jar
mvn install:install-file -DgroupId=guess -DartifactId=stella -Dversion=1.0 -Dpackaging=jar -Dfile=stella.jar

Go to \lib>
mvn install:install-file -DgroupId=guess -DartifactId=miglayout15-swing -Dversion=1.0 -Dpackaging=jar -Dfile=miglayout15-swing.jar

