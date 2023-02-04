package ppssppftpbot.pccb.net.script.requests;

public class Breakpoint extends Request {

	private int address;
	private int size;
	private boolean enabled;
	private boolean read;
	private boolean write;
	private boolean change;
	private String logFormat;
	
	private String scriptToRun;
	
	/**
	 * 
	 * @param address int
	 * @param size int
	 * @param enabled boolean
	 * @param read boolean
	 * @param write boolean
	 * @param change boolean
	 * @param logFormat string
	 * @param scriptToRun string
	 * @param ticketNum long
	 */
	public Breakpoint(int address, int size, boolean enabled, boolean read, boolean write, boolean change, String logFormat, String scriptToRun, long ticketNum) {
		super("memory.breakpoint", null, ticketNum);
		this.address = address;
		this.size = size;
		this.enabled = enabled;
		this.read = read;
		this.write = write;
		this.change = change;
		this.logFormat = logFormat;
		this.scriptToRun = scriptToRun;
	}
	
	
	
	public void setAddress(int address) {
		this.address = address;
	}
	
	public int getAddress() {
		return address;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	public int getSize() {
		return size;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setRead(boolean read) {
		this.read = read;
	}
	
	public boolean isRead() {
		return read;
	}
	
	public void setWrite(boolean write) {
		this.write = write;
	}
	
	public boolean isWrite() {
		return write;
	}
	
	public void setChange(boolean change) {
		this.change = change;
	}
	
	public boolean isChange() {
		return change;
	}
	
	public void setLogFormat(String logFormat) {
		this.logFormat = logFormat;
	}
	
	public String getLogFormat() {
		return logFormat;
	}
	
	public void setScriptToRun(String scriptToRun) {
		this.scriptToRun = scriptToRun;
	}
	
	public String getScriptToRun() {
		return scriptToRun;
	}
	
}
