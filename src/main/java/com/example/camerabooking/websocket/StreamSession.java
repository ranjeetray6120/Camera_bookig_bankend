package com.example.camerabooking.websocket;


import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StreamSession {
    private final String streamId;
    private final MediaPipeline pipeline;
    private final WebRtcEndpoint broadcasterEndpoint;
    private final WebSocketSession broadcasterSession;
    private final Map<String, WebRtcEndpoint> viewers = new ConcurrentHashMap<>();

    public StreamSession(String streamId, String bookingId, MediaPipeline pipeline, WebRtcEndpoint broadcasterEndpoint, WebSocketSession broadcasterSession) {
        this.streamId = streamId;
        this.pipeline = pipeline;
        this.broadcasterEndpoint = broadcasterEndpoint;
        this.broadcasterSession = broadcasterSession;

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
                broadcasterSession.sendMessage(new org.springframework.web.socket.TextMessage(message.toString()));
            } catch (java.io.IOException e) {
                e.printStackTrace();
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

    public void addViewer(String sessionId, WebRtcEndpoint endpoint) {
        viewers.put(sessionId, endpoint);
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
                broadcasterSession.sendMessage(new org.springframework.web.socket.TextMessage(message.toString()));
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        });
    }

    public WebRtcEndpoint getViewerEndpoint(String sessionId) {
        return viewers.get(sessionId);
    }

    public void removeViewer(String sessionId) {
        WebRtcEndpoint endpoint = viewers.remove(sessionId);
        if (endpoint != null) {
            endpoint.release();
        }
    }

    public void release() {
        for (WebRtcEndpoint viewer : viewers.values()) {
            viewer.release();
        }
        broadcasterEndpoint.release();
        pipeline.release();
    }
}