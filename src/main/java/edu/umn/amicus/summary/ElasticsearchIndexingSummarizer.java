package edu.umn.amicus.summary;

import edu.umn.amicus.AlignedTuple;
import edu.umn.amicus.AmicusException;
import edu.umn.amicus.config.ClassConfigurationLoader;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Adds specified fields to an Elasticsearch index.
 *
 */
public class ElasticsearchIndexingSummarizer extends Summarizer implements CollectionSummarizer {

    private static final String[] esFieldNames;
    private static final String esHost;
    private static final String esIndexName;
    private static final String esTypeName;
    private static final String esIdField;
    private static final int esPort;

//    protected ElasticsearchIndexingSummarizer() {
//        super();
//    }

    public ElasticsearchIndexingSummarizer(String[] viewNames, String[] types, String[] fields) {
        super(viewNames, types, fields);
    }

    @Override
    public String summarizeCollection(Iterator<AlignedTuple> tuples, Iterator<String> docIds) throws AmicusException {

        Client client;
        try {
            client = TransportClient.builder().build()
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(esHost), esPort));
        } catch (UnknownHostException e) {
            throw new AmicusException(e);
        }

        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();

        IndexRequestBuilder indexRequestBuilder = client.prepareIndex()
                .setIndex(esIndexName)
                .setType(esTypeName);

        // Go through all tuples and save annotations across each document
        // Whenever the document changes, save all additions to the bulk request builder and start a new doc (matrix)
        String lastDocId = null;
        List<List<String>> valuesMatrix = null;
        while (tuples.hasNext()) {
            AlignedTuple tuple = tuples.next();
            String docId = docIds.next();
            if (!docId.equals(lastDocId)) {
                if (lastDocId != null) {
                    bulkRequestBuilder.add(indexRequestBuilder.setSource(addToIndices(valuesMatrix, lastDocId)));
                }
                valuesMatrix = initValuesMatrix(tuple.size());
                lastDocId = docId;
            }
            for (int i=0; i<tuple.size(); i++) {
                String value = String.valueOf(tuple.get(i));
                if (value != null && value.length() > 0) {
                    valuesMatrix.get(i).add(value);
                }
            }
        }
        bulkRequestBuilder.add(indexRequestBuilder.setSource(addToIndices(valuesMatrix, lastDocId)));

        bulkRequestBuilder.execute();
        client.close();

        // todo: logging as return value?
        return "";
    }

    private static List<List<String>> initValuesMatrix(int n) {
        List<List<String>> valuesMatrix = new ArrayList<>();
        for (int i=0; i<n; i++) {
            valuesMatrix.add(new ArrayList<String>());
        }
        return valuesMatrix;
    }

    private XContentBuilder addToIndices(List<List<String>> valueMat, String docId) throws AmicusException {
        try {
            XContentBuilder xContentBuilder = XContentFactory.jsonBuilder()
                    .startObject()
                    .field(esIdField, docId);
            // this is where you set a name to the array of values
            for (int i = 0; i < valueMat.size(); i++) {
                // Use the name of the UIMA field if the user hasn't configured enough elasticsearch field names for this class config
                xContentBuilder.startArray(i >= esFieldNames.length ? fields[i] : esFieldNames[i]);
                for (String value : valueMat.get(i)) {
                    xContentBuilder.value(value);
                }
            }
            return xContentBuilder.endObject();
        } catch (IOException e) {
            throw new AmicusException(e);
        }
    }

    private static class Config {
        public String[] esFieldNames;
        public String esHost;
        public String esIndexName;
        public String esTypeName;
        public String esIdField;
        public int esPort;
    }

    static {
        Config config;
        try {
            config = (Config) ClassConfigurationLoader.load(Config.class);
        } catch (FileNotFoundException e) {
            config = null;
        }
        if (config == null) {
            esFieldNames = new String[]{};
            esHost = "http://localhost";
            esIndexName = "amicusdocs";
            esTypeName = "amicusdoc";
            esIdField = "id";
            esPort = 9200;
        } else {
            esFieldNames = config.esFieldNames;
            esHost = config.esHost;
            esIndexName = config.esIndexName;
            esTypeName = config.esTypeName;
            esIdField = config.esIdField;
            esPort = config.esPort;
        }
    }

}
