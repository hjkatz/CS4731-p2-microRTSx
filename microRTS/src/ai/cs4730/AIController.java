
package ai.cs4730;

import java.util.ArrayList;
import java.util.HashMap;

import rts.GameState;
import rts.units.Unit;
import ai.AI;

public class AIController extends AI
{
    public final static boolean      DEBUG = true;
    
    private boolean                  init  = false;
    public int                       currentTurn;
    
    public GameState                 gameState;
    
    public TownManager               townManager;
    public ArmyManager               armyManager;
    
    public ArrayList<UnitController> freeUnits;
    public MapUtil                   map;
    
    private enum STATE
    {
        Open, Midgame, Close
    };
    
    private STATE state;
    
    public AIController()
    {
        super();
        currentTurn = 0;
        freeUnits = new ArrayList<UnitController>();
        townManager = new TownManager();
        armyManager = new ArmyManager();
        
        state = STATE.Open;
    }
    
    //things that need to be initialized after the object's init, many rely on state
    public void init()
    {
        for ( Unit u : gameState.getMyUnits() )
        {
            if ( u.isWorker() )
            {
                freeUnits.add( new WorkerUnitController( u, this ) );
            }
            else if ( u.isBuilding() )
            {
                freeUnits.add( new BuildingUnitController( u, this ) );
            }
            else
            {
                freeUnits.add( new ArmyUnitController( u, this ) );
            }
        }
        
        map = new MapUtil( this );
        
        init = true;
        
    }
    
    @Override
    public void getAction( GameState gs, int time_limit )
    {
        gameState = gs;
        if ( !init )
        {
            init();
        }
        
        currentTurn++;
        
        townManager.assignUnits( this );
        armyManager.assignUnits( this );
        
        
        armyManager.update( this );
        townManager.update( this );
    }
}
