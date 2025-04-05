package com.example.camerabooking.websocket;

import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StreamSession {
    private static final Logger logger = LoggerFactory.getLogger(StreamSession.class);

    private final String streamId;
    private final String bookingId; // Keep this for reference, though not used directly
    private final MediaPipeline pipeline;
    private final WebRtcEndpoint broadcasterEndpoint;
    private WebSocketSession broadcasterSession; // Made non-final to allow reassignment
    private final Map<String, WebRtcEndpoint> viewers = new ConcurrentHashMap<>();

    public StreamSession(String streamId, String bookingId, MediaPipeline pipeline, WebRtcEndpoint broadcasterEndpoint, WebSocketSession broadcasterSession) {
        this.streamId = streamId;
        this.bookingId = bookingId;
        this.pipeline = pipeline;
        this.broadcasterEndpoint = broadcasterEndpoint;
        this.broadcasterSession = broadcasterSession;

        // Add error listener for broadcaster endpoint
        broadcasterEndpoint.addErrorListener(event -> {
            logger.error("Error on broadcaster endpoint for stream {}: {}", streamId, event.getDescription());
            sendErrorToBroadcaster("Broadcaster endpoint error: " + event.getDescription());
        });

        // Handle ICE candidates for broadcaster
        broadcasterEndpoint.addIceCandidateFoundListener(event -> {
            com.google.gson.JsonObject message = new com.google.gson.JsonObject();
            message.addProperty("type", "iceCandidate");
            com.google.gson.JsonObject candidateJson = new com.google.gson.JsonObject();
            candidateJson.addProperty("candidate", event.getCandidate().getCandidate());
            candidateJson.addProperty("sdpMid", event.getCandidate().getSdpMid());
            candidateJson.addProperty("sdpMLineIndex", event.getCandidate().getSdpMLineIndex());
            message.add("candidate", candidateJson);
            message.addProperty("streamId", streamId);
            try {
                synchronized (broadcasterSession) { // Ensure thread safety
                    if (broadcasterSession.isOpen()) {
                        broadcasterSession.sendMessage(new TextMessage(message.toString()));
                        logger.debug("Sent ICE candidate to broadcaster for stream: {}", streamId);
                    } else {
                        logger.warn("Broadcaster session closed, cannot send ICE candidate for stream: {}", streamId);
                    }
                }
            } catch (IOException e) {
                logger.error("Failed to send ICE candidate to broadcaster for stream {}: {}", streamId, e.getMessage());
            }
        });
    }

    public String getStreamId() {
        return streamId;
    }

    public MediaPipeline getPipeline() {
        return pipeline;
    }

    public WebRtcEndpoint getBroadcasterEndpoint() {
        return broadcasterEndpoint;
    }

    public WebSocketSession getBroadcasterSession() {
        return broadcasterSession;
    }

    // Allow reassignment of broadcaster session for resumption
    public void setBroadcasterSession(WebSocketSession newSession) {
        synchronized (this) { // Synchronize to avoid race conditions
            this.broadcasterSession = newSession;
            logger.info("Broadcaster session updated for stream: {}", streamId);
        }
    }

    public void addViewer(String sessionId, WebRtcEndpoint endpoint) {
        viewers.put(sessionId, endpoint);

        // Add error listener for viewer endpoint
        endpoint.addErrorListener(event -> {
            logger.error("Error on viewer endpoint for stream {} (viewer {}): {}", streamId, sessionId, event.getDescription());
            sendErrorToBroadcaster("Viewer endpoint error (viewer " + sessionId + "): " + event.getDescription());
        });

        // Handle ICE candidates for viewer
        endpoint.addIceCandidateFoundListener(event -> {
            com.google.gson.JsonObject message = new com.google.gson.JsonObject();
            message.addProperty("type", "iceCandidate");
            com.google.gson.JsonObject candidateJson = new com.google.gson.JsonObject();
            candidateJson.addProperty("candidate", event.getCandidate().getCandidate());
            candidateJson.addProperty("sdpMid", event.getCandidate().getSdpMid());
            candidateJson.addProperty("sdpMLineIndex", event.getCandidate().getSdpMLineIndex());
            message.add("candidate", candidateJson);
            message.addProperty("streamId", streamId);
            try {
                synchronized (broadcasterSession) { // Ensure thread safety
                    if (broadcasterSession.isOpen()) {
                        broadcasterSession.sendMessage(new TextMessage(message.toString()));
                        logger.debug("Sent ICE candidate for viewer {} in stream: {}", sessionId, streamId);
                    } else {
                        logger.warn("Broadcaster session closed, cannot send ICE candidate for viewer {} in stream: {}", sessionId, streamId);
                    }
                }
            } catch (IOException e) {
                logger.error("Failed to send ICE candidate for viewer {} in stream {}: {}", sessionId, streamId, e.getMessage());
            }
        });
        logger.info("Added viewer {} to stream: {}", sessionId, streamId);
    }

    public WebRtcEndpoint getViewerEndpoint(String sessionId) {
        return viewers.get(sessionId);
    }

    public void removeViewer(String sessionId) {
        WebRtcEndpoint endpoint = viewers.remove(sessionId);
        if (endpoint != null) {
            endpoint.release();
            logger.info("Removed and released viewer {} from stream: {}", sessionId, streamId);
        }
    }

    public void release() {
        for (Map.Entry<String, WebRtcEndpoint> entry : viewers.entrySet()) {
            entry.getValue().release();
            logger.info("Released viewer {} endpoint for stream: {}", entry.getKey(), streamId);
        }
        viewers.clear();
        broadcasterEndpoint.release();
        pipeline.release();
        logger.info("Released all resources for stream: {}", streamId);
    }

    private void sendErrorToBroadcaster(String message) {
        try {
            synchronized (broadcasterSession) {
                if (broadcasterSession.isOpen()) {
                    com.google.gson.JsonObject errorMsg = new com.google.gson.JsonObject();
                    errorMsg.addProperty("type", "error");
                    errorMsg.addProperty("message", message);
                    broadcasterSession.sendMessage(new TextMessage(errorMsg.toString()));
                    logger.error("Sent error to broadcaster for stream {}: {}", streamId, message);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to send error to broadcaster for stream {}: {}", streamId, e.getMessage());
        }
    }
}