
package ai.cs4730;

import rts.units.Unit;
import rts.units.UnitAction;

import java.util.ArrayList;
import java.util.HashMap;

public class TownManager extends Manager
{
    public static final int                  STOCKPILE     = 0;
    public static final int                  SOLDIEROFFICE = 1;
    public static final int                  AIRPORT       = 2;
    public HashMap<Integer, Integer>         buildPriority;    //Label and Priority, Bigger Priority == More likely to build
    public HashMap<Integer, Integer>         unitBuildPriority;
    // (Use order of 1 - 100) Every time a unit is made its
    // priority will drop by 1
    public ArrayList<FarmUnitController>     farms;
    public ArrayList<WorkerUnitController>   workers;
    public ArrayList<BuildingUnitController> stockpiles;
    public ArrayList<BuildingUnitController> buildings;
    public ArrayList<Integer>                requestedUnits;
    private ArrayList<Unit>                  _farms;           //backing list of farms as units
                                                                
    public TownManager()
    {
        buildPriority = new HashMap<Integer, Integer>();
        
        farms = new ArrayList<FarmUnitController>();
        _farms = new ArrayList<Unit>();
        workers = new ArrayList<WorkerUnitController>();
        buildings = new ArrayList<BuildingUnitController>();
        stockpiles = new ArrayList<BuildingUnitController>();
        
        requestedUnits = new ArrayList<Integer>();
    }
    
    public static void changeBuildLocation( UnitController unitController, AIController ai )
    {
    }
    
    @Override
    public void update( AIController ai )
    {
        // update farms map
        for ( Unit res : ai.gameState.getNeutralUnits() )
        {
            if ( res.isResources() && !_farms.contains( res ) )
            {
                farms.add( new FarmUnitController( res, ai ) );
                _farms.add( res );
            }
        }
        
        for ( WorkerUnitController worker : workers )
        {
            worker.act( ai );
            
            if ( worker.actions.size() <= 0 ) //no actions?!?!?
            {
                FarmUnitController farm = getClosestFreeFarm( worker );
                if ( farm != null )
                {
                    ArrayList<Integer> openings = new ArrayList<Integer>();
                    openings.add( farm.getHarvestY() * MapUtil.WIDTH + farm.getHarvestX() );
                    
                    ArrayList<Integer[]> rpath = MapUtil.get_path( worker.unit, worker.getY() * MapUtil.WIDTH + worker.getX(), ai.currentTurn, openings );
                    
                    int time = ai.currentTurn;
                    int position = worker.getY() * MapUtil.WIDTH + worker.getX();
                    
                    if ( rpath != null )
                    { // is possible to reach goal
                        boolean there = false;
                        position = rpath.get( 0 )[0];
                        if ( rpath.size() == 0 )
                        {
                            rpath.add( new Integer[]{ worker.unit.getX() + worker.unit.getY() * MapUtil.WIDTH, ai.currentTurn } );
                            there = true;
                        }
                        
                        // set order queue
                        if ( !there )
                        {
                            for ( int i = rpath.size() - 1; i >= 0; i-- )
                            {
                                worker.addAction( new UnitAction( worker.unit, UnitAction.MOVE, rpath.get( i )[0] % MapUtil.WIDTH, rpath.get( i )[0] / MapUtil.WIDTH, -1 ), MapUtil.trafficMap,
                                        rpath.get( i )[0], rpath.get( i )[1], rpath.get( i )[1] + worker.unit.getMoveSpeed() );
                            }
                        }
                        position = rpath.get( 0 )[0];
                        time = rpath.get( 0 )[1];
                    }
                    
                    //harvest
                    worker.addAction( new UnitAction( worker.unit, UnitAction.HARVEST, farm.getX(), farm.getY(), -1 ), MapUtil.trafficMap, position, time, time + farm.getHarvestSpeed() );
                    time += farm.getHarvestSpeed();
                    
                    ArrayList<Integer> destination = new ArrayList<Integer>();
                    destination.add( stockpiles.get( 0 ).getY() * MapUtil.WIDTH + stockpiles.get( 0 ).getX() );
                    
                    //return
                    rpath = MapUtil.get_path( worker.unit, position, time, destination );
                    if ( rpath != null )
                    {
                        boolean there = false;
                        if ( rpath.size() <= 1 )
                        {
                            there = true;
                            rpath.add( new Integer[]{ position, time } );
                            if ( rpath.size() == 1 )
                            {
                                rpath.add( new Integer[]{ position, time } );
                            }
                        }
                        if ( !there )
                        {
                            for ( int i = rpath.size() - 1; i >= 1; i-- )
                            {
                                //unit.actions.add(new UnitAction(worker.unit, UnitAction.MOVE, rpath.get(i)[0]%MapUtils.WIDTH, rpath.get(i)[0]/MapUtils.WIDTH,-1));
                                //System.out.println("adding MOVE");
                                worker.addAction( new UnitAction( worker.unit, UnitAction.MOVE, rpath.get( i )[0] % MapUtil.WIDTH, rpath.get( i )[0] / MapUtil.WIDTH, -1 ), MapUtil.trafficMap,
                                        rpath.get( i )[0], rpath.get( i )[1], rpath.get( i )[1] + worker.unit.getMoveSpeed() );
                            }
                            position = rpath.get( 0 )[0];
                            time = rpath.get( 0 )[1];
                        }
                        worker.addAction( new UnitAction( worker.unit, UnitAction.RETURN, rpath.get( 0 )[0] % MapUtil.WIDTH, rpath.get( 0 )[0] / MapUtil.WIDTH, -1 ), MapUtil.trafficMap, position,
                                time, time + UnitAction.DEFAULT_COOLDOWN );
                    }
                }
            }
        }
        
        //        for ( BuildingUnitController stock : stockpiles )
        //        {
        //            stock.setAction( new UnitAction( stock.unit, UnitAction.BUILD, stock.getX() + 1, stock.getY() + 1, stock.getProduce().get( 0 ) ) );
        //        }
        
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
                int distance = ( int ) ( Math.sqrt( ( ( x - farm.getHarvestX() ) ^ 2 ) + ( ( y - farm.getHarvestY() ) ^ 2 ) ) );
                if ( distance < minDistance )
                {
                    closest = farm;
                    minDistance = distance;
                }
            }
        }
        
        //        if ( closest != null )
        //        {
        //            closest.free = false;
        //        }
        return closest;
    }
    
    @Override
    public void assignUnits( AIController ai )
    {
        //Grab my units!!!
        ArrayList<UnitController> toRemove = new ArrayList<UnitController>();
        for ( UnitController unit : ai.freeUnits )
        {
            if ( unit.getClass() == WorkerUnitController.class )
            {
                workers.add( ( WorkerUnitController ) unit );
                if ( AIController.DEBUG )
                {
                    System.out.println( "acquired worker" );
                }
                toRemove.add( unit );
            }
            else if ( unit.getClass() == BuildingUnitController.class )
            {
                BuildingUnitController bu = ( BuildingUnitController ) unit;
                if ( bu.isStockpile() )
                {
                    stockpiles.add( bu );
                    if ( AIController.DEBUG )
                    {
                        System.out.println( "acquired stockpile" );
                    }
                }
                else
                {
                    buildings.add( bu );
                    if ( AIController.DEBUG )
                    {
                        System.out.println( "acquired building" );
                    }
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
}
