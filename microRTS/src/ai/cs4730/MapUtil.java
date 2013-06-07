
package ai.cs4730;

import java.util.ArrayList;
import java.util.HashMap;

import rts.GameState;
import rts.units.Unit;

public class MapUtil
{
    
    public AIController      ai;
    public static int[]      map;
    public static int        WIDTH;
    public static int        HEIGHT;
    public static TrafficMap trafficMap;
    
    public MapUtil( AIController ai )
    {
        this.ai = ai;
        map = ai.gameState.getMap();
        WIDTH = ai.gameState.getMapWidth();
        HEIGHT = ai.gameState.getMapHeight();
        trafficMap = new TrafficMap( map.length );
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
    private static boolean can_enter( Unit unit, int location, int turn_start, int turn_end )
    {
        if ( ( map[location] & ( GameState.MAP_NEUTRAL | GameState.MAP_NONPLAYER ) ) == 0 && ( ( map[location] & GameState.MAP_WALL ) == 0 || unit.isFlying() )
                && trafficMap.valid( location, turn_start, turn_end ) )
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
    private static int h_score( int start, ArrayList<Integer> goals )
    {
        int h = -1;
        for ( int i = 0; i < goals.size(); i++ )
        {
            int dx = goals.get( i ) % WIDTH - start % WIDTH;
            int dy = goals.get( i ) / WIDTH - start / WIDTH;
            int dh = dx * dx + dy * dy;
            if ( h == -1 || dh < h )
            {
                h = dh;
            }
        }
        return h;
    }
    
    /**
     * Gets the path to a location (reversed)
     * 
     * @param unit
     * @param destinations
     */
    public static ArrayList<Integer[]> get_path( Unit unit, int start, int turn_start, ArrayList<Integer> destinations )
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
            int cx = current % WIDTH;
            int cy = current / WIDTH;
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
            if ( cx < WIDTH - 1 && ( destinations.contains( next ) || ( !closed.contains( next ) && can_enter( unit, next, time, end ) ) ) )
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
            next = current - WIDTH;
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
            next = current + WIDTH;
            if ( cy < HEIGHT - 1 && ( destinations.contains( next ) || ( !closed.contains( next ) && can_enter( unit, next, time, end ) ) ) )
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
    
}