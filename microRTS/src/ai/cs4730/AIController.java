
package ai.cs4730;

import ai.AI;
import ai.general.TrafficMap;
import rts.GameState;
import rts.units.Unit;
import rts.units.UnitAction;

import java.util.ArrayList;

public class AIController extends AI
{
    
    private boolean           init = false;
    private int               currentTurn;
    
    public GameState          gameState;
    public ArrayList<Integer> resources;
    public int[]              map;
    public int                WIDTH;
    public int                HEIGHT;
    
    private TownManager       townManager;
    private ArmyManager       armyManager;
    
    public ArrayList<UnitController>    freeUnits;
    
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
        for(Unit u : gameState.getMyUnits())
        {
        	if(u.isWorker()){freeUnits.add(new WorkerUnitController(u));}
        	else if(u.isBuilding()){freeUnits.add(new BuildingUnitController(u));}
        	else {freeUnits.add(new ArmyUnitController(u));}
        }
        
        freeUnits = state.getMyUnits();
        
        map = state.getMap();
        WIDTH = state.getMapWidth();
        HEIGHT = state.getMapHeight();
        
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
        
        gs.getMyUnits().get( 0 ).setAction( new UnitAction( gs.getMyUnits().get( 0 ), UnitAction.MOVE, gs.getMyUnits().get( 0 ).getX() + 1, gs.getMyUnits().get( 0 ).getY(), -1 ) );
        
        currentTurn++;
        
        armyManager.assignUnits(this);
        townManager.assignUnits(this);
        
        armyManager.update(this);
        townManager.update(this);
    }
    
}
