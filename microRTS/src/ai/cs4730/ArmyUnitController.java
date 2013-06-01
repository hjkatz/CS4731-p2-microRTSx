package ai.cs4730;

import rts.units.Unit;

public class ArmyUnitController extends UnitController {

	public boolean isFlying;
	
	public ArmyUnitController(Unit unit) {
		super(unit);
		isFlying = unit.isFlying();
	}

}
