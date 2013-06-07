
package ai.cs4730;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import rts.units.UnitAction;
import rts.units.UnitStats;

public class ArmyManager extends Manager
{
    
	ArrayList<UnitController> units = new ArrayList<UnitController>();
	ArrayList<UnitController> scouts = new ArrayList<UnitController>();
	
	private int wantedScouts = 1;
	
	//game logic variable
	private boolean foundEnemyBase = false;
	private ArrayList<int[]> enemyBuildings = new ArrayList<int[]>(); //int arrays of size 3: [0] = x, [1] = y, [2] = type of building
	
    private enum STATE
    {
        Explore, Attack, Buildup, Cheese
    };
    
    private STATE state;
    
    public ArmyManager()
    {
        state = STATE.Explore;
    }
    
    @Override
    public void update( AIController ai )
    {
    	switch(state){
    		case Attack:
    			break;
    		case Buildup:
    			break;
    		case Cheese:
    			break;
    		case Explore:
    			//request a scout after some workers are gathering resources
    	    	//if(ai.townManager.numWorkers() > 4){
    	    	//	wantedScouts = 1;
    	    	//}
    	    	
    	    	for ( UnitController scout : scouts )
    	        {
    	            if ( !scout.hasAction() ){
    	            	if(AIController.DEBUG){System.out.println("scout needs action");}
    	            	if(!foundEnemyBase){
    	            		if(AIController.DEBUG){System.out.println("look for enemy base");}
    	            		//find the enemy base first, count on it being in the opposite corner
    	            		int targetX = MapUtil.WIDTH - ai.townManager.stockpiles.get(0).getX();
    	            		int targetY = MapUtil.HEIGHT - ai.townManager.stockpiles.get(0).getY();
    	            		ArrayList<Integer> destination = new ArrayList<Integer>();
    	            		destination.add(targetX + targetY * MapUtil.WIDTH);
    	            		//path to estimated location of enemy base
    	            		ArrayList<Integer[]> path = MapUtil.get_path(scout.unit, scout.getX() + scout.getY() * MapUtil.WIDTH, ai.currentTurn, destination);
    	            		
    	            		if (path != null) { // is possible to reach goal
    	            			// set order queue
    	            			path.add(new Integer[]{scout.getX() + scout.getY() * MapUtil.WIDTH, ai.currentTurn});
    	            			for (int i = path.size() - 1; i >= 0; i--) {
    	            				
    	            				scout.addAction(new UnitAction(scout.unit.copyStats(), UnitAction.MOVE, path.get(i)[0]% MapUtil.WIDTH, path.get(i)[0]/ MapUtil.WIDTH,-1), MapUtil.trafficMap, path.get(i)[0], path.get(i)[1], path.get(i)[1] + scout.unit.getMoveSpeed());
    	            				//scout.setAction(new UnitAction(scout.unit.copyStats(), UnitAction.MOVE, path.get(i)[0]% MapUtil.WIDTH, path.get(i)[0]/ MapUtil.WIDTH,-1));
    	            			}
    	            		}
    	            	}
    	            }
    	            else{
    	            	//execute actions?
    	            }
    	        }
    			break;
    	}
    }
    
    @Override
    public void assignUnits( AIController ai )
    {
    	ArrayList<UnitController> toRemove = new ArrayList<UnitController>();
    	for(UnitController u : ai.freeUnits)
    	{
    		if(u.getClass() == ArmyUnitController.class){
    			units.add(u);
    			toRemove.add(u);
    		}
    		else if(wantedScouts > scouts.size() && u.getClass() == WorkerUnitController.class)
    		{
    			scouts.add(u);
    			if(AIController.DEBUG){System.out.println("acquired scout");}
    			toRemove.add(u);
    		}
    	}
    	
    	//remove any units from freeUnits that were assigend
        for( UnitController u : toRemove){
        	ai.freeUnits.remove(u);
        }
    }
    
}
