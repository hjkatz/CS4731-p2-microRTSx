package ai.teamharrison;

import java.util.ArrayList;
import java.util.HashMap;

import rts.GameState;
import rts.units.Unit;

public class HarrisonMapUtil{

   public static int[]        map;
   public static int          WIDTH;
   public static int          HEIGHT;
   public static HarrisonTrafficMap   harrisonTrafficMap;
   public static HarrisonAIController ai;

   public HarrisonMapUtil(HarrisonAIController ai){
      HarrisonMapUtil.ai = ai;
      map = ai.gameState.getMap();
      WIDTH = ai.gameState.getMapWidth();
      HEIGHT = ai.gameState.getMapHeight();
      harrisonTrafficMap = new HarrisonTrafficMap(map.length);
   }

   public static void update(){
      for(int i = 0; i < ai.gameState.getMap().length; i++){
         if((ai.gameState.getMap()[i] & GameState.MAP_FOG) == 0){
            map[i] = ai.gameState.getMap()[i];
         }
      }

      harrisonTrafficMap.update(ai.currentTurn);
   }

   public static int position(int x, int y){
      return x + y * WIDTH;
   }

   public static int position(HarrisonUnitController uc){
      return uc.getX() + uc.getY() * WIDTH;
   }

   public static int distance(HarrisonUnitController a, HarrisonUnitController b){
      int first = a.getX() - b.getX();
      int second = a.getY() - b.getY();
      double third = Math.pow(first, 2);
      double fourth = Math.pow(second, 2);
      return (int) Math.sqrt(third + fourth);
   }

   public static int distance(int l1, int l2){
      int first = (l1 % HarrisonMapUtil.WIDTH) - (l2 % HarrisonMapUtil.WIDTH);
      int second = (l1 / HarrisonMapUtil.WIDTH) - (l2 / HarrisonMapUtil.WIDTH);
      double third = Math.pow(first, 2);
      double fourth = Math.pow(second, 2);
      return (int) Math.sqrt(third + fourth);
   }

   public static ArrayList<Integer> getSurroundingPositions(int pos){
      ArrayList<Integer> positions = new ArrayList<Integer>();

      int left = pos - 1;
      int right = pos + 1;
      int up = pos - WIDTH;
      int down = pos + WIDTH;

      int bitmask = GameState.MAP_NEUTRAL | GameState.MAP_NONPLAYER | GameState.MAP_PLAYER | GameState.MAP_WALL;

      if(left >= 0){
         if((map[left] & bitmask) == 0){
            positions.add(left);
         }
      }
      if(right < WIDTH){
         if((map[right] & bitmask) == 0){
            positions.add(right);
         }
      }
      if(up >= 0){
         if((map[up] & bitmask) == 0){
            positions.add(up);
         }
      }
      if(down < HEIGHT){
         if((map[down] & bitmask) == 0){
            positions.add(down);
         }
      }

      return positions;
   }

   /**
    * Gets the path to a location (reversed)
    * 
    * @param unit
    * @param destinations
    */
   public static ArrayList<Integer[]> get_path(Unit unit, int start, int turn_start, ArrayList<Integer> destinations){
      // multi-destination A*
      ArrayList<Integer> closed = new ArrayList<Integer>();
      ArrayList<Integer> open = new ArrayList<Integer>();
      open.add(start);
      HashMap<Integer, Integer> came_from = new HashMap<Integer, Integer>();
      HashMap<Integer, Integer> g_score = new HashMap<Integer, Integer>();
      HashMap<Integer, Integer> f_score = new HashMap<Integer, Integer>();
      g_score.put(start, 0);
      f_score.put(start, h_score(start, destinations));

      while(open.size() > 0){
         int m = 0;
         int current = open.get(m);
         for(int i = 1; i < open.size(); i++){
            if(f_score.get(open.get(i)) < f_score.get(current)){
               current = open.get(i);
               m = i;
            }
         }
         int time = g_score.get(current) * unit.getMoveSpeed() + turn_start;
         int end = time + unit.getMoveSpeed();

         if(destinations.contains(current)){
            ArrayList<Integer[]> path = new ArrayList<Integer[]>();
            if(current == start){ return path; }
            path.add(new Integer[]{current, time});
            while(came_from.get(current) != null && came_from.get(current) != start){
               current = came_from.get(current);
               time = g_score.get(current) * unit.getMoveSpeed() + turn_start;
               path.add(new Integer[]{current, time});
            }
            return path;
         }

         open.remove(m);
         closed.add(current);
         int next_g = g_score.get(current) + 1;
         int cx = current % WIDTH;
         int cy = current / WIDTH;
         int next = current - 1;
         if(cx > 0 && (destinations.contains(next) || (!closed.contains(next) && can_enter(unit, next, time, end)))){ // left
                                                                                                                      // exists
            if(!open.contains(next) || next_g < g_score.get(next)){
               came_from.put(next, current);
               g_score.put(next, next_g);
               f_score.put(next, next_g + h_score(next, destinations));
               if(!open.contains(next)){
                  open.add(next);
               }
            }
         }
         next = current + 1;
         if(cx < WIDTH - 1 && (destinations.contains(next) || (!closed.contains(next) && can_enter(unit, next, time, end)))){ // right
                                                                                                                              // exists
            if(!open.contains(next) || next_g < g_score.get(next)){
               came_from.put(next, current);
               g_score.put(next, next_g);
               f_score.put(next, next_g + h_score(next, destinations));
               if(!open.contains(next)){
                  open.add(next);
               }
            }
         }
         next = current - WIDTH;
         if(cy > 0 && (destinations.contains(next) || (!closed.contains(next) && can_enter(unit, next, time, end)))){ // up
                                                                                                                      // exists
            if(!open.contains(next) || next_g < g_score.get(next)){
               came_from.put(next, current);
               g_score.put(next, next_g);
               f_score.put(next, next_g + h_score(next, destinations));
               if(!open.contains(next)){
                  open.add(next);
               }
            }
         }
         next = current + WIDTH;
         if(cy < HEIGHT - 1 && (destinations.contains(next) || (!closed.contains(next) && can_enter(unit, next, time, end)))){ // down
                                                                                                                               // exists
            if(!open.contains(next) || next_g < g_score.get(next)){
               came_from.put(next, current);
               g_score.put(next, next_g);
               f_score.put(next, next_g + h_score(next, destinations));
               if(!open.contains(next)){
                  open.add(next);
               }
            }
         }
      }
      return null;
   }

   /**
    * Calculates the h from start to (a) goal
    * 
    * @param start
    * @param goals
    * @return
    */
   private static int h_score(int start, ArrayList<Integer> goals){
      int h = -1;
      for(int i = 0; i < goals.size(); i++){
         int dx = goals.get(i) % WIDTH - start % WIDTH;
         int dy = goals.get(i) / WIDTH - start / WIDTH;
         int dh = dx * dx + dy * dy;
         if(h == -1 || dh < h){
            h = dh;
         }
      }
      return h;
   }

   /**
    * Checks whether or not a unit can enter a location
    * 
    * @param unit the unit
    * @param location the location
    * @return whether or not
    */
   private static boolean can_enter(Unit unit, int location, int turn_start, int turn_end){
      if((map[location] & (GameState.MAP_NEUTRAL | GameState.MAP_NONPLAYER | GameState.MAP_PLAYER)) == 0 && ((map[location] & GameState.MAP_WALL) == 0 || unit.isFlying()) && harrisonTrafficMap.valid(location, turn_start, turn_end)){ return true; }
      return false;
   }
}
