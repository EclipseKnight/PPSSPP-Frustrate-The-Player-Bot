package ppssppftpbot.pccb.net.websocket;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebSocketConfiguration {

	private URI serverUri;
	
	private int webSocketReconnectTimeout;
	
	public URI getServerUri() {
		return serverUri;
	}
	
	public void setServerUri(URI serverUri) {
		this.serverUri = serverUri;
	}
	
	public int getWebSocketReconnectTimeout() {
		return webSocketReconnectTimeout;
	}
	
	public void setWebSocketReconnectTimeout(int webSocketReconnectTimeout) {
		this.webSocketReconnectTimeout = webSocketReconnectTimeout;
	}

    
    @Override
	public String toString() {
		return String.format("""
				Configuration:
					server_uri = %s
					web_socket_reconnect_timeout = %s
				""", serverUri, webSocketReconnectTimeout);
	}
}
