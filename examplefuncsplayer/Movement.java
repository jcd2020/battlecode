package upgirdplayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;

import java.util.Iterator;

import battlecode.common.*;

public class Movement {
	
	private static final int FLEEING_MIN_BULLET_THRESHOLD = 300;
	private static final double FLEEING_RETALIATION_PROBABILITY = .4;
	private static final float FLEEING_DEGREE_ADJUSTMENT = 10;
	private static final double GARDENER_PRODUCTION_PROB = 0;
	private static final int GARDENER_BC = 0;
	private static final int INCOME_BC = 0;
	static RobotController rc = RobotPlayer.rc;
	static MapLocation myLoc = rc.getLocation();
	
	
	  /**
     * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        return tryMove(dir,20,3);
    }
    
    
    /**
     * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
     *
     * @param dir The intended direction of movement
     * @param degreeOffset Spacing between checked directions (degrees)
     * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
     * @return true if a move was performed
     * @throws GameActionException
     */

    static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

        // First, try intended direction
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }

        // Now try a bunch of similar angles
        boolean moved = false;
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }
    
    static boolean isFleeing(){
    	if(Math.random() > .5){
    		return true;
    	}
    	else{return false;}
    }
    
    static boolean flee(MapLocation closeEn, int bulletCt, BulletInfo[] infoArray) throws GameActionException{
    	//Tries to move away from enemies without intersecting with bullets
    	Direction fleeDir = myLoc.directionTo(closeEn).opposite();
    	
    	//Sorts to get nearest bullet
    	if(infoArray.length != 0){
    		double min = Double.MAX_VALUE;
    		int index = 0;
    		for(int i = 0; i < infoArray.length; i++){
    			double dist = myLoc.distanceTo(infoArray[i].location);

    			if(dist < min){
    				min = dist;
    				index = i;
    			}
    		}
    		//Checks to see if the robot is going to hit a bullet
    		boolean notSafe = RobotPlayer.willCollideWithMe(infoArray[index]);
    		if(notSafe){
    			if(Math.random() < .5){
    				fleeDir = fleeDir.rotateLeftDegrees(FLEEING_DEGREE_ADJUSTMENT);
    			}
    			else{
    				fleeDir = fleeDir.rotateRightDegrees(FLEEING_DEGREE_ADJUSTMENT);
    			}
    		}
    	}
    	boolean fleeBool = tryMove(fleeDir, FLEEING_DEGREE_ADJUSTMENT, 2);
    	    	
    	if(fleeBool && bulletCt > FLEEING_MIN_BULLET_THRESHOLD && Math.random() > FLEEING_RETALIATION_PROBABILITY && rc.canFireSingleShot()){
    		rc.fireSingleShot(fleeDir.opposite());
    		return true;
    	}
    	else{
    		return false;
    	}
    }
    //Script to perform after sensing, broadcasting
    static Direction scoutMotion(Direction dir) throws GameActionException{
    	if(tryMove(dir, 5, 3) == false){
    		if(Math.random() < .5)
    		{
    			dir = dir.rotateRightRads((float) (Math.PI/2));
    		}
    		else
    		{
    			dir = dir.rotateLeftRads((float) (Math.PI/2));
    		}
    	}
    	return dir;
    }
     
    //Script to perform after sensing, broadcasting, and calculatingS
    static void archonAction() throws GameActionException{
    	if(rc.getRoundNum() == 1){
    		if(rc.canBuildRobot(RobotType.GARDENER, Direction.getEast())){
    			rc.buildRobot(RobotType.GARDENER, Direction.getEast());
    		}
    		else{rc.buildRobot(RobotType.GARDENER, Direction.getWest());}
    	}
    	//WHATEVER BROADCAST CHANNEL GARDENER CT IS ON, NOT 100000
    	else if((rc.readBroadcast(GARDENER_BC) < 2 && rc.readBroadcast(INCOME_BC) > 25) || 
    			(rc.readBroadcast(GARDENER_BC) < 5 && rc.readBroadcast(INCOME_BC) > 100) ||
    			(rc.readBroadcast(GARDENER_BC) < 7 && rc.readBroadcast(INCOME_BC) > 200)){
    		if(Math.random() < GARDENER_PRODUCTION_PROB){
    			if(rc.canBuildRobot(RobotType.GARDENER, Direction.getEast())){
    				rc.buildRobot(RobotType.GARDENER, Direction.getEast());
    			}
    			else{rc.buildRobot(RobotType.GARDENER, Direction.getWest());}
    		}
    	}
    	
    	Iterator<RobotPlayer.Robot> itr = RobotPlayer.enemies.iterator();
    	if(itr.hasNext()){
    		MapLocation en = new MapLocation(itr.next().x, itr.next().y);
    		flee(en, (int)rc.getTeamBullets(),rc.senseNearbyBullets());
    	}
    	else{
    		flee(rc.getLocation().translate((float).1, 0), (int)rc.getTeamBullets(),rc.senseNearbyBullets());
    	}
    }
    
    
    //Script for gardener to perform under normal circumstances
    //PUT IN APPROPRIATE BROADCAST CHANNELS
    
    //The boolean parameter is used to make sure a gardener is in 
    //defense mode until the tree is watered
    
    boolean gardenerNormal(boolean isWatering) throws GameActionException{
    	double bullets = rc.getTeamBullets();
    	double portionProducing = (750 + 2250/Math.sqrt(rc.getRoundNum())/3000);
    	double portionNeither = (1 - portionProducing);
    	double choice = Math.random();
    	
    	if(isWatering){
    		boolean watered = gardenerWatering();
    		return watered;
    	}
    	
    	else if(choice < portionProducing){
    		//Always construct a scout first
    		if(rc.readBroadcast(10000) == 0 && rc.canBuildRobot(RobotType.SCOUT, Direction.getEast())){
    			rc.buildRobot(RobotType.SCOUT, Direction.getEast());
    		}
    		else if(rc.readBroadcast(10000) == 0 && rc.canBuildRobot(RobotType.SCOUT, Direction.getWest())){
    			rc.buildRobot(RobotType.SCOUT, Direction.getWest());
    		}
    		else{
    			//Randomize between building robots and trees
    			if(Math.random() < .5){
    				int sw = (int)(Math.random() * 5);
    				//Numbers fairly arbitrary, to be tweaked after testing
    				
    				switch(sw){
    				case 0:
    					if((rc.readBroadcast(10000) < 5 && bullets > 200) ||
    							rc.readBroadcast(10000) < 15 && bullets > 500 || 
    							bullets > 700){
    						if(rc.canBuildRobot(RobotType.SOLDIER, Direction.getEast())){
    							rc.buildRobot(RobotType.SOLDIER, Direction.getEast());
    						}
    						else if(rc.canBuildRobot(RobotType.SOLDIER, Direction.getWest())){
    							rc.buildRobot(RobotType.SOLDIER, Direction.getWest());
    						}
    					}
    					break;
    					
    				case 1: 
    					if((rc.readBroadcast(10000) < 5 && bullets > 200) ||
    							rc.readBroadcast(10000) < 15 && bullets > 500 || 
    							bullets > 700){
    						if(rc.canBuildRobot(RobotType.SOLDIER, Direction.getEast())){
    							rc.buildRobot(RobotType.SOLDIER, Direction.getEast());
    						}
    						else if(rc.canBuildRobot(RobotType.SOLDIER, Direction.getWest())){
    							rc.buildRobot(RobotType.SOLDIER, Direction.getWest());
    						}
    					}
    					break;
    					
    				case 2: 
    					if((rc.readBroadcast(10000) < 3 && bullets > 400) ||
    							rc.readBroadcast(10000) < 6 && bullets > 600 || 
    							bullets > 700){
    						if(rc.canBuildRobot(RobotType.TANK, Direction.getEast())){
    							rc.buildRobot(RobotType.TANK, Direction.getEast());
    						}
    						else if(rc.canBuildRobot(RobotType.TANK, Direction.getWest())){
    							rc.buildRobot(RobotType.TANK, Direction.getWest());
    						}
    					}
    					break;
    					
    				case 3:
    					if((rc.readBroadcast(10000) < 3 && bullets > 300) ||
    							rc.readBroadcast(10000) < 6 && bullets > 500 || 
    							bullets > 800){
    						if(rc.canBuildRobot(RobotType.LUMBERJACK, Direction.getEast())){
    							rc.buildRobot(RobotType.LUMBERJACK, Direction.getEast());
    						}
    						else if(rc.canBuildRobot(RobotType.LUMBERJACK, Direction.getWest())){
    							rc.buildRobot(RobotType.LUMBERJACK, Direction.getWest());
    						}
    					}
    					break;
    					
    				case 4: 
    					if((rc.readBroadcast(10000) < 3 && bullets > 300)){
    						if(rc.canBuildRobot(RobotType.SCOUT, Direction.getEast())){
    							rc.buildRobot(RobotType.SCOUT, Direction.getEast());
    						}
    						else if(rc.canBuildRobot(RobotType.SCOUT, Direction.getWest())){
    							rc.buildRobot(RobotType.SCOUT, Direction.getWest());
    						}
    					}
    					break;
    					
    				}    			
    			}
    			
    			else{
    				if(bullets > 100 && rc.canPlantTree(Direction.getEast())){
    					rc.plantTree(Direction.getEast());
    				}
    				else if(bullets > 100 && rc.canPlantTree(Direction.getWest())){
    					rc.plantTree(Direction.getWest());
    				}
    			}	
    		}
    	}
    	// Move Randomly
    	else if (choice > portionProducing && choice < portionProducing + portionNeither){
    		double direction = Math.random();
    		if(direction < .25){
    			Movement.tryMove(Direction.getEast());
    		}
    		else  if(direction < .5){
    			Movement.tryMove(Direction.getWest());
    		}
    		else if(direction < .75){
    			Movement.tryMove(Direction.getNorth());
    		}
    		else{
    			Movement.tryMove(Direction.getSouth());
    		}
    	}
    	
    	//To be used as parameter for next call to this method
		return false;
    }
  
    private boolean gardenerWatering() throws GameActionException {
    	Iterator<RobotPlayer.Tree> itr = RobotPlayer.friendlyTrees.iterator();
    	if(itr.hasNext()){
    		MapLocation tree = new MapLocation(itr.next().x, itr.next().y);
    		if(rc.canInteractWithTree(tree)){
    			rc.water(tree);
    			return true;
    		}
    		else{
    			tryMove(myLoc.directionTo(tree), 5, 1);
    		}
    	}
    	return false;
	}
    
    


	
}
