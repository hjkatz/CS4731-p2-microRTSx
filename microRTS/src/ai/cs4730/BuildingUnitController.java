package ai.cs4730;

import java.util.ArrayList;

import rts.units.Unit;

public class BuildingUnitController extends UnitController{

   private boolean            isStockpile;
   private ArrayList<Integer> produces;
   private boolean            isFree = true;
   private WantedUnit         wanted = null;

   public BuildingUnitController(Unit unit, AIController ai){
      super(unit, ai);
      isStockpile = unit.isStockpile();
      produces = unit.getProduce();
   }

   public boolean isStockpile(){
      return isStockpile;
   }

   public ArrayList<Integer> getProduce(){
      return produces;
   }

   public WantedUnit getWanted(){
      return wanted;
   }

   public void setWanted(WantedUnit wanted){
      this.wanted = wanted;
   }

   public boolean isFree(){
      return isFree;
   }

   public void setFree(boolean isFree){
      this.isFree = isFree;
   }
   
   @Override public void death(){
      super.death();
      wanted.beingBuilt = false;
   }
}
