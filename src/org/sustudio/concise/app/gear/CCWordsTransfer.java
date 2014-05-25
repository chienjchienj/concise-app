package org.sustudio.concise.app.gear;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;
import org.sustudio.concise.core.wordlister.Word;

public class CCWordsTransfer extends ByteArrayTransfer {

	/* The data flavor must be MIME type-like */
	static final String MIME_TYPE = "custom/ConciseWord"; // $NON-NLS-1$
	
	private static CCWordsTransfer _instance;
	
	public static CCWordsTransfer getInstance() {
		if (_instance == null) {
			_instance = new CCWordsTransfer();
		}
		return _instance;
	}
	
	final int MIME_TYPE_ID = registerType(MIME_TYPE);
	
	@Override
	protected int[] getTypeIds() {
		return new int[] { MIME_TYPE_ID };
	}
	
	@Override
	protected String[] getTypeNames() {
		return new String[] { MIME_TYPE };
	}
	
	@Override
	public void javaToNative(Object object, TransferData transferData) {
		if (!checkCCWords(object) || !isSupportedType(transferData)) {
			DND.error(DND.ERROR_INVALID_DATA);
		}
		Word[] words = (Word[]) object;
		byte[] bytes = convertToByteArray(words);
		if (bytes != null) {
			super.javaToNative(bytes, transferData);
		}
	}
	
	@Override
	public Object nativeToJava(TransferData transferData) {
		if (!isSupportedType(transferData)) return null;
		byte[] bytes = (byte[])super.nativeToJava(transferData);
		return bytes == null ? null : restoreFromByteArray(bytes);
	}
	
	boolean checkCCWords(Object object) {
		if (object == null) return false;
		return object instanceof Word[];
	}
	
	@Override
	protected boolean validate(Object object) {
		return checkCCWords(object);
	}
	
	
/* shared methods for converting instances of CCWord <-> byte[] */
	
	static byte[] convertToByteArray(Word[] words) {
		
		try {
			ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(byteOutStream);
			oos.writeObject(words);
			oos.close();
			byteOutStream.close();
			return byteOutStream.toByteArray();
			
		} catch (IOException e) { 
			return null;
		}
	}

	static Word[] restoreFromByteArray(byte[] bytes) {
		try {
			ByteArrayInputStream byteInStream = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = new ObjectInputStream(byteInStream);
			Word[] words = (Word[]) ois.readObject();
			ois.close();
			byteInStream.close();
			return words;
			
		} catch (IOException ex) {
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		} 
	}
}
