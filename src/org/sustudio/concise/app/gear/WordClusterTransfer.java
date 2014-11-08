package org.sustudio.concise.app.gear;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;
import org.sustudio.concise.core.cluster.Cluster;

public class WordClusterTransfer extends ByteArrayTransfer {

	/* The data flavor must be MIME type-like */
	static final String MIME_TYPE = "custom/ConciseWordCluster"; // $NON-NLS-1$
	
	private static WordClusterTransfer _instance;
	
	public static WordClusterTransfer getInstance() {
		if (_instance == null) {
			_instance = new WordClusterTransfer();
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
		if (!checkCCWordCluster(object) || !isSupportedType(transferData)) {
			DND.error(DND.ERROR_INVALID_DATA);
		}
		Cluster[] clusters = (Cluster[]) object;
		byte[] bytes = convertToByteArray(clusters);
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
	
	boolean checkCCWordCluster(Object object) {
		if (object == null) return false;
		return object instanceof Cluster[];
	}
	
	@Override
	protected boolean validate(Object object) {
		return checkCCWordCluster(object);
	}
	
	
/* shared methods for converting instances of CCWord <-> byte[] */
	
	static byte[] convertToByteArray(Cluster[] words) {
		
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

	static Cluster[] restoreFromByteArray(byte[] bytes) {
		try {
			ByteArrayInputStream byteInStream = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = new ObjectInputStream(byteInStream);
			Cluster[] words = (Cluster[]) ois.readObject();
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
