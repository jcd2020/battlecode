package upgirdplayer;
import java.util.LinkedHashSet;

import battlecode.common.*;

public strictfp class RobotPlayer {
	
	static RobotController rc;
    public static final Team myTeam = rc.getTeam();
    LinkedHashSet<Tree> neutralTrees = new LinkedHashSet<>();
    LinkedHashSet<Tree> friendlyTrees = new LinkedHashSet<>();
    LinkedHashSet<Tree> enemyTrees = new LinkedHashSet<>();

    static LinkedHashSet<Robot> enemies = new LinkedHashSet<>();
    LinkedHashSet<Robot> friendlies = new LinkedHashSet<>();

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
    **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        // Here, we've separated the controls into a different method for each RobotType.
        // You can add the missing ones or rewrite this into your own control structure.
        switch (rc.getType()) {
            case ARCHON:
                runArchon();
                break;
            case GARDENER:
                runGardener();
                break;
            case SOLDIER:
                runSoldier();
                break;
            case LUMBERJACK:
                runLumberjack();
                break;
        }
	}

    static void runArchon() throws GameActionException {
        System.out.println("I'm an archon!");

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            		
                // Generate a random direction
                Direction dir = randomDirection();

                // Randomly attempt to build a gardener in this direction
                if (rc.canHireGardener(dir) && Math.random() < .01) {
                    rc.hireGardener(dir);
                }

                // Move randomly
                Movement.tryMove(randomDirection());

                // Broadcast archon's location for other robots on the team to know
                MapLocation myLocation = rc.getLocation();
                rc.broadcast(0,(int)myLocation.x);
                rc.broadcast(1,(int)myLocation.y);

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }

	static void runGardener() throws GameActionException {
        System.out.println("I'm a gardener!");

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                // Listen for home archon's location
                int xPos = rc.readBroadcast(0);
                int yPos = rc.readBroadcast(1);
                MapLocation archonLoc = new MapLocation(xPos,yPos);

                // Generate a random direction
                Direction dir = randomDirection();

                // Randomly attempt to build a soldier or lumberjack in this direction
                if (rc.canBuildRobot(RobotType.SOLDIER, dir) && Math.random() < .01) {
                    rc.buildRobot(RobotType.SOLDIER, dir);
                } else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && Math.random() < .01 && rc.isBuildReady()) {
                    rc.buildRobot(RobotType.LUMBERJACK, dir);
                }

                // Move randomly
                Movement.tryMove(randomDirection());

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }
    }

    static void runSoldier() throws GameActionException {
        System.out.println("I'm an soldier!");
        Team enemy = rc.getTeam().opponent();

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                MapLocation myLocation = rc.getLocation();

                // See if there are any nearby enemy robots
                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

                // If there are some...
                if (robots.length > 0) {
                    // And we have enough bullets, and haven't attacked yet this turn...
                    if (rc.canFireSingleShot()) {
                        // ...Then fire a bullet in the direction of the enemy.
                        rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
                    }
                }

                // Move randomly
                Movement.tryMove(randomDirection());

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
        }
    }

    static void runLumberjack() throws GameActionException {
        System.out.println("I'm a lumberjack!");
        Team enemy = rc.getTeam().opponent();

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
                RobotInfo[] robots = rc.senseNearbyRobots(RobotType.LUMBERJACK.bodyRadius+GameConstants.LUMBERJACK_STRIKE_RADIUS, enemy);

                if(robots.length > 0 && !rc.hasAttacked()) {
                    // Use strike() to hit all nearby robots!
                    rc.strike();
                } else {
                    // No close robots, so search for robots within sight radius
                    robots = rc.senseNearbyRobots(-1,enemy);

                    // If there is a robot, move towards it
                    if(robots.length > 0) {
                        MapLocation myLocation = rc.getLocation();
                        MapLocation enemyLocation = robots[0].getLocation();
                        Direction toEnemy = myLocation.directionTo(enemyLocation);

                        Movement.tryMove(toEnemy);
                    } else {
                        // Move Randomly
                        Movement.tryMove(randomDirection());
                    }
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Lumberjack Exception");
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns a random Direction
     * @return a random Direction
     */
    static Direction randomDirection() {
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
    }



    /**
     * A slightly more complicated example function, this returns true if the given bullet is on a collision
     * course with the current robot. Doesn't take into account objects between the bullet and this robot.
     *
     * @param bullet The bullet in question
     * @return True if the line of the bullet's path intersects with this robot's current position.
     */
    static boolean willCollideWithMe(BulletInfo bullet) {
        MapLocation myLocation = rc.getLocation();

        // Get relevant bullet information
        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        // Calculate bullet relations to this robot
        Direction directionToRobot = bulletLocation.directionTo(myLocation);
        float distToRobot = bulletLocation.distanceTo(myLocation);
        float theta = propagationDirection.radiansBetween(directionToRobot);

        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
        if (Math.abs(theta) > Math.PI/2) {
            return false;
        }

        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float)Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

        return (perpendicularDist <= rc.getType().bodyRadius);
    }
    
    //Personal Confidence measure used to determine fleeing v normal behavior
    
    static double personalConfidence(RobotInfo[] friendlyInfo, RobotInfo[] enemyInfo, RobotInfo selfInfo) throws GameActionException{
    
    	double health = selfInfo.health;
    	double bulletCt = rc.readBroadcast(6);
    	RobotType myType = selfInfo.type;
    	int attackPwr = 0;
    	switch(myType){
    	case ARCHON:	attackPwr = 0;
    	case GARDENER:	attackPwr = 0;
    	case LUMBERJACK: attackPwr = 1;
    	case SOLDIER: attackPwr = 2;
    	case TANK: attackPwr = 4;
    	case SCOUT: attackPwr = 1;
    	}
    	
    	int totalFriendlyPwr = 0;
    	for(RobotInfo info : friendlyInfo){
    		RobotType type = info.type;
    		switch(type){
        	case ARCHON:	totalFriendlyPwr += 0;
        	case GARDENER:	totalFriendlyPwr += 0;
        	case LUMBERJACK: totalFriendlyPwr += 1;
        	case SOLDIER: totalFriendlyPwr += 2;
        	case TANK: totalFriendlyPwr += 4;
        	case SCOUT: totalFriendlyPwr += 1;
        	}
    	}
    	
    	int totalEnemyPwr = 0;
     	for(RobotInfo info : enemyInfo){
    		RobotType type = info.type;
    		switch(type){
        	case ARCHON:	totalEnemyPwr += 0;
        	case GARDENER:	totalEnemyPwr += 0;
        	case LUMBERJACK: totalEnemyPwr += 1;
        	case SOLDIER: totalEnemyPwr += 2;
        	case TANK: totalEnemyPwr += 4;
        	case SCOUT: totalEnemyPwr += 1;
        	}
    	}
     	
     	double confidence = ((bulletCt/5) + health*attackPwr) * ((health*totalFriendlyPwr + 1)/(health*totalEnemyPwr + 1));
    	return confidence;
    }
    
    public class Tree implements Comparable<Tree>
    {
    	int id;
		int health;
		int maxHealth;
		int robotType ;
		int x;
		int y;
		int team;
		int bulletsContained;
		
		int totalVal;
    	
    	public Tree(int[] a)
    	{
    		this.id = a[0];
    		this.health = a[1];
    		this.maxHealth = a[2];
    		this.robotType = a[3];
    		this.x = a[4];
    		this.y = a[5];
    		this.team = a[6];
    		this.bulletsContained = a[7];
    		
    		
    		RobotType r = null;
    		
    		switch(robotType)
    		{
    			
    			case 0:
    				r = RobotType.ARCHON;
    				break;
    			case 1:
    				r = RobotType.GARDENER;
    			case 2:
    				r = RobotType.LUMBERJACK;
    			case 3:
    				r = RobotType.SCOUT;
    			case 4:
    				r = RobotType.SOLDIER;
    			case 5:
    				r = RobotType.TANK;
    			
    			
    		}
    		totalVal = bulletsContained + r.bulletCost;
    	}
    	
    	
    	double getValue()
    	{
    		MapLocation loc = rc.getLocation();
			double dist = Math.sqrt(Math.pow(loc.x - this.x, 2) + Math.pow(loc.y - y, 2));
			
			//Heuristics to estimate the value of a tree to a robot
			if(team == 2)
			{
				return totalVal / dist;
			}
			
			if(team == rc.getTeam().ordinal())
			{
				return dist * maxHealth / health;
			}
			else
			{
				return health / dist;
			}
			
    	}
		@Override
		public int compareTo(Tree o) 
		{
			return Double.compare(this.getValue(), o.getValue());
		}
    	
    }
    
    public class Robot implements Comparable<Robot>
    {
    	int id;
		int health;
		int robotType;
		int x;
		int y;
		
		int totalVal;
		
		public Robot(int[] a)
		{
			id = a[0];
			health = a[1];
			robotType = a[2];
			x = a[3];
			y = a[4];
			
    		
    		switch(robotType)
    		{
    			
    			case 0:
    				totalVal = 1000;
    				break;
    			case 1:
    				totalVal = 300;
    				break;
    			case 2:
    				totalVal = 150;
    				break;
    			case 3:
    				totalVal = 100;
    				break;
    			case 4:
    				totalVal = 200;
    				break;
    			case 5:
    				totalVal = 300;
    				break;   			
    		}
		}
		
		double getValue()
    	{
    		MapLocation loc = rc.getLocation();
			double dist = Math.sqrt(Math.pow(loc.x - this.x, 2) + Math.pow(loc.y - y, 2));
			
			return totalVal / (health * dist);
			
			
    	}
		@Override
		public int compareTo(Robot o)
		{
			return Double.compare(this.getValue(), o.getValue());
		}
    	
    }
	
	  //fudge
    static void getGlobalConfidence() throws GameActionException
    {
    	double friendlyAttackingUnits = 0;
    	double friendlyNonAttackingUnits = 0;

    	for(Robot r : friendlies)
    	{
    		if(r.robotType <= 1 || r.robotType == 3)
    		{
    			friendlyNonAttackingUnits++;
    		}
    		else
    		{
    			friendlyAttackingUnits++;
    		}
    	}
    	
    	
    	double enemyAttackingUnits = 0;
    	double enemyNonAttackingUnits = 0;

    	for(Robot r : enemies)
    	{
    		if(r.robotType <= 1 || r.robotType == 3)
    		{
    			enemyNonAttackingUnits++;
    		}
    		else
    		{
    			enemyAttackingUnits++;
    		}
    	}
    	
    	double enemyTreeCount = 50*enemyTrees.size();
    	double friendlyTreeCount = 50*friendlyTrees.size();

    	double attack = friendlyAttackingUnits/enemyAttackingUnits;
    	double nonattack = friendlyNonAttackingUnits/enemyNonAttackingUnits;
    	double value = friendlyTreeCount/enemyTreeCount;
    	
    	double enemyArchonHealth = rc.readBroadcast(8);
    	double archonHealth = rc.readBroadcast(7);
    	
    	double conf = (attack*1000 + rc.getTeamBullets())*archonHealth/((nonattack*500 + value*250 + rc.getTeamVictoryPoints()*enemyArchonHealth));
    	
    	rc.broadcast((int) conf, 9);
    	
    	
    }

}
