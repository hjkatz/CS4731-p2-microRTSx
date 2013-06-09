package ai.cs4730;

import java.util.ArrayList;

import rts.units.Unit;
import rts.units.UnitAction;

public class ArmyManager extends Manager{

   AIController    ai;
   private STATE   state;
   private boolean foundEnemyBase;

   public ArmyManager(AIController ai){
      this.ai = ai;
      state = STATE.Explore;
      foundEnemyBase = false;
   }

   @Override public void update(){
      for(Unit unit : ai.gameState.getOtherUnits()){
         if(unit.isBuilding()){
            BuildingUnitController bc = new BuildingUnitController(unit, ai);
            if(!ai.enemyBuildings.contains(bc)){
               int[] b = new int[3];
               b[0] = unit.getX();
               b[1] = unit.getY();
               b[2] = unit.getType();
               ai.enemyBuildings.add(bc);
               if(AIController.DEBUG){
                  System.out.println("AM: found enemy building");
               }
            }
         }
      }

      switch (state){
         case Attack:
            break;
         case Buildup:
            break;
         case Cheese:
            break;
         case Explore:
            // request a scout after some workers are gathering resources
            if(ai.townManager.numWorkers() > 4){
               ai.wantedScouts = 1;
            }

            for(UnitController scout : ai.scouts){
               if(scout.actions.size() <= 0){
                  if(ai.enemyBuildings.size() == 0){
                     // find the enemy base first, count on it being in the opposite corner
                     int targetX = MapUtil.WIDTH - ai.stockpiles.get(0).getX();
                     int targetY = MapUtil.HEIGHT - ai.stockpiles.get(0).getY();
                     ArrayList<Integer> destination = new ArrayList<Integer>();
                     destination.add(targetX + targetY * MapUtil.WIDTH);
                     // path to estimated location of enemy base
                     ArrayList<Integer[]> path = MapUtil.get_path(scout.unit, scout.getX() + scout.getY() * MapUtil.WIDTH, ai.currentTurn, destination);
                     int time = ai.currentTurn;
                     int position = scout.getY() * MapUtil.WIDTH + scout.getX();

                     if(path != null){ // is possible to reach goal
                        boolean there = false;
                        if(path.size() == 0){
                           path.add(new Integer[]{scout.unit.getX() + scout.unit.getY() * MapUtil.WIDTH, ai.currentTurn});
                           there = true;
                        }

                        // set order queue
                        if(!there){
                           for(int i = path.size() - 1; i >= 0; i--){
                              scout.addAction(new UnitAction(scout.unit, UnitAction.MOVE, path.get(i)[0] % MapUtil.WIDTH, path.get(i)[0] / MapUtil.WIDTH, -1), MapUtil.trafficMap, path.get(i)[0], path.get(i)[1], path.get(i)[1] + scout.unit.getMoveSpeed());
                              time = path.get(i)[1];
                              position = path.get(i)[1];
                           }
                        }
                     }
                  }
               }
               scout.act(ai);
            }
            break;
      }
   }

   @Override public void assignUnits(){
      ArrayList<UnitController> toRemove = new ArrayList<UnitController>();
      for(UnitController u : ai.freeUnits){
         if(u.getClass() == ArmyUnitController.class){
            ai.groundUnits.add(u);
            if(AIController.DEBUG){
               System.out.println("AM: acquired army unit");
            }
            toRemove.add(u);
         }
         else
            if(ai.wantedScouts > ai.scouts.size() && u.getClass() == WorkerUnitController.class){
               ai.scouts.add(u);
               if(AIController.DEBUG){
                  System.out.println("AM: acquired scout");
               }
               toRemove.add(u);
            }
      }

      // remove any units from freeUnits that were assigend
      for(UnitController u : toRemove){
         ai.freeUnits.remove(u);
      }
   }

   private enum STATE{
      Explore, Attack, Buildup, Cheese
   }

}
