package ai.teamharrison;

import java.util.ArrayList;

import rts.units.Unit;
import rts.units.UnitAction;

/**
 * Represents a general unit including different unit types and buildings
 */
public class HarrisonUnitController{

   public Unit                        unit;
   public HarrisonAIController        ai;
   public ArrayList<UnitAction>       actions;         // this is a queue
   public int                         lastAction;
   private int                        maxHp;
   private int                        vision;
   private int                        type;
   private int                        buildTime;
   private long                       id;
   private ArrayList<Integer>         cost;
   private ArrayList<HarrisonTraffic> traffic;
   private HarrisonTraffic            last_traffic;
   private HarrisonTraffic            building_traffic;
   private boolean                    hasActed;

   public HarrisonUnitController(Unit unit, HarrisonAIController ai){
      this.unit = unit;
      this.ai = ai;
      actions = new ArrayList<UnitAction>();

      maxHp = unit.getMaxHP();
      cost = unit.getCost();
      type = unit.getType();
      buildTime = unit.getBuildSpeed();
      vision = unit.getVision();
      hasActed = false;
      id = unit.getID();
      cost = unit.getCost();

      if(unit.isBuilding()){
         building_traffic = new HarrisonTraffic(unit.getX() + unit.getY() * HarrisonMapUtil.WIDTH, ai.currentTurn, -1);
         HarrisonMapUtil.trafficMap.reserve(building_traffic);
      }
      else{
         building_traffic = null;
      }

      last_traffic = null;
      traffic = new ArrayList<HarrisonTraffic>();
   }

   public ArrayList<UnitAction> getActions(){
      return unit.getActions();
   }

   public UnitAction getAction(){
      return unit.getAction();
   }

   public void setAction(UnitAction act){
      unit.setAction(act);
   }

   public boolean hasAction(){
      return unit.hasAction();
   }

   public int getType(){
      return type;
   }

   public boolean lastActionSucceeded(){
      return unit.lastActionSucceeded();
   }

   public int getX(){
      return unit.getX();
   }

   public int getY(){
      return unit.getY();
   }

   public int getHP(){
      return unit.getHP();
   }

   public int getMaxHP(){
      return maxHp;
   }

   public int getVision(){
      return vision;
   }

   public ArrayList<Integer> getCost(){
      return cost;
   }

   public int getBuildTime(){
      return buildTime;
   }

   public int getCost(int resourceType){
      if(resourceType >= 0 && resourceType < cost.size()){ return cost.get(resourceType); }
      return -1;
   }

   public void act(){
      if(unit.copyStats().seen_dead){
         death();
      }
      else
         if(!unit.hasAction()){
            if(hasActed && actions.size() != 0){
               if(unit.lastActionSucceeded()){
                  if(actions.size() != 0){
                     actions.remove(0);
                  }
                  if(traffic.size() != 0){
                     last_traffic = traffic.get(0);
                     traffic.remove(0);
                  }
               }
            }
            nextAction();
         }
   }

   public void nextAction(){
      if(actions.size() != 0){
         hasActed = false;
         UnitAction action = actions.get(0);

         // ensure that action can be performed
         for(int i = 0; i < unit.getActions().size(); i++){
            if(action.equals(unit.getActions().get(i))){
               hasActed = true;
               lastAction = action.getType();
               unit.setAction(unit.getActions().get(i));
               return;
            }
         }

         clearActions(HarrisonMapUtil.trafficMap);
      }
   }

   /**
    * Clears all actions for this unit
    * 
    * @param traffic_map the traffic map
    */
   public void clearActions(HarrisonTrafficMap traffic_map){
      actions.clear();
      for(int i = 0; i < traffic.size(); i++){
         traffic_map.unreserve(traffic.get(i));
      }
      traffic.clear();
      if(last_traffic != null){
         traffic_map.unreserve(last_traffic);
         last_traffic = null;
      }
   }

   /**
    * Adds an action to this unit
    * 
    * @param action the action to add
    * @param traffic_map the traffic map this unit is in
    * @param location the traffic location this action corresponds with
    * @param start the traffic start
    * @param end the traffic end
    */
   public void addAction(UnitAction action, HarrisonTrafficMap traffic_map, int location, int start, int end){
      actions.add(action);
      if(traffic_map != null){
         HarrisonTraffic t = new HarrisonTraffic(location, start, end);
         traffic_map.reserve(t);
         traffic.add(t);
      }
   }

   /**
    * Removes the unit
    * 
    * @param traffic_map the traffic map
    */
   public void remove(HarrisonTrafficMap traffic_map){
      actions.clear();
      for(int i = 0; i < traffic.size(); i++){
         traffic_map.unreserve(traffic.get(i));
      }
      traffic.clear();
      if(building_traffic != null){
         traffic_map.unreserve(building_traffic);
      }
      if(last_traffic != null){
         traffic_map.unreserve(last_traffic);
         last_traffic = null;
      }
   }

   @Override public int hashCode(){
      return unit.hashCode();
   }

   @Override public boolean equals(Object other){
      HarrisonUnitController uc = (HarrisonUnitController) other;
      if(uc.getID() == id){ return true; }
      return false;
   }

   public long getID(){
      return id;
   }

   public void death(){
      remove(HarrisonMapUtil.trafficMap);
      ai.deadUnits.add(this);
   }
}
