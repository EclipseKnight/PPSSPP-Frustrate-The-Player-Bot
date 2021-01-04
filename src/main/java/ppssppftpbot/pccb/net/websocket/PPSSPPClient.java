package ppssppftpbot.pccb.net.websocket;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import ppssppftpbot.pccb.net.Launcher;
import ppssppftpbot.pccb.net.Logger;
import ppssppftpbot.pccb.net.Logger.Level;

public class PPSSPPClient extends WebSocketClient {
	
	

	public PPSSPPClient(URI serverUri, Draft draft) {
		super(serverUri, draft);
	}

	public PPSSPPClient(URI serverUri) {
		super(serverUri);
	}
	
	
	@Override
	public void onOpen(ServerHandshake handshakedata) {
		Logger.log(Level.SUCCESS, "WebSocket successfully connected to " + this.getRemoteSocketAddress());
	}
	
	@Override
	public void onClose(int code, String reason, boolean remote) {
		Logger.log(Level.ERROR, "[" + this.hashCode() +"] WebSocket connection closed with exit code " + code + " additional info: " + reason + " By Host:" + remote);
		
		// If connection is closed by the host attempt a reconnect.
		// Otherwise client closed it, assumed intentionally. 
		if (remote) {
			Launcher.socketHandler.reconnect();
		}
	}

	@Override
	public void onMessage(String message) {
		Logger.log(Level.INFO, "received message: " + message);
	}
	
	@Override
	public void onError(Exception ex) {
		Logger.log(Level.ERROR, "an error occurred:" + ex);
	}
	
}
