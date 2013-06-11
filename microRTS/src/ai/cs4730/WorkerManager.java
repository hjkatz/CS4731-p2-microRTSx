package ai.cs4730;

import java.util.ArrayList;
import java.util.Random;

import rts.units.Unit;
import rts.units.UnitAction;

public class WorkerManager extends Manager{

   private AIController ai;

   public WorkerManager(AIController ai){
      this.ai = ai;
   }

   @Override public void update(){
      // update farms map
      for(Unit u : ai.gameState.getNeutralUnits()){
         if(u.isResources()){
            FarmUnitController fc = new FarmUnitController(u, ai);
            if(!ai.farms.contains(fc)){
               ai.farms.add(new FarmUnitController(u, ai));
            }
         }
      }

      // remove farms if they are out of resources
      ArrayList<FarmUnitController> toRemove = new ArrayList<FarmUnitController>();
      for(FarmUnitController farm : ai.farms){
         if(farm.getResourceAmount() <= 0){
            farm.freeUp();
            toRemove.add(farm);
         }
      }
      for(FarmUnitController farm : toRemove){
         ai.farms.remove(farm);
      }

      ai.wantedWorkers = (int) (ai.farms.size() * .75);

      // give farmers orders
      for(WorkerUnitController worker : ai.farmers){
         worker.act();// carry out action, it wont do anything if unit doesn't have one

         if(worker.actions.size() <= 0 || !worker.lastActionSucceeded()) // no actions
         {
            if(worker.unit.getResources() <= 0){
               if(!worker.hasFarm()){
                  // has no resources but is assigned a farm
                  FarmUnitController farm = nextFreeFarm();
                  if(farm != null){
                     farm.setWorker(worker);
                     worker.setFarm(farm);
                     harvestFarm(worker, farm);
                  }
               }
               else{
                  // has no resources and not yet assigned a farm
                  FarmUnitController farm = worker.getFarm();
                  harvestFarm(worker, farm);
               }
            }
            else{
               // has resources in tow
               // worker.freeFarm();
               returnResources(worker, worker.getY() * MapUtil.WIDTH + worker.getX(), ai.currentTurn);
            }
         }
      }

      // ArrayList<UnitController> buildersToSwitch = new ArrayList<UnitController>();
      for(WantedUnit u : UnitQueue.wantedBuildingUnits){
         if(u.beingBuilt){
            boolean flag = false;
            for(BuilderUnitController builder : ai.builders){
               if(builder.getBuilding() == u){
                  flag = true;
               }
            }
            if(!flag){
               u.beingBuilt = false;
            }
         }
      }
      for(BuilderUnitController builder : ai.builders){
         builder.act();

         if(builder.actions.size() <= 0) // no actions?!
         {
            if(builder.getBuilding() != null){
               builder.setFree(true);
            }
            WantedUnit wanted = nextBuildingToBeBuilt();
            if(wanted != null){
               if(ai.buildingManager.canAffordBuilding(wanted.unitType)){
                  ArrayList<Integer> pos = new ArrayList<Integer>();
                  pos.add(wanted.location);
                  ArrayList<Integer[]> path = MapUtil.get_path(builder.unit, MapUtil.position(builder), ai.currentTurn, pos);

                  if(path != null){ // is possible to reach goal
                     if(path.size() != 0){
                        for(int i = path.size() - 1; i >= 1; i--){
                           builder.addAction(new UnitAction(builder.unit, UnitAction.MOVE, path.get(i)[0] % MapUtil.WIDTH, path.get(i)[0] / MapUtil.WIDTH, -1), MapUtil.trafficMap, path.get(i)[0], path.get(i)[1], path.get(i)[1] + builder.unit.getMoveSpeed());
                        }
                        // first item in path is the planned location
                        int position = path.get(0)[0];
                        int time = path.get(0)[1];

                        builder.addAction(new UnitAction(builder.unit, UnitAction.BUILD, position % MapUtil.WIDTH, position / MapUtil.WIDTH, wanted.unitType), MapUtil.trafficMap, position, time, time + builder.getBuildSpeed());
                        wanted.beingBuilt = true;
                        builder.setBuilding(wanted);

                        for(int i = 0; i < ai.buildingTypes.get(wanted.unitType).produce_speed - 1; i++){
                           builder.addAction(new UnitAction(builder.unit, UnitAction.NONE, builder.getX(), builder.getY(), -1), MapUtil.trafficMap, position, time, time + 1);
                        }
                        ai.buildingManager.makePurchase(ai.buildingTypes.get(wanted.unitType).cost);
                     }
                  }
               }
            }
            else{
               // pick random corner and send builder there
               ArrayList<Integer> pos = new ArrayList<Integer>();
               Random r = new Random();
               int c = r.nextInt(4);
               if(c == 0){
                  pos.add(MapUtil.position(MapUtil.WIDTH - 1, 1));
               }
               else
                  if(c == 1){
                     pos.add(MapUtil.position(1, MapUtil.HEIGHT - 1));
                  }
                  else
                     if(c == 2){
                        pos.add(MapUtil.position(MapUtil.WIDTH - 1, MapUtil.HEIGHT - 1));
                     }
                     else{
                        pos.add(MapUtil.position(1, 1));
                     }
               ArrayList<Integer[]> path = MapUtil.get_path(builder.unit, MapUtil.position(builder), ai.currentTurn, pos);

               if(path != null){ // is possible to reach goal
                  if(path.size() != 0){
                     for(int i = path.size() - 1; i >= 0; i--){
                        builder.addAction(new UnitAction(builder.unit, UnitAction.MOVE, path.get(i)[0] % MapUtil.WIDTH, path.get(i)[0] / MapUtil.WIDTH, -1), MapUtil.trafficMap, path.get(i)[0], path.get(i)[1], path.get(i)[1] + builder.unit.getMoveSpeed());
                     }
                  }
               }
            }
         }
      }
   }

   public WantedUnit nextBuildingToBeBuilt(){
      for(WantedUnit unit : UnitQueue.wantedBuildingUnits){
         if(!unit.beingBuilt){ return unit; }
      }

      return null;
   }

   public boolean needNewStockpile(FarmUnitController uc){
      int distance = 10000;

      for(BuildingUnitController stock : ai.stockpiles){
         if(MapUtil.distance(stock, uc) < distance){
            distance = MapUtil.distance(stock, uc);
         }
      }

      for(WantedUnit wanted : UnitQueue.wantedBuildingUnits){
         if(wanted.unitType == AIController.STOCKPILE){
            int x = wanted.location % MapUtil.WIDTH;
            int y = wanted.location / MapUtil.WIDTH;
            int a = uc.getX();
            int b = uc.getY();

            int first = x - a;
            int second = y - b;
            int third = (int) Math.pow(first, 2);
            int fourth = (int) Math.pow(second, 2);
            int ans = (int) Math.sqrt(third + fourth);
            if(ans < distance){
               distance = ans;
            }
         }
      }

      return distance >= 6;
   }

   public FarmUnitController nextFreeFarm(){
      for(FarmUnitController farm : ai.farms){
         if(farm.isFree() && MapUtil.getSurroundingPositions(MapUtil.position(farm)).size() > 0){ return farm; }
      }
      return null;
   }

   private void returnResources(WorkerUnitController worker, int position, int time){
      ArrayList<Integer> destination = new ArrayList<Integer>();
      for(BuildingUnitController stock : ai.stockpiles){
         destination.add(MapUtil.position(stock));
      }

      // return
      ArrayList<Integer[]> path = MapUtil.get_path(worker.unit, position, time, destination);
      if(path != null){
         for(int i = path.size() - 1; i >= 1; i--){
            worker.addAction(new UnitAction(worker.unit, UnitAction.MOVE, path.get(i)[0] % MapUtil.WIDTH, path.get(i)[0] / MapUtil.WIDTH, -1), MapUtil.trafficMap, path.get(i)[0], path.get(i)[1], path.get(i)[1] + worker.unit.getMoveSpeed());
         }
         // first element is the stockpile
         position = path.get(0)[0];
         time = path.get(0)[1];
         worker.addAction(new UnitAction(worker.unit, UnitAction.RETURN, position % MapUtil.WIDTH, position / MapUtil.WIDTH, -1), MapUtil.trafficMap, position, time, time + UnitAction.DEFAULT_COOLDOWN);
      }
   }

   private void harvestFarm(WorkerUnitController worker, FarmUnitController farm){
      if(needNewStockpile(farm)){
         int x;
         int y;
         if(ai.stockpiles.get(0).getX() < MapUtil.WIDTH / 2){
            x = farm.getX() - 2;
         }
         else{
            x = farm.getX() + 2;
         }
         if(ai.stockpiles.get(0).getY() < MapUtil.HEIGHT / 2){
            y = farm.getY() - 2;
         }
         else{
            y = farm.getY() + 2;
         }

         if(x < 0){
            x = 0;
         }
         else
            if(x >= MapUtil.WIDTH){
               x = MapUtil.WIDTH - 1;
            }
         if(y < 0){
            y = 0;
         }
         else
            if(y >= MapUtil.HEIGHT){
               y = MapUtil.HEIGHT - 1;
            }
         ArrayList<Integer> positions = MapUtil.getSurroundingPositions(MapUtil.position(x, y));
         if(positions.size() > 0){
            UnitQueue.requestBuilding(AIController.STOCKPILE, 1000, positions.get(0));
         }
      }

      ArrayList<Integer> pos = new ArrayList<Integer>();
      pos.add(MapUtil.position(farm));
      ArrayList<Integer[]> path = MapUtil.get_path(worker.unit, MapUtil.position(worker), ai.currentTurn, pos);

      if(path != null){ // is possible to reach goal
         for(int i = path.size() - 1; i >= 1; i--){
            worker.addAction(new UnitAction(worker.unit, UnitAction.MOVE, path.get(i)[0] % MapUtil.WIDTH, path.get(i)[0] / MapUtil.WIDTH, -1), MapUtil.trafficMap, path.get(i)[0], path.get(i)[1], path.get(i)[1] + worker.unit.getMoveSpeed());
         }
         // first item in path is the farm location
         int position = path.get(0)[0];
         int time = path.get(0)[1];

         worker.addAction(new UnitAction(worker.unit, UnitAction.HARVEST, farm.getX(), farm.getY(), -1), MapUtil.trafficMap, position, time, time + farm.getHarvestSpeed());
      }
   }
}
