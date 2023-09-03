package es.omarall.camel.components.videoio;

import lombok.Getter;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriParams;
import org.apache.camel.spi.UriPath;

@Getter
@UriParams
public class VideoIOConfiguration {

    /**
     * -- GETTER --
     *  this is what ever it is
     *
     * @return
     */
    @UriPath
    @Metadata(required = true)
    private String channelName;
    /**
     * -- GETTER --
     *  The expected Frame Rate
     *
     * @return
     */
    @UriParam(label = "common")
    private Double frameRate;

    /**
     * -- GETTER --
     *  The expected frame width
     *
     * @return
     */
    @UriParam(label = "common")
    private Integer frameWidth;
    /**
     * -- GETTER --
     *  The expected frame height
     *
     * @return
     */
    @UriParam(label = "common")
    private Integer frameHeight;
    /**
     * -- GETTER --
     *  The capture address to use: filename, rtsp, mpeg4, v4l2 etc
     */
    @UriParam(label = "consumer")
    private String captureAddress;

    /**
     * -- GETTER --
     *  The expected analysis quality. The higher the quality, the more frames are analyzed.
     *
     * @return
     */
    @UriParam(label = "consumer", enums = "FAST,BALANCED,DEEP,SUPER_DEEP,ULTRA_DEEP", defaultValue = "BALANCED", description = "The analysis quality determines the number of frames to analyze per second. The higher the quality, the more frames are analyzed.")
    private String analysisQuality = AnalysisQuality.BALANCED.name();

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public void setFrameRate(Double frameRate) {
        this.frameRate = frameRate;
    }

    public void setCaptureAddress(String captureAddress) {
        this.captureAddress = captureAddress;
    }

    public void setFrameWidth(Integer frameWidth) {
        this.frameWidth = frameWidth;
    }

    public void setFrameHeight(Integer frameHeight) {
        this.frameHeight = frameHeight;
    }

    public void setAnalysisQuality(String analysisQuality) {
        this.analysisQuality = analysisQuality;
    }

}