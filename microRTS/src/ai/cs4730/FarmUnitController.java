package ai.cs4730;

import rts.GameState;
import rts.units.Unit;

/** Created By: harrison on Date: 6/5/13 */
public class FarmUnitController extends UnitController{
   private int x;
   private int y;
   private int harvestSpeed;
   private int harvestAmount;

   public FarmUnitController(Unit unit, AIController ai){
      super(unit, ai);

      y = unit.getY();
      x = unit.getX();
      harvestSpeed = unit.getHarvestSpeed();
      harvestAmount = unit.getHarvestAmount();

      updateOpenings();
   }

   public void updateOpenings(){
      if(y > 0 && (MapUtil.map[(y - 1) * MapUtil.WIDTH + x] & (GameState.MAP_FOG | GameState.MAP_WALL | GameState.MAP_NEUTRAL)) == 0){
         int loc = (((y - 1) * MapUtil.WIDTH) + (x));
         if(!ai.farmOpenings.containsKey(loc)){
            ai.farmOpenings.put(loc, true);
         }
      }
      if(y < MapUtil.HEIGHT - 1 && (MapUtil.map[(y + 1) * MapUtil.WIDTH + x] & (GameState.MAP_FOG | GameState.MAP_WALL | GameState.MAP_NEUTRAL)) == 0){
         int loc = (((y + 1) * MapUtil.WIDTH) + (x));
         if(!ai.farmOpenings.containsKey(loc)){
            ai.farmOpenings.put(loc, true);
         }
      }
      if(x > 0 && (MapUtil.map[y * MapUtil.WIDTH + (x - 1)] & (GameState.MAP_FOG | GameState.MAP_WALL | GameState.MAP_NEUTRAL)) == 0){
         int loc = (((y) * MapUtil.WIDTH) + (x - 1));
         if(!ai.farmOpenings.containsKey(loc)){
            ai.farmOpenings.put(loc, true);
         }
      }
      if(x < MapUtil.WIDTH - 1 && (MapUtil.map[y * MapUtil.WIDTH + (x + 1)] & (GameState.MAP_FOG | GameState.MAP_WALL | GameState.MAP_NEUTRAL)) == 0){
         int loc = (((y) * MapUtil.WIDTH) + (x + 1));
         if(!ai.farmOpenings.containsKey(loc)){
            ai.farmOpenings.put(loc, true);
         }
      }
   }

   public boolean hasThisOpening(int pos){
      if(y > 0 && (MapUtil.map[(y - 1) * MapUtil.WIDTH + x] & (GameState.MAP_FOG | GameState.MAP_WALL | GameState.MAP_NEUTRAL)) == 0){
         int loc = (((y - 1) * MapUtil.WIDTH) + (x));
         if(loc == pos){ return true; }
      }
      if(y < MapUtil.HEIGHT - 1 && (MapUtil.map[(y + 1) * MapUtil.WIDTH + x] & (GameState.MAP_FOG | GameState.MAP_WALL | GameState.MAP_NEUTRAL)) == 0){
         int loc = (((y + 1) * MapUtil.WIDTH) + (x));
         if(loc == pos){ return true; }
      }
      if(x > 0 && (MapUtil.map[y * MapUtil.WIDTH + (x - 1)] & (GameState.MAP_FOG | GameState.MAP_WALL | GameState.MAP_NEUTRAL)) == 0){
         int loc = (((y) * MapUtil.WIDTH) + (x - 1));
         if(loc == pos){ return true; }
      }
      if(x < MapUtil.WIDTH - 1 && (MapUtil.map[y * MapUtil.WIDTH + (x + 1)] & (GameState.MAP_FOG | GameState.MAP_WALL | GameState.MAP_NEUTRAL)) == 0){
         int loc = (((y) * MapUtil.WIDTH) + (x + 1));
         if(loc == pos){ return true; }
      }
      return false;
   }

   public int getHarvestSpeed(){
      return harvestSpeed;
   }

   public int getHarvestAmount(){
      return harvestAmount;
   }

   public int getX(){
      return x;
   }

   public int getY(){
      return y;
   }
}
