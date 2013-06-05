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
	
	public UnitController(Unit unit) {
		this.unit = unit;
	}
	
	public ArrayList<UnitAction> getActions(){return unit.getActions();}
	public UnitAction getAction(){return unit.getAction();}
	public int getType(){return unit.getType();}
	public void setAction(UnitAction act){unit.setAction(act);}

}
