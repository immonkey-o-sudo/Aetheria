package io.hamlook.aetheria.features.diana.party;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class DianaPartyClient extends WebSocketClient {

    private final Map<String, CompletableFuture<String>> pendingRequests = new ConcurrentHashMap<>();

    public DianaPartyClient() {
        super(URI.create("wss://capeapi.qzz.io"));
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        DianaPartyConnector.isConnected = true;
    }

    @Override
    public void onMessage(String message) {
        String requestId = extractID(message);
        if(requestId != null) {
            if (pendingRequests.containsKey(requestId)) {
                pendingRequests.remove(requestId).complete(message);
            }
        }
        DianaPartyConnector.process(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        DianaPartyConnector.processClose(code,reason,remote);
        DianaPartyConnector.isConnected = false;
    }

    public CompletableFuture<String> sendAndRecieve(String message) {
        String id = UUID.randomUUID().toString();
        CompletableFuture<String> future = new CompletableFuture<>();
        pendingRequests.put(id, future);
        String formattedMessage = attachIdToJson(message, id);
        send(formattedMessage);

        return future;
    }

    private String attachIdToJson(String message, String id) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", id);
        jsonObject.addProperty("message", message);
        return DianaPartyConnector.GSON.toJson(jsonObject);
    }

    private String extractID(String data) {
        JsonObject obj = JsonParser.parseString(data).getAsJsonObject();
        if(obj == null) return null;
        return obj.has("id") ? obj.get("id").getAsString() : null;
    }


    @Override
    public void onError(Exception ex) {
        DianaPartyConnector.processError(ex);
    }
}
