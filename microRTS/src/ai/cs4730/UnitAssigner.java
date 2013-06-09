package ai.cs4730;

import java.util.ArrayList;

import rts.units.Unit;

public class UnitAssigner extends Manager{

   AIController ai;

   public UnitAssigner(AIController ai){
      this.ai = ai;
   }

   public void update(){
      //grab any new units produced and add them to notFreeUnits as well as their respective list
      for(Unit u : ai.gameState.getMyUnits()){
         UnitController uc = new UnitController(u, ai);
         if(!ai.notFreeUnits.contains(uc)){
            if(u.isBuilding()){
               BuildingUnitController bc = new BuildingUnitController(u, ai);
               if(bc.isStockpile()){
                  ai.stockpiles.add(bc);
                  ai.notFreeUnits.add(bc);
               }
               else{
                  ai.buildings.add(bc);
                  ai.notFreeUnits.add(bc);
               }
            }
            else
               if(u.isWorker()){
                  WorkerUnitController wc = new WorkerUnitController(u, ai);
                  if(ai.workers.size() < ai.wantedWorkers){
                     ai.workers.add(wc);
                     ai.notFreeUnits.add(wc);
                  }
                  else
                     if(ai.scouts.size() < ai.wantedScouts){
                        ai.scouts.add((UnitController) wc);
                        ai.notFreeUnits.add(wc);
                     }
               }
               else{
                  ArmyUnitController ac = new ArmyUnitController(u, ai);
                  if(ac.isFlying()){
                     ai.airUnits.add(ac);
                  }
                  else{
                     ai.groundUnits.add(ac);
                  }
                  ai.notFreeUnits.add(ac);
               }
         }
      }
   }

}
