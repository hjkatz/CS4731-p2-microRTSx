package ai.cs4730;

import java.util.PriorityQueue;

/** Created By: Harrison Katz on Date: 6/9/13 */
public class UnitQueue{
   public static PriorityQueue<WantedUnit> wantedArmyUnits;
   public static PriorityQueue<WantedUnit> wantedBuildingUnits;

   public UnitQueue(){
      wantedArmyUnits = new PriorityQueue<WantedUnit>();
      wantedBuildingUnits = new PriorityQueue<WantedUnit>();
   }

   public static void requestUnit(int unitType, int priority){
      wantedArmyUnits.add(new WantedUnit(unitType, priority, -1));
   }

   public static void requestBuilding(int unitType, int priority, int location){
      wantedBuildingUnits.add(new WantedUnit(unitType, priority, location));
   }
}
