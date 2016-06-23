package hu.barbar.comm.util;

import java.io.Serializable;

public class Msg implements Serializable{
	
	public static final int versionCode = 100;

	/**
	 * 
	 */
	private static final long serialVersionUID = 4035266283582076042L;


	public static class Types{
		public static final int UNDEFINED = 0,
								COMMAND = 1,
								PLAIN_TEXT = 2, 
								RGB_COMMAND = 3;
		
		public static String getTypeText(int type){
			switch (type) {
			case Types.COMMAND:
				return "Command";
			case Types.PLAIN_TEXT:
				return "Plain text";
			case Types.RGB_COMMAND:
				return "RGB command";

			default:
				return "Undefined";
			}
		}
		
	}

	private int type = Types.UNDEFINED; 
	private String content = null;
	
	
	public Msg(String text){
		this.content = text;
		this.type = Types.PLAIN_TEXT;
	}
	
	public Msg(String text, int type){
		this.content = text;
		this.type = type;
	}
	
	
	public int getType(){
		return this.type;
	}
	
	public String getContent(){
		return this.content;
	}

	protected String getFirstPartOfToString(){
		return "Msg={"
				+ "Type: \"" + Types.getTypeText(this.getType()) + "\",";
	}
	
	protected static String getLastPartOfToString(){
		return "}";
	}
	
	public String toString(boolean needToAppendEndPart){
		String result = this.getFirstPartOfToString();
		if(this.getContent() != null){
			result += " Content: \"" + this.getContent() + "\"";
		}
		if(needToAppendEndPart){
			result += Msg.getLastPartOfToString();
		}
		return result;
	}
	
	@Override
	public String toString(){
		return this.toString(true);
	}
	
}
