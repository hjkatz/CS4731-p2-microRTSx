package ai.teamharrison;

import rts.units.Unit;

public class HarrisonBuilderUnitController extends HarrisonUnitController{
   private int                buildSpeed;
   private int                attackRange;
   private int                attackMin;
   private int                attackMax;
   private int                moveSpeed;
   private int                attackSpeed;
   private boolean            free;
   private HarrisonWantedUnit building;

   public HarrisonBuilderUnitController(Unit unit, HarrisonAIController ai){
      super(unit, ai);
      buildSpeed = unit.getBuildSpeed();
      attackRange = unit.getAttackRange();
      attackMin = unit.getAttackMin();
      attackMax = unit.getAttackMax();
      attackSpeed = unit.getAttackSpeed();
      moveSpeed = unit.getMoveSpeed();
      free = true;
      building = null;
   }

   public int getBuildSpeed(){
      return buildSpeed;
   }

   public int getAttackRange(){
      return attackRange;
   }

   public int getAttackMin(){
      return attackMin;
   }

   public int getAttackMax(){
      return attackMax;
   }

   public int getMoveSpeed(){
      return moveSpeed;
   }

   public int getAttackSpeed(){
      return attackSpeed;
   }

   public HarrisonWantedUnit getBuilding(){
      return building;
   }

   public void setBuilding(HarrisonWantedUnit building){
      this.building = building;
   }

   public boolean isFree(){
      return free;
   }

   public void setFree(boolean free){
      this.free = free;
      if(building != null){
         building = null;
      }
   }

   @Override public void death(){
      super.death();
      if(building != null){
         building.beingBuilt = false;
      }
   }
}
