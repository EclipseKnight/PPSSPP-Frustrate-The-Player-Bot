package ppssppftpbot.pccb.net.websocket;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import ppssppftpbot.pccb.net.Launcher;

public class PPSSPPClient extends WebSocketClient {
	
	

	public PPSSPPClient(URI serverUri, Draft draft) {
		super(serverUri, draft);
	}

	public PPSSPPClient(URI serverUri) {
		super(serverUri);
	}
	
	
	@Override
	public void onOpen(ServerHandshake handshakedata) {
		System.out.println("WebSocket successfully connected to " + this.getRemoteSocketAddress());
	}
	
	@Override
	public void onClose(int code, String reason, boolean remote) {
		System.out.println("[" + this.hashCode() +"] WebSocket connection closed with exit code " + code + " additional info: " + reason + " By Host:" + remote);
		
		// If connection is closed by the host attempt a reconnect.
		// Otherwise client closed it, assumed intentionally. 
		if (remote) {
			Launcher.socketHandler.reconnect();
		}
			
		
	}

	@Override
	public void onMessage(String message) {
		System.out.println("received message: " + message);
		
	}
	
	@Override
	public void onError(Exception ex) {
		System.err.println("an error occurred:" + ex);

	}
	
}
