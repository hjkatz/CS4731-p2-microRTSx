package ai.teamharrison;

import rts.units.Unit;

public class HarrisonArmyUnitController extends HarrisonUnitController{

   private boolean isFlying;
   private int     attackRange;
   private int     attackMin;
   private int     attackMax;
   private int     moveSpeed;
   private int     attackSpeed;

   public HarrisonArmyUnitController(Unit unit, HarrisonAIController ai){
      super(unit, ai);
      isFlying = unit.isFlying();

      attackRange = unit.getAttackRange();
      attackMin = unit.getAttackMin();
      attackMax = unit.getAttackMax();
      attackSpeed = unit.getAttackSpeed();
      moveSpeed = unit.getMoveSpeed();
   }

   public boolean isFlying(){
      return isFlying;
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
}
