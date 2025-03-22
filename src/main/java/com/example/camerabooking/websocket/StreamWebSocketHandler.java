package com.example.camerabooking.websocket;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.kurento.client.IceCandidate;
import org.kurento.client.IceCandidateFoundEvent;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StreamWebSocketHandler extends TextWebSocketHandler {
    private final KurentoClient kurento = KurentoClient.create("ws://localhost:8888/kurento");
    private final Map<String, StreamSession> streams = new ConcurrentHashMap<>();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonObject json = JsonParser.parseString(message.getPayload()).getAsJsonObject();
        JsonElement typeElement = json.get("type");

        if (typeElement == null || !typeElement.isJsonPrimitive() || !typeElement.getAsJsonPrimitive().isString()) {
            sendError(session, "Missing or invalid 'type' field in message");
            return;
        }

        String type = typeElement.getAsString();

        switch (type) {
            case "startStream":
                startStream(session, json);
                break;
            case "stopStream":
                stopStream(session, json);
                break;
            case "viewStream":
                viewStream(session, json);
                break;
            case "leaveStream":
                leaveStream(session, json);
                break;
            case "sdpOffer":
                processSdpOffer(session, json);
                break;
            case "iceCandidate":
                addIceCandidate(session, json);
                break;
            default:
                sendError(session, "Invalid message type: " + type);
        }
    }

    private void startStream(WebSocketSession session, JsonObject json) throws IOException {
        JsonElement bookingIdElement = json.get("bookingId");
        JsonElement sdpElement = json.get("sdp");
        if (bookingIdElement == null || !bookingIdElement.isJsonPrimitive() || 
            sdpElement == null || !sdpElement.isJsonPrimitive()) {
            sendError(session, "Missing or invalid 'bookingId' or 'sdp' field");
            return;
        }
        String bookingId = bookingIdElement.getAsString();
        String sdpOffer = sdpElement.getAsString();

        String streamId = UUID.randomUUID().toString();
        MediaPipeline pipeline = kurento.createMediaPipeline();
        WebRtcEndpoint broadcasterEndpoint = new WebRtcEndpoint.Builder(pipeline).build();
        StreamSession streamSession = new StreamSession(streamId, bookingId, pipeline, broadcasterEndpoint, session);
        streams.put(streamId, streamSession);

        // Add error listener
        broadcasterEndpoint.addErrorListener(event -> {
            System.err.println("Error on broadcaster endpoint for stream " + streamId + ": " + event.getDescription());
        });

        // Process SDP offer and send answer
        System.out.println("Processing SDP offer for stream " + streamId + ": " + sdpOffer);
        String sdpAnswer = broadcasterEndpoint.processOffer(sdpOffer);
        System.out.println("Generated SDP answer for stream " + streamId + ": " + sdpAnswer);
        broadcasterEndpoint.gatherCandidates();

        // Handle ICE candidates
        broadcasterEndpoint.addIceCandidateFoundListener(event -> {
            IceCandidate candidate = event.getCandidate();
            System.out.println("Broadcaster ICE candidate found for stream " + streamId + ": " + candidate.toString());
            JsonObject response = new JsonObject();
            response.addProperty("type", "iceCandidate");
            response.add("candidate", new JsonParser().parse(candidate.toString()));
            try {
                session.sendMessage(new TextMessage(response.toString()));
                System.out.println("Sent ICE candidate from broadcaster for stream: " + streamId);
            } catch (IOException e) {
                System.err.println("Error sending ICE candidate for stream " + streamId + ": " + e.getMessage());
            }
        });

        // Send response
        JsonObject response = new JsonObject();
        response.addProperty("type", "streamStarted");
        response.addProperty("streamId", streamId);
        response.addProperty("sdp", sdpAnswer);
        session.sendMessage(new TextMessage(response.toString()));
        System.out.println("Stream started with ID: " + streamId);
    }

    private void stopStream(WebSocketSession session, JsonObject json) throws IOException {
        JsonElement streamIdElement = json.get("streamId");
        if (streamIdElement == null || !streamIdElement.isJsonPrimitive()) {
            sendError(session, "Missing or invalid 'streamId' field");
            return;
        }
        String streamId = streamIdElement.getAsString();

        StreamSession streamSession = streams.remove(streamId);
        if (streamSession != null) {
            streamSession.release();
            System.out.println("Stream stopped and resources released for ID: " + streamId);
        } else {
            sendError(session, "Stream not found for ID: " + streamId);
        }
    }

    private void viewStream(WebSocketSession session, JsonObject json) throws IOException {
        JsonElement streamIdElement = json.get("streamId");
        if (streamIdElement == null || !streamIdElement.isJsonPrimitive()) {
            sendError(session, "Missing or invalid 'streamId' field");
            return;
        }
        String streamId = streamIdElement.getAsString();

        StreamSession streamSession = streams.get(streamId);
        if (streamSession == null) {
            sendError(session, "Stream not found for ID: " + streamId);
            return;
        }
        WebRtcEndpoint viewerEndpoint = new WebRtcEndpoint.Builder(streamSession.getPipeline()).build();
        streamSession.addViewer(session.getId(), viewerEndpoint);
        streamSession.getBroadcasterEndpoint().connect(viewerEndpoint);
        System.out.println("Connected viewer to broadcaster for stream: " + streamId);

        // Add error listener
        viewerEndpoint.addErrorListener(event -> {
            System.err.println("Error on viewer endpoint for stream " + streamId + ": " + event.getDescription());
        });

        // Handle ICE candidates for viewer
        viewerEndpoint.addIceCandidateFoundListener(event -> {
            IceCandidate candidate = event.getCandidate();
            System.out.println("Viewer ICE candidate found for stream " + streamId + ": " + candidate.toString());
            JsonObject response = new JsonObject();
            response.addProperty("type", "iceCandidate");
            response.add("candidate", new JsonParser().parse(candidate.toString()));
            try {
                session.sendMessage(new TextMessage(response.toString()));
                System.out.println("Sent ICE candidate from viewer for stream: " + streamId);
            } catch (IOException e) {
                System.err.println("Error sending ICE candidate for stream " + streamId + ": " + e.getMessage());
            }
        });

        JsonObject response = new JsonObject();
        response.addProperty("type", "viewerConnected");
        response.addProperty("streamId", streamId);
        session.sendMessage(new TextMessage(response.toString()));
    }

    private void leaveStream(WebSocketSession session, JsonObject json) throws IOException {
        JsonElement streamIdElement = json.get("streamId");
        if (streamIdElement == null || !streamIdElement.isJsonPrimitive()) {
            sendError(session, "Missing or invalid 'streamId' field");
            return;
        }
        String streamId = streamIdElement.getAsString();

        StreamSession streamSession = streams.get(streamId);
        if (streamSession != null) {
            streamSession.removeViewer(session.getId());
            JsonObject response = new JsonObject();
            response.addProperty("type", "viewerDisconnected");
            response.addProperty("streamId", streamId);
            session.sendMessage(new TextMessage(response.toString()));
            System.out.println("Viewer left stream: " + streamId);
        }
    }

    private void processSdpOffer(WebSocketSession session, JsonObject json) throws IOException {
        JsonElement streamIdElement = json.get("streamId");
        JsonElement sdpElement = json.get("sdp");
        if (streamIdElement == null || !streamIdElement.isJsonPrimitive() || 
            sdpElement == null || !sdpElement.isJsonPrimitive()) {
            sendError(session, "Missing or invalid 'streamId' or 'sdp' field");
            return;
        }
        String streamId = streamIdElement.getAsString();
        String sdpOffer = sdpElement.getAsString();

        StreamSession streamSession = streams.get(streamId);
        if (streamSession == null) {
            sendError(session, "Stream not found for ID: " + streamId);
            return;
        }

        WebRtcEndpoint endpoint = session.equals(streamSession.getBroadcasterSession()) 
            ? streamSession.getBroadcasterEndpoint() 
            : streamSession.getViewerEndpoint(session.getId());

        if (endpoint != null) {
            System.out.println("Processing SDP offer for stream " + streamId + ": " + sdpOffer);
            String sdpAnswer = endpoint.processOffer(sdpOffer);
            System.out.println("Generated SDP answer for stream " + streamId + ": " + sdpAnswer);
            endpoint.gatherCandidates();
            System.out.println("Processed SDP offer, sending answer for stream: " + streamId);

            JsonObject response = new JsonObject();
            response.addProperty("type", "sdpAnswer");
            response.addProperty("sdp", sdpAnswer);
            session.sendMessage(new TextMessage(response.toString()));
        } else {
            sendError(session, "No endpoint found for session in stream: " + streamId);
        }
    }

    private void addIceCandidate(WebSocketSession session, JsonObject json) throws IOException {
        JsonElement streamIdElement = json.get("streamId");
        JsonElement candidateElement = json.get("candidate");
        if (streamIdElement == null || !streamIdElement.isJsonPrimitive() || 
            candidateElement == null || !candidateElement.isJsonObject()) {
            sendError(session, "Missing or invalid 'streamId' or 'candidate' field");
            return;
        }
        String streamId = streamIdElement.getAsString();
        JsonObject candidateJson = candidateElement.getAsJsonObject();

        JsonElement candidateStr = candidateJson.get("candidate");
        JsonElement sdpMid = candidateJson.get("sdpMid");
        JsonElement sdpMLineIndex = candidateJson.get("sdpMLineIndex");
        if (candidateStr == null || !candidateStr.isJsonPrimitive() || 
            sdpMid == null || !sdpMid.isJsonPrimitive() || 
            sdpMLineIndex == null || !sdpMLineIndex.isJsonPrimitive()) {
            sendError(session, "Invalid 'candidate' object structure");
            return;
        }

        IceCandidate candidate = new IceCandidate(
            candidateStr.getAsString(),
            sdpMid.getAsString(),
            sdpMLineIndex.getAsInt()
        );

        StreamSession streamSession = streams.get(streamId);
        if (streamSession == null) {
            System.out.println("No stream session found for ICE candidate: " + streamId);
            return;
        }

        WebRtcEndpoint endpoint = session.equals(streamSession.getBroadcasterSession()) 
            ? streamSession.getBroadcasterEndpoint() 
            : streamSession.getViewerEndpoint(session.getId());

        if (endpoint != null) {
            System.out.println("Adding ICE candidate to endpoint for stream " + streamId + ": " + candidate.toString());
            endpoint.addIceCandidate(candidate);
            System.out.println("Added ICE candidate to endpoint for stream: " + streamId);
        } else {
            System.out.println("No endpoint found for ICE candidate in stream: " + streamId);
        }
    }

    private void sendError(WebSocketSession session, String message) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("type", "error");
        response.addProperty("message", message);
        session.sendMessage(new TextMessage(response.toString()));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        for (StreamSession stream : streams.values()) {
            if (session.equals(stream.getBroadcasterSession())) {
                stream.release();
                streams.remove(stream.getStreamId());
                System.out.println("Released broadcaster session and removed stream: " + stream.getStreamId());
            } else {
                stream.removeViewer(session.getId());
                System.out.println("Removed viewer from stream: " + stream.getStreamId());
            }
        }
    }
}
