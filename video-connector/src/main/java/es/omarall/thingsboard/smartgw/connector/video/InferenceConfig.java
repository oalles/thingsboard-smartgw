package es.omarall.thingsboard.smartgw.connector.video;

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.function.Supplier;

@Configuration
@Slf4j
public class InferenceConfig {

    @Bean
    Criteria<Image, DetectedObjects> yolov5Criteria() {
        String backbone;
        if ("TensorFlow".equals(Engine.getDefaultEngineName())) {
            backbone = "mobilenet_v2";
        } else {
            backbone = "resnet50";
        }
        // Just pick it from the catalogue
        return Criteria.builder()
                .optApplication(Application.CV.OBJECT_DETECTION)
                .setTypes(Image.class, DetectedObjects.class)
                .optFilter("backbone", backbone)
                .optEngine(Engine.getDefaultEngineName())
                .optProgress(new ProgressBar())
                .build();
    }

    @Bean
    public ZooModel<Image, DetectedObjects> zooModel(Criteria<Image, DetectedObjects> criteria) throws MalformedModelException, ModelNotFoundException, IOException {
        try {
            var zooModel = criteria.loadModel();
            log.info("Successfully loaded model {}", zooModel.getName());
            return zooModel;
        } catch (ModelNotFoundException ex) {
            Yaml yaml = createYamlDumper();
            log.error("Requested model was not found");
            log.error("List of available models {}", yaml.dump(ModelZoo.listModels()));
            throw ex;
        }
    }

    @Bean
    public Supplier<Predictor<Image, DetectedObjects>> predictorProvider(ZooModel<Image, DetectedObjects> model) {
        return model::newPredictor;
    }

    private Yaml createYamlDumper() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        return new Yaml(options);
    }
}
