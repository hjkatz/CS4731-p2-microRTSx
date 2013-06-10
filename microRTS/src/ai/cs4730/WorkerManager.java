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

      ai.wantedWorkers = ai.farms.size() - 2;
      ai.wantedBuilders = UnitQueue.wantedBuildingUnits.size();

      // give farmers orders
      for(WorkerUnitController worker : ai.farmers){
         worker.act(ai);// carry out action, it wont do anything if unit doesn't have one

         if(worker.actions.size() <= 0) // no actions
         {
            if(worker.unit.getResources() <= 0){
               if(!worker.hasFarm()){
                  // has no resources but is assigned a farm
                  FarmUnitController farm = nextFreeFarm();
                  if(farm != null){
                     farm.setWorker(worker);
                     worker.setFarm(farm);
                     harvestFarm(worker, MapUtil.position(farm));
                  }
               }
               else{
                  // has no resources and not yet assigned a farm
                  FarmUnitController farm = worker.getFarm();
                  if(needNewStockpile(farm)){
                     int x = -1;
                     int y = -1;
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
                     ArrayList<Integer> positions = MapUtil.getSurroundingPositions(MapUtil.position(x, y));
                     if(positions.size() > 0){
                        UnitQueue.requestBuilding(AIController.STOCKPILE, 1000, positions.get(0));
                     }
                     else{
                        // TODO something?
                     }
                  }
                  harvestFarm(worker, MapUtil.position(farm));
               }
            }
            else{
               // has resources in tow
               worker.freeFarm();
               returnResources(worker, worker.getY() * MapUtil.WIDTH + worker.getX(), ai.currentTurn);
            }
         }
      }

      for(BuilderUnitController builder : ai.builders){
         if(builder.actions.size() <= 0) // no actions?!
         {
            if(builder.getBuilding() != null){
               UnitQueue.wantedBuildingUnits.remove(builder.getBuilding());
               builder.setFree(true);
            }
            WantedUnit wanted = nextBuildingToBeBuilt();
            if(wanted != null){
               ArrayList<Integer> pos = new ArrayList<Integer>();
               pos.add(wanted.location);
               ArrayList<Integer[]> path = MapUtil.get_path(builder.unit, MapUtil.position(builder), ai.currentTurn, pos);

               if(path != null){ // is possible to reach goal
                  for(int i = path.size() - 1; i >= 1; i--){
                     builder.addAction(new UnitAction(builder.unit, UnitAction.MOVE, path.get(i)[0] % MapUtil.WIDTH, path.get(i)[0] / MapUtil.WIDTH, -1), MapUtil.trafficMap, path.get(i)[0], path.get(i)[1], path.get(i)[1] + builder.unit.getMoveSpeed());
                  }
                  // first item in path is the planned location
                  int position = path.get(0)[0];
                  int time = path.get(0)[1];

                  builder.addAction(new UnitAction(builder.unit, UnitAction.BUILD, position % MapUtil.WIDTH, position / MapUtil.WIDTH, wanted.unitType), MapUtil.trafficMap, position, time, time + builder.getBuildSpeed());
                  wanted.beingBuilt = true;
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
            if(((int) Math.sqrt(((x) - (a)) ^ 2 + ((y)) - (b)) ^ 2) < distance){
               distance = ((int) Math.sqrt(((x) - (a)) ^ 2 + ((y)) - (b)) ^ 2);
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

   private void harvestFarm(WorkerUnitController worker, int farmPos){
      ArrayList<Integer> pos = new ArrayList<Integer>();
      pos.add(farmPos);
      ArrayList<Integer[]> path = MapUtil.get_path(worker.unit, MapUtil.position(worker), ai.currentTurn, pos);

      if(path != null){ // is possible to reach goal
         for(int i = path.size() - 1; i >= 1; i--){
            worker.addAction(new UnitAction(worker.unit, UnitAction.MOVE, path.get(i)[0] % MapUtil.WIDTH, path.get(i)[0] / MapUtil.WIDTH, -1), MapUtil.trafficMap, path.get(i)[0], path.get(i)[1], path.get(i)[1] + worker.unit.getMoveSpeed());
         }
         // first item in path is the farm location
         int position = path.get(0)[0];
         int time = path.get(0)[1];

         FarmUnitController farm = worker.getFarm();
         worker.addAction(new UnitAction(worker.unit, UnitAction.HARVEST, farm.getX(), farm.getY(), -1), MapUtil.trafficMap, position, time, time + farm.getHarvestSpeed());
      }
   }
}
