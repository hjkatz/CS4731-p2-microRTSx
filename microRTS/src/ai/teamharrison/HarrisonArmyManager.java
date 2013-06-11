package ai.teamharrison;

import java.util.ArrayList;
import java.util.Random;

import rts.GameState;
import rts.units.Unit;
import rts.units.UnitAction;
import rts.units.UnitDefinition;

public class HarrisonArmyManager extends HarrisonManager{

   HarrisonAIController ai;
   private STATE        state;

   // different unit types
   private int          bestAttacker      = 0;
   private int          bestDefender      = 0;
   private int          fastestGround     = 0;
   private int          fastestAir        = 0;
   private int          bestAirAttacker   = 0;
   private int          bestAirDefender   = 0;
   private int          farthestRange     = 0;
   private int          farthestAirRange  = 0;
   private int          quickestToProduce = 0;    // and not worker

   private boolean      foundEnemyBase    = false;
   private int          enemyBaseGuess;

   public HarrisonArmyManager(HarrisonAIController ai){
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
      enemyBaseGuess = HarrisonMapUtil.position(HarrisonMapUtil.WIDTH - ai.stockpiles.get(0).getX(), HarrisonMapUtil.HEIGHT - ai.stockpiles.get(0).getY());
   }

   @Override public void update(){
      for(Unit unit : ai.gameState.getOtherUnits()){
         if(unit.isBuilding()){
            HarrisonBuildingUnitController bc = new HarrisonBuildingUnitController(unit, ai);
            if(!ai.enemyBuildings.contains(bc)){
               ai.enemyBuildings.add(bc);
               foundEnemyBase = true;
            }
         }
         else{
            HarrisonUnitController uc = new HarrisonUnitController(unit, ai);
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
            HarrisonUnitQueue.requestUnit(bestAttacker, 50);
            HarrisonUnitQueue.requestUnit(bestAirAttacker, 40);
            enemyBasePos = findRandomEnemyBuilding();
            if(enemyBasePos != -1){
               for(HarrisonArmyUnitController uc : ai.armyUnits){
                  uc.act();
                  enemyBasePos = findRandomEnemyBuilding();
                  if(uc.actions.size() <= 0){
                     boolean canAttack = false;
                     ArrayList<UnitAction> legalActions = uc.unit.getActions();
                     for(UnitAction action : legalActions){
                        if(action.getType() == UnitAction.ATTACK && (HarrisonMapUtil.map[HarrisonMapUtil.position(action.getTargetX(), action.getTargetY())] & GameState.MAP_PLAYER) == 0){
                           uc.addAction(action, HarrisonMapUtil.trafficMap, HarrisonMapUtil.position(uc), ai.currentTurn, ai.currentTurn + uc.getAttackSpeed());
                           int time = ai.currentTurn;
                           for(int i = 0; i < uc.getAttackSpeed() - 1; i++){
                              uc.addAction(new UnitAction(uc.unit, UnitAction.NONE, uc.getX(), uc.getY(), -1), HarrisonMapUtil.trafficMap, HarrisonMapUtil.position(
                                      uc ), time, time + 1);
                              time++;
                           }
                           canAttack = true;
                           break;
                        }
                     }
                     if(!canAttack){
                        moveTowards(uc, enemyBasePos % HarrisonMapUtil.WIDTH, enemyBasePos / HarrisonMapUtil.WIDTH);
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
            HarrisonUnitQueue.requestUnit(bestAirAttacker, 40);
            HarrisonUnitQueue.requestUnit(bestAttacker, 40);
            HarrisonUnitQueue.requestUnit(farthestRange, 40);
            enemyUnitPos = findRandomEnemyUnit();
            if(enemyUnitPos != -1){
               for(HarrisonArmyUnitController uc : ai.armyUnits){
                  uc.act();
                  enemyUnitPos = findRandomEnemyUnit();
                  if(uc.actions.size() <= 0){
                     boolean canAttack = false;
                     ArrayList<UnitAction> legalActions = uc.unit.getActions();
                     for(UnitAction action : legalActions){
                        if(action.getType() == UnitAction.ATTACK && (HarrisonMapUtil.map[HarrisonMapUtil.position(action.getTargetX(), action.getTargetY())] & GameState.MAP_PLAYER) == 0){
                           uc.addAction(action, HarrisonMapUtil.trafficMap, HarrisonMapUtil.position(uc), ai.currentTurn, ai.currentTurn + uc.getAttackSpeed());
                           int time = ai.currentTurn;
                           for(int i = 0; i < uc.getAttackSpeed() - 1; i++){
                              uc.addAction(new UnitAction(uc.unit, UnitAction.NONE, uc.getX(), uc.getY(), -1), HarrisonMapUtil.trafficMap, HarrisonMapUtil.position(
                                      uc ), time, time + 1);
                              time++;
                           }
                           canAttack = true;
                           break;
                        }
                     }
                     if(!canAttack){
                        moveTowards(uc, enemyUnitPos % HarrisonMapUtil.WIDTH, enemyUnitPos / HarrisonMapUtil.WIDTH);
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
            HarrisonUnitQueue.requestUnit(bestAttacker, 50);
            HarrisonUnitQueue.requestUnit(bestDefender, 50);
            HarrisonUnitQueue.requestUnit(bestAirAttacker, 40);

            for(HarrisonArmyUnitController uc : ai.armyUnits){
               if(uc.actions.size() <= 0){ // no actions?!?!?
                  moveTowards(uc, HarrisonMapUtil.WIDTH / 2, HarrisonMapUtil.HEIGHT / 2);
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

            HarrisonUnitQueue.requestUnit(bestAttacker, 50);
            HarrisonUnitQueue.requestUnit(farthestRange, 50);
            HarrisonUnitQueue.requestUnit(bestAirAttacker, 40);
            for(HarrisonArmyUnitController uc : ai.armyUnits){
               uc.act();
               if(uc.actions.size() <= 0){
                  boolean canAttack = false;
                  ArrayList<UnitAction> legalActions = uc.unit.getActions();
                  for(UnitAction action : legalActions){
                     if(action.getType() == UnitAction.ATTACK && (HarrisonMapUtil.map[HarrisonMapUtil.position(action.getTargetX(), action.getTargetY())] & GameState.MAP_PLAYER) == 0){
                        uc.addAction(action, HarrisonMapUtil.trafficMap, HarrisonMapUtil.position(uc), ai.currentTurn, ai.currentTurn + uc.getAttackSpeed());
                        int time = ai.currentTurn;
                        for(int i = 0; i < uc.getAttackSpeed() - 1; i++){
                           uc.addAction(new UnitAction(uc.unit, UnitAction.NONE, uc.getX(), uc.getY(), -1), HarrisonMapUtil.trafficMap, HarrisonMapUtil.position(
                                   uc ), time, time + 1);
                           time++;
                        }
                        canAttack = true;
                        break;
                     }
                  }
                  if(!canAttack){
                     if(!foundEnemyBase){
                        moveTowards(uc, enemyBaseGuess % HarrisonMapUtil.WIDTH, enemyBaseGuess / HarrisonMapUtil.WIDTH);
                     }
                     else{
                        enemyBasePos = findRandomEnemyBuilding();
                        enemyUnitPos = findRandomEnemyUnit();
                        if(enemyBasePos != -1){
                           moveTowards(uc, enemyBasePos % HarrisonMapUtil.WIDTH, enemyBasePos / HarrisonMapUtil.WIDTH);
                        }
                        else
                           if(enemyUnitPos != -1){
                              moveTowards(uc, enemyUnitPos % HarrisonMapUtil.WIDTH, enemyUnitPos / HarrisonMapUtil.WIDTH);
                           }
                           else{
                              ArrayList<Integer> pos = new ArrayList<Integer>();
                              Random r = new Random();
                              int c = r.nextInt(4);
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
                                       uc.addAction(new UnitAction(uc.unit, UnitAction.MOVE, path.get(i)[0] % HarrisonMapUtil.WIDTH, path.get(i)[0] / HarrisonMapUtil.WIDTH, -1), HarrisonMapUtil.trafficMap, path.get(i)[0], path.get(i)[1], path.get(i)[1] + uc.unit.getMoveSpeed());
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
                           scout.addAction(new UnitAction(scout.unit, UnitAction.MOVE, path.get(i)[0] % HarrisonMapUtil.WIDTH, path.get(i)[0] / HarrisonMapUtil.WIDTH, -1), HarrisonMapUtil.trafficMap, path.get(i)[0], path.get(i)[1], path.get(i)[1] + scout.unit.getMoveSpeed());
                        }
                     }
                  }
                  else{
                     // turn scout into builder (for cheesing)
                     ai.wantedScouts = 0;
                     ai.wantedBuilders = 3;
                     HarrisonBuilderUnitController wc = new HarrisonBuilderUnitController(ai.scouts.get(0).unit, ai);
                     toRemove.add(ai.scouts.get(0));
                     ai.builders.add(wc);

                     HarrisonUnitQueue.requestBuilding(HarrisonAIController.SOLDIEROFFICE, 5500, HarrisonMapUtil.position(wc) - 1);
                     HarrisonUnitQueue.requestBuilding(HarrisonAIController.SOLDIEROFFICE, 5100, HarrisonMapUtil.position(wc) + 3);
                     HarrisonUnitQueue.requestBuilding(HarrisonAIController.SOLDIEROFFICE, 4200, HarrisonMapUtil.position(wc) + 4);
                     HarrisonUnitQueue.requestBuilding(HarrisonAIController.AIRPORT, 4000, HarrisonMapUtil.position(wc) + 4 + HarrisonMapUtil.WIDTH * 3);

                     state = STATE.Cheese;
                  }
               }
               scout.act();
            }
            for(HarrisonUnitController uc : toRemove){
               ai.scouts.remove(uc);
            }
            break;
      }
   }

   private int findRandomEnemyBuilding(){
      if(ai.enemyBuildings.size() == 0){ return -1; }
      Random r = new Random();
      int choice = r.nextInt(ai.enemyBuildings.size());
      HarrisonUnitController enemy = ai.enemyBuildings.get(choice);
      if(enemy.unit.getHP() <= 0){
         ai.enemyBuildings.remove(enemy);
         return findRandomEnemyBuilding();
      }
      return HarrisonMapUtil.position(ai.enemyBuildings.get(choice));
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
            uc.addAction(new UnitAction(uc.unit, UnitAction.MOVE, path.get(i)[0] % HarrisonMapUtil.WIDTH, path.get(i)[0] / HarrisonMapUtil.WIDTH, -1), HarrisonMapUtil.trafficMap, path.get(i)[0], path.get(i)[1], path.get(i)[1] + uc.unit.getMoveSpeed());
         }
      }

   }

   private int findRandomEnemyUnit(){
      if(ai.enemyUnits.size() == 0){ return -1; }
      Random r = new Random();
      int choice = r.nextInt(ai.enemyUnits.size());
      HarrisonUnitController enemy = ai.enemyUnits.get(choice);
      if(enemy.unit.getHP() <= 0){
         ai.enemyUnits.remove(enemy);
         return findRandomEnemyUnit();
      }
      return HarrisonMapUtil.position(enemy);
   }

   private enum STATE{
      Establish, Attack, Buildup, Cheese, Raid
   }

}
