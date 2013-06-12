package ai.teamharrison;

import java.util.ArrayList;
import java.util.Random;

import rts.GameState;
import rts.units.UnitAction;
import rts.units.UnitDefinition;

public class HarrisonArmyManager extends HarrisonManager{

   HarrisonAIController              ai;
   private STATE             state;

   // different unit types
   private int               bestAttacker          = 0;
   private int               bestDefender          = 0;
   private int               fastestGround         = 0;
   private int               fastestAir            = 0;
   private int               bestAirAttacker       = 0;
   private int               bestAirDefender       = 0;
   private int               farthestRange         = 0;
   private int               farthestAirRange      = 0;
   private int               quickestToProduce     = 0; // and not worker

   public ArrayList<Integer> buildingsNeeded;
   public int                nextBuildingToRequest = 0;

   private int               enemyBaseGuess;
   public int                targetingPosition;
   private Random            random;

   public HarrisonArmyManager(HarrisonAIController ai){
      this.ai = ai;
      state = STATE.Establish;
      buildingsNeeded = new ArrayList<Integer>();
   }

   public void init(){
      // find the best unit types at diff things
      for(int type : ai.unitTypes.keySet()){
         UnitDefinition def = ai.unitTypes.get(type);
         if(def.attack_max > ai.unitTypes.get(bestAttacker).attack_max){
            bestAttacker = type;
         }

         if(def.hp > ai.unitTypes.get(bestDefender).hp){
            bestDefender = type;
         }

         if(def.attack_max > ai.unitTypes.get(bestAirAttacker).attack_max && def.is_flying){
            bestAirAttacker = type;
         }

         if(def.hp > ai.unitTypes.get(bestAirDefender).hp && def.is_flying){
            bestAirDefender = type;
         }

         if(def.attack_max > ai.unitTypes.get(fastestGround).attack_max){
            fastestGround = type;
         }

         if(def.move_speed > ai.unitTypes.get(fastestAir).move_speed && def.is_flying){
            fastestAir = type;
         }

         if(def.produce_speed < ai.unitTypes.get(quickestToProduce).produce_speed && !def.is_worker){
            fastestAir = type;
         }

         if(def.attack_range > ai.unitTypes.get(farthestRange).attack_range && !def.is_flying){
            farthestRange = type;
         }

         if(def.attack_range > ai.unitTypes.get(farthestAirRange).attack_range && def.is_flying){
            farthestAirRange = type;
         }
      }

      for(int type : ai.buildingTypes.keySet()){
         UnitDefinition def = ai.buildingTypes.get(type);
         if(def.produces.contains(bestAttacker)){
            if(!buildingsNeeded.contains(type)){
               buildingsNeeded.add(type);
            }
         }

         if(def.produces.contains(bestDefender)){
            if(!buildingsNeeded.contains(type)){
               buildingsNeeded.add(type);
            }
         }

         if(def.produces.contains(fastestGround)){
            if(!buildingsNeeded.contains(type)){
               buildingsNeeded.add(type);
            }
         }

         if(def.produces.contains(fastestAir)){
            if(!buildingsNeeded.contains(type)){
               buildingsNeeded.add(type);
            }
         }

         if(def.produces.contains(bestAirAttacker)){
            if(!buildingsNeeded.contains(type)){
               buildingsNeeded.add(type);
            }
         }

         if(def.produces.contains(bestAirDefender)){
            if(!buildingsNeeded.contains(type)){
               buildingsNeeded.add(type);
            }
         }

         if(def.produces.contains(farthestRange)){
            if(!buildingsNeeded.contains(type)){
               buildingsNeeded.add(type);
            }
         }

         if(def.produces.contains(farthestAirRange)){
            if(!buildingsNeeded.contains(type)){
               buildingsNeeded.add(type);
            }
         }

         if(def.produces.contains(quickestToProduce)){
            if(!buildingsNeeded.contains(type)){
               buildingsNeeded.add(type);
            }
         }
      }

      // guess a good location for the enemy base, opposite corner
      enemyBaseGuess = HarrisonMapUtil.position(HarrisonMapUtil.WIDTH - ai.stockpiles.get(0).getX(), HarrisonMapUtil.HEIGHT - ai.stockpiles.get(0).getY());
      random = new Random();
      targetingPosition = randomPosition();
   }

   private int randomPosition(){
      int position = HarrisonMapUtil.position(random.nextInt(HarrisonMapUtil.WIDTH), random.nextInt(HarrisonMapUtil.HEIGHT));
      while((HarrisonMapUtil.map[position] & (GameState.MAP_FOG | GameState.MAP_WALL | GameState.MAP_NEUTRAL | GameState.MAP_PLAYER)) == 0){
         position = HarrisonMapUtil.position(random.nextInt(HarrisonMapUtil.WIDTH), random.nextInt(HarrisonMapUtil.HEIGHT));
      }
      System.out.println(position % HarrisonMapUtil.WIDTH + ", " + position / HarrisonMapUtil.WIDTH);
      return position;
   }

   @Override public void update(){
      requestUnits();
      switch (state){
         case MoveTowardsBuilding:
            // move attack towards enemy building
            if(ai.enemyBuildings.size() > 0){
               for(HarrisonArmyUnitController uc : ai.armyUnits){
                  uc.act();
                  if(HarrisonMapUtil.position(uc) == targetingPosition){
                     targetingPosition = findRandomEnemyBuilding();
                  }
                  if(uc.actions.size() <= 0 || !uc.lastActionSucceeded()){
                     boolean canAttack = false;
                     ArrayList<UnitAction> legalActions = uc.unit.getActions();
                     for(UnitAction action : legalActions){
                        if(action.getType() == UnitAction.ATTACK && (HarrisonMapUtil.map[HarrisonMapUtil.position(action.getTargetX(), action.getTargetY())] & GameState.MAP_PLAYER) == 0){
                           uc.addAction(action, HarrisonMapUtil.harrisonTrafficMap, HarrisonMapUtil.position(uc), ai.currentTurn, ai.currentTurn + uc.getAttackSpeed());
                           for(int i = ai.currentTurn; i < ai.currentTurn + uc.getAttackSpeed() - 1; i++){// i is time
                              uc.addAction(new UnitAction(uc.unit, UnitAction.NONE, uc.getX(), uc.getY(), -1), HarrisonMapUtil.harrisonTrafficMap, HarrisonMapUtil.position(uc), i, i + 1);
                           }
                           canAttack = true;
                           break;
                        }
                     }
                     if(!canAttack){
                        moveTowards(uc, targetingPosition % HarrisonMapUtil.WIDTH, targetingPosition / HarrisonMapUtil.WIDTH);
                     }
                  }
               }
            }
            else{
               // if none left look for units
               if(ai.enemyUnits.size() > 0){
                  state = STATE.MoveTowardsUnit;
                  targetingPosition = findRandomEnemyUnit();
               }
               // if no enemy units, explore
               else{
                  state = STATE.Explore;
                  targetingPosition = randomPosition();
               }
            }
            break;
         case MoveTowardsUnit:
            if(ai.enemyUnits.size() > 0){
               for(HarrisonArmyUnitController uc : ai.armyUnits){
                  uc.act();
                  if(HarrisonMapUtil.position(uc) == targetingPosition){
                     targetingPosition = findRandomEnemyUnit();
                  }
                  if(uc.actions.size() <= 0 || !uc.lastActionSucceeded()){
                     boolean canAttack = false;
                     ArrayList<UnitAction> legalActions = uc.unit.getActions();
                     for(UnitAction action : legalActions){
                        if(action.getType() == UnitAction.ATTACK && (HarrisonMapUtil.map[HarrisonMapUtil.position(action.getTargetX(), action.getTargetY())] & GameState.MAP_PLAYER) == 0){
                           uc.addAction(action, HarrisonMapUtil.harrisonTrafficMap, HarrisonMapUtil.position(uc), ai.currentTurn, ai.currentTurn + uc.getAttackSpeed());
                           int time = ai.currentTurn;
                           for(int i = 0; i < uc.getAttackSpeed() - 1; i++){
                              uc.addAction(new UnitAction(uc.unit, UnitAction.NONE, uc.getX(), uc.getY(), -1), HarrisonMapUtil.harrisonTrafficMap, HarrisonMapUtil.position(uc), time, time + 1);
                              time++;
                           }
                           canAttack = true;
                           break;
                        }
                     }
                     if(!canAttack){
                        moveTowards(uc, targetingPosition % HarrisonMapUtil.WIDTH, targetingPosition / HarrisonMapUtil.WIDTH);
                     }
                  }
               }
            }
            else
               if(ai.enemyBuildings.size() == 0){
                  targetingPosition = randomPosition();
                  state = STATE.Explore;
               }
               else{
                  state = STATE.MoveTowardsBuilding;
                  targetingPosition = findRandomEnemyBuilding();
               }

            break;
         case Cheese:
            // only active at beginning, until we find enemy base, this is the opening strategy
            // keep them attacking
            if(ai.enemyBuildings.size() > 0 || ai.enemyUnits.size() > 0){
               state = STATE.MoveTowardsBuilding;
               targetingPosition = findRandomEnemyBuilding();
               if(targetingPosition == -1){
                  targetingPosition = findRandomEnemyUnit();
                  state = STATE.MoveTowardsUnit;
               }

            }
            for(HarrisonArmyUnitController uc : ai.armyUnits){
               uc.act();
               if(uc.actions.size() <= 0 || !uc.lastActionSucceeded()){
                  boolean canAttack = false;
                  ArrayList<UnitAction> legalActions = uc.unit.getActions();
                  for(UnitAction action : legalActions){
                     if(action.getType() == UnitAction.ATTACK && (HarrisonMapUtil.map[HarrisonMapUtil.position(action.getTargetX(), action.getTargetY())] & GameState.MAP_PLAYER) == 0){
                        uc.addAction(action, HarrisonMapUtil.harrisonTrafficMap, HarrisonMapUtil.position(uc), ai.currentTurn, ai.currentTurn + uc.getAttackSpeed());
                        int time = ai.currentTurn;
                        for(int i = 0; i < uc.getAttackSpeed() - 1; i++){
                           uc.addAction(new UnitAction(uc.unit, UnitAction.NONE, uc.getX(), uc.getY(), -1), HarrisonMapUtil.harrisonTrafficMap, HarrisonMapUtil.position(uc), time, time + 1);
                           time++;
                        }
                        canAttack = true;
                        break;
                     }
                  }
                  if(!canAttack){
                     if(!ai.foundEnemyBase){
                        moveTowards(uc, enemyBaseGuess % HarrisonMapUtil.WIDTH, enemyBaseGuess / HarrisonMapUtil.WIDTH);
                     }
                     else{
                        targetingPosition = findRandomEnemyBuilding();
                        if(targetingPosition != -1){
                           moveTowards(uc, targetingPosition % HarrisonMapUtil.WIDTH, targetingPosition / HarrisonMapUtil.WIDTH);
                        }
                        else targetingPosition = findRandomEnemyUnit();
                        if(targetingPosition != -1){
                           moveTowards(uc, targetingPosition % HarrisonMapUtil.WIDTH, targetingPosition / HarrisonMapUtil.WIDTH);
                        }
                        else{
                           ArrayList<Integer> pos = new ArrayList<Integer>();
                           int c = random.nextInt(4);
                           if(c == 0){
                              pos.add(HarrisonMapUtil.position(HarrisonMapUtil.WIDTH - 1, 1));
                           }
                           else
                              if(c == 1){
                                 pos.add(HarrisonMapUtil.position(1, HarrisonMapUtil.HEIGHT - 1));
                              }
                              else
                                 if(c == 2){
                                    pos.add(HarrisonMapUtil.position(HarrisonMapUtil.WIDTH - 1, HarrisonMapUtil.HEIGHT - 1));
                                 }
                                 else{
                                    pos.add(HarrisonMapUtil.position(1, 1));
                                 }

                           ArrayList<Integer[]> path = HarrisonMapUtil.get_path(uc.unit, HarrisonMapUtil.position(uc), ai.currentTurn, pos);

                           if(path != null){ // is possible to reach goal
                              if(path.size() != 0){
                                 for(int i = path.size() - 1; i >= 0; i--){
                                    uc.addAction(new UnitAction(uc.unit, UnitAction.MOVE, path.get(i)[0] % HarrisonMapUtil.WIDTH, path.get(i)[0] / HarrisonMapUtil.WIDTH, -1), HarrisonMapUtil.harrisonTrafficMap, path.get(i)[0], path.get(i)[1], path.get(i)[1] + uc.unit.getMoveSpeed());
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
            break;
         case Establish:
            // request a scout after some farmers are gathering resources
            if(ai.farmers.size() > ai.wantedWorkers / 2){
               ai.wantedScouts = 1;
            }

            ArrayList<HarrisonUnitController> toRemove = new ArrayList<HarrisonUnitController>();
            for(HarrisonUnitController scout : ai.scouts){
               if(scout.actions.size() <= 0){
                  int targetX = HarrisonMapUtil.WIDTH / 2;
                  int targetY = (HarrisonMapUtil.HEIGHT / 2) + 1;
                  if(HarrisonMapUtil.position(targetX, targetY) != HarrisonMapUtil.position(scout)){
                     ArrayList<Integer> destination = new ArrayList<Integer>();
                     destination.add(targetX + targetY * HarrisonMapUtil.WIDTH);
                     ArrayList<Integer[]> path = HarrisonMapUtil.get_path(scout.unit, HarrisonMapUtil.position(scout), ai.currentTurn, destination);

                     if(path != null){ // is possible to reach goal
                        for(int i = path.size() - 1; i >= 0; i--){
                           scout.addAction(new UnitAction(scout.unit, UnitAction.MOVE, path.get(i)[0] % HarrisonMapUtil.WIDTH, path.get(i)[0] / HarrisonMapUtil.WIDTH, -1), HarrisonMapUtil.harrisonTrafficMap, path.get(i)[0], path.get(i)[1], path.get(i)[1] + scout.unit.getMoveSpeed());
                        }
                     }
                  }
                  else{
                     // turn scout into builder (for cheesing)
                     ai.wantedScouts = 0;
                     HarrisonBuilderUnitController wc = new HarrisonBuilderUnitController(ai.scouts.get(0).unit, ai);
                     toRemove.add(ai.scouts.get(0));
                     ai.builders.add(wc);
                     ai.wantedBuilders = 2;

                     HarrisonUnitQueue.requestBuilding(HarrisonAIController.SOLDIEROFFICE, 5500, HarrisonMapUtil.position(wc) - 1);
                     HarrisonUnitQueue.requestBuilding(HarrisonAIController.SOLDIEROFFICE, 5100, HarrisonMapUtil.position(wc) + 1);
                     HarrisonUnitQueue.requestBuilding(HarrisonAIController.AIRPORT, 4000, HarrisonMapUtil.position(wc) - 1 + HarrisonMapUtil.WIDTH * 3);

                     state = STATE.Cheese;
                  }
               }
               scout.act();
            }
            for(HarrisonUnitController uc : toRemove){
               ai.scouts.remove(uc);
            }
            break;
         case Explore:
            for(HarrisonArmyUnitController uc : ai.armyUnits){
               if(uc.actions.size() <= 0){
                  ArrayList<UnitAction> legalActions = uc.getActions();
                  for(UnitAction action : legalActions){
                     if(action.getType() == UnitAction.MOVE){
                        uc.addAction(action, HarrisonMapUtil.harrisonTrafficMap, HarrisonMapUtil.position(uc), ai.currentTurn, ai.currentTurn + uc.getMoveSpeed());
                        break;
                     }
                  }
               }
            }

            if(ai.enemyBuildings.size() > 0){
               state = STATE.MoveTowardsBuilding;
               targetingPosition = findRandomEnemyBuilding();
            }
            else
               if(ai.enemyUnits.size() > 0){
                  state = STATE.MoveTowardsUnit;
                  targetingPosition = findRandomEnemyUnit();
               }
            break;
      }
   }

   private void requestUnits(){
      if(ai.buildings.size() == 0){ return; }
      // TODO: add check based on units in the queue vs buildings vs buildings making units, etc..
      switch (state){
         case Establish:
            break;
         case Cheese:
            HarrisonUnitQueue.requestUnit(bestAttacker, 50);
            HarrisonUnitQueue.requestUnit(farthestRange, 50);
            break;
         case Explore:
            HarrisonUnitQueue.requestUnit(bestAttacker, 50);
            HarrisonUnitQueue.requestUnit(bestDefender, 50);
            HarrisonUnitQueue.requestUnit(bestAirAttacker, 55);
            break;
         case MoveTowardsBuilding:
            HarrisonUnitQueue.requestUnit(bestAttacker, 50);
            HarrisonUnitQueue.requestUnit(farthestRange, 50);
            HarrisonUnitQueue.requestUnit(bestAirAttacker, 55);
            break;
         case MoveTowardsUnit:
            HarrisonUnitQueue.requestUnit(bestAttacker, 50);
            HarrisonUnitQueue.requestUnit(farthestRange, 50);
            HarrisonUnitQueue.requestUnit(bestAirAttacker, 55);
            break;

      }

   }

   private int findRandomEnemyBuilding(){
      if(ai.enemyBuildings.size() == 0){ return -1; }
      return HarrisonMapUtil.position(ai.enemyBuildings.get(random.nextInt(ai.enemyBuildings.size())));
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

   private int findRandomEnemyUnit(){
      if(ai.enemyUnits.size() == 0){ return -1; }
      return HarrisonMapUtil.position(ai.enemyUnits.get(random.nextInt(ai.enemyUnits.size())));
   }

   private enum STATE{
      Establish, MoveTowardsBuilding, MoveTowardsUnit, Cheese, Explore
   }

}
