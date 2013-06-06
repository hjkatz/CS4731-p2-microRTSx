package ai.cs4730;

public class MapUtil {

	public AIController ai;
	public static int[] map;
    public static int   WIDTH;
    public static int   HEIGHT;
	
	public MapUtil(AIController ai) 
	{
		this.ai = ai;
		map = ai.gameState.getMap();
        WIDTH = ai.gameState.getMapWidth();
        HEIGHT = ai.gameState.getMapHeight();
	}
	
	

}
