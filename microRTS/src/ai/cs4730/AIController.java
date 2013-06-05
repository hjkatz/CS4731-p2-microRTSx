
package ai.cs4730;

import java.util.ArrayList;

import rts.GameState;
import rts.units.Unit;
import ai.AI;
import ai.general.TrafficMap;

public class AIController extends AI
{
    
    private boolean           init = false;
    private int               currentTurn;
    
    public GameState          state;
    public TrafficMap         trafficMap;
    public ArrayList<Integer> resources;
    public int[]              map;
    public int                WIDTH;
    public int                HEIGHT;
    
    private TownManager       townManager;
    private ArmyManager       armyManager;
    
    public ArrayList<Unit>    freeUnits;
    
    public AIController()
    {
        super();
        currentTurn = 0;
        freeUnits = new ArrayList<Unit>();
        townManager = new TownManager();
    }
    
    //things that need to be initialized after the object's init, many rely on state
    public void init()
    {
        trafficMap = new TrafficMap( state.getMap().length );
        
        freeUnits = state.getMyUnits();
        
        map = state.getMap();
        WIDTH = state.getMapWidth();
        HEIGHT = state.getMapHeight();
        
        init = true;
    }
    
    @Override
    public void getAction( GameState gs, int time_limit )
    {
        state = gs;
        if ( !init )
        {
            init();
        }
        
        currentTurn++;
        trafficMap.update( currentTurn );
        
        townManager.assignUnits( this );
        
        townManager.update( this );
    }
    
}
