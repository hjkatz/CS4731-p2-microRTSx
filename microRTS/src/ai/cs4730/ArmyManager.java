package ai.cs4730;

import java.util.ArrayList;
import java.util.Random;

import rts.GameState;
import rts.units.Unit;
import rts.units.UnitAction;
import rts.units.UnitDefinition;

public class ArmyManager extends Manager{

   AIController    ai;
   private STATE   state;

   // different unit types
   private int     bestAttacker      = 0;
   private int     bestDefender      = 0;
   private int     fastestGround     = 0;
   private int     fastestAir        = 0;
   private int     bestAirAttacker   = 0;
   private int     bestAirDefender   = 0;
   private int     farthestRange     = 0;
   private int     farthestAirRange  = 0;
   private int     quickestToProduce = 0;    // and not worker

   private boolean foundEnemyBase    = false;
   private int     enemyBaseGuess;

   public ArmyManager(AIController ai){
      this.ai = ai;
      state = STATE.Establish;
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

      // guess a good location for the enemy base, opposite corner
      enemyBaseGuess = MapUtil.position(MapUtil.WIDTH - ai.stockpiles.get(0).getX(), MapUtil.HEIGHT - ai.stockpiles.get(0).getY());
   }

   @Override public void update(){
      for(Unit unit : ai.gameState.getOtherUnits()){
         if(unit.isBuilding()){
            BuildingUnitController bc = new BuildingUnitController(unit, ai);
            if(!ai.enemyBuildings.contains(bc)){
               ai.enemyBuildings.add(bc);
               foundEnemyBase = true;
            }
         }
         else{
            UnitController uc = new UnitController(unit, ai);
            if(!ai.enemyUnits.contains(uc)){
               ai.enemyUnits.add(uc);
            }
         }
      }

      int enemyBasePos, enemyUnitPos;
      switch (state){
         case Attack:
            // find largest concentration of enemy buildings
            // keep requesting strongest attacker and fastest unit
            UnitQueue.requestUnit(bestAttacker, 50);
            UnitQueue.requestUnit(bestAirAttacker, 40);
            enemyBasePos = findRandomEnemyBuilding();
            if(enemyBasePos != -1){
               for(ArmyUnitController uc : ai.armyUnits){
                  uc.act();
                  enemyBasePos = findRandomEnemyBuilding();
                  if(uc.actions.size() <= 0){
                     boolean canAttack = false;
                     ArrayList<UnitAction> legalActions = uc.unit.getActions();
                     for(UnitAction action : legalActions){
                        if(action.getType() == UnitAction.ATTACK && (MapUtil.map[MapUtil.position(action.getTargetX(), action.getTargetY())] & GameState.MAP_PLAYER) == 0){
                           uc.addAction(action, MapUtil.trafficMap, MapUtil.position(uc), ai.currentTurn, ai.currentTurn + uc.getAttackSpeed());
                           int time = ai.currentTurn;
                           for(int i = 0; i < uc.getAttackSpeed() - 1; i++){
                              uc.addAction(new UnitAction(uc.unit, UnitAction.NONE, uc.getX(), uc.getY(), -1), MapUtil.trafficMap, MapUtil.position(uc), time, time + 1);
                              time++;
                           }
                           canAttack = true;
                           break;
                        }
                     }
                     if(!canAttack){
                        moveTowards(uc, enemyBasePos % MapUtil.WIDTH, enemyBasePos / MapUtil.WIDTH);
                     }
                  }
               }
            }
            else{
               if(findRandomEnemyUnit() != -1){
                  state = STATE.Raid;
               }
               else{
                  state = STATE.Buildup;
               }
            }
            break;
         case Raid:
            // find largest concentration of enemy units
            // send 50% of units to attack their units
            // requesting strongest attacker and defenders
            UnitQueue.requestUnit(bestAirAttacker, 40);
            UnitQueue.requestUnit(bestAttacker, 40);
            UnitQueue.requestUnit(farthestRange, 40);
            enemyUnitPos = findRandomEnemyUnit();
            if(enemyUnitPos != -1){
               for(ArmyUnitController uc : ai.armyUnits){
                  uc.act();
                  enemyUnitPos = findRandomEnemyUnit();
                  if(uc.actions.size() <= 0){
                     boolean canAttack = false;
                     ArrayList<UnitAction> legalActions = uc.unit.getActions();
                     for(UnitAction action : legalActions){
                        if(action.getType() == UnitAction.ATTACK && (MapUtil.map[MapUtil.position(action.getTargetX(), action.getTargetY())] & GameState.MAP_PLAYER) == 0){
                           uc.addAction(action, MapUtil.trafficMap, MapUtil.position(uc), ai.currentTurn, ai.currentTurn + uc.getAttackSpeed());
                           int time = ai.currentTurn;
                           for(int i = 0; i < uc.getAttackSpeed() - 1; i++){
                              uc.addAction(new UnitAction(uc.unit, UnitAction.NONE, uc.getX(), uc.getY(), -1), MapUtil.trafficMap, MapUtil.position(uc), time, time + 1);
                              time++;
                           }
                           canAttack = true;
                           break;
                        }
                     }
                     if(!canAttack){
                        moveTowards(uc, enemyUnitPos % MapUtil.WIDTH, enemyUnitPos / MapUtil.WIDTH);
                     }
                  }
               }
            }
            else{
               if(findRandomEnemyBuilding() != -1){
                  state = STATE.Attack;
               }
               else{
                  state = STATE.Buildup;
               }
            }
            break;
         case Buildup:
            // request attack and defense units
            // patrol around home base
            // look for more resources and enemy positions
            UnitQueue.requestUnit(bestAttacker, 50);
            UnitQueue.requestUnit(bestDefender, 50);
            UnitQueue.requestUnit(bestAirAttacker, 40);

            for(ArmyUnitController uc : ai.armyUnits){
               if(uc.actions.size() <= 0){ // no actions?!?!?
                  moveTowards(uc, MapUtil.WIDTH / 2, MapUtil.HEIGHT / 2);
               }
            }

            if(findRandomEnemyBuilding() != -1){
               state = STATE.Attack;
            }

            if(findRandomEnemyUnit() != -1){
               state = STATE.Raid;
            }
            break;
         case Cheese:
            // only active at beginning, if worker finds enemy base
            // should have soldier office near enemy base, have it produce quickest not worker
            // and keep them attacking

            UnitQueue.requestUnit(bestAttacker, 50);
            UnitQueue.requestUnit(farthestRange, 50);
            UnitQueue.requestUnit(bestAirAttacker, 40);
            for(ArmyUnitController uc : ai.armyUnits){
               uc.act();
               if(uc.actions.size() <= 0){
                  boolean canAttack = false;
                  ArrayList<UnitAction> legalActions = uc.unit.getActions();
                  for(UnitAction action : legalActions){
                     if(action.getType() == UnitAction.ATTACK && (MapUtil.map[MapUtil.position(action.getTargetX(), action.getTargetY())] & GameState.MAP_PLAYER) == 0){
                        uc.addAction(action, MapUtil.trafficMap, MapUtil.position(uc), ai.currentTurn, ai.currentTurn + uc.getAttackSpeed());
                        int time = ai.currentTurn;
                        for(int i = 0; i < uc.getAttackSpeed() - 1; i++){
                           uc.addAction(new UnitAction(uc.unit, UnitAction.NONE, uc.getX(), uc.getY(), -1), MapUtil.trafficMap, MapUtil.position(uc), time, time + 1);
                           time++;
                        }
                        canAttack = true;
                        break;
                     }
                  }
                  if(!canAttack){
                     if(!foundEnemyBase){
                        moveTowards(uc, enemyBaseGuess % MapUtil.WIDTH, enemyBaseGuess / MapUtil.WIDTH);
                     }
                     else{
                        enemyBasePos = findRandomEnemyBuilding();
                        enemyUnitPos = findRandomEnemyUnit();
                        if(enemyBasePos != -1){
                           moveTowards(uc, enemyBasePos % MapUtil.WIDTH, enemyBasePos / MapUtil.WIDTH);
                        }
                        else
                           if(enemyUnitPos != -1){
                              moveTowards(uc, enemyUnitPos % MapUtil.WIDTH, enemyUnitPos / MapUtil.WIDTH);
                           }
                           else{
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

                              ArrayList<Integer[]> path = MapUtil.get_path(uc.unit, MapUtil.position(uc), ai.currentTurn, pos);

                              if(path != null){ // is possible to reach goal
                                 if(path.size() != 0){
                                    for(int i = path.size() - 1; i >= 0; i--){
                                       uc.addAction(new UnitAction(uc.unit, UnitAction.MOVE, path.get(i)[0] % MapUtil.WIDTH, path.get(i)[0] / MapUtil.WIDTH, -1), MapUtil.trafficMap, path.get(i)[0], path.get(i)[1], path.get(i)[1] + uc.unit.getMoveSpeed());
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

            ArrayList<UnitController> toRemove = new ArrayList<UnitController>();
            for(UnitController scout : ai.scouts){
               if(scout.actions.size() <= 0){
                  int targetX = MapUtil.WIDTH / 2;
                  int targetY = (MapUtil.HEIGHT / 2) + 1;
                  if(MapUtil.position(targetX, targetY) != MapUtil.position(scout)){
                     ArrayList<Integer> destination = new ArrayList<Integer>();
                     destination.add(targetX + targetY * MapUtil.WIDTH);
                     ArrayList<Integer[]> path = MapUtil.get_path(scout.unit, MapUtil.position(scout), ai.currentTurn, destination);

                     if(path != null){ // is possible to reach goal
                        for(int i = path.size() - 1; i >= 0; i--){
                           scout.addAction(new UnitAction(scout.unit, UnitAction.MOVE, path.get(i)[0] % MapUtil.WIDTH, path.get(i)[0] / MapUtil.WIDTH, -1), MapUtil.trafficMap, path.get(i)[0], path.get(i)[1], path.get(i)[1] + scout.unit.getMoveSpeed());
                        }
                     }
                  }
                  else{
                     // turn scout into builder (for cheesing)
                     ai.wantedScouts = 0;
                     ai.wantedBuilders = 3;
                     BuilderUnitController wc = new BuilderUnitController(ai.scouts.get(0).unit, ai);
                     toRemove.add(ai.scouts.get(0));
                     ai.builders.add(wc);

                     UnitQueue.requestBuilding(AIController.SOLDIEROFFICE, 5500, MapUtil.position(wc) - 1);
                     UnitQueue.requestBuilding(AIController.SOLDIEROFFICE, 5100, MapUtil.position(wc) + 3);
                     UnitQueue.requestBuilding(AIController.SOLDIEROFFICE, 4200, MapUtil.position(wc) + 4);
                     UnitQueue.requestBuilding(AIController.AIRPORT, 4000, MapUtil.position(wc) + 4 + MapUtil.WIDTH * 3);

                     state = STATE.Cheese;
                  }
               }
               scout.act();
            }
            for(UnitController uc : toRemove){
               ai.scouts.remove(uc);
            }
            break;
      }
   }

   private int findRandomEnemyBuilding(){
      if(ai.enemyBuildings.size() == 0){ return -1; }
      Random r = new Random();
      int choice = r.nextInt(ai.enemyBuildings.size());
      UnitController enemy = ai.enemyBuildings.get(choice);
      if(enemy.unit.getHP() <= 0){
         ai.enemyBuildings.remove(enemy);
         return findRandomEnemyBuilding();
      }
      return MapUtil.position(ai.enemyBuildings.get(choice));
   }

   private void moveTowards(UnitController uc, int dx, int dy){
      int ux = uc.getX();
      int uy = uc.getY();
      int t = MapUtil.position(uc);

      if(ux < dx){
         t += 1;
      }
      else
         if(ux > dx){
            t -= 1;
         }
      if(uy < dy){
         t += MapUtil.WIDTH;
      }
      else
         if(uy > dy){
            t -= MapUtil.WIDTH;
         }

      ArrayList<Integer> destination = new ArrayList<Integer>();
      destination.add(t);
      ArrayList<Integer[]> path = MapUtil.get_path(uc.unit, MapUtil.position(uc), ai.currentTurn, destination);
      if(path != null){
         for(int i = path.size() - 1; i >= 0; i--){
            uc.addAction(new UnitAction(uc.unit, UnitAction.MOVE, path.get(i)[0] % MapUtil.WIDTH, path.get(i)[0] / MapUtil.WIDTH, -1), MapUtil.trafficMap, path.get(i)[0], path.get(i)[1], path.get(i)[1] + uc.unit.getMoveSpeed());
         }
      }

   }

   private int findRandomEnemyUnit(){
      if(ai.enemyUnits.size() == 0){ return -1; }
      Random r = new Random();
      int choice = r.nextInt(ai.enemyUnits.size());
      UnitController enemy = ai.enemyUnits.get(choice);
      if(enemy.unit.getHP() <= 0){
         ai.enemyUnits.remove(enemy);
         return findRandomEnemyUnit();
      }
      return MapUtil.position(enemy);
   }

   private enum STATE{
      Establish, Attack, Buildup, Cheese, Raid
   }

}
