
package ai.cs4730;

import java.util.ArrayList;
import java.util.HashMap;

import rts.units.Unit;
import rts.units.UnitAction;

public class TownManager extends Manager
{
    public HashMap<Integer, Integer>         buildPriority;    //Label and Priority, Bigger Priority == More likely to build
    public HashMap<Integer, Integer>         unitBuildPriority;
    // (Use order of 1 - 100) Every time a unit is made its
    // priority will drop by 1
    public ArrayList<FarmUnitController>     farms;            // int correlating to int[] map location of a resource patch                                                          
    public ArrayList<WorkerUnitController>   workers;
    public ArrayList<BuildingUnitController> stockpiles;
    public ArrayList<BuildingUnitController> buildings;
    
    public ArrayList<Integer>                requestedUnits;
    
    public static final int                  STOCKPILE     = 0;
    public static final int                  SOLDIEROFFICE = 1;
    public static final int                  AIRPORT       = 2;
    
    public TownManager()
    {
        buildPriority = new HashMap<Integer, Integer>();
        
        farms = new ArrayList<FarmUnitController>();
        workers = new ArrayList<WorkerUnitController>();
        buildings = new ArrayList<BuildingUnitController>();
        stockpiles = new ArrayList<BuildingUnitController>();
        
        requestedUnits = new ArrayList<Integer>();
    }
    
    @Override
    public void update( AIController ai )
    {
        // update farms map
        for ( Unit res : ai.gameState.getNeutralUnits() )
        {
            if ( res.isResources() && !farms.contains( res ) )
            {
                farms.add( new FarmUnitController( res, ai ) );
            }
        }
        
        for ( WorkerUnitController worker : workers )
        {
            if ( worker.actions.size() <= 0 ) //no actions?!?!?
            {
                FarmUnitController farm = getClosestFreeFarm( worker );
                if ( farm != null )
                {
                    worker.setAction( new UnitAction( worker.unit, UnitAction.MOVE, worker.getX() + 1, worker.getY(), -1 ) );
                    //                    worker.actions.add( new UnitAction( worker.unit, UnitAction.HARVEST, farm.getX(), farm.getY(), -1 ) );
                    //                    worker.actions.add( new UnitAction( worker.unit, UnitAction.MOVE, stockpiles.get( 0 ).unit.getX(), stockpiles.get( 0 ).unit.getY() - 1, -1 ) );
                    //                    worker.actions.add( new UnitAction( worker.unit, UnitAction.RETURN, stockpiles.get( 0 ).unit.getX(), stockpiles.get( 0 ).unit.getY(), -1 ) );
                }
            }
            
            //            if ( worker.lastActionSucceeded() )
            //            {
            //                worker.setAction( worker.actions.get( 0 ) );
            //                worker.actions.remove( 0 );
            //            }
        }
        
        //        for ( BuildingUnitController stock : stockpiles )
        //        {
        //            stock.setAction( new UnitAction( stock.unit, UnitAction.BUILD, stock.getX() + 1, stock.getY() + 1, stock.getProduce().get( 0 ) ) );
        //        }
    }
    
    @Override
    public void assignUnits( AIController ai )
    {
        //Grab my units!!!
        ArrayList<UnitController> toRemove = new ArrayList<UnitController>();
        for ( UnitController unit : ai.freeUnits )
        {
            if ( unit.unit.isWorker() )
            {
                workers.add( ( WorkerUnitController ) unit );
                toRemove.add( unit );
            }
            else if ( unit.unit.isBuilding() )
            {
                BuildingUnitController bu = ( BuildingUnitController ) unit;
                if ( bu.isStockpile() )
                {
                    stockpiles.add( bu );
                }
                else
                {
                    buildings.add( bu );
                }
                toRemove.add( unit );
            }
        }
        
        //remove any units from freeUnits that were assigned
        for ( UnitController unit : toRemove )
        {
            ai.freeUnits.remove( unit );
        }
    }
    
    public int numWorkers()
    {
        return workers.size();
    }
    
    private FarmUnitController getClosestFreeFarm( WorkerUnitController worker )
    {
        int x = worker.getX();
        int y = worker.getY();
        int minDistance = 10000; //large int =P
        FarmUnitController closest = null;
        for ( FarmUnitController farm : farms )
        {
            if ( farm.free )
            {
                int distance = ( int ) ( Math.sqrt( ( x + farm.getHarvestX() ) ^ 2 + ( y + farm.getHarvestY() ) ^ 2 ) );
                if ( distance < minDistance )
                {
                    closest = farm;
                    minDistance = distance;
                }
            }
        }
        
        if ( closest != null )
        {
            closest.free = false;
        }
        return closest;
    }
}
