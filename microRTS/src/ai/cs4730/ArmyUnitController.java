
package ai.cs4730;

import rts.units.Unit;

public class ArmyUnitController extends UnitController
{
    
    public boolean isFlying;
    
    public ArmyUnitController( Unit unit, AIController ai )
    {
        super( unit, ai );
        isFlying = unit.isFlying();
    }
    
}
