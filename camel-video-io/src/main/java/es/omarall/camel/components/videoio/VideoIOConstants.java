package es.omarall.camel.components.videoio;

import org.apache.camel.spi.Metadata;

public interface VideoIOConstants {

    @Metadata(description = "The video channel name.", javaType = "String")
    String VIDEO_IO_CHANNEL_NAME = "CamelVideoIOChannelName";
    @Metadata(description = "The video channel capture address.", javaType = "String")
    String VIDEO_IO_CHANNEL_CAPTURE_ADDRESS = "CamelVideoIOChannelCaptureAddress";
    @Metadata(description = "The video timestamp of a consumed frame.", javaType = "long")
    String VIDEO_IO_FRAME_TIMESTAMP = "CamelVideoIOFrameTimestamp";
    @Metadata(description = "The frame idx in the secuence.", javaType = "long")
    String VIDEO_IO_FRAME_IDX = "CamelVideoIOFrameIdx";
    @Metadata(description = "The video frame width.", javaType = "long")
    String VIDEO_IO_ORIGINAL_FRAME_WIDTH = "CamelVideoIOOriginalFrameWidth";
    @Metadata(description = "The video frame height.", javaType = "long")
    String VIDEO_IO_FRAME_ORIGINAL_HEIGHT = "CamelVideoIOOriginalFrameHeight";
    @Metadata(description = "The original video frame rate.", javaType = "long")
    String VIDEO_IO_FRAME_RATE = "CamelVideoIOFrameRate";
    @Metadata(description = "The video frame format.", javaType = "String")
    String VIDEO_IO_FRAME_FORMAT = "CamelVideoIOFrameFormat";
}
