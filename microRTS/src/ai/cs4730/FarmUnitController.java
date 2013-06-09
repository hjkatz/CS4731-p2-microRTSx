package ai.cs4730;

import rts.GameState;
import rts.units.Unit;

/** Created By: harrison on Date: 6/5/13 */
public class FarmUnitController extends UnitController{
   private int                  x;
   private int                  y;
   private int                  harvestSpeed;
   private int                  harvestAmount;
   private int                  resourceType;
   private boolean              free;
   private WorkerUnitController worker;

   public FarmUnitController(Unit unit, AIController ai){
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
      worker.unsetFarm();
      worker = null;
      free = true;
   }

   public void setWorker(WorkerUnitController w){
      worker = w;
      free = false;
   }

   public WorkerUnitController getWorker(){
      return worker;
   }

   public boolean isFree(WorkerUnitController w){
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
}
