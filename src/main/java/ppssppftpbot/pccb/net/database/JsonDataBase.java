package ppssppftpbot.pccb.net.database;

import java.io.File;

import io.jsondb.JsonDBTemplate;
import ppssppftpbot.pccb.net.Launcher;

public class JsonDataBase {

	String dbFilesLocation = Launcher.configPath + File.separator + "configs" + File.separator + "database";
	String baseScanPackage = "ppssppftpbot.pccb.net.database";
	public static JsonDBTemplate jsonDB;
	
	/**
	 * Constructor
	 * @param dbFilesLocation set custom location
	 */
	public JsonDataBase(String dbFilesLocation) {
		this.dbFilesLocation = dbFilesLocation;
	}
	
	/**
	 * Constructor with default location. 
	 */
	public JsonDataBase() {
		
	}
}
