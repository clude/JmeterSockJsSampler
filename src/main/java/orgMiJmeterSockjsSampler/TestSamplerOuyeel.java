package orgMiJmeterSockjsSampler;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.tomcat.websocket.Constants;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.converter.SimpleMessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.*;
import org.springframework.web.socket.sockjs.frame.Jackson2SockJsMessageCodec;
import org.springframework.web.socket.sockjs.transport.TransportType;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static orgMiJmeterSockjsSampler.SockJsSampler.HANDSHAKE_HEADERS_COOKIE;

public class TestSamplerOuyeel {

	private String transport = "websocket";
    private String host = "https://ouyeel.liveall.cn";
    private String path = "/olive-chat/chat/ws";
    private long connectionTime = 5000;
    private long responseBufferTime = 30000;
    private String connectionHeadersLogin = "roomId:947";
    private String connectionHeadersPasscode = "passcode:xxx";
    private String connectionHeadersHost = "host:xxx";
    private String connectionHeadersAcceptVersion = "accept-version:1.1,1.0";
    private String connectionHeadersHeartbeat = "heart-beat:0,0";
    private String subscribeHeadersId = "id:sub-0";
    private String subscribeHeadersDestination = "destination:/topic/chat.room.947";
    private String connectionHeadersCookie= "Cookie: JSESSIONID=43d23278-3808-4410-b273-cb7a445d9af6";

	public static void main(String[] args)
    {
		TestSamplerOuyeel test = new TestSamplerOuyeel();
		ResponseMessage responseMessage = new ResponseMessage();

		try {
			if (test.transport == "xhr-streaming") {
				test.createXhrStreamingConnection(responseMessage);
			} else {
				test.createWebsocketConnection(responseMessage);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	// Websocket test
	public void createWebsocketConnection(ResponseMessage responseMessage) throws Exception {
		StandardWebSocketClient simpleWebSocketClient = new StandardWebSocketClient();

		// set up a TrustManager that trusts everything
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, new TrustManager[] { new BlindTrustManager() }, null);
		Map<String, Object> userProperties = new HashMap<>();
		userProperties.put(Constants.SSL_CONTEXT_PROPERTY, sslContext);
		simpleWebSocketClient.setUserProperties(userProperties);

		List<Transport> transports = new ArrayList<>(1);
     	transports.add(new WebSocketTransport(simpleWebSocketClient));

 		SockJsClient sockJsClient = new SockJsClient(transports);
 		WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
// 		stompClient.setMessageConverter(new StringMessageConverter());
		stompClient.setMessageConverter(new SimpleMessageConverter());
 		sockJsClient.setHttpHeaderNames();

//		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
//		taskScheduler.afterPropertiesSet();
// 		stompClient.setTaskScheduler(taskScheduler);

 		URI stompUrlEndpoint = new URI(this.host + this.path);
 		StompSessionHandler sessionHandler = new SockJsWebsocketStompSessionHandler(
			this.getSubscribeHeaders(),
			this.connectionTime,
			this.responseBufferTime,
			responseMessage
		);

 		WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
 		handshakeHeaders.setAll(this.getHandshakeHeaders());
// 		handshakeHeaders.set("Cookie", "JSESSIONID=43d23278-3808-4410-b273-cb7a445d9af6");
 		StompHeaders connectHeaders = new StompHeaders();
 		String connectionHeadersString = this.getConnectionsHeaders();
 		String[] splitHeaders = connectionHeadersString.split("\n");

 		for (int i = 0; i < splitHeaders.length; i++) {
 			int key = 0;
 			int value = 1;
 			String[] headerParameter = splitHeaders[i].split(":");

 			connectHeaders.add(headerParameter[key], headerParameter[value]);
 		}

 		String startMessage = "\n[Execution Flow]"
 						    + "\n - Opening new connection"
 							+ "\n - Using response message pattern \"a[\"CONNECTED\""
 							+ "\n - Using response message pattern \"a[\"MESSAGE\\nsubscription:sub-0\""
 							+ "\n - Using disconnect pattern \"\"";

 		responseMessage.addMessage(startMessage);

 		System.out.println(startMessage);
		ListenableFuture<StompSession> stompSession = stompClient.connect(stompUrlEndpoint.toString(), handshakeHeaders, connectHeaders, sessionHandler, new Object[0]);

 		// wait some time till killing the stomp connection
 		Thread.sleep(this.connectionTime + this.responseBufferTime);
 		stompSession.get().disconnect();
 		stompClient.stop();

 		String messageVariables = "\n[Variables]"
 								+ "\n" + " - Message count: " + responseMessage.getMessageCounter();

 		responseMessage.addMessage(messageVariables);

 		String messageProblems = "\n[Problems]"
								+ "\n" + responseMessage.getProblems();

		System.out.println(messageVariables);

		System.out.println(responseMessage.getMessage());

 		System.out.println(messageProblems);

 		responseMessage.addMessage(messageProblems);
 	}

	// XHR-Streaming test
    public void createXhrStreamingConnection(ResponseMessage responseMessage) throws Exception {
 	  RestTemplate restTemplate = new RestTemplate();
 	  RestTemplateXhrTransport transport = new RestTemplateXhrTransport(restTemplate);
 	  transport.setTaskExecutor(new SyncTaskExecutor());

 	  SockJsUrlInfo urlInfo = new SockJsUrlInfo(new URI(this.host + this.path));
 	  HttpHeaders headers = new HttpHeaders();
 	  SockJsXhrTransportRequest request = new SockJsXhrTransportRequest(
 		  urlInfo,
 		  headers,
 		  headers,
 		  transport,
 		  TransportType.XHR,
 		  new Jackson2SockJsMessageCodec()
 	  );
 	  SockJsXhrSessionHandler xhrSessionHandler = new SockJsXhrSessionHandler(
 		  this.getConnectionsHeaders(),
 		  this.getSubscribeHeaders(),
 		  this.connectionTime,
		  this.responseBufferTime,
		  responseMessage
 	  );

 	  String startMessage = "\n[Execution Flow]"
					     + "\n - Opening new connection"
						 + "\n - Using response message pattern \"a[\"CONNECTED\""
						 + "\n - Using response message pattern \"a[\"MESSAGE\\nsubscription:sub-0\""
						 + "\n - Using disconnect pattern \"\"";

 	  responseMessage.addMessage(startMessage);

 	  System.out.println(startMessage);

 	  transport.connect(request, xhrSessionHandler);

 	  String messageVariables = "\n[Variables]"
 							  + "\n" + " - Message count: " + responseMessage.getMessageCounter();

 	  responseMessage.addMessage(messageVariables);

 	  String messageProblems = "\n[Problems]"
 						     + "\n" + responseMessage.getProblems();

 	  responseMessage.addMessage(messageProblems);

 	  System.out.println(messageVariables);

 	  System.out.println(responseMessage.getMessage());

 	  System.out.println(messageProblems);
   }

	private Map<String, String> getHandshakeHeaders() {
		Map<String, String> headers = new HashMap<>();

		String cookeHeader = connectionHeadersCookie;
		String[] cookieArr = cookeHeader.split(":");
		if(cookieArr.length > 1) {
			headers.put(cookieArr[0].trim(), cookieArr[1].trim());
		}

		return headers;
	}

	private String getSubscribeHeaders() {
    	return String.format(
			"%s\n%s",
			this.subscribeHeadersId,
			this.subscribeHeadersDestination
		);
    }

    private String getConnectionsHeaders() {
    	return String.format(
			"%s\n%s\n%s\n%s\n%s",
			this.connectionHeadersLogin,
			this.connectionHeadersPasscode,
			this.connectionHeadersHost,
			this.connectionHeadersAcceptVersion,
			this.connectionHeadersHeartbeat
		);
    }

}
