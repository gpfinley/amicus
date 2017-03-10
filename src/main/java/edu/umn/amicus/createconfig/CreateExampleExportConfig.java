package edu.umn.amicus.createconfig;

import edu.umn.amicus.config.*;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by gpfinley on 2/15/17.
 */
@Deprecated
public class CreateExampleExportConfig {

    public static String GOLD_DATA = "/Users/gpfinley/hermes/home/gpfinley/annotations/manual-annotations";
    public static String BIOM_DATA = "/Users/gpfinley/hermes/home/gpfinley/annotations/biomedicus-1.6.0-october2016";
    public static String XMI_OUT   = "/Users/gpfinley/hermes/home/gpfinley/xmi-export-out-temp";

    public static void main(String[] args) throws IOException {

        String outYml = "example_export_pipeline_config.yml";

        AmicusPipelineConfiguration config = new AmicusPipelineConfiguration();

        config.pipelineName = "Example Amicus pipeline";

        config.allSystemsUsed = new SourceSystemConfig[] {
                new SourceSystemConfig().useSystemName("gold")
                        .useDataPath(GOLD_DATA)
                        .useReadFromView("SystemView")
                        .useSaveIntoView("GoldView"),
                new SourceSystemConfig().useSystemName("biomedicus")
                        .useDataPath(BIOM_DATA)
                        .useReadFromView("SystemView")
                        .useSaveIntoView("BiomedicusView"),
        };

        ExporterConfig goldExporter = new ExporterConfig();
        ExporterConfig biomedicusExporter = new ExporterConfig();
        SummarizerConfig goldCollector = new SummarizerConfig();
        SummarizerConfig biomedicusCollector = new SummarizerConfig();

        goldExporter.name = "gold exporter";
        goldExporter.alignerClass = "edu.umn.amicus.aligners.EachSoloAligner";
        goldExporter.microSummarizer = "edu.umn.amicus.export.EachSoloTsvMicroSummarizer";
        goldExporter.inputs = new AnnotationInputConfig[] {
                new AnnotationInputConfig()
                        .annotationType("edu.umn.biomedicus.type.TokenAnnotation")
                        .annotationField("acronymAbbrevExpansion")
                        .pullerClass("edu.umn.amicus.pullers.EquivalentMapperPuller")
                        .fromView("GoldView")
                };
        goldExporter.microSummaryOutDirectory = "data/out/goldExports";

        biomedicusExporter.name = "biomedicus exporter";
        biomedicusExporter.alignerClass = "edu.umn.amicus.aligners.EachSoloAligner";
        biomedicusExporter.microSummarizer = "edu.umn.amicus.export.EachSoloTsvMicroSummarizer";
        biomedicusExporter.inputs = new AnnotationInputConfig[] {
                new AnnotationInputConfig()
                        .annotationType("edu.umn.biomedicus.uima.type1_6.Acronym")
                        .annotationField("text")
                        .pullerClass("edu.umn.amicus.pullers.EquivalentMapperPuller")
                        .fromView("BiomedicusView")
        };
        biomedicusExporter.microSummaryOutDirectory = "data/out/biomedicusExports";

        goldCollector.name = "gold collector";
        goldCollector.input = new AnnotationInputConfig()
                .annotationType("edu.umn.biomedicus.type.TokenAnnotation")
                .annotationField("acronymAbbrevExpansion")
                .pullerClass("edu.umn.amicus.pullers.EquivalentMapperPuller")
                .fromView("GoldView");
        goldCollector.summaryWriter = "edu.umn.amicus.summary.CounterMacroSummarizer";

        biomedicusCollector.name = "biomedicus collector";
        biomedicusCollector.input = new AnnotationInputConfig()
                .annotationType("edu.umn.biomedicus.uima.type1_6.Acronym")
                .annotationField("text")
                .pullerClass("edu.umn.amicus.pullers.EquivalentMapperPuller")
                .fromView("BiomedicusView");
        biomedicusCollector.summaryWriter = "edu.umn.amicus.summary.CounterMacroSummarizer";

        PipelineComponentConfig[] components = {goldExporter, biomedicusExporter, goldCollector, biomedicusCollector};

        config.pipelineComponents = components;

        config.xmiOutPath = XMI_OUT;

        config.verify();
        Yaml yaml = new Yaml();
        yaml.dump(config, new FileWriter(outYml));
        AmicusPipelineConfiguration pipeTest = (AmicusPipelineConfiguration) yaml.load(new FileInputStream(outYml));


    }
}
