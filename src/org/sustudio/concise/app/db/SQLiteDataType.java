package org.sustudio.concise.app.db;

public enum SQLiteDataType {

	ObjectType (null),
	TextType (null),
	NumericType (null),
	
	VARCHAR (TextType),
	DOUBLE (NumericType),
	INTEGER (NumericType),
	BIGINT (NumericType),
	BLOB (ObjectType)
	;
	
	SQLiteDataType parentType;
	SQLiteDataType(SQLiteDataType parentType) {
		this.parentType = parentType;
	}
	
	public boolean isNumericType() {
		return parentType == NumericType;
	}
	
	public boolean isTextType() {
		return parentType == TextType;
	}
	
	public boolean isObjectType() {
		return parentType == ObjectType;
	}
}
