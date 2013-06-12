package ai.teamharrison;

import java.util.ArrayList;

import rts.units.Unit;

public class HarrisonUnitAssigner extends HarrisonManager{

   HarrisonAIController ai;

   public HarrisonUnitAssigner(HarrisonAIController ai){
      this.ai = ai;
   }

   public void update(){
      // update farms with new ones
      for(Unit u : ai.gameState.getNeutralUnits()){
         if(u.isResources()){
            HarrisonFarmUnitController fc = new HarrisonFarmUnitController(u, ai);
            if(!ai.farms.contains(fc)){
               ai.farms.add(new HarrisonFarmUnitController(u, ai));
            }
         }
      }

      // remove farms if they are out of resources
      ArrayList<HarrisonFarmUnitController> toRemove = new ArrayList<HarrisonFarmUnitController>();
      for(HarrisonFarmUnitController farm : ai.farms){
         if(farm.getResourceAmount() <= 0){
            farm.freeUp();
            toRemove.add(farm);
         }
      }
      for(HarrisonFarmUnitController farm : toRemove){
         ai.farms.remove(farm);
      }

      //adjust number of wanted workers
      ai.wantedWorkers = (int) (ai.farms.size() * .8);
      
      //remove dead units
      for(HarrisonUnitController uc : ai.deadUnits){
         if(ai.farmers.contains(uc)){
            ai.farmers.remove(uc);
         }
         if(ai.builders.contains(uc)){
            ai.builders.remove(uc);
         }
         if(ai.buildings.contains(uc)){
            ai.buildings.remove(uc);
         }
         if(ai.armyUnits.contains(uc)){
            ai.armyUnits.remove(uc);
         }
         if(ai.scouts.contains(uc)){
            ai.scouts.remove(uc);
         }
         if(ai.stockpiles.contains(uc)){
            ai.stockpiles.remove(uc);
         }
         if(ai.notFreeUnits.contains(uc)){
            ai.notFreeUnits.remove(uc);
         }
      }

      // grab any new units produced and add them to notFreeUnits as well as their respective list
      for(Unit u : ai.gameState.getMyUnits()){
         HarrisonUnitController uc = new HarrisonUnitController(u, ai);
         if(!ai.notFreeUnits.contains(uc)){
            if(u.isBuilding()){
               HarrisonBuildingUnitController bc = new HarrisonBuildingUnitController(u, ai);
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
                  HarrisonUnitController wc = new HarrisonWorkerUnitController(u, ai);
                  if(ai.scouts.size() < ai.wantedScouts){
                     ai.scouts.add(wc);
                     ai.notFreeUnits.add(wc);
                  }
                  else
                     if(ai.farmers.size() < ai.wantedWorkers){
                        ai.farmers.add((HarrisonWorkerUnitController) wc);
                        ai.notFreeUnits.add(wc);
                     }
                     else{
                        ai.builders.add(new HarrisonBuilderUnitController(wc.unit, ai));
                        ai.notFreeUnits.add(wc);
                     }
               }
               else{
                  HarrisonArmyUnitController ac = new HarrisonArmyUnitController(u, ai);
                  ai.armyUnits.add(ac);
                  ai.notFreeUnits.add(ac);
               }
         }
      }
      
      //mark buildings as completed
      for(HarrisonWantedUnit u : HarrisonUnitQueue.wantedBuildingUnits){
         if(u.beingBuilt){
            boolean flag = false;
            for(HarrisonBuilderUnitController builder : ai.builders){
               if(builder.getBuilding() == u){
                  flag = true;
               }
            }
            if(!flag){
               u.beingBuilt = false;
            }
         }
      }
      
      //reset each time, quicker than checking their health, which also didnt seem to work
      ai.enemyUnits = new ArrayList<HarrisonUnitController>();
      ai.enemyBuildings = new ArrayList<HarrisonBuildingUnitController>();
      for(Unit unit : ai.gameState.getOtherUnits()){
         if(unit.isBuilding()){
               ai.enemyBuildings.add(new HarrisonBuildingUnitController(unit, ai));
               ai.foundEnemyBase = true;
         }
         else{
            ai.enemyUnits.add(new HarrisonUnitController(unit, ai));
         }
      }
   }
}
