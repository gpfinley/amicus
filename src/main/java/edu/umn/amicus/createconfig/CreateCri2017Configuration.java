//package edu.umn.amicus.createconfig;
//
//import edu.umn.amicus.SingleFieldAnnotation;
//import edu.umn.amicus.config.*;
//import edu.umn.amicus.distillers.PriorityDistiller;
//import edu.umn.amicus.eval.*;
//import edu.umn.amicus.pullers.CtakesCuiPuller;
//import edu.umn.amicus.pullers.MultipleFieldsToConcatStringPuller;
//import edu.umn.biomedicus.type.TokenAnnotation;
//import edu.umn.biomedicus.uima.type1_6.Acronym;
//import edu.uth.clamp.nlp.typesystem.ClampNameEntityUIMA;
//import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
//import org.yaml.snakeyaml.Yaml;
//
//import java.io.FileInputStream;
//import java.io.FileWriter;
//import java.io.IOException;
//
///**
// * Created by gpfinley on 2/27/17.
// */
//@Deprecated
//public class CreateCri2017Configuration {
//
//
//    public static void main(String[] args) throws IOException {
//
//        String outYml = "cri_2017_acronyms_pipeline.yml";
//
//        AmicusPipelineConfiguration config = new AmicusPipelineConfiguration();
//
//        config._pipelineName = "Example Amicus pipeline";
//        config.xmiOutPath = "acronymdata/xmi_out";
//        config.allSystemsUsed = new SourceSystemConfig[] {
//                new SourceSystemConfig().useSystemName("gold")
//                        .useDataPath("acronymdata/gold")
//                        .useReadFromView("SystemView")
//                        .useSaveIntoView("GoldView"),
//                new SourceSystemConfig().useSystemName("biomedicus")
//                        .useDataPath("acronymdata/biomedicus")
//                        .useReadFromView("SystemView")
//                        .useSaveIntoView("BiomedicusView"),
//                new SourceSystemConfig().useSystemName("ctakes")
//                        .useDataPath("acronymdata/ctakes")
//                        .useReadFromView("_InitialView")
//                        .useSaveIntoView("CtakesView"),
//                new SourceSystemConfig().useSystemName("clamp")
//                        .useDataPath("acronymdata/clamp")
//                        .useReadFromView("_InitialView")
//                        .useSaveIntoView("ClampView")
//        };
//
//        // pipeline components and sequence
//
//        TranslatorConfig clampCuiToEquiv = new TranslatorConfig();
//        TranslatorConfig ctakesCuiToEquiv = new TranslatorConfig();
//        TranslatorConfig biomedicusStringToEquiv = new TranslatorConfig();
//        TranslatorConfig goldStringToEquiv = new TranslatorConfig();
//        MergerConfig merger = new MergerConfig();
//        MergerConfig evalMerger = new MergerConfig();
//        SummarizerConfig evalSummarizer = new SummarizerConfig();
//
//        config.pipelineComponents = new PipelineComponentConfig[] {
//                clampCuiToEquiv,
//                ctakesCuiToEquiv,
//                biomedicusStringToEquiv,
//                goldStringToEquiv,
//                merger,
//                evalMerger,
//                evalSummarizer
//        };
//
//        // module details
//
//        clampCuiToEquiv._translatorName = "CLAMP CUI extractor";
//        clampCuiToEquiv.input = new AnnotationInputConfig()
//                .fromView("ClampView")
//                .annotationType(ClampNameEntityUIMA.class.getName())
//                .annotationField("cui");
//        clampCuiToEquiv.outputs = new AnnotationOutputConfig[] {
//                new AnnotationOutputConfig()
//                        .annotationType(SingleFieldAnnotation.class.getName())
//                        .annotationField("field")
//                        .writeView("ClampMappedView")
//        };
//        clampCuiToEquiv.mapperConfigPaths = new String[] {
//                "mapperConfigurations/CuiMapper.yml",
//                "mapperConfigurations/equivalentAnswerMapperConfig.yml"
//        };
//
//        ctakesCuiToEquiv._translatorName = "cTAKES CUI extractor";
//        ctakesCuiToEquiv.input = new AnnotationInputConfig()
//                .fromView("CtakesView")
//                .annotationType(IdentifiedAnnotation.class.getName())
//                .annotationField("ontologyConceptArr")
//                .pullerClass(CtakesCuiPuller.class.getName());
//        ctakesCuiToEquiv.outputs = new AnnotationOutputConfig[] {
//                new AnnotationOutputConfig()
//                        .annotationType(SingleFieldAnnotation.class.getName())
//                        .annotationField("field")
//                        .writeView("CtakesMappedView")
//        };
//        ctakesCuiToEquiv.mapperConfigPaths = new String[] {
//                "mapperConfigurations/CuiMapper.yml",
//                "mapperConfigurations/equivalentAnswerMapperConfig.yml"
//        };
//
//        biomedicusStringToEquiv._translatorName = "BioMedICUS answer mapper";
//        biomedicusStringToEquiv.input = new AnnotationInputConfig()
//                .fromView("BiomedicusView")
//                .annotationType(Acronym.class.getName())
//                .annotationField("text");
//        biomedicusStringToEquiv.outputs = new AnnotationOutputConfig[] {
//                new AnnotationOutputConfig()
//                        .annotationType(SingleFieldAnnotation.class.getName())
//                        .annotationField("field")
//                        .writeView("BiomedicusMappedView")
//        };
//        biomedicusStringToEquiv.mapperConfigPaths = new String[]{
//                "mapperConfigurations/equivalentAnswerMapperConfig.yml"
//        };
//
//        goldStringToEquiv._translatorName = "Gold answer mapper";
//        goldStringToEquiv.filterPattern = ".*true$";
//        goldStringToEquiv.input = new AnnotationInputConfig()
//                .annotationType(TokenAnnotation.class.getName())
//                .annotationField("acronymAbbrevExpansion;isAcronymAbbrev")
//                .pullerClass(MultipleFieldsToConcatStringPuller.class.getName())
//                .fromView("GoldView");
//        goldStringToEquiv.outputs = new AnnotationOutputConfig[] {
//                new AnnotationOutputConfig()
//                        .annotationType(SingleFieldAnnotation.class.getName())
//                        .annotationField("field;")
//                        .writeView("GoldMappedView"),
//        };
//        goldStringToEquiv.mapperConfigPaths = new String[]{
//                "mapperConfigurations/equivalentAnswerMapperConfig.yml"
//        };
//
//        merger._mergerName = "Acronym answer merger";
////        merger.alignerClass = AllOverlapsAligner.class.getName();
//        merger.inputs = new AnnotationInputConfig[] {
//                new AnnotationInputConfig()
//                        .fromView("BiomedicusMappedView")
//                        .annotationType(SingleFieldAnnotation.class.getName())
//                        .annotationField("field"),
//                new AnnotationInputConfig()
//                        .fromView("ClampMappedView")
//                        .annotationType(SingleFieldAnnotation.class.getName())
//                        .annotationField("field"),
//                new AnnotationInputConfig()
//                        .fromView("CtakesMappedView")
//                        .annotationType(SingleFieldAnnotation.class.getName())
//                        .annotationField("field")
//        };
//        merger.outputs = new AnnotationOutputConfig[] {
//                new AnnotationOutputConfig()
//                        .writeView("MergedView")
//                        .annotationType(SingleFieldAnnotation.class.getName())
//                        .annotationField("field")
//                        .distillerClass(PriorityDistiller.class.getName())
//        };
//
//
//        evalMerger._mergerName = "Eval merger";
//        // todo: implement partial overlap aligner for merger
//        evalMerger.alignerClass = EvalPerfectOverlapAligner.class.getName();
//        evalMerger.inputs = new AnnotationInputConfig[] {
//                new AnnotationInputConfig()
//                        .fromView("GoldMappedView")
//                        .annotationType(SingleFieldAnnotation.class.getName())
//                        .annotationField("field"),
//                new AnnotationInputConfig()
//                        .fromView("BiomedicusMappedView")
//                        .annotationType(SingleFieldAnnotation.class.getName())
//                        .annotationField("field"),
//                new AnnotationInputConfig()
//                        .fromView("ClampMappedView")
//                        .annotationType(SingleFieldAnnotation.class.getName())
//                        .annotationField("field"),
//                new AnnotationInputConfig()
//                        .fromView("CtakesMappedView")
//                        .annotationType(SingleFieldAnnotation.class.getName())
//                        .annotationField("field"),
//                new AnnotationInputConfig()
//                        .fromView("MergedView")
//                        .annotationType(SingleFieldAnnotation.class.getName())
//                        .annotationField("field")
//        };
//        evalMerger.outputs = new AnnotationOutputConfig[] {
//                new AnnotationOutputConfig()
//                        .writeView("EvalIntermediateView")
//                        .annotationType(edu.umn.amicus.EvalAnnotation.class.getName())
//                        .distillerClass(EvalMatchDistiller.class.getName())
//                        .pusherClass(EvalMatchPusher.class.getName())
//        };
//
//        evalSummarizer.name = "Evaluation summarizer";
//        evalSummarizer.input = new AnnotationInputConfig()
//                .fromView("EvalIntermediateView")
//                .annotationType(edu.umn.amicus.EvalAnnotation.class.getName())
//                .pullerClass(EvalMatchPuller.class.getName());
//        evalSummarizer.summaryWriter = EvalPrfSummaryWriter.class.getName();
//        evalSummarizer.outPath = "acronym_evaluation_summary.txt";
//
//
//        config.verify();
//        Yaml yaml = new Yaml();
//        yaml.dump(config, new FileWriter(outYml));
//        AmicusPipelineConfiguration pipeTest = (AmicusPipelineConfiguration) yaml.load(new FileInputStream(outYml));
//
//    }
//
//}
