# Database
__ext.hydratight.db__

The Database (ext.hydratight.db) package contains classes that can be used to query databases - primarily Oracle databases.

The package consists of the following classes:
-	Query: A generic class that can query any Oracle-type database (this can be extended to all databases by altering the driver used)
-	WindchillQuery: A class for querying the database associated with the Windchill instance running the code - the details are extracted from db.properties
-	OracleQuery: A class for querying an Oracle Enteprise Business Suite database based on the calling Windchill instance.