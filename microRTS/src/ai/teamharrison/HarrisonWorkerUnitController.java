package ai.teamharrison;

import rts.units.Unit;

public class HarrisonWorkerUnitController extends HarrisonUnitController{
   private int                        buildSpeed;
   private int                        attackRange;
   private int                        attackMin;
   private int                        attackMax;
   private int                        moveSpeed;
   private int                        attackSpeed;
   private boolean                    hasFarm;
   private HarrisonFarmUnitController farm;

   public HarrisonWorkerUnitController(Unit unit, HarrisonAIController ai){
      super(unit, ai);
      buildSpeed = unit.getBuildSpeed();
      attackRange = unit.getAttackRange();
      attackMin = unit.getAttackMin();
      attackMax = unit.getAttackMax();
      attackSpeed = unit.getAttackSpeed();
      moveSpeed = unit.getMoveSpeed();
      hasFarm = false;
      farm = null;
   }

   public void setFarm(HarrisonFarmUnitController f){
      farm = f;
      hasFarm = true;
   }

   public void unsetFarm(){
      farm = null;
      hasFarm = false;
   }

   public void freeFarm(){
      if(farm != null){
         farm.freeUp();
      }
   }

   public boolean hasFarm(){
      return hasFarm;
   }

   public HarrisonFarmUnitController getFarm(){
      return farm;
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

   @Override public void death(){
      super.death();
      freeFarm();
   }

}
