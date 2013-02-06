

-- No es soportado un slot chain de mas de dos tablas.


- Generación de tabla dinámica dependiendo de instancias.
- Revisar cuando no hay ruta para llegar del padre al hijo por instancias.
- Revisar mecanismo de parada.
- Nodo de finalización o nodo de incertidumbre.
 


- aggregate function
- A node is parent and child of different tables.



FIXED
- Adding evidence is bad.
- Null value. 
- Unique node name.
- PRM algorithm
	> evidences
	> Recursive parents ** (it is not recursive for more than two levels).
- only one path
- cpt is not working.
- bloodtype example
- same attribute: parent and child.

;create=true

-------------------------------------------------
Future work
- save probabilistic model.
- A foreign key pointing to many other attributes.

-------------------------------------------------
TODO
- Add reference to jgraphx
mvn install:install-file -Dfile=jgraphx.jar -DgroupId=jgraphx -DartifactId=jgraphx -Dversion=1 -Dpackaging=jar


Current version
* only supports Derby DB.
* Parent and Child to the same attribute is not supported.