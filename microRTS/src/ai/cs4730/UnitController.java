
package ai.cs4730;

import java.util.ArrayList;

import rts.units.Unit;
import rts.units.UnitAction;
/**
 * Represents a general unit
 * including different unit types and buildings
 */
public abstract class UnitController {

	public Unit unit;
	
	public ArrayList<UnitAction> getActions(){return unit.getActions();}
	public UnitAction getAction(){return unit.getAction();}
	public int getType(){return unit.getType();}
	public void setAction(UnitAction act){unit.setAction(act);}

    public AIController          ai;
    public ArrayList<UnitAction> actions; // this is a queue
                                          
    public UnitController( Unit unit, AIController ai )
    {
        this.unit = unit;
        this.ai = ai;
        actions = new ArrayList<UnitAction>();
    }
    
}
