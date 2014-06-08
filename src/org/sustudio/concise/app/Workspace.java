package org.sustudio.concise.app;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import org.sustudio.concise.app.gear.Gear;
import org.sustudio.concise.app.gear.concordancePlotter.ConcPlotData;
import org.sustudio.concise.app.preferences.CAPrefs;
import org.sustudio.concise.core.cluster.Cluster;
import org.sustudio.concise.core.collocation.Collocate;
import org.sustudio.concise.core.concordance.ConcLine;
import org.sustudio.concise.core.corpus.ConciseDocument;
import org.sustudio.concise.core.keyword.Keyword;
import org.sustudio.concise.core.wordlister.Word;

/**
 * Concise App 用的 Workspace，增加 log 和資料庫的部分。
 * 
 * @author Kuan-ming Su
 *
 */
public class Workspace extends org.sustudio.concise.core.Workspace {
	
	public Data DATA = new Data();

	/** 資料庫的檔名 */
	private static final String DB_FILENAME = "concise-db";
	/** 資料庫的副檔名 */
	private static final String DB_EXTENSION = "sqlite";
	
	protected ConciseApp app;
	protected final Logger logger;
	protected File lock;
	protected Connection connection;
	protected File temporaryDBFile;
	
	public Workspace(File workpath) throws IOException, WorkspaceLockedException {
		super(workpath);
		
		// start logger
		logger = startLogger();
		
		// check workspace lock flag file
		lock = new File(getFile(), "concise.lock");
		if (lock.exists()) {
			// check if the PID is running
			Process p = new ProcessBuilder("ps", "-p", FileUtils.readFileToString(lock), "-o", "command=").start();
			byte[] b = new byte[1024];
			int readbytes = -1;
			StringBuilder sb = new StringBuilder();
			InputStream in = p.getInputStream();
			while ( (readbytes = in.read(b)) != -1) {
				sb.append(new String(b, 0, readbytes));
			}
			p.destroy();
			if (sb.toString().substring(sb.lastIndexOf(" ")).trim().equals(Concise.class.getName())) {
				throw new WorkspaceLockedException("PID " + FileUtils.readFileToString(lock) + " is using this workspace.");
			}
			
		}
		lock.createNewFile();
		// write system PID to lock file
		FileUtils.write(lock, ManagementFactory.getRuntimeMXBean().getName().replaceAll("^(\\d+)@.+$", "$1"));		
		lock.deleteOnExit();
	}
	
	public Directory getTempDirectory() {
		// use RAM
		if (CAPrefs.TEMPORARY_FOLDER == null || 
			(
				!CAPrefs.TEMPORARY_FOLDER.equals(getFile()) && 
				!CAPrefs.TEMPORARY_FOLDER.equals(new File(System.getProperty("java.io.tmpdir")))
			)
		) {
			CAPrefs.TEMPORARY_FOLDER = null;
			return super.getTempDirectory();
		}
		
		try {
			Path tmpPath = Files.createTempDirectory(CAPrefs.TEMPORARY_FOLDER.toPath(), "concise-");
			tmpPath.toFile().deleteOnExit();	// just in case... 
			return FSDirectory.open(tmpPath.toFile());
			
		} catch (IOException e) {
			logError(null, e);
			return super.getTempDirectory();
		}
	}
	
	
	/**
	 * get {@link Logger} instance and start logger
	 * @return
	 * @throws IOException
	 */
	protected Logger startLogger() throws IOException {
		
		Logger logger = Logger.getLogger(getFile().getPath());
		
		// create log directory
		File log = new File(getFile(), ".log");
		if (!log.exists())	log.mkdir();
		
		// set up file handler
		int limit = 10000000; // 10 Mb
		int numLogFiles = 10;
		FileHandler logHandler = new FileHandler(log.getPath()+"/conciseLog%g.log", limit, numLogFiles);
		//logHandler.setFormatter(new XMLFormatter());	// <- XMLFormatter is default
		logHandler.setFormatter(new SimpleFormatter());
		logger.addHandler(logHandler);
		
		return logger;
	}
	
	/**
	 * 停止 Logger
	 */
	protected void stopLogger() {
		// There should be only 1 handler
		for (Handler logHandler : logger.getHandlers()) {
			logHandler.close();
		}
		lock.delete();
	}
	
	public void close() throws IOException {
		try {
			closeConnection();
		} catch (Exception e) {
			logError(null, e);
		}
		logInfo(toString() + " closed.");
		stopLogger();
		super.close();
	}
	
	/**
	 * 設定對應的 {@link ConciseApp}
	 * @param app 對應的 {@link ConciseApp}
	 */
	public void setApp(ConciseApp app) {
		this.app = app;
	}
	
	/**
	 * 傳回對應的 {@link ConciseApp}
	 * @return 對應的 {@link ConciseApp}
	 */
	public ConciseApp getApp() {
		return app;
	}
	
	
	/**
	 * 傳回資料庫的 Connection 物件
	 * @return 資料庫的 Connection 物件
	 */
	public Connection getConnection() throws SQLException, IOException
	{
		if (connection != null && !connection.isClosed()) {
			return connection;
		}
		
		SQLiteConfig config = new SQLiteConfig();
		config.setSharedCache(true);
		config.enableRecursiveTriggers(true);
		
		File db = new File(getFile(), DB_FILENAME+"."+DB_EXTENSION);
		File dbAlias = new File(getFile(), DB_FILENAME+" alias."+DB_EXTENSION);
		File dbBackup = new File(getFile(), DB_FILENAME+"."+DB_EXTENSION+".bak");
		
		// 檢查看看有沒有之前當掉的 database
		if (!db.exists() && dbAlias.exists() &&	FileUtils.isSymlink(dbAlias)) {
			// 從 temp file 恢復
			logInfo(toString() + " did not close properly. " +
					"restore temp: " + dbAlias.getCanonicalPath());
			temporaryDBFile = dbAlias.getCanonicalFile();
		}
		else if (!db.exists() && dbBackup.exists()) { // 從 backup file 著手，換電腦的時候可能會發生
			logInfo(toString() + " did not close properly. " +
					"restore backup: " + dbBackup.getPath());
			FileUtils.moveFile(dbBackup, db);
		}
		
		// jdbc 沒辦法處理中文字，當 workspace 的路徑含有中文時就會出錯。
		// for safety reason, move db to java's temp file location
		if (temporaryDBFile == null)
			temporaryDBFile = File.createTempFile(DB_FILENAME, DB_EXTENSION);
		dbAlias.delete();
		Files.createSymbolicLink(dbAlias.toPath(), temporaryDBFile.toPath());
		
		if (db.exists()) {
			temporaryDBFile.delete();
			dbAlias.delete();
			FileUtils.moveFile(db, temporaryDBFile);
			
			// 移檔案的同時也創造一個 symbol link ，免得當掉找不到檔案
			Files.createSymbolicLink(dbAlias.toPath(), temporaryDBFile.toPath());
		}
		
		if (temporaryDBFile.exists()) {
			FileUtils.copyFile(temporaryDBFile, dbBackup);
			logInfo("Auto Backup");
		}
		
		String file = temporaryDBFile.getAbsolutePath();
		
		SQLiteDataSource dataSource = new SQLiteDataSource(config);
		dataSource.setUrl("jdbc:sqlite:"+file);
				
		connection = dataSource.getConnection();
		connection.setAutoCommit(false);
		
		return connection;
	}
	
	/**
	 * 關閉資料庫
	 * @throws SQLException
	 * @throws IOException
	 */
	protected void closeConnection() throws SQLException, IOException {
		if (connection != null) {
			connection.close();
			connection = null;
		}
		// 正常關閉的話，把資料庫檔案移回來，然後把 symbol link 刪掉
		FileUtils.moveFile(temporaryDBFile, new File(getFile(), DB_FILENAME+"."+DB_EXTENSION));
		new File(getFile(), DB_FILENAME+" alias."+DB_EXTENSION).delete();
	}

	
	/**
	 * Log a INFO message.
	 * @param info		INFO message.
	 */
	public void logInfo(String info) {
		logger.info(info);
	}
	
	/**
	 * Log a SEVERE error.
	 * @param gear		Concise Gear
	 * @param e			Exception.
	 */
	public void logError(Gear gear, Exception e) {
		String msg = "Error";
		if (gear != null)
			msg = msg + " in " + gear.label();
		logger.log(Level.SEVERE, msg, e);
		
		// also print stack trace.
		e.printStackTrace();
		
		if (e instanceof SQLException) {
			try {
				closeConnection();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
	
	
	/**
	 * 存資料表的內容用的
	 * 
	 * @author Kuan-ming Su
	 *
	 */
	public class Data {
		
		public List<ConciseDocument> documentList;
		
		public List<ConciseDocument> referenceDocumentList;
		
		public List<Word> wordlist;
		
		public List<ConcLine> concLineList;

		public List<Cluster> clusterList;
		
		public List<Keyword> keywordList;
		
		public List<Collocate> collocateList;
		
		public List<ConcPlotData> plotDataList;
		
		public long totalTermFreq = -1;

		public long totalRefTermFreq = -1;

		public String[] SearchWorders;
	}
}
