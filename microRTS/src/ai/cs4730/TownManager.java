package ai.cs4730;

import rts.units.Unit;
import rts.units.UnitAction;

import java.util.ArrayList;
import java.util.HashMap;

public class TownManager extends Manager{

	private AIController	ai;

	public TownManager(AIController ai){
		this.ai = ai;
	}

	public static void changeBuildLocation(UnitController unitController, AIController ai){}

	@Override public void update(AIController ai){
		// update farms map
		for(Unit res : ai.gameState.getNeutralUnits()){
			if(res.isResources()){
				FarmUnitController fc = new FarmUnitController(res, ai);
				if(!ai.farms.contains(fc)){
					if(AIController.DEBUG){
						System.out.println("found a farm at " + res.getX() + ", " + res.getY());
					}
					ai.farms.add(new FarmUnitController(res, ai));
				}
			}
		}

		for(WorkerUnitController worker : ai.workers){
			worker.act(ai);// carry out action, it wont do anything if unit
			// doesnt have one

			if(worker.actions.size() <= 0) // no actions
			{
				// if(AIController.DEBUG){System.out.println("TM: ordering worker around");}
				FarmUnitController farm = getClosestFreeFarm(worker);
				if(farm != null){
					ArrayList<Integer> openings = new ArrayList<Integer>();
					openings.add(farm.getHarvestY() * MapUtil.WIDTH + farm.getHarvestX());

					ArrayList<Integer[]> rpath = MapUtil.get_path(worker.unit, worker.getY() * MapUtil.WIDTH + worker.getX(), ai.currentTurn, openings);

					int time = ai.currentTurn;
					int position = worker.getY() * MapUtil.WIDTH + worker.getX();

					if(rpath != null){ // is possible to reach goal
						boolean there = false;
						position = rpath.get(0)[0];
						if(rpath.size() == 0){
							rpath.add(new Integer[]{worker.unit.getX() + worker.unit.getY() * MapUtil.WIDTH, ai.currentTurn});
							there = true;
						}

						// set order queue
						if(!there){
							for(int i = rpath.size() - 1; i >= 0; i--){
								worker.addAction(new UnitAction(worker.unit, UnitAction.MOVE, rpath.get(i)[0] % MapUtil.WIDTH, rpath.get(i)[0] / MapUtil.WIDTH, -1), MapUtil.trafficMap, rpath.get(i)[0], rpath.get(i)[1], rpath.get(i)[1] + worker.unit.getMoveSpeed());
							}
						}
						position = rpath.get(0)[0];
						time = rpath.get(0)[1];
					}
					else{
						worker.clearActions(MapUtil.trafficMap);
						if(AIController.DEBUG){
							System.out.println("TM: invalid path");
						}
					}

					// harvest
					worker.addAction(new UnitAction(worker.unit, UnitAction.HARVEST, farm.getX(), farm.getY(), -1), MapUtil.trafficMap, position, time, time + farm.getHarvestSpeed());
					time += farm.getHarvestSpeed();

					ArrayList<Integer> destination = new ArrayList<Integer>();
					destination.add(ai.stockpiles.get(0).getY() * MapUtil.WIDTH + ai.stockpiles.get(0).getX());

					// return
					rpath = MapUtil.get_path(worker.unit, position, time, destination);
					if(rpath != null){
						boolean there = false;
						if(rpath.size() <= 1){
							there = true;
							rpath.add(new Integer[]{position, time});
							if(rpath.size() == 1){
								rpath.add(new Integer[]{position, time});
							}
						}
						if(!there){
							for(int i = rpath.size() - 1; i >= 1; i--){
								// unit.actions.add(new UnitAction(worker.unit, UnitAction.MOVE, rpath.get(i)[0]%MapUtils.WIDTH, rpath.get(i)[0]/MapUtils.WIDTH,-1));
								// System.out.println("adding MOVE");
								worker.addAction(new UnitAction(worker.unit, UnitAction.MOVE, rpath.get(i)[0] % MapUtil.WIDTH, rpath.get(i)[0] / MapUtil.WIDTH, -1), MapUtil.trafficMap, rpath.get(i)[0], rpath.get(i)[1], rpath.get(i)[1] + worker.unit.getMoveSpeed());
							}
							position = rpath.get(0)[0];
							time = rpath.get(0)[1];
						}
						worker.addAction(new UnitAction(worker.unit, UnitAction.RETURN, position % MapUtil.WIDTH, position / MapUtil.WIDTH, -1), MapUtil.trafficMap, position, time, time + UnitAction.DEFAULT_COOLDOWN);
					}
					else{
						worker.clearActions(MapUtil.trafficMap);
						if(AIController.DEBUG){
							System.out.println("TM: invalid path");
						}
					}
				}
			}
		}

		for(BuildingUnitController stock : ai.stockpiles){// all the bases, tell em to make workers
			stock.act(ai);

			if(ai.workers.size() + ai.scouts.size() < ai.wantedScouts + ai.wantedWorkers){
				if(stock.actions.size() <= 0){ // no actions?!?!?
					int time = ai.currentTurn;
					int position = stock.getY() * MapUtil.WIDTH + stock.getX() + 1;
					stock.addAction(new UnitAction(stock.unit, UnitAction.BUILD, stock.getX(), stock.getY() + 1, ai.WORKER), MapUtil.trafficMap, position, time, time + ai.workers.get(0).getBuildSpeed());
					// add no actions for the rest of the build time so it doesnt keep giving it build orders each turn
					for(int i = 0; i < ai.workers.get(0).getBuildSpeed() - 1; i++){
						stock.addAction(new UnitAction(stock.unit, UnitAction.NONE, stock.getX(), stock.getY(), -1), MapUtil.trafficMap, position, time, time + 1);
					}
					if(AIController.DEBUG){
						System.out.println("TM: recruiting worker");
					}
				}
			}
		}

	}

	private FarmUnitController getClosestFreeFarm(WorkerUnitController worker){
		int x = worker.getX();
		int y = worker.getY();
		int minDistance = 10000; // large int =P
		FarmUnitController closest = null;
		for(FarmUnitController farm : ai.farms){
			if(farm.free){
				int distance = (int) (Math.sqrt(((x - farm.getHarvestX()) ^ 2) + ((y - farm.getHarvestY()) ^ 2)));
				if(distance < minDistance){
					closest = farm;
					minDistance = distance;
				}
			}
		}

		// if ( closest != null )
		// {
		// closest.free = false;
		// }
		return closest;
	}

	@Override public void assignUnits(AIController ai){
		// Grab my units!!!
		ArrayList<UnitController> toRemove = new ArrayList<UnitController>();
		for(UnitController unit : ai.freeUnits){
			if(unit.getClass() == WorkerUnitController.class){
				ai.workers.add((WorkerUnitController) unit);
				if(AIController.DEBUG){
					System.out.println("TM: acquired worker");
				}
				toRemove.add(unit);
			}
			else
				if(unit.getClass() == BuildingUnitController.class){
					BuildingUnitController bu = (BuildingUnitController) unit;
					if(bu.isStockpile()){
						ai.stockpiles.add(bu);
						if(AIController.DEBUG){
							System.out.println("TM: acquired stockpile");
						}
					}
					else{
						ai.buildings.add(bu);
						if(AIController.DEBUG){
							System.out.println("TM: acquired building");
						}
					}
					toRemove.add(unit);
				}
		}

		// remove any units from freeUnits that were assigned
		for(UnitController unit : toRemove){
			ai.freeUnits.remove(unit);
		}
	}

	public int numWorkers(){
		return ai.workers.size();
	}
}
