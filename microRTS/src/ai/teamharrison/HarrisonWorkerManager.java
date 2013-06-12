package ai.teamharrison;

import java.util.ArrayList;

import rts.units.Unit;
import rts.units.UnitAction;

public class HarrisonWorkerManager extends HarrisonManager{

   private HarrisonAIController ai;
   private Rule         hasResourcesRule;
   private Rule         buildingRequestedRule;
   private Action       returnResourcesAction;
   private Action       harvestAction;
   private Action       buildBuildingAction;

   public HarrisonWorkerManager(HarrisonAIController ai){
      this.ai = ai;
      hasResourcesRule = new WorkerHasResourcesRule(ai);
      buildingRequestedRule = new BuildingRequestedRule(ai);
      returnResourcesAction = new WorkerReturnResourcesAction(ai);
      harvestAction = new WorkerHarvestAction(ai);
      buildBuildingAction = new BuildBuildingAction(ai);
   }

   @Override public void update(){
      //check if rules fire, and if so execute their actions
      // farmers rules
      for(HarrisonWorkerUnitController worker : ai.farmers){
         worker.act();// carry out action, it wont do anything if unit doesn't have one

         if(worker.actions.size() <= 0 || !worker.lastActionSucceeded()) // no actions
         {
            if(!hasResourcesRule.isTriggered(worker)){
               harvestAction.execute(worker);
            }
            else{
               // has resources in tow
               returnResourcesAction.execute(worker);
            }
         }
      }

      //builders rules
      for(HarrisonBuilderUnitController builder : ai.builders){
         builder.act();

         if(builder.actions.size() <= 0) // no actions?!
         {
            if(buildingRequestedRule.isTriggered(builder)){
               buildBuildingAction.execute(builder);
            }
            else{
               //do some scouting while idle
               moveTowards(builder, ai.harrisonArmyManager.targetingPosition % 2, ai.harrisonArmyManager.targetingPosition / 2);
            }
         }
      }
   }

   private void moveTowards(HarrisonUnitController uc, int dx, int dy){
      int ux = uc.getX();
      int uy = uc.getY();
      int t = HarrisonMapUtil.position(uc);

      if(ux < dx){
         t += 1;
      }
      else
         if(ux > dx){
            t -= 1;
         }
      if(uy < dy){
         t += HarrisonMapUtil.WIDTH;
      }
      else
         if(uy > dy){
            t -= HarrisonMapUtil.WIDTH;
         }

      ArrayList<Integer> destination = new ArrayList<Integer>();
      destination.add(t);
      ArrayList<Integer[]> path = HarrisonMapUtil.get_path(uc.unit, HarrisonMapUtil.position(uc), ai.currentTurn, destination);
      if(path != null){
         for(int i = path.size() - 1; i >= 0; i--){
            uc.addAction(new UnitAction(uc.unit, UnitAction.MOVE, path.get(i)[0] % HarrisonMapUtil.WIDTH, path.get(i)[0] / HarrisonMapUtil.WIDTH, -1), HarrisonMapUtil.harrisonTrafficMap, path.get(i)[0], path.get(i)[1], path.get(i)[1] + uc.unit.getMoveSpeed());
         }
      }

   }

   protected abstract class Rule{
      protected HarrisonAIController ai;

      public Rule(HarrisonAIController ai){
         this.ai = ai;
      }

      public abstract boolean isTriggered(HarrisonUnitController unit);
   }

   protected abstract class Action{
      protected HarrisonAIController ai;

      public Action(HarrisonAIController ai){
         this.ai = ai;
      }

      public abstract void execute(HarrisonUnitController unit);
   }

   protected class BuildingRequestedRule extends Rule{

      public BuildingRequestedRule(HarrisonAIController ai){
         super(ai);
      }

      @Override public boolean isTriggered(HarrisonUnitController worker){
         if(HarrisonUnitQueue.wantedBuildingUnits.size() > 0){
            return true;
         }
         else{
            return false;
         }
      }
   }

   protected class WorkerHasResourcesRule extends Rule{

      public WorkerHasResourcesRule(HarrisonAIController ai){
         super(ai);
      }

      @Override public boolean isTriggered(HarrisonUnitController worker){
         HarrisonWorkerUnitController w = (HarrisonWorkerUnitController) worker;
         if(w.getResources() > 0){
            return true;
         }
         else{
            return false;
         }
      }
   }

   protected class WorkerAssignedFarmRule extends Rule{

      public WorkerAssignedFarmRule(HarrisonAIController ai){
         super(ai);
      }

      @Override public boolean isTriggered(HarrisonUnitController worker){
         HarrisonWorkerUnitController w = (HarrisonWorkerUnitController) worker;
         return w.hasFarm();
      }
   }

   protected class ResourcesTooFarRule extends Rule{

      public ResourcesTooFarRule(HarrisonAIController ai){
         super(ai);
      }

      @Override public boolean isTriggered(HarrisonUnitController worker){
         HarrisonWorkerUnitController w = (HarrisonWorkerUnitController) worker;
         int distance = 10000;
         for(HarrisonBuildingUnitController stock : ai.stockpiles){
            if(HarrisonMapUtil.distance(stock, w.getFarm()) < distance){
               distance = HarrisonMapUtil.distance(stock, w.getFarm());
            }
         }
         if(distance >= 5){
            for(HarrisonWantedUnit wanted : HarrisonUnitQueue.wantedBuildingUnits){
               if(wanted.unitType == HarrisonAIController.STOCKPILE){
                  if(HarrisonMapUtil.distance(wanted.location, HarrisonMapUtil.position(w.getFarm())) < 2){ return false; }
               }
            }
            return true;
         }
         return false;
      }
   }

   protected class BuildBuildingAction extends Action{

      public BuildBuildingAction(HarrisonAIController ai){
         super(ai);
      }

      @Override public void execute(HarrisonUnitController unit){
         HarrisonBuilderUnitController builder = (HarrisonBuilderUnitController) unit;
         HarrisonWantedUnit wanted = nextBuildingToBeBuilt();
         if(ai.harrisonBuildingManager.canAffordBuilding(wanted.unitType)){
            ArrayList<Integer> pos = new ArrayList<Integer>();
            pos.add(wanted.location);
            ArrayList<Integer[]> path = HarrisonMapUtil.get_path(builder.unit, HarrisonMapUtil.position(builder), ai.currentTurn, pos);

            if(path != null){ // is possible to reach goal
               if(path.size() != 0){
                  for(int i = path.size() - 1; i >= 1; i--){
                     builder.addAction(new UnitAction(builder.unit, UnitAction.MOVE, path.get(i)[0] % HarrisonMapUtil.WIDTH, path.get(i)[0] / HarrisonMapUtil.WIDTH, -1), HarrisonMapUtil.harrisonTrafficMap, path.get(i)[0], path.get(i)[1], path.get(i)[1] + builder.unit.getMoveSpeed());
                  }
                  // first item in path is the planned location
                  int position = path.get(0)[0];
                  int time = path.get(0)[1];

                  builder.addAction(new UnitAction(builder.unit, UnitAction.BUILD, position % HarrisonMapUtil.WIDTH, position / HarrisonMapUtil.WIDTH, wanted.unitType), HarrisonMapUtil.harrisonTrafficMap, position, time, time + builder.getBuildSpeed());
                  wanted.beingBuilt = true;
                  builder.setBuilding(wanted);

                  for(int i = 0; i < ai.buildingTypes.get(wanted.unitType).produce_speed - 1; i++){
                     builder.addAction(new UnitAction(builder.unit, UnitAction.NONE, builder.getX(), builder.getY(), -1), HarrisonMapUtil.harrisonTrafficMap, position, time, time + 1);
                  }
                  ai.harrisonBuildingManager.makePurchase(ai.buildingTypes.get(wanted.unitType).cost);
               }
            }
         }
      }
      
      public HarrisonWantedUnit nextBuildingToBeBuilt(){
         for(HarrisonWantedUnit unit : HarrisonUnitQueue.wantedBuildingUnits){
            if(!unit.beingBuilt){ return unit; }
         }

         return null;
      }
   }

   protected class RequestNewStockpileAction extends Action{

      public RequestNewStockpileAction(HarrisonAIController ai){
         super(ai);
      }

      @Override public void execute(HarrisonUnitController unit){
         HarrisonWorkerUnitController worker = (HarrisonWorkerUnitController) unit;
         HarrisonFarmUnitController farm = worker.getFarm();
         int y;

         if(ai.stockpiles.get(0).getY() < HarrisonMapUtil.HEIGHT / 2){
            y = farm.getY() - 2;
         }
         else{
            y = farm.getY() + 2;
         }

         if(y < 0){
            y = 0;
         }
         else
            if(y >= HarrisonMapUtil.HEIGHT){
               y = HarrisonMapUtil.HEIGHT - 1;
            }
         ArrayList<Integer> positions = HarrisonMapUtil.getSurroundingPositions(HarrisonMapUtil.position(farm.getX(), y));
         if(positions.size() > 0){
            HarrisonUnitQueue.requestBuilding(HarrisonAIController.STOCKPILE, 4900, positions.get(0));
            HarrisonUnitQueue.requestBuilding(ai.harrisonArmyManager.nextBuildingToRequest, 5000, positions.get(0) + 2);
            ai.harrisonArmyManager.nextBuildingToRequest++;
            if(ai.harrisonArmyManager.nextBuildingToRequest >= ai.harrisonArmyManager.buildingsNeeded.size()){
               ai.harrisonArmyManager.nextBuildingToRequest = 0;
            }
         }
      }

   }

   protected class WorkerReturnResourcesAction extends Action{

      public WorkerReturnResourcesAction(HarrisonAIController ai){
         super(ai);
      }

      @Override public void execute(HarrisonUnitController worker){
         HarrisonWorkerUnitController w = (HarrisonWorkerUnitController) worker;
         int time = ai.currentTurn;
         w.freeFarm();
         ArrayList<Integer> destination = new ArrayList<Integer>();
         for(HarrisonBuildingUnitController stock : ai.stockpiles){
            destination.add(HarrisonMapUtil.position(stock));
         }

         // return
         ArrayList<Integer[]> path = HarrisonMapUtil.get_path(worker.unit, HarrisonMapUtil.position(worker), time, destination);
         if(path != null){
            for(int i = path.size() - 1; i >= 1; i--){
               worker.addAction(new UnitAction(worker.unit, UnitAction.MOVE, path.get(i)[0] % HarrisonMapUtil.WIDTH, path.get(i)[0] / HarrisonMapUtil.WIDTH, -1), HarrisonMapUtil.harrisonTrafficMap, path.get(i)[0], path.get(i)[1], path.get(i)[1] + worker.unit.getMoveSpeed());
            }
            // first element is the stockpile
            int position = path.get(0)[0];
            time = path.get(0)[1];
            worker.addAction(new UnitAction(worker.unit, UnitAction.RETURN, position % HarrisonMapUtil.WIDTH, position / HarrisonMapUtil.WIDTH, -1), HarrisonMapUtil.harrisonTrafficMap, position, time, time + UnitAction.DEFAULT_COOLDOWN);
         }
         else{
            for(UnitAction action : worker.unit.getActions()){
               if(action.getType() == UnitAction.MOVE){
                  worker.addAction(action, HarrisonMapUtil.harrisonTrafficMap, HarrisonMapUtil.position(action.getTargetX(), action.getTargetY()), ai.currentTurn, ai.currentTurn + w.getMoveSpeed());
                  break;
               }
            }
         }
      }
   }

   protected class WorkerHarvestAction extends Action{

      Rule   workerAssignedFarmRule;
      Rule   resourceTooFarRule;
      Action requestNewStockpileAction;

      public WorkerHarvestAction(HarrisonAIController ai){
         super(ai);
         workerAssignedFarmRule = new WorkerAssignedFarmRule(ai);
         resourceTooFarRule = new ResourcesTooFarRule(ai);
         requestNewStockpileAction = new RequestNewStockpileAction(ai);
      }

      @Override public void execute(HarrisonUnitController worker){
         HarrisonWorkerUnitController w = (HarrisonWorkerUnitController) worker;
         if(!workerAssignedFarmRule.isTriggered(worker)){
            // find the next available farm and harvest it
            HarrisonFarmUnitController farm = nextFreeFarm();
            if(farm != null){
               farm.setWorker(w);
               w.setFarm(farm);
               harvestFarm(w, farm);
            }
         }
         else{
            // farms the workers current farm
            HarrisonFarmUnitController farm = w.getFarm();
            harvestFarm(w, farm);
         }
      }

      private HarrisonFarmUnitController nextFreeFarm(){
         for(HarrisonFarmUnitController farm : ai.farms){
            if(farm.isFree() && HarrisonMapUtil.getSurroundingPositions(HarrisonMapUtil.position(farm)).size() > 0){ return farm; }
         }
         return null;
      }

      private void harvestFarm(HarrisonWorkerUnitController worker, HarrisonFarmUnitController farm){
         if(resourceTooFarRule.isTriggered(worker)){
            requestNewStockpileAction.execute(worker);
         }

         ArrayList<Integer> pos = new ArrayList<Integer>();
         pos.add(HarrisonMapUtil.position(farm));
         ArrayList<Integer[]> path = HarrisonMapUtil.get_path(worker.unit, HarrisonMapUtil.position(worker), ai.currentTurn, pos);

         if(path != null){ // is possible to reach goal
            for(int i = path.size() - 1; i >= 1; i--){
               worker.addAction(new UnitAction(worker.unit, UnitAction.MOVE, path.get(i)[0] % HarrisonMapUtil.WIDTH, path.get(i)[0] / HarrisonMapUtil.WIDTH, -1), HarrisonMapUtil.harrisonTrafficMap, path.get(i)[0], path.get(i)[1], path.get(i)[1] + worker.unit.getMoveSpeed());
            }
            // first item in path is the farm location
            int position = path.get(0)[0];
            int time = path.get(0)[1];

            worker.addAction(new UnitAction(worker.unit, UnitAction.HARVEST, farm.getX(), farm.getY(), -1), HarrisonMapUtil.harrisonTrafficMap, position, time, time + farm.getHarvestSpeed());
         }
      }

   }
}
