
package ai.cs4730;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ArmyManager extends Manager
{
    
	ArrayList<UnitController> units = new ArrayList<UnitController>();
	ArrayList<UnitController> scouts = new ArrayList<UnitController>();
	
	private int wantedScouts;
	
    private enum STATE
    {
        Explore, Attack, Defend
    };
    
    private STATE state;
    
    public ArmyManager()
    {
        state = STATE.Explore;
        
        wantedScouts = 1;
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
    		else if(wantedScouts > scouts.size())
    		{
    			if(u.getClass() == WorkerUnitController.class)
    			{
    				units.add(u);
    			}
    		}
    	}
    }
    
}
