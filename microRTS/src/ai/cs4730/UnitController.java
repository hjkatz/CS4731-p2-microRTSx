
package ai.cs4730;

import java.util.ArrayList;

import rts.units.Unit;
import rts.units.UnitAction;

/**
 * Represents a general unit
 * including different unit types and buildings
 */
public abstract class UnitController
{
    
    public Unit                  unit;
    public AIController          ai;
    public ArrayList<UnitAction> actions;         // this is a queue
                                                   
    private int                  maxHp;
    private int                  vision;
    private int                  type;
    private int                  buildTime;
    private ArrayList<Integer>   cost;
    private ArrayList<Traffic>   traffic;
    private Traffic              last_traffic;
    private Traffic              building_traffic;
    
    public UnitController( Unit unit, AIController ai )
    {
        this.unit = unit;
        this.ai = ai;
        actions = new ArrayList<UnitAction>();
        
        maxHp = unit.getMaxHP();
        cost = unit.getCost();
        type = unit.getType();
        buildTime = unit.getBuildSpeed();
        traffic = new ArrayList<Traffic>();
    }
    
    public ArrayList<UnitAction> getActions()
    {
        return unit.getActions();
    }
    
    public UnitAction getAction()
    {
        return unit.getAction();
    }
    
    public boolean hasAction()
    {
        return unit.hasAction();
    }
    
    public int getType()
    {
        return type;
    }
    
    public void setAction( UnitAction act )
    {
        unit.setAction( act );
    }
    
    public boolean lastActionSucceeded()
    {
        return unit.lastActionSucceeded();
    }
    
    public int getX()
    {
        return unit.getX();
    }
    
    public int getY()
    {
        return unit.getY();
    }
    
    public int getHP()
    {
        return unit.getHP();
    }
    
    public int getMaxHP()
    {
        return maxHp;
    }
    
    public int getVision()
    {
        return unit.getVision();
    }
    
    public ArrayList<Integer> getCost()
    {
        return cost;
    }
    
    public int getBuildTime()
    {
        return buildTime;
    }
    
    public int getCost( int resourceType )
    {
        if ( resourceType >= 0 && resourceType < cost.size() )
        {
            return cost.get( resourceType );
        }
        return -1;
    }
    
    /**
     * Adds an action to this unit
     * 
     * @param action
     *            the action to add
     * @param traffic_map
     *            the traffic map this unit is in
     * @param location
     *            the traffic location this action corresponds with
     * @param start
     *            the traffic start
     * @param end
     *            the traffic end
     */
    public void addAction( UnitAction action, TrafficMap traffic_map, int location, int start, int end )
    {
        actions.add( action );
        if ( traffic_map != null )
        {
            Traffic t = new Traffic( location, start, end );
            traffic_map.reserve( t );
            traffic.add( t );
        }
    }
    
    /**
     * Clears all actions for this unit
     * 
     * @param traffic_map
     *            the traffic map
     */
    public void clearActions( TrafficMap traffic_map )
    {
        actions.clear();
        for ( int i = 0; i < traffic.size(); i++ )
        {
            traffic_map.unreserve( traffic.get( i ) );
        }
        traffic.clear();
        if ( last_traffic != null )
        {
            traffic_map.unreserve( last_traffic );
            last_traffic = null;
        }
    }
    
    /**
     * Removes the unit
     * 
     * @param traffic_map
     *            the traffic map
     */
    public void remove( TrafficMap traffic_map )
    {
        actions.clear();
        for ( int i = 0; i < traffic.size(); i++ )
        {
            traffic_map.unreserve( traffic.get( i ) );
        }
        traffic.clear();
        if ( building_traffic != null )
        {
            traffic_map.unreserve( building_traffic );
        }
        if ( last_traffic != null )
        {
            traffic_map.unreserve( last_traffic );
            last_traffic = null;
        }
    }
}
