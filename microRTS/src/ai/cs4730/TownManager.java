
package ai.cs4730;

import java.util.ArrayList;
import java.util.HashMap;

import rts.units.Unit;
import rts.units.UnitAction;

public class TownManager extends Manager
{
    public HashMap<String, Integer> buildPriority; //Label and Priority, Bigger Priority == More likely to build
                                                   // (Use order of 1 - 100) Every time a unit is made its 
                                                   // priority will drop by 1
    public ArrayList<Integer>       farms;        // int correlating to int[] map location of a resource patch
                                                   
    private ArrayList<WorkerUnitController>         workers;
    private ArrayList<BuildingUnitController> stockpiles;
    
    public TownManager()
    {
        buildPriority = new HashMap<String, Integer>();
        buildPriority.put( "Worker", 50 );
        
        workers = new ArrayList<WorkerUnitController>();
        stockpiles = new ArrayList<BuildingUnitController>();
    }
    
    @Override
    public void update( AIController ai )
    {
        //resource gathering and building construction
        
        for ( WorkerUnitController worker : workers )
        {
            worker.unit.setAction( new UnitAction( worker.unit, UnitAction.MOVE, worker.unit.getX() + 1, worker.unit.getY(), -1 ) );
        }
        
    }
    
    @Override
    public void assignUnits( AIController ai )
    {
        //Grab my units!!!
        for ( UnitController unit : ai.freeUnits )
        {
            if ( unit.getClass() == WorkerUnitController.class )
            {
                workers.add( (WorkerUnitController) unit );
            }
            else if ( unit.getClass() == BuildingUnitController.class )
            {
            	BuildingUnitController bu = (BuildingUnitController) unit;
                if ( bu.isStockpile )
                {
                    stockpiles.add( bu );
                }
            }
        }
        
        //give it the workers
        //give it any buildings that deal with workers / new buildings / non-military stuff
    }

	@Override
	public void requestUnits(AIController ai) {
		// TODO Auto-generated method stub
		
	}
}
