
package ai.cs4730;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ArmyManager extends Manager
{
    
	ArrayList<UnitController> units = new ArrayList<UnitController>();
	ArrayList<BuildingUnitController> buildings = new ArrayList<BuildingUnitController>();
	
	private int numScouts;
	private int wantedScouts;
	
    private enum GOAL
    {
        Explore, Attack, Defend
    };
    
    private LinkedHashMap<GOAL, Float> goals;
    
    public ArmyManager()
    {
        goals.put( GOAL.Explore, 0.8f );
        goals.put( GOAL.Attack, 0.0f );
        goals.put( GOAL.Defend, 0.2f );
        
        numScouts = 0;
        wantedScouts = 0;
    }
    
    @Override
    public void update( AIController ai )
    {
        //build new units and do militaristic activities
    }
    
    @Override
    public void assignUnits( AIController ai )
    {
        //give it all military units including scouts
        //give it buildings that produce military units
    	
    	for(UnitController u : ai.freeUnits)
    	{
    		if(u.getClass() == ArmyUnitController.class){
    			units.add(u);
    		}
    		else if(u.getClass() == BuildingUnitController.class){
    			BuildingUnitController bu = (BuildingUnitController) u;
    			if(!bu.isStockpile){units.add(u);}
    		}
    		else if(wantedScouts > numScouts)
    		{
    			if(u.getClass() == WorkerUnitController.class)
    			{
    				units.add(u);
    			}
    		}
    	}
    }
    
}
