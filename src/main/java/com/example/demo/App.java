package com.example.demo;

import com.microsoft.azure.cosmosdb.ConnectionMode;
import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.DataType;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.IncludedPath;
import com.microsoft.azure.cosmosdb.Index;
import com.microsoft.azure.cosmosdb.IndexingMode;
import com.microsoft.azure.cosmosdb.IndexingPolicy;
import com.microsoft.azure.cosmosdb.JsonSerializable;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

public class App {
    private static final String DATABASE = "test_db";
    private static final String COLLECTION = "test_coll";

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("main <cosmos_host> <cosmos_master_key>");
            System.exit(1);
        }

        AsyncDocumentClient client = null;
        try {
            ConnectionPolicy connectionPolicy = new ConnectionPolicy();
            connectionPolicy.setConnectionMode(ConnectionMode.Gateway);
            client = new AsyncDocumentClient.Builder()
                    .withServiceEndpoint(args[0])
                    .withMasterKeyOrResourceToken(args[1])
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.Eventual)
                    .build();

            try {
                client.deleteDatabase(String.format("/dbs/%s", DATABASE), null).toBlocking().single();
            } catch (RuntimeException e) {
                if (!isNotFound(e)) {
                    throw e;
                }
            }

            Database dbDefinition = new Database();
            dbDefinition.setId(DATABASE);
            client.createDatabase(dbDefinition, null).toBlocking().single();


            DocumentCollection documentCollection = new DocumentCollection();
            documentCollection.setId(COLLECTION);
            IndexingPolicy indexingPolicy = new IndexingPolicy();
            indexingPolicy.setIndexingMode(IndexingMode.Consistent);
            IncludedPath includedPath = new IncludedPath();
            includedPath.setPath("/*");
            Index numberIndex = Index.Range(DataType.Number, -1);
            Index stringIndex = Index.Range(DataType.String);
            includedPath.setIndexes(Arrays.asList(numberIndex, stringIndex));
            indexingPolicy.setIncludedPaths(Collections.singletonList(includedPath));
            documentCollection.setIndexingPolicy(indexingPolicy);

            ResourceResponse<DocumentCollection> response = client.createCollection(String.format("/dbs/%s", DATABASE), documentCollection, null).toBlocking().single();

            System.out.println("Status code: " + response.getStatusCode());
            for (IncludedPath path : response.getResource().getIndexingPolicy().getIncludedPaths()) {
                System.out.println("path: " + path.getPath());
                System.out.println("indexes: [" + indexesAsString(path) + "]");
                System.out.println("---");
            }
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    private static String indexesAsString(IncludedPath path) {
        return path.getIndexes().stream().map(JsonSerializable::toString).collect(Collectors.joining(","));
    }

    private static boolean isNotFound(RuntimeException e) {
        return (e.getCause() instanceof DocumentClientException) && ((DocumentClientException) e.getCause()).getStatusCode() == 404;
    }
}
