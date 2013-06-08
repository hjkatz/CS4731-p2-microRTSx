package ai.cs4730;

import rts.units.Unit;

public class WorkerUnitController extends UnitController{
   private int attackRange;
   private int attackMin;
   private int attackMax;
   private int moveSpeed;
   private int attackSpeed;

   public WorkerUnitController(Unit unit, AIController ai){
      super(unit, ai);
      attackRange = unit.getAttackRange();
      attackMin = unit.getAttackMin();
      attackMax = unit.getAttackMax();
      attackSpeed = unit.getAttackSpeed();
      moveSpeed = unit.getMoveSpeed();
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
