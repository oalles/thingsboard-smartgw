package es.omarall.camel.components.videoio;

import lombok.Getter;

@Getter
public enum AnalysisQuality {

    FAST(1), BALANCED(2), DEEP(4), SUPER_DEEP(8), ULTRA_DEEP(16);

    private final int value;

    AnalysisQuality(int value) {
        this.value = value;
    }

    public int getExpectedFramePerSecond() {
        return this.value;
    }
}
