
package ai.cs4730;

import rts.GameState;
import rts.units.Unit;

/** Created By: harrison on Date: 6/5/13 */
public class FarmUnitController extends UnitController
{
    public boolean free;
    private int    harvestX = -1;
    private int    harvestY = -1;
    private int    x;
    private int    y;
    
    private int    harvestSpeed;
    private int    harvestAmount;
    
    public FarmUnitController( Unit unit, AIController ai )
    {
        super( unit, ai );
        
        free = true;
        
        y = unit.getY();
        x = unit.getX();
        harvestSpeed = unit.getHarvestSpeed();
        harvestAmount = unit.getHarvestAmount();
        
        if ( y > 0 && ( MapUtil.map[( y - 1 ) * MapUtil.WIDTH + x] & ( GameState.MAP_FOG | GameState.MAP_WALL | GameState.MAP_NEUTRAL ) ) != 0 )
        {
            harvestY = y - 1;
            harvestX = x;
        }
        else if ( y < MapUtil.HEIGHT - 1 && ( MapUtil.map[( y + 1 ) * MapUtil.WIDTH + x] & ( GameState.MAP_FOG | GameState.MAP_WALL | GameState.MAP_NEUTRAL ) ) != 0 )
        {
            harvestY = y + 1;
            harvestX = x;
        }
        else if ( x > 0 && ( MapUtil.map[y * MapUtil.WIDTH + x - 1] & ( GameState.MAP_FOG | GameState.MAP_WALL | GameState.MAP_NEUTRAL ) ) != 0 )
        {
            harvestY = y;
            harvestX = x - 1;
        }
        else if ( x < MapUtil.WIDTH - 1 && ( MapUtil.map[y * MapUtil.WIDTH + x + 1] & ( GameState.MAP_FOG | GameState.MAP_WALL | GameState.MAP_NEUTRAL ) ) != 0 )
        {
            harvestY = y;
            harvestX = x + 1;
        }
    }
    
    public int getHarvestSpeed()
    {
        return harvestSpeed;
    }
    
    public int getHarvestAmount()
    {
        return harvestAmount;
    }
    
    public int getHarvestX()
    {
        return harvestX;
    }
    
    public int getHarvestY()
    {
        return harvestY;
    }
    
    public int getX()
    {
        return x;
    }
    
    public int getY()
    {
        return y;
    }
}
