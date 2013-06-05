
package ai.cs4730;

import java.util.ArrayList;

import rts.units.Unit;
import rts.units.UnitAction;

/**
 * Represents a genreal unit
 * including different unit types and buildings
 */
public abstract class UnitController
{
    
    public Unit                  unit;
    public AIController          ai;
    public ArrayList<UnitAction> actions; // this is a queue
                                          
    public UnitController( Unit unit, AIController ai )
    {
        this.unit = unit;
        this.ai = ai;
        actions = new ArrayList<UnitAction>();
    }
    
}
