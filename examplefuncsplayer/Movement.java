package upgirdplayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;

import java.util.Iterator;

import battlecode.common.*;

public class Movement {
	
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
    		double min = 100;
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
    				fleeDir = fleeDir.rotateLeftDegrees(10);
    			}
    			else{
    				fleeDir = fleeDir.rotateRightDegrees(10);
    			}
    		}
    	}
    	boolean fleeBool = tryMove(fleeDir, 10, 2);
    	    	
    	if(fleeBool && bulletCt > 300 && Math.random() > .4 && rc.canFireSingleShot()){
    		rc.fireSingleShot(fleeDir.opposite());
    		return true;
    	}
    	else{
    		return false;
    	}
    }
    //Script to perform after sensing, broadcasting
    static boolean scoutMotion(int scoutType) throws GameActionException{
    	if(scoutType == 0){
    		if(tryMove(Direction.getEast().rotateLeftRads((float)Math.PI), 5, 3)){
    			return true;
    		}
    		else if(tryMove(Direction.getWest().rotateLeftRads((float)Math.PI), 5, 3)){
    			return true;
    		}
    		else{return false;}
    				
    	}
    	else{
    		if(tryMove(Direction.getEast().rotateRightRads((float)Math.PI), 5, 3)){
    			return true;
    		}
    		else if(tryMove(Direction.getWest().rotateRightRads((float)Math.PI), 5, 3)){
    			return true;
    		}
    		else{return false;}
    				
    	}
    }
     
    //Script to perform after sensing, broadcasting, and calculating
    static void archonAction() throws GameActionException{
    	if(rc.getRoundNum() == 1){
    		if(rc.canBuildRobot(RobotType.GARDENER, Direction.EAST)){
    			rc.buildRobot(RobotType.GARDENER, Direction.EAST);
    		}
    		else{rc.buildRobot(RobotType.GARDENER, Direction.WEST);}
    	}
    	//WHATEVER BROADCAST CHANNEL GARDENER CT IS ON, NOT 100000
    	else if((rc.readBroadcast(100000) < 2 && rc.getTeamBullets() > 200) || 
    			(rc.readBroadcast(100000) < 5 && rc.getTeamBullets() > 800) ||
    			(rc.readBroadcast(100000) < 7 && rc.getTeamBullets() > 1200)){
    		if(Math.random() > .8){
    			if(rc.canBuildRobot(RobotType.GARDENER, Direction.EAST)){
    				rc.buildRobot(RobotType.GARDENER, Direction.EAST);
    			}
    			else{rc.buildRobot(RobotType.GARDENER, Direction.WEST);}
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
    
    boolean gardenerNormal(boolean isDefending) throws GameActionException{
    	double bullets = rc.getTeamBullets();
    	double portionProducing = (750 + 2250/Math.sqrt(rc.getRoundNum())/3000);
    	double portionNeither = (1 - portionProducing);
    	double choice = Math.random();
    	
    	if(isDefending){
    		boolean watered = gardenerDefend();
    		return watered;
    	}
    	
    	else if(choice < portionProducing){
    		//Always construct a scout first
    		if(rc.readBroadcast(10000) == 0 && rc.canBuildRobot(RobotType.SCOUT, Direction.EAST)){
    			rc.buildRobot(RobotType.SCOUT, Direction.EAST);
    		}
    		else if(rc.readBroadcast(10000) == 0 && rc.canBuildRobot(RobotType.SCOUT, Direction.WEST)){
    			rc.buildRobot(RobotType.SCOUT, Direction.WEST);
    		}
    		else{
    			//Randomize between building robots and trees
    			if(Math.random() < .5){
    				int sw = (int)Math.random() * 5;
    				//Numbers fairly arbitrary, to be tweaked after testing
    				
    				switch(sw){
    				case 0:
    					if((rc.readBroadcast(10000) < 5 && bullets > 200) ||
    							rc.readBroadcast(10000) < 15 && bullets > 500 || 
    							bullets > 700){
    						if(rc.canBuildRobot(RobotType.SOLDIER, Direction.EAST)){
    							rc.buildRobot(RobotType.SOLDIER, Direction.EAST);
    						}
    						else if(rc.canBuildRobot(RobotType.SOLDIER, Direction.WEST)){
    							rc.buildRobot(RobotType.SOLDIER, Direction.WEST);
    						}
    					}
    					break;
    					
    				case 1: 
    					if((rc.readBroadcast(10000) < 5 && bullets > 200) ||
    							rc.readBroadcast(10000) < 15 && bullets > 500 || 
    							bullets > 700){
    						if(rc.canBuildRobot(RobotType.SOLDIER, Direction.EAST)){
    							rc.buildRobot(RobotType.SOLDIER, Direction.EAST);
    						}
    						else if(rc.canBuildRobot(RobotType.SOLDIER, Direction.WEST)){
    							rc.buildRobot(RobotType.SOLDIER, Direction.WEST);
    						}
    					}
    					break;
    					
    				case 2: 
    					if((rc.readBroadcast(10000) < 3 && bullets > 400) ||
    							rc.readBroadcast(10000) < 6 && bullets > 600 || 
    							bullets > 700){
    						if(rc.canBuildRobot(RobotType.TANK, Direction.EAST)){
    							rc.buildRobot(RobotType.TANK, Direction.EAST);
    						}
    						else if(rc.canBuildRobot(RobotType.TANK, Direction.WEST)){
    							rc.buildRobot(RobotType.TANK, Direction.WEST);
    						}
    					}
    					break;
    					
    				case 3:
    					if((rc.readBroadcast(10000) < 3 && bullets > 300) ||
    							rc.readBroadcast(10000) < 6 && bullets > 500 || 
    							bullets > 800){
    						if(rc.canBuildRobot(RobotType.LUMBERJACK, Direction.EAST)){
    							rc.buildRobot(RobotType.LUMBERJACK, Direction.EAST);
    						}
    						else if(rc.canBuildRobot(RobotType.LUMBERJACK, Direction.WEST)){
    							rc.buildRobot(RobotType.LUMBERJACK, Direction.WEST);
    						}
    					}
    					break;
    					
    				case 4: 
    					if((rc.readBroadcast(10000) < 3 && bullets > 300)){
    						if(rc.canBuildRobot(RobotType.SCOUT, Direction.EAST)){
    							rc.buildRobot(RobotType.SCOUT, Direction.EAST);
    						}
    						else if(rc.canBuildRobot(RobotType.SCOUT, Direction.WEST)){
    							rc.buildRobot(RobotType.SCOUT, Direction.WEST);
    						}
    					}
    					break;
    					
    				}    			
    			}
    			
    			else{
    				if(bullets > 100 && rc.canPlantTree(Direction.EAST)){
    					rc.plantTree(Direction.EAST);
    				}
    				else if(bullets > 100 && rc.canPlantTree(Direction.WEST)){
    					rc.plantTree(Direction.WEST);
    				}
    			}	
    		}
    	}
    	// Move Randomly
    	else if (choice > portionProducing && choice < portionProducing + portionNeither){
    		double direction = Math.random();
    		if(direction < .25){
    			Movement.tryMove(Direction.EAST);
    		}
    		else  if(direction < .5){
    			Movement.tryMove(Direction.WEST);
    		}
    		else if(direction < .75){
    			Movement.tryMove(Direction.NORTH);
    		}
    		else{
    			Movement.tryMove(Direction.SOUTH);
    		}
    	}
    	
    	//To be used as parameter for next call to this method
		return false;
    }
  
    static boolean gardenerDefend(){
    	//@todo fill stub
    	return false;
    }
}
