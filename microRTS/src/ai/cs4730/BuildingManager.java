package ai.cs4730;

import java.util.ArrayList;

import rts.units.UnitAction;

public class BuildingManager extends Manager{

   private AIController ai;

   public BuildingManager(AIController ai){
      this.ai = ai;
   }

   public static void changeBuildLocation(UnitController unitController){}

   @Override public void update(){
      for(BuildingUnitController stock : ai.stockpiles){// all the bases, tell em to make workers
         stock.act(ai);

         if(ai.workers.size() + ai.scouts.size() < ai.wantedScouts + ai.wantedWorkers){// do we want scouts?
            // check if we have the cash
            boolean enoughMoney = canAffordUnit(AIController.WORKER);
            if(enoughMoney){
               if(stock.actions.size() <= 0){ // no actions?!?!?
                  int time = ai.currentTurn;
                  int position = stock.getY() * MapUtil.WIDTH + stock.getX() + 1;
                  stock.addAction(new UnitAction(stock.unit, UnitAction.BUILD, stock.getX(), stock.getY() + 1, AIController.WORKER), MapUtil.trafficMap, position, time, time + ai.unitTypes.get(AIController.WORKER).produce_speed);
                  // add no actions for the rest of the build time so it doesnt keep giving it build orders each turn
                  for(int i = 0; i < ai.unitTypes.get(AIController.WORKER).produce_speed - 1; i++){
                     stock.addAction(new UnitAction(stock.unit, UnitAction.NONE, stock.getX(), stock.getY(), -1), MapUtil.trafficMap, position, time, time + 1);
                  }
                  makePurchase(ai.unitTypes.get(AIController.WORKER).cost);
                  if(AIController.DEBUG){
                     System.out.println("TM: recruiting worker");
                  }
               }
            }
         }
      }
   }

   public boolean canAffordUnit(int type){
      for(int i = 0; i < ai.resources.size(); i++){
         int cost = ai.unitTypes.get(type).cost.get(i);
         if(cost > ai.resources.get(i)){ return false; }
      }
      return true;
   }

   public boolean canAffordBuilding(int type){
      for(int i = 0; i < ai.resources.size(); i++){
         int cost = ai.buildingTypes.get(type).cost.get(i);
         if(cost > ai.resources.get(i)){ return false; }
      }
      return true;
   }

   public void makePurchase(ArrayList<Integer> cost){
      for(int i = 0; i < cost.size(); i++){
         int newAmount = ai.resources.get(i) - cost.get(i);
         ai.resources.set(i, newAmount);
      }
   }

}
