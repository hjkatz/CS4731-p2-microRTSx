package ai.teamharrison;

/**
 * \brief HarrisonTraffic used by units so that complex routing can be done where they do not collide with each other
 * 
 * @author Jeff Bernard
 * 
 */
public class HarrisonTraffic implements Comparable<Object>{

   public int location;
   /** < the lcoation of the traffic */
   public int start;
   /** < start of traffic */
   public int end;

   /** < end of traffic */

   /**
    * Creates a traffic object
    * 
    * @param _location where this traffic is
    * @param _start when this traffic starts
    * @param _end when this traffic ends
    */
   public HarrisonTraffic(int _location, int _start, int _end){
      location = _location;
      start = _start;
      end = _end;
   }

   /**
    * Compares this traffic object with another
    * 
    * @param arg0 the one to compare with
    * @return -1 < , 1 > , 0 =
    */
   @Override public int compareTo(Object arg0){
      HarrisonTraffic other = (HarrisonTraffic) arg0;
      if(other.start < start){
         return 1;
      }
      else
         if(other.start > start){ return -1; }
      return 0;
   }

}
