
package ai.cs4730;

import rts.units.Unit;

public class WorkerUnitController extends UnitController
{
	public int buildSpeed;
	public int attackRange;
    public int attackMin;
    public int attackMax;
    public int moveSpeed;
    public int attackSpeed;
	
    public WorkerUnitController( Unit unit, AIController ai )
    {
        super( unit, ai );
        buildSpeed = unit.getBuildSpeed();
        attackRange = unit.getAttackRange();
        attackMin = unit.getAttackMin();
        attackMax = unit.getAttackMax();
        attackSpeed = unit.getAttackSpeed();
        moveSpeed = unit.getMoveSpeed();
    }
    
    public int getBuildSpeed() {return buildSpeed;}
	public int getAttackRange() {return attackRange;}
	public int getAttackMin() {return attackMin;}
	public int getAttackMax() {return attackMax;}
	public int getMoveSpeed() {return moveSpeed;}
	public int getAttackSpeed() {return attackSpeed;}
    
}
