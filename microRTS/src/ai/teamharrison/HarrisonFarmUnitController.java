package ai.teamharrison;

import rts.units.Unit;

/** Created By: harrison on Date: 6/5/13 */
public class HarrisonFarmUnitController extends HarrisonUnitController{
   private int                          x;
   private int                          y;
   private int                          harvestSpeed;
   private int                          harvestAmount;
   private int                          resourceType;
   private boolean                      free;
   private HarrisonWorkerUnitController worker;

   public HarrisonFarmUnitController(Unit unit, HarrisonAIController ai){
      super(unit, ai);

      y = unit.getY();
      x = unit.getX();
      harvestSpeed = unit.getHarvestSpeed();
      harvestAmount = unit.getHarvestAmount();
      resourceType = unit.getResourcesType();
      free = true;
      worker = null;
   }

   public void freeUp(){
      if(worker != null){
         worker.unsetFarm();
      }
      worker = null;
      free = true;
   }

   public void setWorker(HarrisonWorkerUnitController w){
      worker = w;
      free = false;
   }

   public HarrisonWorkerUnitController getWorker(){
      return worker;
   }

   public boolean isFree(HarrisonWorkerUnitController w){
      if(worker != null){
         if(w.equals(worker)){ return true; }
      }
      return free;
   }

   public boolean isFree(){
      return free;
   }

   public int getHarvestSpeed(){
      return harvestSpeed;
   }

   public int getHarvestAmount(){
      return harvestAmount;
   }

   public int getResourceAmount(){
      return unit.getResources();
   }

   public int getResourceType(){
      return resourceType;
   }

   public int getX(){
      return x;
   }

   public int getY(){
      return y;
   }

   @Override public void death(){
      super.death();
      freeUp();
   }
}
