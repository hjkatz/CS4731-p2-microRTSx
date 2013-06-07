
package ai.cs4730;

import ai.AI;
import rts.GameState;
import rts.units.Unit;

import java.util.ArrayList;
import java.util.HashMap;

public class AIController extends AI
{
    public final static boolean      DEBUG = true;
    public GameState                 gameState;
    public TownManager               townManager;
    public ArmyManager               armyManager;
    public ArrayList<UnitController> freeUnits;
    public MapUtil                   map;
    public int                       currentTurn;
    public STATE                     state;
    private boolean                  init  = false;
    
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
        
        //        armyManager.update( this );
        townManager.update( this );
    }
    
    /**
     * Gets the path to a location (reversed)
     * 
     * @param unit
     * @param destinations
     */
    public ArrayList<Integer[]> get_path( Unit unit, int start, int turn_start, ArrayList<Integer> destinations )
    {
        // multi-destination A*
        ArrayList<Integer> closed = new ArrayList<Integer>();
        ArrayList<Integer> open = new ArrayList<Integer>();
        open.add( start );
        HashMap<Integer, Integer> came_from = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> g_score = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> f_score = new HashMap<Integer, Integer>();
        g_score.put( start, 0 );
        f_score.put( start, h_score( start, destinations ) );
        
        while ( open.size() > 0 )
        {
            int m = 0;
            int current = open.get( m );
            for ( int i = 1; i < open.size(); i++ )
            {
                if ( f_score.get( open.get( i ) ) < f_score.get( current ) )
                {
                    current = open.get( i );
                    m = i;
                }
            }
            int time = g_score.get( current ) * unit.getMoveSpeed() + turn_start;
            int end = time + unit.getMoveSpeed();
            
            if ( destinations.contains( current ) )
            {
                ArrayList<Integer[]> path = new ArrayList<Integer[]>();
                if ( current == start )
                {
                    return path;
                }
                path.add( new Integer[]{ current, time } );
                while ( came_from.get( current ) != null && came_from.get( current ) != start )
                {
                    current = came_from.get( current );
                    time = g_score.get( current ) * unit.getMoveSpeed() + turn_start;
                    path.add( new Integer[]{ current, time } );
                }
                return path;
            }
            
            open.remove( m );
            closed.add( current );
            int next_g = g_score.get( current ) + 1;
            int cx = current % gameState.getMapWidth();
            int cy = current / gameState.getMapWidth();
            int next = current - 1;
            if ( cx > 0 && ( destinations.contains( next ) || ( !closed.contains( next ) && can_enter( unit, next, time, end ) ) ) )
            { // left exists
                if ( !open.contains( next ) || next_g < g_score.get( next ) )
                {
                    came_from.put( next, current );
                    g_score.put( next, next_g );
                    f_score.put( next, next_g + h_score( next, destinations ) );
                    if ( !open.contains( next ) )
                    {
                        open.add( next );
                    }
                }
            }
            next = current + 1;
            if ( cx < gameState.getMapWidth() - 1 && ( destinations.contains( next ) || ( !closed.contains( next ) && can_enter( unit, next, time, end ) ) ) )
            { // right exists
                if ( !open.contains( next ) || next_g < g_score.get( next ) )
                {
                    came_from.put( next, current );
                    g_score.put( next, next_g );
                    f_score.put( next, next_g + h_score( next, destinations ) );
                    if ( !open.contains( next ) )
                    {
                        open.add( next );
                    }
                }
            }
            next = current - gameState.getMapWidth();
            if ( cy > 0 && ( destinations.contains( next ) || ( !closed.contains( next ) && can_enter( unit, next, time, end ) ) ) )
            { // up exists
                if ( !open.contains( next ) || next_g < g_score.get( next ) )
                {
                    came_from.put( next, current );
                    g_score.put( next, next_g );
                    f_score.put( next, next_g + h_score( next, destinations ) );
                    if ( !open.contains( next ) )
                    {
                        open.add( next );
                    }
                }
            }
            next = current + gameState.getMapWidth();
            if ( cy < gameState.getMapHeight() - 1 && ( destinations.contains( next ) || ( !closed.contains( next ) && can_enter( unit, next, time, end ) ) ) )
            { // down exists
                if ( !open.contains( next ) || next_g < g_score.get( next ) )
                {
                    came_from.put( next, current );
                    g_score.put( next, next_g );
                    f_score.put( next, next_g + h_score( next, destinations ) );
                    if ( !open.contains( next ) )
                    {
                        open.add( next );
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Checks whether or not a unit can enter a location
     * 
     * @param unit
     *            the unit
     * @param location
     *            the location
     * @return whether or not
     */
    private boolean can_enter( Unit unit, int location, int turn_start, int turn_end )
    {
        if ( ( MapUtil.map[location] & ( GameState.MAP_NEUTRAL | GameState.MAP_NONPLAYER ) ) == 0 && ( ( MapUtil.map[location] & GameState.MAP_WALL ) == 0 || unit.isFlying() )
                && MapUtil.trafficMap.valid( location, turn_start, turn_end ) )
        {
            return true;
        }
        return false;
    }
    
    /**
     * Calculates the h from start to (a) goal
     * 
     * @param start
     * @param goals
     * @return
     */
    private int h_score( int start, ArrayList<Integer> goals )
    {
        int h = -1;
        for ( int i = 0; i < goals.size(); i++ )
        {
            int dx = goals.get( i ) % MapUtil.WIDTH - start % MapUtil.WIDTH;
            int dy = goals.get( i ) / MapUtil.WIDTH - start / MapUtil.WIDTH;
            int dh = dx * dx + dy * dy;
            if ( h == -1 || dh < h )
            {
                h = dh;
            }
        }
        return h;
    }
    
    private enum STATE
    {
        Open, Midgame, Close
    }
}
