PRM PLUGIN
----------

REQUIREMENT: Configuring Maven
------------------------------
The first thing needed is to install the maven project in your computer.
After that, it is necessary to install the maven plugin for your IDE, in this case, the Eclipse IDE.
The update site for this plugin is: http://q4e.googlecode.com/svn/trunk/updatesite/.

Additionally, the unbbayes-code project is required to solve the unbbayes dependency.


RUNNING THE PROJECT
-------------------
This project is a plugin of UnBBayes. Running UnBBayes and the PRM Plugin can be made executing 
the class UnBBayesMainDelegator located in the folder src/test/java/main. It creates a new 
button aside of the button for "create a new BN". After click on it, it requires a SQL URL.
Some databases are supported like OracleDB, and PostgreSQL, but only MySQL and DerbyDB has 
been tested. An URL example is the next one:

jdbc:mysql://localhost:3306/MDA?user=root&password=123

This is an URL for MySQL, the server is localhost, port 3306, database MDA, user is root and
password is 123. After a successful connection, the database structure will be displayed. Now,
it is possible to do the next actions:

- Defining new relationships: the user can define the relationships by clicking on the source column name
  and clicking on the target column name.

- Defining CPTs: after defining the relationship, a new button appear at the right side of the column name.
  Click on it to define the CPT for that attribute.

- Browsing data: Looking the stored entries for each table is possible by clicking on the glass button (located
  on the right side of the name of the table). 

- PRM Query: to create a bayesian network based on the PRM algorithm is necessary to click on the interrogation
  button in the table of browing data.


TESTING
-------
Some unit tests have been created to test the algorithms, db connection and third party libraries.
- ReadDBSchema: test the DB connection and the ddlutils library.
- PrmCompilerTest: test the PRM algorithm.
- DynamicTableHelperTest: test the dynamic table algorithm for prm.
- PathFinderTest: test the algorithm to find possible routes between two columns.


BUGS
----
- The test cases for the PrmCompiler need to be validated.
- Aggregate functions like mean or median  are not implemented.
- Save the probabilistic model is needed.
- RU is not implemented.
- Apply Filter for non-useful nodes.



