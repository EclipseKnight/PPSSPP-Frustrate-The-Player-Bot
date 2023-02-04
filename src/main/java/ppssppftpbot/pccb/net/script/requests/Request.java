package ppssppftpbot.pccb.net.script.requests;

import java.util.concurrent.CompletableFuture;

public class Request {

	private String event;
	private String message;
	private CompletableFuture<String> responseFuture;
	private long ticketNum;
	private boolean isComplete;
	
	private long startTime;
	private long totalTime;
	
	public Request(String event, String message, CompletableFuture<String> responseFuture, long ticketNum) {
		this.event = event;
		this.message = message;
		this.responseFuture = responseFuture;
		this.ticketNum = ticketNum;
		this.isComplete = responseFuture.isDone();
		this.startTime = System.currentTimeMillis();
	}
	
	public Request(String event, String message, long ticketNum) {
		this.event = event;
		this.message = message;
		this.responseFuture = new CompletableFuture<>();
		this.ticketNum = ticketNum;
		this.isComplete = false;
		this.startTime = System.currentTimeMillis();
	}
	
	public String getEvent() {
		return event;
	}
	
	public void setEvent(String event) {
		this.event = event;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public CompletableFuture<String> getResponseFuture() {
		return responseFuture;
	}
	
	public void setResponseFuture(CompletableFuture<String> responseFuture) {
		this.responseFuture = responseFuture;
	}
	
	public long getTicketNum() {
		return ticketNum;
	}
	
	public void setTicketNum(long ticketNum) {
		this.ticketNum = ticketNum;
	}
	
	public boolean isComplete() {
		return isComplete;
	}
	
	public void setComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}
	
	public long getTotalTime() {
		if (isComplete) {
			return totalTime;
		}
		return -1L;
	}
	
	public boolean completeRequest(String message) {
		isComplete = true;
		totalTime = System.currentTimeMillis() - startTime;
		
		return responseFuture.complete(message);
	}
}
