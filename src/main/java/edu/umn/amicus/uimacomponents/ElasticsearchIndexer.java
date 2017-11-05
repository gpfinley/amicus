package edu.umn.amicus.uimacomponents;

/**
 * UIMA CAS consumer that will add specified fields to an index.
 * Not used in this state for AMICUS, but available as a backup.
 *
 * Created by Ben Knoll on 10/16/17.
 */

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.CasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.resource.ResourceInitializationException;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

public class ElasticsearchIndexer extends CasAnnotator_ImplBase {

    private Client client;

    private String indexName;

    private String textView;

    private String[] indexedFeatures;
    private String[] indexedFeaturesNames;

    private int bulkSize;

    private BulkRequestBuilder bulkRequestBuilder = null;

    private ListenableActionFuture<BulkResponse> future = null;


    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        indexName = ((String) aContext.getConfigParameterValue("indexName"));

        String esHost = (String) aContext.getConfigParameterValue("esHost");
        Integer esPort = (Integer) aContext.getConfigParameterValue("esPort");

        textView = ((String) aContext.getConfigParameterValue("textView"));

        bulkSize = ((Integer) aContext.getConfigParameterValue("bulkSize"));

        indexedFeatures = ((String[]) aContext.getConfigParameterValue("indexedFeatures"));
        indexedFeaturesNames = ((String[]) aContext.getConfigParameterValue("indexedFeaturesNames"));

        try {
            client = TransportClient.builder().build()
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(esHost), esPort));
        } catch (UnknownHostException e) {
            throw new ResourceInitializationException(e);
        }
    }

    public void process(CAS aCAS) throws AnalysisEngineProcessException {
        CAS view = aCAS.getView(textView);

        if (bulkRequestBuilder == null) {
            bulkRequestBuilder = client.prepareBulk();
        }

        IndexRequestBuilder indexRequestBuilder = client.prepareIndex()
                .setIndex(indexName)
                .setType("Document");
        // .setId( if it has an id add here );

        try {
            XContentBuilder xContentBuilder = XContentFactory.jsonBuilder()
                    .startObject()
                    .field("text", view.getDocumentText());

            for (int i = 0; i < indexedFeatures.length; i++) {
                Feature feature = view.getTypeSystem().getFeatureByFullName(indexedFeatures[i]);
                Type domain = feature.getDomain();
                FSIterator<FeatureStructure> allIndexedFS = view.getIndexRepository()
                        .getAllIndexedFS(domain);

                // this is where you set a name to the array of values
                xContentBuilder.startArray(indexedFeaturesNames[i]);

                while (allIndexedFS.hasNext()) {
                    FeatureStructure next = allIndexedFS.next();
                    String value = next.getFeatureValueAsString(feature);

                    // this is where you are adding values to the array.

                    xContentBuilder.value(value);
                }

                xContentBuilder.endArray();
            }

            bulkRequestBuilder.add(indexRequestBuilder.setSource(xContentBuilder.endObject()));

            if (bulkRequestBuilder.numberOfActions() % bulkSize == 0) {
                if (future != null) {
                    future.actionGet();
                }

                future = bulkRequestBuilder.execute();
                bulkRequestBuilder = client.prepareBulk();
            }
        } catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException {
        if (bulkRequestBuilder.numberOfActions() > 0) {
            if (future != null) {
                future.actionGet();
                future = null;
            }

            bulkRequestBuilder.execute().actionGet();
            bulkRequestBuilder = client.prepareBulk();
        }
    }

    @Override
    public void destroy() {
        client.close();
    }
}