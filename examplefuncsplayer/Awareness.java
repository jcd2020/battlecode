package examplefuncsplayer;
import battlecode.common.*;

public class Awareness {
	
	static RobotController rc = RobotPlayer.rc;
	
	static int botCount(){
		return RobotPlayer.rc.getRobotCount();
	}
	
	//Scans for all enemy robots within a range so as not to overlap with other friendlies doing
	//the same thing, avoiding double counting of enemies. Could be ubiquitously called every
	//few turns
	
	static int enemiesInSight(){
		Team myTeam = rc.getTeam();
		MapLocation myLoc = rc.getLocation();

		RobotInfo[] friendlyInfo = rc.senseNearbyRobots(-1, myTeam);
		float min = 100;
		for(RobotInfo info : friendlyInfo){
			float dist = myLoc.distanceTo(info.location);
			if(dist < min){
				min = dist;
			}
		}
		
		if(myTeam == Team.A){
			
			RobotInfo[] enemyInfo = rc.senseNearbyRobots(min/2, Team.B);
			return enemyInfo.length;
		}
		else{
			
			RobotInfo[] enemyInfo = rc.senseNearbyRobots(min/2, Team.A);
			return enemyInfo.length;
		}
	}
	
}
