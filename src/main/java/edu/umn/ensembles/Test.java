package edu.umn.ensembles;

import edu.umn.ensembles.coreference.I2b2MarkablesReader;
import edu.umn.ensembles.coreference.StanfordNerInterceptor;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.yaml.snakeyaml.Yaml;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gpfinley on 1/20/17.
 */
public class Test {
    public static void main(String[] args) throws IOException {

        List<Map> mapList = new ArrayList<>();
        mapList.add(new HashMap<Integer, Integer>());
        mapList.add(new HashMap<Integer, Integer>());
        mapList.add(new HashMap<Integer, Integer>());

        mapList.get(0).put(1, 10);
        mapList.get(0).put(10, 100);
        mapList.get(0).put(100, 1000);
        mapList.get(1).put(1, 2);
        mapList.get(1).put(10, 20);
        mapList.get(1).put(100, 200);
        mapList.get(2).put(1, 5);
        mapList.get(2).put(10, 50);
        mapList.get(2).put(100, 500);

        Yaml yaml = new Yaml();
        yaml.dump(mapList, new FileWriter("yaml_test.txt"));

        Map<String, String> map = new HashMap<>();
        map.put("key A", "value A");
        map.put("key B", "value B");
        map.put("key C", "value C");
        yaml.dump(map, new FileWriter("classConfigurations/edu.umn.ensembles.processing.Mapper.yml"));


//        String textDir = "/Users/gpfinley/i2b2_past/2011/Beth_Train/docs";
//        String markablesDir = "/Users/gpfinley/i2b2_past/2011/Beth_Train/concepts";
////        String xmiOut = "data/xmiOutTest";
//
//        CollectionReader reader;
//        List<AnalysisEngine> engines = new ArrayList<>();
//        try {
//            TypeSystemDescription typeSystem = TypeSystemDescriptionFactory.createTypeSystemDescriptionFromPath("typeSystems/EnsemblesTypeSystem.xml");
//            reader = CollectionReaderFactory.createReader(I2b2MarkablesReader.class,
//                    typeSystem,
//                    I2b2MarkablesReader.TEXT_DIRECTORY, textDir,
//                    I2b2MarkablesReader.MARKABLES_DIRECTORY, markablesDir);
//
//            engines.add(AnalysisEngineFactory.createEngine(StanfordNerInterceptor.class));
//
////            engines.add(AnalysisEngineFactory.createEngine(XmiWriter.class,
////                    XmiWriter.CONFIG_OUTPUT_DIR, xmiOut));
//
//        } catch (ResourceInitializationException e) {
//            e.printStackTrace();
//            throw new EnsemblesException();
//        }
//        try {
//            SimplePipeline.runPipeline(reader,
//                    engines.toArray(new AnalysisEngine[engines.size()]));
//        } catch (IOException | UIMAException e) {
//            throw new EnsemblesException(e);
//        }


    }
}