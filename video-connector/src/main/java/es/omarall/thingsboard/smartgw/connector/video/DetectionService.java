package es.omarall.thingsboard.smartgw.connector.video;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.translate.TranslateException;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.function.Supplier;

@Service
public class DetectionService {
    private final Predictor<Image, DetectedObjects> predictor;

    public DetectionService(final Supplier<Predictor<Image, DetectedObjects>> predictorSupplier) {
        this.predictor = predictorSupplier.get();
    }

    public DetectedObjects detect(BufferedImage image) {
        Image input = ImageFactory.getInstance().fromImage(image);
        try {
            return predictor.predict(input);
        } catch (TranslateException e) {
            throw new RuntimeException("Cannot be translated", e);
        }
    }
}
