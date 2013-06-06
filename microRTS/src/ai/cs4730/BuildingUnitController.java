
package ai.cs4730;

import java.util.ArrayList;

import rts.units.Unit;

public class BuildingUnitController extends UnitController
{
    
    public boolean            isStockpile;
    public ArrayList<Integer> produces;
    
    public BuildingUnitController( Unit unit, AIController ai )
    {
        super( unit, ai );
        isStockpile = unit.isStockpile();
        produces = unit.getProduce();
    }
    
    public boolean isStockpile(){return isStockpile;}
    public ArrayList<Integer> getProduces(){return produces;}
}
