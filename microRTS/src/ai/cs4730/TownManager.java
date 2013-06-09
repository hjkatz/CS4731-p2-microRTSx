package ai.cs4730;

import java.util.ArrayList;

import rts.units.Unit;
import rts.units.UnitAction;

public class TownManager extends Manager{

   private AIController ai;

   public TownManager(AIController ai){
      this.ai = ai;
   }

   public static void changeBuildLocation(UnitController unitController){}

   @Override public void update(){
      for(FarmUnitController farm : ai.farms){
         farm.updateOpenings();
      }

      // update farms map
      for(Unit res : ai.gameState.getNeutralUnits()){
         if(res.isResources()){
            FarmUnitController fc = new FarmUnitController(res, ai);
            if(!ai.farms.contains(fc)){
               if(AIController.DEBUG){
                  System.out.println("found a farm at " + res.getX() + ", " + res.getY());
               }
               ai.farms.add(new FarmUnitController(res, ai));
            }
         }
      }

      for(WorkerUnitController worker : ai.workers){
         worker.act(ai);// carry out action, it wont do anything if unit
         // doesn't have one

         if(worker.actions.size() <= 0) // no actions
         {
            if(worker.unit.getHarvestAmount() <= 0){
               int farm = getClosestFreeFarm(worker);
               if(farm != -1){
                  pathToFarm(worker, farm);
               }
            }
            else{
               pathToStockpile(worker, worker.getY() * MapUtil.WIDTH + worker.getX(), ai.currentTurn, -1);
            }
         }
      }

      for(BuildingUnitController stock : ai.stockpiles){// all the bases, tell em to make workers
         stock.act(ai);

         if(ai.workers.size() + ai.scouts.size() < ai.wantedScouts + ai.wantedWorkers){// do we want scouts?
            // check if we have the cash
            boolean enoughMoney = true;
            for(int i = 0; i < ai.resources.size(); i++){
               int cost = ai.unitTypes.get(AIController.WORKER).cost.get(i);
               if(cost > ai.resources.get(i)){
                  enoughMoney = false;
                  break;
               }
            }
            if(enoughMoney){
               if(stock.actions.size() <= 0){ // no actions?!?!?
                  int time = ai.currentTurn;
                  int position = stock.getY() * MapUtil.WIDTH + stock.getX() + 1;
                  stock.addAction(new UnitAction(stock.unit, UnitAction.BUILD, stock.getX(), stock.getY() + 1, AIController.WORKER), MapUtil.trafficMap, position, time, time + ai.unitTypes.get(AIController.WORKER).produce_speed);
                  // add no actions for the rest of the build time so it doesnt keep giving it build orders each turn
                  for(int i = 0; i < ai.unitTypes.get(AIController.WORKER).produce_speed - 1; i++){
                     stock.addAction(new UnitAction(stock.unit, UnitAction.NONE, stock.getX(), stock.getY(), -1), MapUtil.trafficMap, position, time, time + 1);
                  }
                  for(int i = 0; i < ai.unitTypes.get(AIController.WORKER).cost.size(); i++){
                     int newAmount = ai.resources.get(i) - ai.unitTypes.get(AIController.WORKER).cost.get(i);
                     ai.resources.set(i, newAmount);
                  }
                  if(AIController.DEBUG){
                     System.out.println("TM: recruiting worker");
                  }
               }
            }
         }
      }
   }

   private int getClosestFreeFarm(WorkerUnitController worker){
      int x = worker.getX();
      int y = worker.getY();
      int minDistance = 10000; // large int =P
      int closest = -1;
      for(Integer farm : ai.farmOpenings.keySet()){
         if(ai.farmOpenings.get(farm)){
            int distance = (int) (Math.sqrt(((x - farm % MapUtil.WIDTH)) ^ 2) + ((y - farm / MapUtil.WIDTH) ^ 2));
            if(distance < minDistance){
               closest = farm;
               minDistance = distance;
            }
         }
      }

      if(closest != -1){
         ai.farmOpenings.put(closest, false);
      }

      return closest;
   }

   @Override public void assignUnits(){
      // Grab my units!!!
      ArrayList<UnitController> toRemove = new ArrayList<UnitController>();
      for(UnitController unit : ai.freeUnits){
         if(unit.getClass() == WorkerUnitController.class){
            ai.workers.add((WorkerUnitController) unit);
            if(AIController.DEBUG){
               System.out.println("TM: acquired worker");
            }
            toRemove.add(unit);
         }
         else
            if(unit.getClass() == BuildingUnitController.class){
               BuildingUnitController bu = (BuildingUnitController) unit;
               if(bu.isStockpile()){
                  ai.stockpiles.add(bu);
                  if(AIController.DEBUG){
                     System.out.println("TM: acquired stockpile");
                  }
               }
               else{
                  ai.buildings.add(bu);
                  if(AIController.DEBUG){
                     System.out.println("TM: acquired building");
                  }
               }
               toRemove.add(unit);
            }
      }

      // remove any units from freeUnits that were assigned
      for(UnitController unit : toRemove){
         ai.freeUnits.remove(unit);
      }
   }

   private void pathToFarm(WorkerUnitController worker, int farm){
      ArrayList<Integer> openings = new ArrayList<Integer>();
      openings.add(farm);

      ArrayList<Integer[]> rpath = MapUtil.get_path(worker.unit, worker.getY() * MapUtil.WIDTH + worker.getX(), ai.currentTurn, openings);

      int time = ai.currentTurn;
      int position = worker.getY() * MapUtil.WIDTH + worker.getX();

      if(rpath != null){ // is possible to reach goal
         boolean there = false;
         position = rpath.get(0)[0];
         if(rpath.size() == 0){
            rpath.add(new Integer[]{worker.unit.getX() + worker.unit.getY() * MapUtil.WIDTH, ai.currentTurn});
            there = true;
         }

         // set order queue
         if(!there){
            for(int i = rpath.size() - 1; i >= 0; i--){
               worker.addAction(new UnitAction(worker.unit, UnitAction.MOVE, rpath.get(i)[0] % MapUtil.WIDTH, rpath.get(i)[0] / MapUtil.WIDTH, -1), MapUtil.trafficMap, rpath.get(i)[0], rpath.get(i)[1], rpath.get(i)[1] + worker.unit.getMoveSpeed());
            }
         }
         position = rpath.get(0)[0];
         time = rpath.get(0)[1];

         for(FarmUnitController res : ai.farms){
            if(res.hasThisOpening(farm)){

               // harvest
               worker.addAction(new UnitAction(worker.unit, UnitAction.HARVEST, res.getX(), res.getY(), -1), MapUtil.trafficMap, position, time, time + res.getHarvestSpeed());
               time += res.getHarvestSpeed();

               // close that opening, happens in getClosestFreeFarm()

               pathToStockpile(worker, position, time, farm);

               break;
            }
         }
      }
      else{
         // worker.clearActions(MapUtil.trafficMap);
         // if(AIController.DEBUG){
         // System.out.println("TM: invalid path");
         // }
      }
   }

   private void pathToStockpile(WorkerUnitController worker, int position, int time, int farm){
      // if we were just at a farm, free it!
      if(farm != -1){
         ai.farmOpenings.put(farm, true);
      }

      ArrayList<Integer> destination = new ArrayList<Integer>();
      destination.add(ai.stockpiles.get(0).getY() * MapUtil.WIDTH + ai.stockpiles.get(0).getX());

      // return
      ArrayList<Integer[]> rpath = MapUtil.get_path(worker.unit, position, time, destination);
      if(rpath != null){
         boolean there = false;
         if(rpath.size() <= 1){
            there = true;
            rpath.add(new Integer[]{position, time});
            if(rpath.size() == 1){
               rpath.add(new Integer[]{position, time});
            }
         }
         if(!there){
            for(int i = rpath.size() - 1; i >= 1; i--){
               // unit.actions.add(new UnitAction(worker.unit, UnitAction.MOVE, rpath.get(i)[0]%MapUtils.WIDTH,
               // rpath.get(i)[0]/MapUtils.WIDTH,-1));
               // System.out.println("adding MOVE");
               worker.addAction(new UnitAction(worker.unit, UnitAction.MOVE, rpath.get(i)[0] % MapUtil.WIDTH, rpath.get(i)[0] / MapUtil.WIDTH, -1), MapUtil.trafficMap, rpath.get(i)[0], rpath.get(i)[1], rpath.get(i)[1] + worker.unit.getMoveSpeed());
            }
            position = rpath.get(0)[0];
            time = rpath.get(0)[1];
         }
         // return
         worker.addAction(new UnitAction(worker.unit, UnitAction.RETURN, position % MapUtil.WIDTH, position / MapUtil.WIDTH, -1), MapUtil.trafficMap, position, time, time + UnitAction.DEFAULT_COOLDOWN);
      }
      else{
         // worker.clearActions(MapUtil.trafficMap);
         // if(AIController.DEBUG){
         // System.out.println("TM: invalid path");
         // }
      }
   }
}
