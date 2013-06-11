package ai.teamharrison;

import java.util.PriorityQueue;

/** Created By: Harrison Katz on Date: 6/9/13 */
public class HarrisonUnitQueue{
   public static PriorityQueue<HarrisonWantedUnit> wantedArmyUnits;
   public static PriorityQueue<HarrisonWantedUnit> wantedBuildingUnits;

   public HarrisonUnitQueue(){
      wantedArmyUnits = new PriorityQueue<HarrisonWantedUnit>();
      wantedBuildingUnits = new PriorityQueue<HarrisonWantedUnit>();
   }

   public static void requestUnit(int unitType, int priority){
      wantedArmyUnits.add(new HarrisonWantedUnit(unitType, priority, -1));
   }

   public static void requestBuilding(int unitType, int priority, int location){
      wantedBuildingUnits.add(new HarrisonWantedUnit(unitType, priority, location));
   }
}
