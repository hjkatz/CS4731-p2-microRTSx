
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
                                                   
    private ArrayList<Unit>         workers;
    private ArrayList<Unit>         buildings;
    private ArrayList<Unit>         stockpiles;
    
    public TownManager()
    {
        buildPriority = new HashMap<String, Integer>();
        buildPriority.put( "Worker", 50 );
        
        workers = new ArrayList<Unit>();
        buildings = new ArrayList<Unit>();
        stockpiles = new ArrayList<Unit>();
    }
    
    @Override
    public void update( AIController ai )
    {
        //resource gathering and building construction
        
        for ( Unit worker : workers )
        {
            worker.setAction( new UnitAction( worker, UnitAction.MOVE, worker.getX() + 1, worker.getY(), -1 ) );
        }
        
    }
    
    @Override
    public void assignUnits( AIController ai )
    {
        //Grab my units!!!
        for ( Unit unit : ai.freeUnits )
        {
            if ( unit.isWorker() )
            {
                workers.add( unit );
            }
            else if ( unit.isBuilding() )
            {
                if ( unit.isStockpile() )
                {
                    stockpiles.add( unit );
                }
                else
                {
                    buildings.add( unit );
                }
            }
        }
        
        //give it the workers
        //give it any buildings that deal with workers / new buildings / non-military stuff
    }
    
    @Override
    public void assignResources( AIController ai )
    {
        //give it a certain amount of the resources
    }
}
