package ai.cs4730;

import java.util.ArrayList;

public class UnitAssigner extends Manager{

   AIController ai;

   public UnitAssigner(AIController ai){
      this.ai = ai;
   }

   public void update(){
      ArrayList<UnitController> toRemove = new ArrayList<UnitController>();
      for(UnitController u : ai.freeUnits){
         if(u.getClass() == WorkerUnitController.class){
            if(ai.workers.size() < ai.wantedWorkers){
               ai.workers.add((WorkerUnitController) u);
               toRemove.add(u);
            }
            else
               if(ai.scouts.size() < ai.wantedScouts){
                  ai.scouts.add(u);
                  toRemove.add(u);
               }
         }
         else
            if(u.getClass() == BuildingUnitController.class){
               BuildingUnitController bu = (BuildingUnitController) u;
               if(bu.isStockpile()){
                  ai.stockpiles.add(bu);
               }
               else{
                  ai.buildings.add(bu);
               }
               toRemove.add(u);
            }
            else
               if(u.getClass() == ArmyUnitController.class){
                  ai.groundUnits.add((ArmyUnitController) u);
                  toRemove.add(u);
               }

         if(ai.wantedScouts > ai.scouts.size() && u.getClass() == WorkerUnitController.class){
            ai.scouts.add(u);
            toRemove.add(u);
         }
      }

      // remove any units from freeUnits that were assigend
      for(UnitController u : toRemove){
         ai.freeUnits.remove(u);
         ai.notFreeUnits.add(u);
      }
   }

}
