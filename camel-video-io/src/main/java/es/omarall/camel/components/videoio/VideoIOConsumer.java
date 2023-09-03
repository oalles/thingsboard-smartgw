package es.omarall.camel.components.videoio;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.support.DefaultConsumer;
import org.bytedeco.javacv.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;

public class VideoIOConsumer extends DefaultConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(VideoIOConsumer.class);

    private FrameGrabber frameGrabber;
    private FFmpegFrameFilter frameFilter;

    private int frameCounter;

    public VideoIOConsumer(VideoIOEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
        FFmpegLogCallback.set();
    }

    @Override
    public VideoIOEndpoint getEndpoint() {
        return (VideoIOEndpoint) super.getEndpoint();
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        LOG.debug("Starting VideoIO Consumer");

        final VideoIOConfiguration configuration = this.getEndpoint().getConfiguration();
        this.frameGrabber = new FFmpegFrameGrabber(configuration.getCaptureAddress());

        try {

            int frameCounter = 0;
            frameGrabber.start();

            this.frameFilter = this.buildFrameFilter(frameGrabber);
            this.frameFilter.start();

            // Frame Stream
            Frame frame;
            while ((frame = frameGrabber.grab()) != null) {

                if (Thread.currentThread().isInterrupted()) {
                    this.releaseResources();
                    break;
                }
                if (frame.image != null) {
                    frameFilter.push(frame);
                }

                if ((frame = frameFilter.pull()) != null) {

                    LOG.debug("Frame Counter: {}", ++frameCounter);

                    Long channelTs = frame.timestamp;
                    LOG.info("Frame Counter: {} - ChannelTs: {}", frameCounter, channelTs);

                    BufferedImage image = Java2DFrameUtils.toBufferedImage(frame);

                    if (image != null) {
                        frame.close();

                        // Build Exchange
                        final Exchange exchange = createExchange(false);
                        final Message message = getMessage(exchange, channelTs, frameCounter);

                        message.setBody(image);

                        try {
                            getProcessor().process(exchange);
                        } catch (Exception e) {
                            getExceptionHandler().handleException("Error processing exchange", exchange, e);
                        } finally {
                            releaseExchange(exchange, false);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            if (this.getExceptionHandler() != null) {
                this.getExceptionHandler().handleException("Error during processing", e);
            } else {
                throw new RuntimeException(e);
            }
        } finally {
            this.releaseResources();
        }

    }

    private Message getMessage(Exchange exchange, Long channelTs, int frameCounter) {
        final Message message = exchange.getIn();
        message.setHeader(VideoIOConstants.VIDEO_IO_CHANNEL_NAME, getEndpoint().getConfiguration().getChannelName());
        message.setHeader(VideoIOConstants.VIDEO_IO_CHANNEL_CAPTURE_ADDRESS, getEndpoint().getConfiguration().getCaptureAddress());
        message.setHeader(VideoIOConstants.VIDEO_IO_FRAME_TIMESTAMP, channelTs);
        message.setHeader(VideoIOConstants.VIDEO_IO_FRAME_IDX, frameCounter);
        message.setHeader(VideoIOConstants.VIDEO_IO_ORIGINAL_FRAME_WIDTH, frameGrabber.getImageWidth());
        message.setHeader(VideoIOConstants.VIDEO_IO_FRAME_ORIGINAL_HEIGHT, frameGrabber.getImageHeight());
        message.setHeader(VideoIOConstants.VIDEO_IO_FRAME_RATE, frameGrabber.getFrameRate());
        message.setHeader(VideoIOConstants.VIDEO_IO_FRAME_FORMAT, frameGrabber.getPixelFormat());
        return message;
    }

    @Override
    protected void doStop() throws Exception {
        final VideoIOConfiguration configuration = this.getEndpoint().getConfiguration();
        LOG.debug("Stopping Video IO Consumer");

        this.releaseResources();

        super.doStop();
    }

    /**
     * Expeccted number of frames per second.
     *
     * @param frameGrabber
     * @return
     */
    private FFmpegFrameFilter buildFrameFilter(FrameGrabber frameGrabber) {
        AnalysisQuality analysisQuality = AnalysisQuality.valueOf(getEndpoint().getConfiguration().getAnalysisQuality());
        FFmpegFrameFilter frameFilter = new FFmpegFrameFilter(String.format("fps=fps=%d", analysisQuality.getExpectedFramePerSecond()), // TODO: Externalizes
                "anull", frameGrabber.getImageWidth(), frameGrabber.getImageHeight(), frameGrabber.getAudioChannels());
        frameFilter.setAspectRatio(frameGrabber.getAspectRatio());
        frameFilter.setPixelFormat(frameGrabber.getPixelFormat());
        // 2.set fps
//        frameFilter.setFrameRate(frameGrabber.getFrameRate());
        return frameFilter;
    }

    private void releaseResources() {
        try {
            if (frameGrabber != null) {
                frameGrabber.stop();
                frameGrabber.release();
            }
            if (frameFilter != null) {
                frameFilter.stop();
                frameFilter.release();
            }
        } catch (Throwable e) {
            LOG.error("Error while releasing resources", e);
        }
    }
}