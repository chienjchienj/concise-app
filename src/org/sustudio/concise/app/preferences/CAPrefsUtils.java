package org.sustudio.concise.app.preferences;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.sustudio.concise.app.Concise;
import org.sustudio.concise.core.CCPrefs;


/**
 * Read and write Concise Preferences
 * 
 * @author Kuan-ming Su
 *
 */
public class CAPrefsUtils {
	
	/** 預設的 preferences 檔案路徑（在 workspace 之下） */
	static final String PREFERENCES_FILE = "concisepreferences.properties";
	
	/**
	 * 寫入 preferences 檔案
	 * @throws Exception
	 */
	public static void writePrefs() throws Exception {
		
		// 寫入的時候，即便 CAPrefs extends CCPrefs ，還是要把兩個 class 都寫進去
		Properties p = new Properties();
		writePrefs(p, CCPrefs.class);
		writePrefs(p, CAPrefs.class);
		FileOutputStream out = new FileOutputStream(new File(Concise.getCurrentWorkspace().getFile(), PREFERENCES_FILE));
		//p.storeToXML(out, "Concise Preferences", "UTF-8");
		p.store(out, "Concise Preferences (Base64 encoded)");
		p.clear();
		out.close();
	}
	
	private static void writePrefs(Properties p, Class<?> clz) throws Exception {
		for (Field field : clz.getDeclaredFields()) {
			if (!Modifier.isFinal(field.getModifiers())) {
				p.setProperty(field.getName(), serializeObjectToString(field.get(clz)));
			}
		}
	}
	
	
	/**
	 * covert serialize object to String.  Check <a href="http://stackoverflow.com/questions/6055476/how-to-convert-object-to-string-in-java">http://stackoverflow.com/questions/6055476/how-to-convert-object-to-string-in-java</a> for detail information.
	 * @param object
	 * @return
	 * @throws IOException
	 */
	public static String serializeObjectToString(Object object) throws IOException {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
		ObjectOutputStream oos = new ObjectOutputStream(gzipOut);
		
		oos.writeObject(object);
		
		oos.flush();
		gzipOut.close();
		baos.close();
		oos.close();
		String objString = new String(new Base64().encode(baos.toByteArray()));
		return objString;
	}
	
	
	/**
	 * 讀取 preferences 檔案
	 * @throws Exception
	 */
	public static void readPrefs() throws Exception {
		
		File prefsFile = new File(Concise.getCurrentWorkspace().getFile(), PREFERENCES_FILE);
		if (prefsFile.exists()) {
			FileInputStream in = new FileInputStream(prefsFile);
			Properties p = new Properties();
			//p.loadFromXML(in);
			p.load(in);
			in.close();
			
			readPrefs(p, CAPrefs.class);
			readPrefs(p, CCPrefs.class);
			
			p.clear();
		}
	}
	
	protected static void readPrefs(Properties p, Class<?> clz) throws Exception {
		for (Field field : clz.getDeclaredFields()) {
			if (!Modifier.isFinal(field.getModifiers())) {
				Object obj = deSerializeObjectFromString(p.getProperty(field.getName()));
				if (obj != null) {
					field.set(clz, obj);
				}
			}
		}
	}
		
	
	/**
	 * 將Base64編碼的字串轉回原本的物件
	 * @param objString
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object deSerializeObjectFromString(String objString) throws IOException, ClassNotFoundException {
		if (objString == null) return null;
		
		ByteArrayInputStream bais = new ByteArrayInputStream(new Base64().decode(objString));
		GZIPInputStream gzipIn = new GZIPInputStream(bais);
		ObjectInputStream ois = new ObjectInputStream(gzipIn);
		
		Object object = null;
		try {
			object = ois.readObject();
		} catch (InvalidObjectException e) {
			// refuse to handle
		}
		
		ois.close();
		gzipIn.close();
		bais.close();
		
		return object;
		
	}
}
