package orgMiJmeterSockjsSampler;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

public class SockJsWebsocketSubscriptionHandler implements StompFrameHandler
{
	private long messageCounter = 1;
	private long responseBufferTime;
	private ResponseMessage responseMessage;

	public SockJsWebsocketSubscriptionHandler(ResponseMessage responseMessage, long responseBufferTime) {
		this.responseMessage = responseMessage;
		this.responseBufferTime = responseBufferTime;

		String subscribeMessage = " - Leaving streaming connection open"
								+ "\n - Waiting for messages for " + this.responseBufferTime + " MILLISECONDS";
		this.responseMessage.addMessage(subscribeMessage);
	}

//	@Override
//	public Type getPayloadType(StompHeaders headers) {
//		return String.class;
//	}

	@Override
	public Type getPayloadType(StompHeaders headers) {
		return byte[].class;
	}

	@Override
	public void handleFrame(StompHeaders headers, Object payload) {
		String payloadMessage = new String((byte[]) payload, StandardCharsets.UTF_8);
		String message = "MESSAGE\\n" + headers.toString() + payloadMessage.toString();


		StringBuilder sb = new StringBuilder();
		Map<String, String> map = headers.toSingleValueMap();

		sb.append(" - Received message #" + this.messageCounter + " (" + message.length() + " bytes)a[\"MESSAGE");

		for (Map.Entry<String, String> entry : map.entrySet()) {
			sb.append("\\n" + entry.getKey() + ":" + entry.getValue());
		}

		sb.append("\\n\\n" + payloadMessage.toString() + "\\u0000\\n\"];");

		try{
			SocketJsHelper.messageQueue.add(sb.toString());
		} catch (Exception ex) {
			// do nothing
		}

		this.responseMessage.addMessage(sb.toString());
		this.responseMessage.setMessageCounter(this.messageCounter);
		this.messageCounter++;
	}
}
