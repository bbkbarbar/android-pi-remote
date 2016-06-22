package hu.barbar.comm.util;

/**
 * 
 * @author Barbar
 */
public class RGBMessage extends Msg {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9005683801357654801L;

	private Integer colorR = -1,
					colorG = -1,
					colorB = -1;
	
	public RGBMessage(String content, int r, int g, int b) {
		super(content, Msg.Types.RGB_COMMAND);

		int red   = r;
		int green = g;
		int blue  = b;
		
		cribTo8bit(red);
		cribTo8bit(green);
		cribTo8bit(blue);
		
		this.colorR = Integer.valueOf(red);
		this.colorG = Integer.valueOf(green);
		this.colorB = Integer.valueOf(blue);
		
	}
	
	private static int cribTo8bit(int val){
		if(val < 0)
			return 0;
		if(val > 255)
			return 255;
		return val;
	}

	public int getRed(){
		return this.colorR;
	}
	
	public int getGreen(){
		return this.colorG;
	}
	
	public int getBlue(){
		return this.colorB;
	}

	
	protected String getColorCompontents(){
		return "R: " + this.colorR +
			   " G: " + this.colorG +
			   " B: " + this.colorB;
	}
	
	public String getColorHex(){
		return Integer.toHexString(getRed()) + Integer.toHexString(getGreen()) + Integer.toHexString(getBlue());
	}
	
	public String getColorHexWithTitle(){
		return "Color: " + this.getContent();
	}
	
	@Override
	public String toString(){
		return super.getFirstPartOfToString() +
				getColorCompontents() + 
				super.getLastPartOfToString();
	}
	
}
