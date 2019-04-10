# Azure Cosmos DB Indexing Policy Example
Minimal example for creating a collection with an index policy. It produces different results when using `com.microsoft.azure:azure-cosmosdb:2.4.4` and `com.microsoft.azure:azure-cosmosdb:2.3.1`.

# Change Azure Cosmos DB client
Replace dependency in `pom.xml` from
```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-cosmosdb</artifactId>
    <version>2.4.4</version>
</dependency>
```
to 
```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-cosmosdb</artifactId>
    <version>2.3.1</version>
</dependency>
```

or vice versa.

# Build
```bash
mvn clean package
```

# Run
```bash
mvn exec:java -Dexec.mainClass="com.example.demo.App" -Dexec.args="<cosmos_host> <cosmos_master_key>"
```
