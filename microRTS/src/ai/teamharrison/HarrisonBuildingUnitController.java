package ai.teamharrison;

import java.util.ArrayList;

import rts.units.Unit;

public class HarrisonBuildingUnitController extends HarrisonUnitController{

   private boolean            isStockpile;
   private ArrayList<Integer> produces;
   private boolean            isFree = true;
   private HarrisonWantedUnit wanted = null;

   public HarrisonBuildingUnitController(Unit unit, HarrisonAIController ai){
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

   public HarrisonWantedUnit getWanted(){
      return wanted;
   }

   public void setWanted(HarrisonWantedUnit wanted){
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
