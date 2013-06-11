package ai.teamharrison;

/** Created By: Harrison Katz on Date: 6/9/13 */
public class HarrisonWantedUnit implements Comparable<HarrisonWantedUnit>{
   public int     unitType;
   public int     priority;
   public int     location;
   public boolean beingBuilt;

   public HarrisonWantedUnit(int unitType, int priority, int location){
      this.unitType = unitType;
      this.priority = priority;
      this.location = location;
      this.beingBuilt = false;
   }

   @Override public int compareTo(HarrisonWantedUnit o){
      return o.priority - this.priority;
   }
}
