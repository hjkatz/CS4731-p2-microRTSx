package ai.cs4730;

import rts.units.Unit;

public class BuilderUnitController extends UnitController{
   private int        buildSpeed;
   private int        attackRange;
   private int        attackMin;
   private int        attackMax;
   private int        moveSpeed;
   private int        attackSpeed;
   private boolean    free;
   private WantedUnit building;

   public BuilderUnitController(Unit unit, AIController ai){
      super(unit, ai);
      buildSpeed = unit.getBuildSpeed();
      attackRange = unit.getAttackRange();
      attackMin = unit.getAttackMin();
      attackMax = unit.getAttackMax();
      attackSpeed = unit.getAttackSpeed();
      moveSpeed = unit.getMoveSpeed();
      free = false;
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

   public WantedUnit getBuilding(){
      return building;
   }

   public void setBuilding(WantedUnit building){
      this.building = building;
   }

   public boolean isFree(){
      return free;
   }

   public void setFree(boolean free){
      this.free = free;
   }
}
