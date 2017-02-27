package edu.umn.amicus.createconfig;

import edu.umn.amicus.config.*;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by gpfinley on 2/15/17.
 */
public class CreateExampleEvalConfig {

    public static String GOLD_DATA = "/Users/gpfinley/hermes/home/gpfinley/annotations/manual-annotations";
    public static String BIOM_DATA = "/Users/gpfinley/hermes/home/gpfinley/annotations/biomedicus-1.6.0-october2016";
    public static String XMI_OUT   = "/Users/gpfinley/hermes/home/gpfinley/xmi-export-out-temp";

    public static void main(String[] args) throws IOException {

        String outYml = "example_export_pipeline_config.yml";

        AmicusPipelineConfiguration config = new AmicusPipelineConfiguration();

        config._pipelineName = "Example Amicus pipeline";

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

        MergerConfig evalMerger = new MergerConfig();
        SummarizerConfig evalCollector = new SummarizerConfig();

        AnnotationInputConfig goldInput = new AnnotationInputConfig()
                .annotationType("edu.umn.biomedicus.type.TokenAnnotation")
                .annotationField("acronymAbbrevExpansion")
                .pullerClass("edu.umn.amicus.pullers.EquivalentMapperPuller")
                .fromView("GoldView");

        AnnotationInputConfig biomedicusInput = new AnnotationInputConfig()
                .annotationType("edu.umn.biomedicus.uima.type1_6.Acronym")
                .annotationField("text")
                .pullerClass("edu.umn.amicus.pullers.EquivalentMapperPuller")
                .fromView("BiomedicusView");

        goldExporter._exporterName = "gold exporter";
        goldExporter.alignerClass = "edu.umn.amicus.aligners.EachSoloAligner";
        goldExporter.exporterClass = "edu.umn.amicus.export.EachSoloTsvExportWriter";
        goldExporter.inputs = new AnnotationInputConfig[] {goldInput};
        goldExporter.outputDirectory = "data/out/goldExports";

        biomedicusExporter._exporterName = "biomedicus exporter";
        biomedicusExporter.alignerClass = "edu.umn.amicus.aligners.EachSoloAligner";
        biomedicusExporter.exporterClass = "edu.umn.amicus.export.EachSoloTsvExportWriter";
        biomedicusExporter.inputs = new AnnotationInputConfig[] {biomedicusInput};
        biomedicusExporter.outputDirectory = "data/out/biomedicusExports";

        goldCollector.name = "gold collector";
        goldCollector.input = goldInput;
        goldCollector.summarizerClass = "edu.umn.amicus.summary.CounterSummaryWriter";

        biomedicusCollector.name = "biomedicus collector";
        biomedicusCollector.input = biomedicusInput;
        biomedicusCollector.summarizerClass = "edu.umn.amicus.summary.CounterSummaryWriter";


        evalMerger._mergerName = "acronym eval merger";
        evalMerger.alignerClass = "edu.umn.amicus.eval.EvalPerfectOverlapAligner";
        evalMerger.inputs = new AnnotationInputConfig[] {goldInput, biomedicusInput};
        evalMerger.outputs = new AnnotationOutputConfig[] {
                new AnnotationOutputConfig()
                        .annotationType("edu.umn.amicus.SingleFieldAnnotation")
                        .annotationField("field")
                        .distillerClass("edu.umn.amicus.eval.EvalMatchDistiller")
                        .pusherClass("edu.umn.amicus.eval.EvalMatchPusher")
                        .writeView("EvalView")
            };

        evalCollector.name = "eval collector";
        evalCollector.summarizerClass = "edu.umn.amicus.eval.EvalPrfSummaryWriter";
        evalCollector.input = new AnnotationInputConfig()
                .annotationType("edu.umn.amicus.SingleFieldAnnotation")
                .annotationField("field")
                .fromView("EvalView")
                .pullerClass("edu.umn.amicus.eval.EvalMatchPuller");

        config.pipelineComponents = new PipelineComponentConfig[] {
                goldExporter,
                biomedicusExporter,
                goldCollector,
                biomedicusCollector,
                evalMerger,
                evalCollector
        };

        config.xmiOutPath = XMI_OUT;

        config.verify();
        Yaml yaml = new Yaml();
        yaml.dump(config, new FileWriter(outYml));
        AmicusPipelineConfiguration pipeTest = (AmicusPipelineConfiguration) yaml.load(new FileInputStream(outYml));


    }
}
