package ai.cs4730;

import java.util.ArrayList;

import rts.units.Unit;
import rts.units.UnitAction;

public class WorkerManager extends Manager{

   private AIController ai;

   public WorkerManager(AIController ai){
      this.ai = ai;
   }

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

      //give workers orders
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

   private void pathToFarm(WorkerUnitController worker, int farm){
      ArrayList<Integer> openings = new ArrayList<Integer>();
      openings.add(farm);

      ArrayList<Integer[]> rpath = MapUtil.get_path(worker.unit, MapUtil.position(worker), ai.currentTurn, openings);

      int time = ai.currentTurn;
      int position = MapUtil.position(worker);

      if(rpath != null){ // is possible to reach goal
         boolean there = false;
         position = rpath.get(0)[0];
         if(rpath.size() == 0){
            rpath.add(new Integer[]{MapUtil.position(worker), ai.currentTurn});
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
