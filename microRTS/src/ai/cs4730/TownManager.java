
package ai.cs4730;

import java.util.ArrayList;
import java.util.HashMap;

import rts.units.Unit;
import rts.units.UnitAction;

public class TownManager extends Manager
{
    public HashMap<Integer, Integer>          buildPriority;
    // Production Id and Priority, Bigger Priority == More likely to build
    // (Use order of 1 - 100) Every time a unit is made its 
    // priority will drop by 1
    public ArrayList<FarmUnitController>      farms;            // resource patches
                                                                 
    private ArrayList<WorkerUnitController>   workers;
    private ArrayList<BuildingUnitController> stockpiles;
    
    public static final int                   STOCKPILE     = 0;
    public static final int                   SOLDIEROFFICE = 1;
    public static final int                   AIRPORT       = 2;
    
    public TownManager()
    {
        buildPriority = new HashMap<Integer, Integer>();
        
        workers = new ArrayList<WorkerUnitController>();
        stockpiles = new ArrayList<BuildingUnitController>();
        farms = new ArrayList<FarmUnitController>();
    }
    
    @Override
    public void update( AIController ai )
    {
        // update farms map
        for ( Unit res : ai.state.getNeutralUnits() )
        {
            if ( !farms.contains( res ) )
            {
                farms.add( new FarmUnitController( res, ai ) );
            }
        }
        
        for ( WorkerUnitController worker : workers )
        {
            if ( worker.actions.size() <= 0 ) //no actions?!?!?
            {
                for ( FarmUnitController farm : farms )
                {
                    if ( farm.free )
                    {
                        worker.actions.add( new UnitAction( worker.unit, UnitAction.MOVE, farm.harvestX, farm.harvestY, -1 ) );
                        worker.actions.add( new UnitAction( worker.unit, UnitAction.HARVEST, farm.unit.getX(), farm.unit.getY(), -1 ) );
                        worker.actions.add( new UnitAction( worker.unit, UnitAction.MOVE, stockpiles.get( 0 ).unit.getX(), stockpiles.get( 0 ).unit.getY() - 1, -1 ) );
                        worker.actions.add( new UnitAction( worker.unit, UnitAction.RETURN, stockpiles.get( 0 ).unit.getX(), stockpiles.get( 0 ).unit.getY(), -1 ) );
                    }
                }
            }
            
            if ( !worker.unit.hasAction() )
            {
                worker.unit.setAction( worker.actions.get( 0 ) );
                worker.actions.remove( 0 );
            }
        }
        
        for ( BuildingUnitController stock : stockpiles )
        {
            stock.unit.setAction( new UnitAction( stock.unit, UnitAction.BUILD, stock.unit.getX() + 1, stock.unit.getY() + 1, stock.unit.getProduce().get( 0 ) ) );
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
                workers.add( new WorkerUnitController( unit, ai ) );
            }
            else if ( unit.isStockpile() )
            {
                stockpiles.add( new BuildingUnitController( unit, ai ) );
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
