package org.mwg.ws;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedBinaryMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.core.CoreConstants;
import org.mwg.core.utility.BufferView;
import org.mwg.plugin.Storage;
import org.mwg.struct.Buffer;
import org.mwg.struct.BufferIterator;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Add a WS wrapper on another storage to allow a remote access on this storage
 *
 * Each received messages finish with 5 specific bytes (added by the client, that are: 4 bytes for the message ID
 * (specific and only useful for the client) and one byte for the type of request (see {@link WSMessageType}).
 */
public class WSStorageWrapper implements Storage, WebSocketConnectionCallback{
    private final Storage _wrapped;
    private final Undertow _server;
    private Graph _graph;

    public WSStorageWrapper(Storage _wrapped, int port) {
        this._wrapped = _wrapped;
        HttpHandler handler = Handlers.websocket(this);
        _server = Undertow.builder().addHttpListener(port,"0.0.0.0").setHandler(handler).build();
    }

    @Override
    public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
        channel.getReceiveSetter().set(new WSListener());
        channel.resumeReceives();
    }

    @Override
    public void get(Buffer keys, Callback<Buffer> callback) {
        _wrapped.get(keys,callback);
    }

    @Override
    public void put(Buffer stream, Callback<Boolean> callback) {
        _wrapped.put(stream,callback);
    }

    @Override
    public void remove(Buffer keys, Callback<Boolean> callback) {
        _wrapped.remove(keys,callback);
    }

    @Override
    public void connect(Graph graph, Callback<Short> callback) {
        _server.start();
        _graph = graph;
        _wrapped.connect(graph,callback);
    }

    @Override
    public void disconnect(Short prefix, Callback<Boolean> callback) {
        _server.stop();
        _graph = null;
        _wrapped.disconnect(prefix,callback);
    }

    private class WSListener extends AbstractReceiveListener {
        @Override
        protected void onFullBinaryMessage(WebSocketChannel channel, BufferedBinaryMessage message) throws IOException {
            ByteBuffer[] data = message.getData().getResource();
            for(ByteBuffer byteBuffer : data) {
                final Buffer buffer = _graph.newBuffer();
                byte[] bytes = new byte[byteBuffer.limit()];
                byteBuffer.get(bytes);
                buffer.writeAll(bytes);
                bytes = null;

                BufferView wsInfo = null;
                BufferIterator it = buffer.iterator();
                while(it.hasNext()) {
                    wsInfo = (BufferView) it.next();
                }

                //Contains the 5 bytes representing the WS info
                final byte[] wsIndoData = wsInfo.data();

                //remove the WS info
                for(int i= 0;i<6;i++) {
                    buffer.removeLast();
                }

                switch (wsIndoData[wsIndoData.length - 1]) {
                    case WSMessageType.RQST_GET: {
                        get(buffer, new Callback<Buffer>() {
                            @Override
                            public void on(Buffer result) {
                                result.write(CoreConstants.BUFFER_SEP);
                                for (int i = 0; i < 4; i++) {
                                    result.write(wsIndoData[i]);
                                }
                                result.write(WSMessageType.RESP_GET);
                                ByteBuffer toSend = ByteBuffer.wrap(result.data());
                                WebSockets.sendBinary(toSend, channel, null);
                                buffer.free();
                            }
                        });
                        break;
                    }
                    case WSMessageType.RQST_PUT: {
                        put(buffer, new Callback<Boolean>() {
                            @Override
                            public void on(Boolean result) {
                                byte[] toSend = new byte[6];
                                toSend[0] = (byte) ((result) ? 1 : 0);
                                System.arraycopy(wsIndoData, 0, toSend, 1, 4);
                                toSend[5] = WSMessageType.RESP_PUT;
                                ByteBuffer bufferToSend = ByteBuffer.wrap(toSend);
                                WebSockets.sendBinary(bufferToSend, channel, null);
                                buffer.free();
                            }
                        });
                        break;
                    }
                    case WSMessageType.RQST_REMOVE: {
                        remove(buffer, new Callback<Boolean>() {
                            @Override
                            public void on(Boolean result) {
                                byte[] toSend = new byte[6];
                                toSend[0] = (byte) ((result) ? 1 : 0);
                                System.arraycopy(wsIndoData, 0, toSend, 1, 4);
                                toSend[5] = WSMessageType.RESP_REMOVE;
                                ByteBuffer bufferToSend = ByteBuffer.wrap(toSend);
                                WebSockets.sendBinary(bufferToSend, channel, null);
                                buffer.free();
                            }
                        });
                        break;
                    }
                    default:
                        System.err.println("Unknown message with code " + wsIndoData[wsIndoData.length - 1]);
                }
            }
        }

    }


}