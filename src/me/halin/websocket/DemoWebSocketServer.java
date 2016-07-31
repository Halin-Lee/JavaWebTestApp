package me.halin.websocket;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.spi.JsonProvider;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/WebSocket/DemoWebSocketServer", encoders = { DemoWebSocketServer.DemoEncoder.class }, decoders = { DemoWebSocketServer.DemoDecoder.class })
public class DemoWebSocketServer {

	private static final List<DemoObject> OBJECTS = Collections
			.synchronizedList(new LinkedList<DemoObject>());
	private static final Set<Session> sessions = Collections
			.synchronizedSet(new HashSet<Session>());

	@OnOpen
	public void onOpen(Session session) {
		System.out.println(session.getId() + " has opened a connection");
		sessions.add(session);
		try {
			session.getBasicRemote().sendText("Connection Established");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * When a user sends a message to the server, this method will intercept the
	 * message and allow us to react to it. For now the message is read as a
	 * String.
	 * 
	 * @throws EncodeException
	 */
	@OnMessage
	public void onMessage(DemoObject object, Session session)
			throws EncodeException {
		System.out.println("Message from " + session.getId() + ": "
				+ object.code);
		postAll(object);
	}

	private void postAll(DemoObject object) throws EncodeException {

		for (Session session : sessions) {
			try {
				session.getBasicRemote().sendObject(object);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}

	/**
	 * The user closes the connection.
	 * 
	 * Note: you can't send messages to the client from this method
	 */
	@OnClose
	public void onClose(Session session) {
		System.out.println("Session " + session.getId() + " has ended");
		sessions.remove(session);
	}

	public static class DemoObject {
		public int code;
	}

	public static class DemoEncoder implements Encoder.TextStream<DemoObject> {

		@Override
		public void destroy() {
		}

		@Override
		public void init(EndpointConfig arg0) {
		}

		@Override
		public void encode(DemoObject obj, Writer writer)
				throws EncodeException, IOException {
			JsonProvider provider = JsonProvider.provider();
			JsonObject jsonObject = provider.createObjectBuilder()
					.add("code", obj.code).build();
			try (JsonWriter jsonWriter = provider.createWriter(writer)) {
				jsonWriter.write(jsonObject);
			}
		}
	}

	public static class DemoDecoder implements Decoder.TextStream<DemoObject> {

		@Override
		public void destroy() {
		}

		@Override
		public void init(EndpointConfig arg0) {
		}

		@Override
		public DemoObject decode(Reader reader) throws DecodeException,
				IOException {
			JsonProvider provider = JsonProvider.provider();
			JsonReader jsonReader = provider.createReader(reader);
			JsonObject jsonObject = jsonReader.readObject();
			DemoObject sticker = new DemoObject();
			sticker.code = jsonObject.getInt("code");
			return sticker;
		}

	}
}
