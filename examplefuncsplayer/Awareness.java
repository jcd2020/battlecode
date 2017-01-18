package examplefuncsplayer;
import battlecode.common.*;

public class Awareness {
	
	private static final int ENEMY_ARCHON_HEALTH_BC = 3;
	private static final int FRIENDLY_TREE_COUNT = 0;
	private static final int VICTORY_POINT_COUNT = 1;
	private static final int BULLET_COUNT = 2;
	static RobotController rc = RobotPlayer.rc;
	static Team myTeam = RobotPlayer.myTeam;
	
	
	
	public static void sensing() throws GameActionException
	{
		RobotInfo[] enemies = enemiesInSight(); //100-200
		TreeInfo[] neutTrees = neutTreesInSight(); //200-300
		TreeInfo[] enemyTrees = enemyTreesInSight(); //300-400
		TreeInfo[] friendlyTrees = friendlyTreesInSight(); //400-500
		int friendlyTree = friendlyTreeCount(); //0
		int vpCount = vpCount(); //1
		int bulletCount = bulletCount(); //2
		// enemy archon health in bc 3
		
		rc.broadcast(friendlyTree, FRIENDLY_TREE_COUNT);
		rc.broadcast(vpCount, VICTORY_POINT_COUNT);
		rc.broadcast(bulletCount, BULLET_COUNT);

		int enemy_ind = rc.readBroadcast(100);
		int neut_ind = rc.readBroadcast(200);
		int enemy_tree_ind = rc.readBroadcast(300);
		int friendly_tree_ind = rc.readBroadcast(400);
		
		for(RobotInfo r : enemies)
		{
			if(r.type.ordinal() == RobotType.ARCHON.ordinal())
			{
				rc.broadcast((int)r.health, ENEMY_ARCHON_HEALTH_BC);
			}
			
			int message = encodeEnemyMessage(r);
			if(enemy_ind >= 200 || enemy_ind == 0)
			{
				enemy_ind = 101;
			}
			rc.broadcast(message, enemy_ind);
			enemy_ind++;
			rc.broadcast(enemy_ind, 100);
		}
		
		for(TreeInfo t : neutTrees)
		{
			
			int message = encodeTreeMessage(t);
			if(neut_ind >= 300 || neut_ind == 0)
			{
				neut_ind = 201;
			}
			rc.broadcast(message, neut_ind);
			neut_ind++;
			rc.broadcast(neut_ind, 200);
		}
		
		for(TreeInfo t : enemyTrees)
		{
			
			int message = encodeTreeMessage(t);
			if(enemy_tree_ind >= 400 || enemy_tree_ind == 0)
			{
				enemy_tree_ind = 301;
			}
			rc.broadcast(message, enemy_tree_ind);
			enemy_tree_ind++;
			rc.broadcast(enemy_tree_ind, 100);
		}
		
		for(TreeInfo t :friendlyTrees)
		{
			
			int message = encodeTreeMessage(t);
			if(friendly_tree_ind >= 500 || friendly_tree_ind == 0)
			{
				friendly_tree_ind = 401;
			}
			rc.broadcast(message, friendly_tree_ind);
			friendly_tree_ind++;
			rc.broadcast(friendly_tree_ind, 100);
		}
		
		
		
		

	}
	
	private static TreeInfo[] friendlyTreesInSight() 
	{
		if(rc.getTeam() == Team.A)
		{
			return rc.senseNearbyTrees(-1, Team.A);
		}
		else 
		{
			return rc.senseNearbyTrees(-1, Team.B);
		}
	}
	private static TreeInfo[] enemyTreesInSight() 
	{

		if(rc.getTeam() == Team.A)
		{
			return rc.senseNearbyTrees(-1, Team.B);
		}
		else 
		{
			return rc.senseNearbyTrees(-1, Team.A);
		}
	}
	private static TreeInfo[] neutTreesInSight() 
	{
		return rc.senseNearbyTrees(-1, Team.NEUTRAL);

	}
	
	static int friendlyCount(){
		return RobotPlayer.rc.getRobotCount();
	}

	static RobotInfo[] enemiesInSight(){
		
		if(myTeam == Team.A){
			
			RobotInfo[] enemyInfo = rc.senseNearbyRobots(-1, Team.B);
			return enemyInfo;	
		}
		else{
			RobotInfo[] enemyInfo = rc.senseNearbyRobots(-1, Team.A);
			return enemyInfo;
		}
	}
	
	
	
	
	//BC 4
	static int friendlyTreeCount(){
		return rc.getTreeCount();	
	}
	
	//BC 5
	static int vpCount(){
		return rc.getTeamVictoryPoints();
	}
	
	//BC 6
	static int bulletCount(){
		return (int)rc.getTeamBullets();
	}
	
	//BC 7 ~ Only call in the runArchon Method
	static int friendlyArchonHealth(){
		return (int)rc.getHealth();
	}
	
	

	static int encodeTreeMessage(TreeInfo t)
	{
		int id = t.ID;
		int health = (int) t.health;
		int maxHealth = (int) t.maxHealth;
		int robotType = (int) convertEnumToInt(t.getContainedRobot());
		int x = (int)t.location.x;
		int y = (int)t.location.y;
		int bulletsContained = t.containedBullets;
		int team = t.getTeam().ordinal();

		String message = convertIntToBinaryString(id, 15) + convertIntToBinaryString(health, 11) + convertIntToBinaryString(maxHealth, 11)+ convertIntToBinaryString(robotType, 3) + convertIntToBinaryString(x, 7) + convertIntToBinaryString(y, 7) + convertIntToBinaryString(team, 2) + convertIntToBinaryString(bulletsContained, 10);

		return Integer.parseInt(message, 2);

	}

	static int[] decodeTreeMessage(int m)
	{
		String message = convertIntToBinaryString(m, 64);

		int id = Integer.parseInt(message.substring(0, 15), 2);
		int health = Integer.parseInt(message.substring(15, 26), 2);
		int maxHealth = Integer.parseInt(message.substring(26, 37), 2);
		int robotType = Integer.parseInt(message.substring(37, 40), 2);
		int x = Integer.parseInt(message.substring(40, 47), 2);
		int y = Integer.parseInt(message.substring(47, 54), 2);
		int team = Integer.parseInt(message.substring(54, 56), 2);
		int bulletsContained = Integer.parseInt(message.substring(56, 64), 2);

		return new int[]{id, health, maxHealth, robotType, x, y, team, bulletsContained};
	}

	static int encodeEnemyMessage(RobotInfo r)
	{
		int id = r.ID;
		int health = (int) r.health;
		int x = (int)r.location.x;
		int y = (int)r.location.y;
		int robotType = convertEnumToInt(r.type);

		String message = convertIntToBinaryString(id, 15) + convertIntToBinaryString(health, 11) + convertIntToBinaryString(robotType, 3) + convertIntToBinaryString(x, 7) + convertIntToBinaryString(y, 7);

		return Integer.parseInt(message, 2);

	}
	static int[] decodeEnemyMessage(int m)
	{
		String message = convertIntToBinaryString(m, 43);

		int id = Integer.parseInt(message.substring(0, 15), 2);
		int health = Integer.parseInt(message.substring(15, 26), 2);
		int robotType = Integer.parseInt(message.substring(26, 29), 2);
		int x = Integer.parseInt(message.substring(29, 36), 2);
		int y = Integer.parseInt(message.substring(36, 43), 2);

		return new int[]{id, health, robotType, x, y};
	}

	private static int convertEnumToInt(RobotType containedRobot) 
	{
		if(containedRobot == null)
		{
			return 7;
		}
		else
		{
			return containedRobot.ordinal();

		}

	}


	private static String convertIntToBinaryString(int number, int numBits)
	{
		String binaryString = Integer.toBinaryString(number);
		binaryString = "00000000000000000000000000000000000000000000000000000000" + binaryString;
		binaryString = binaryString.substring(binaryString.length() - numBits);
		return binaryString;
	}




	static BulletInfo[] bulletSense(){
		return rc.senseNearbyBullets();
	}
}


