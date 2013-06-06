
package ai.cs4730;

import java.util.ArrayList;

import rts.units.Unit;
import rts.units.UnitAction;
/**
 * Represents a general unit
 * including different unit types and buildings
 */
public abstract class UnitController {

	public Unit 				 unit;
	public AIController          ai;
    public ArrayList<UnitAction> actions; // this is a queue
	
	private int maxHp;
	private int vision;
	private int type;
	private int buildTime;
	private ArrayList<Integer> cost;
                                          
    public UnitController( Unit unit, AIController ai )
    {
        this.unit = unit;
        this.ai = ai;
        actions = new ArrayList<UnitAction>();
        
        maxHp = unit.getMaxHP();
        cost = unit.getCost();
        type = unit.getType();
        buildTime = unit.getBuildSpeed();
    }
    
    public ArrayList<UnitAction> getActions(){return unit.getActions();}
	public UnitAction getAction(){return unit.getAction();}
	public boolean hasAction(){return unit.hasAction();}
	public int getType(){return type;}
	public void setAction(UnitAction act){unit.setAction(act);}
	public boolean lastActionSucceeded(){return unit.lastActionSucceeded();}
	public int getX(){return unit.getX();}
	public int getY(){return unit.getY();}
	public int getHP(){return unit.getHP();}
	public int getMaxHP(){return maxHp;}
	public int getVision(){return unit.getVision();}
	public ArrayList<Integer> getCost(){return cost;}
	public int getBuildTime(){return buildTime;}
	
	public int getCost(int resourceType) {
		if (resourceType >= 0 && resourceType < cost.size()) {
			return cost.get(resourceType);
		}
		return -1;
	}
    
}
