package ai.teamharrison;

import java.util.ArrayList;

import rts.units.UnitAction;

public class HarrisonBuildingManager extends HarrisonManager{

   private HarrisonAIController ai;

   public HarrisonBuildingManager(HarrisonAIController ai){
      this.ai = ai;
   }

   @Override public void update(){
      for(HarrisonBuildingUnitController stock : ai.stockpiles){// all the bases, tell em to make workers
         stock.act();

         if(stock.actions.size() <= 0){ // no actions?!?!?
            if(ai.farmers.size() + ai.scouts.size() + ai.builders.size() < ai.wantedScouts + ai.wantedWorkers + ai.wantedBuilders){// do we want scouts?
               if(canAffordUnit(HarrisonAIController.WORKER)){ // check if we have the cash
                  if(ai.workerManager.nextFreeFarm() != null){
                     buildUnit(stock, HarrisonAIController.WORKER);
                  }
               }
            }
         }
      }

      for(HarrisonBuildingUnitController b : ai.buildings){// all the bases, tell em to make workers
         b.act();

         if(b.actions.size() <= 0){ // no actions?!?!?
            HarrisonWantedUnit w = getNextUnit(b);
            if(w != null){
               if(!w.beingBuilt && b.getProduce().contains(w.unitType)){
                  if(canAffordUnit(w.unitType)){
                     buildUnit(b, w.unitType);
                     b.setFree(false);
                     b.setWanted(w);
                     w.beingBuilt = true;
                     makePurchase(ai.unitTypes.get(w.unitType).cost);
                  }
               }
            }
         }
      }
   }

   public HarrisonWantedUnit getNextUnit(HarrisonBuildingUnitController b){
      for(HarrisonWantedUnit w : HarrisonUnitQueue.wantedArmyUnits){
         if(!w.beingBuilt && b.getProduce().contains(w.unitType)){ return w; }
      }

      return null;
   }

   public void buildUnit(HarrisonBuildingUnitController building, int unitType){
      int time = ai.currentTurn;
      int position = HarrisonMapUtil.position(building) + 1;
      building.addAction(new UnitAction(building.unit, UnitAction.BUILD, building.getX(), building.getY() + 1, unitType), HarrisonMapUtil.trafficMap, position, time, time + ai.unitTypes.get(unitType).produce_speed);
      // add no actions for the rest of the build time so it doesnt keep giving it build orders each turn
      for(int i = 0; i < ai.unitTypes.get(unitType).produce_speed - 1; i++){
         building.addAction(new UnitAction(building.unit, UnitAction.NONE, building.getX(), building.getY(), -1), HarrisonMapUtil.trafficMap, position, time, time + 1);
         time++;
      }
      makePurchase(ai.unitTypes.get(HarrisonAIController.WORKER).cost);
   }

   public void makePurchase(ArrayList<Integer> cost){
      for(int i = 0; i < cost.size(); i++){
         int newAmount = ai.resources.get(i) - cost.get(i);
         ai.resources.set(i, newAmount);
      }
   }

   public boolean canAffordUnit(int type){
      for(int i = 0; i < ai.resources.size(); i++){
         int cost = ai.unitTypes.get(type).cost.get(i);
         if(cost > ai.resources.get(i)){ return false; }
      }
      return true;
   }

   public void buildBuilding(HarrisonWorkerUnitController bc, int buildingType){

   }

   public boolean canAffordBuilding(int type){
      for(int i = 0; i < ai.resources.size(); i++){
         int cost = ai.buildingTypes.get(type).cost.get(i);
         if(cost > ai.resources.get(i)){ return false; }
      }
      return true;
   }

}
