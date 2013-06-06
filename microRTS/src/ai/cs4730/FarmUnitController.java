
package ai.cs4730;

import rts.GameState;
import rts.units.Unit;

/** Created By: harrison on Date: 6/5/13 */
public class FarmUnitController extends UnitController
{
    public boolean free;
    public int     harvestX = -1;
    public int     harvestY = -1;
    
    private int harvestSpeed;
    private int harvestAmount;
    
    public FarmUnitController( Unit unit, AIController ai )
    {
        super( unit, ai );
        
        free = true;
        
        int y = unit.getY();
        int x = unit.getX();
        harvestSpeed = unit.getHarvestSpeed();
        harvestAmount = unit.getHarvestAmount();
        
        if ( y > 0 && ( ai.map[( y - 1 ) * ai.WIDTH + x] & ( GameState.MAP_FOG | GameState.MAP_WALL | GameState.MAP_NEUTRAL ) ) != 0 )
        {
            harvestY = y - 1;
            harvestX = x;
        }
        else if ( y < ai.HEIGHT - 1 && ( ai.map[( y + 1 ) * ai.WIDTH + x] & ( GameState.MAP_FOG | GameState.MAP_WALL | GameState.MAP_NEUTRAL ) ) != 0 )
        {
            harvestY = y + 1;
            harvestX = x;
        }
        else if ( x > 0 && ( ai.map[y * ai.WIDTH + x - 1] & ( GameState.MAP_FOG | GameState.MAP_WALL | GameState.MAP_NEUTRAL ) ) != 0 )
        {
            harvestY = y;
            harvestX = x - 1;
        }
        else if ( x < ai.WIDTH - 1 && ( ai.map[y * ai.WIDTH + x + 1] & ( GameState.MAP_FOG | GameState.MAP_WALL | GameState.MAP_NEUTRAL ) ) != 0 )
        {
            harvestY = y;
            harvestX = x + 1;
        }
    }
    
    public int getHarvestSpeed() {return harvestSpeed;}
    public int getHarvestAmount() {return harvestAmount;}
}
