# Camel IO Component

## Overview

The Camel Video-IO Component is designed to facilitate the integration of various video sources. This component offers a
consumer that allows you to capture video streams from different sources, such as files or webcams.

## Usage

### Maven Dependency

To use the Camel IO Component in your Apache Camel project, add the following Maven dependency:

```xml
<dependency>
    <groupId>es.omarall</groupId>
    <artifactId>camel-video-io</artifactId>
    <version>0.0.2-SNAPSHOT</version> <!-- Replace with the actual version -->
</dependency>
```

## URI FORMAT

```
video-io:options
```

### Options

The Camel IO Component comes with several configurable parameters to adapt to your specific use case:

| Name             | Description                                                                                                                          | Default  | Type                                                                |
|------------------|--------------------------------------------------------------------------------------------------------------------------------------|----------|---------------------------------------------------------------------|
| channel-name     | A unique name to identify the video source channel.                                                                                  |          | String                                                              |
| capture-address  | Specifies the source of video data. It can be a file path or the address of a webcam.                                                |          | String                                                              |
| analysis-quality | Controls the quality of video analysis by specifying the number of frames to extract per second.  If not provided, BALANCED is used. | BALANCED | Enum:  FAST(1), BALANCED(2), DEEP(4), SUPER_DEEP(8), ULTRA_DEEP(16) |

## MESSAGE HEADERS

The Video-IO component supports 5 message header(s), which is/are listed below:

| Name                              | Description                                                                           | 
|-----------------------------------|---------------------------------------------------------------------------------------|
| CamelVideoIOChannelName           | The unique name to identify the video source channel.                                 |
| CamelVideoIOChannelCaptureAddress | Specifies the source of video data. It can be a file path or the address of a webcam. |
| CamelVideoIOFrameTimestamp        | The video timestamp of a consumed frame                                               |
| CamelVideoIOFrameIdx              | The frame idx in the secuence.                                                        |
| CamelVideoIOOriginalFrameWidth    | The video frame width.                                                                |
| CamelVideoIOOriginalFrameHeight   | The video frame height.                                                               |
| CamelVideoIOFrameRate             | The original video frame rate.                                                        |
| CamelVideoIOFrameFormat           | The video frame format.                                                               |

## EXAMPLES

```java
from("video-io://garage-cam?captureAddress=~/Downloads/thieves.webm&analysisQuality=ULTRA_DEEP")
        .to("mock:result");
```