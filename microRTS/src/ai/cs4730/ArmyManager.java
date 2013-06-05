
package ai.cs4730;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ArmyManager extends Manager
{
    
	ArrayList<UnitController> units = new ArrayList<UnitController>();
	ArrayList<UnitController> scouts = new ArrayList<UnitController>();
	
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

	@Override
	public void requestUnits(AIController ai) {
		// TODO Auto-generated method stub
		
	}
    
}
