package ai.cs4730;

/** Created By: Harrison Katz on Date: 6/9/13 */
public class WantedUnit implements Comparable<WantedUnit>{
   public int     unitType;
   public int     priority;
   public int     location;
   public boolean beingBuilt;

   public WantedUnit(int unitType, int priority, int location){
      this.unitType = unitType;
      this.priority = priority;
      this.location = location;
      this.beingBuilt = false;
   }

   @Override public int compareTo(WantedUnit o){
      return this.priority - o.priority;
   }
}
