package ai.teamharrison;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * \brief HarrisonTraffic used by units so that complex routing can be done where they do not collide with each other
 * 
 * @author Jeff Bernard
 * 
 */
public class HarrisonTrafficMap{
   private ArrayList<PriorityQueue<HarrisonTraffic>> map;

   /** < the traffic map */

   /**
    * Creates a new traffic map
    * 
    * @param size size of the map
    */
   public HarrisonTrafficMap(int size){
      map = new ArrayList<PriorityQueue<HarrisonTraffic>>();
      for(int i = 0; i < size; i++){
         map.add(new PriorityQueue<HarrisonTraffic>());
      }
   }

   /**
    * Updates the traffic map
    * 
    * @param turn the current turn number
    */
   public void update(int turn){
      for(int i = 0; i < map.size(); i++){
         if(map.get(i).size() != 0 && map.get(i).peek().end != -1 && map.get(i).peek().end <= turn){
            map.get(i).poll();
         }
      }
   }

   /**
    * Determines if the traffic can pass through this location at the given time
    * 
    * @param location the location in the map
    * @param start when the traffic will start
    * @param end when the traffic will end
    * @return true if the traffic is valid, otherwise false
    */
   public boolean valid(int location, int start, int end){
      for(Iterator<HarrisonTraffic> it = map.get(location).iterator(); it.hasNext();){
         HarrisonTraffic i = it.next();
         if(end == -1 || i.end == -1 || (i.start <= end && i.end >= start) || i.start == end || i.end == start){ return false; }
      }
      return true;
   }

   /**
    * Reserves the traffic at the specified location
    * 
    * @param location the location to reserve
    * @param harrisonTraffic the traffic
    */
   public void reserve(HarrisonTraffic harrisonTraffic){
      map.get(harrisonTraffic.location).add(harrisonTraffic);
   }

   /**
    * Unreserves traffic, for whatever reason
    * 
    * @param harrisonTraffic the traffic to remove
    */
   public void unreserve(HarrisonTraffic harrisonTraffic){
      map.get(harrisonTraffic.location).remove(harrisonTraffic);
   }
}
