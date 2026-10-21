package com.spaceship.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.sql.Database;
import com.badlogic.gdx.sql.DatabaseCursor;
import com.badlogic.gdx.sql.DatabaseFactory;
import com.badlogic.gdx.sql.SQLiteGdxException;
import com.spaceship.handlers.ActionResolver;

public class Main extends Game{
	

	private static ActionResolver actionResolver;
    public SpriteBatch batch;
    public BitmapFont font;
    public AssetManager assetManager;
    
    private int statusBarHeight;
    private int navigationBarHeight;
    
    private Database numbersDB, shipsDB, galaxiesDB, upgradesDB, upgradesPriceDB, statsDB;
    private DatabaseCursor cursor;
    
    private static boolean tutorial;
    
    private MainMenuScreen mms;
    private PlayScreen ps;
		
    public Main(int sbh, int nbh){
    	statusBarHeight = sbh;
    	navigationBarHeight = nbh;
    }
    
    public Main(ActionResolver actionResolver) {
        Main.setActionResolver(actionResolver);
      }

    
	@Override
	public void create(){
	
		assetManager = new com.spaceship.handlers.GameAssetManager();
		batch = new SpriteBatch();
	    font = new BitmapFont();
	    setTutorial(false);
	    	    
		assetManager.load("res/hud/progress_bar.png", Texture.class);
		assetManager.load("res/splash.png", Texture.class);
		assetManager.finishLoading();
	    
	    //database stuff
//	    numbersDB = DatabaseFactory.getNewDatabase("seleccionsAndNumbers.db", 1, "CREATE TABLE IF NOT EXISTS seleccionsAndNumbers (highScore integer, " +
//	    		"lifeBalls integer, blueBalls integer, redBalls integer, ffSelected integer, shipSelected integer, galaxySelected integer, " +
//	    		"sound integer, vibration integer, controls integer, ships integer, galaxies integer, upgrades integer);", null);
//		numbersDB.setupDatabase();
//		try {
//			numbersDB.openOrCreateDatabase();
//			numbersDB.execSQL("CREATE TABLE IF NOT EXISTS seleccionsAndNumbers (highScore integer, lifeBalls integer, blueBalls integer, redBalls integer, " +
//					"ffSelected integer, shipSelected integer, galaxySelected integer, sound integer, vibration integer, controls integer," +
//					"ships integer, galaxies integer, upgrades integer);");
//			cursor = numbersDB.rawQuery("SELECT COUNT(highScore) FROM seleccionsAndNumbers;");
//			if(cursor != null && cursor.next())
//				if(String.valueOf(cursor.getInt(0)).equals("0")){ 
//					numbersDB.execSQL("INSERT INTO seleccionsAndNumbers (highScore, lifeBalls, blueBalls, redBalls, ffSelected, shipSelected, " +
//							"galaxySelected, sound, vibration, controls, ships, galaxies, upgrades)" +
//							" VALUES (0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0)");
//					setTutorial(true);
//				}	
//			cursor.close();
//		} catch (SQLiteGdxException e) {
//			e.printStackTrace();
//		}
		
		numbersDB = DatabaseFactory.getNewDatabase("seleccionsAndNumbers.db", 1, "CREATE TABLE IF NOT EXISTS seleccionsAndNumbers (highScore integer, " +
				"lifeBalls integer, blueBalls integer, redBalls integer, ffSelected integer, shipSelected integer, galaxySelected integer, " +
				"sound integer, vibration integer, controls integer, language integer, ships integer, galaxies integer, upgrades integer);", null);
		numbersDB.setupDatabase();
		try {
			numbersDB.openOrCreateDatabase();
			numbersDB.execSQL("CREATE TABLE IF NOT EXISTS seleccionsAndNumbers (highScore integer, lifeBalls integer, blueBalls integer, redBalls integer, " +
					"ffSelected integer, shipSelected integer, galaxySelected integer, sound integer, vibration integer, controls integer, language integer," +
					"ships integer, galaxies integer, upgrades integer);");
			cursor = numbersDB.rawQuery("SELECT COUNT(highScore) FROM seleccionsAndNumbers;");
			if(cursor != null && cursor.next())
				if(String.valueOf(cursor.getInt(0)).equals("0")){ 
					numbersDB.execSQL("INSERT INTO seleccionsAndNumbers (highScore, lifeBalls, blueBalls, redBalls, ffSelected, shipSelected, " +
							"galaxySelected, sound, vibration, controls, language, ships, galaxies, upgrades)" +
							" VALUES (0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0)");
					setTutorial(true);
				}	
			cursor.close();
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}
		
		//upgrades dataBase
		upgradesDB = DatabaseFactory.getNewDatabase("upgrades.db", 1, "CREATE TABLE IF NOT EXISTS upgrades (shipSpeed integer, lifesTotal integer, " +
	    		"lifesStarts integer, objectsRoom integer, objectsStarts integer, objectsDuration integer);", null);
		upgradesDB.setupDatabase();
		try {
			upgradesDB.openOrCreateDatabase();
			upgradesDB.execSQL("CREATE TABLE IF NOT EXISTS upgrades (shipSpeed integer, lifesTotal integer, " +
	    		"lifesStarts integer, objectsRoom integer, objectsStarts, objectsDuration integer);");
			cursor = upgradesDB.rawQuery("SELECT COUNT(lifesTotal) FROM upgrades;");
			if(cursor != null && cursor.next())
				if(String.valueOf(cursor.getInt(0)).equals("0")){ 
					upgradesDB.execSQL("INSERT INTO upgrades (shipSpeed, lifesTotal, lifesStarts, objectsRoom, objectsStarts, objectsDuration)" +
							" VALUES (1, 3, 2, 1, 0, 1)");
				}	
			cursor.close();
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}
		
		//upgradesPrice dataBase
		upgradesPriceDB = DatabaseFactory.getNewDatabase("upgradesPrice.db", 1, "CREATE TABLE IF NOT EXISTS upgradesPrice (id integer, " +
	    		"bought integer, lifeBalls integer);", null);
		upgradesPriceDB.setupDatabase();
		try {
			upgradesPriceDB.openOrCreateDatabase();
			upgradesPriceDB.execSQL("CREATE TABLE IF NOT EXISTS upgradesPrice (id integer, " +
	    		"bought integer, lifeBalls integer);");
			cursor = upgradesPriceDB.rawQuery("SELECT COUNT(id) FROM upgradesPrice;");
			if(cursor != null && cursor.next())
				if(String.valueOf(cursor.getInt(0)).equals("0")){ 
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (0, 0, 50)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (1, 0, 100)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (2, 0, 250)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (3, 0, 500)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (4, 0, 250)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (5, 0, 500)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (6, 0, 100)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (7, 0, 250)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (8, 0, 500)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (9, 0, 250)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (10, 0, 500)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (11, 0, 250)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (12, 0, 500)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (13, 0, 1000)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (14, 0, 50)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (15, 0, 100)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (16, 0, 250)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (17, 0, 500)");
					
				}	

			cursor.close();
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}

		//ships database stuff
	    shipsDB = DatabaseFactory.getNewDatabase("ships.db", 1, "CREATE TABLE IF NOT EXISTS ships (id integer, bought integer, reds integer, blues integer);", null);
		shipsDB.setupDatabase();
		try {
			shipsDB.openOrCreateDatabase();
			shipsDB.execSQL("CREATE TABLE IF NOT EXISTS ships (id integer, bought integer, reds integer, blues integer);");
			cursor = shipsDB.rawQuery("SELECT COUNT(id) FROM ships;");
			if(cursor != null && cursor.next())
				if(String.valueOf(cursor.getInt(0)).equals("0")){
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (0, 1, 0, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (1, 0, 50, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (2, 0, 0, 50)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (3, 0, 50, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (4, 0, 0, 50)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (5, 0, 0, 100)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (6, 0, 50, 50)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (7, 0, 100, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (8, 0, 50, 50)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (9, 0, 0, 150)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (10, 0, 150, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (11, 0, 200, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (12, 0, 100, 100)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (13, 0, 100, 100)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (14, 0, 0, 200)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (15, 0, 0, 250)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (16, 0, 250, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (17, 0, 0, 250)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (18, 0, 250, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (19, 0, 0, 350)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (20, 0, 350, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (21, 0, 250, 100)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (22, 0, 200, 200)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (23, 0, 400, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (24, 0, 0, 400)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (25, 0, 150, 250)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (26, 0, 0, 500)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (27, 0, 500, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (28, 0, 500, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (29, 0, 0, 500)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (30, 0, 300, 300)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (31, 0, 300, 300)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (32, 0, 300, 300)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (33, 0, 300, 300)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (34, 0, 500, 220)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (35, 0, 0, 750)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (36, 0, 750, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (37, 0, 0, 750)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (38, 0, 750, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (39, 0, 400, 500)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (40, 0, 500, 400)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (41, 0, 400, 500)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (42, 0, 550, 420)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (43, 0, 0, 1000)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (44, 0, 1000, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (45, 0, 0, 1000)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (46, 0, 1000, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (47, 0, 770, 300)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (48, 0, 750, 350)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (49, 0, 800, 400)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (50, 0, 0, 1250)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (51, 0, 1250, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (52, 0, 620, 870)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (53, 0, 1500, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (54, 0, 0, 1500)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (55, 0, 1750, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (56, 0, 0, 1750)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (57, 0, 900, 900)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (58, 0, 900, 900)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (59, 0, 800, 1200)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (60, 0, 1200, 800)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (61, 0, 0, 2000)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (62, 0, 2000, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (63, 0, 0, 2500)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (64, 0, 2500, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (65, 0, 1500, 1500)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (66, 0, 1500, 1500)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (67, 0, 1700, 1800)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (68, 0, 2500, 2000)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (69, 0, 2000, 2500)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (70, 0, 0, 5000)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (71, 0, 5000, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (72, 0, 4000, 3000)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (73, 0, 5000, 5000)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (74, 0, 5000, 5000)");
				}		
			cursor.close();
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}	
		
		//ships database stuff
	    galaxiesDB = DatabaseFactory.getNewDatabase("galaxies.db", 1, "CREATE TABLE IF NOT EXISTS galaxies (id integer, bought integer, reds integer, blues integer);", null);
	    galaxiesDB.setupDatabase();
		try {
			galaxiesDB.openOrCreateDatabase();
			galaxiesDB.execSQL("CREATE TABLE IF NOT EXISTS galaxies (id integer, bought integer, reds integer, blues integer);");
			cursor = galaxiesDB.rawQuery("SELECT COUNT(id) FROM galaxies;");
			if(cursor != null && cursor.next())
				if(String.valueOf(cursor.getInt(0)).equals("0")){
					galaxiesDB.execSQL("INSERT INTO galaxies (id, bought, reds, blues) VALUES (0, 1, 0, 0)");
					galaxiesDB.execSQL("INSERT INTO galaxies (id, bought, reds, blues) VALUES (1, 0, 250, 250)");
					galaxiesDB.execSQL("INSERT INTO galaxies (id, bought, reds, blues) VALUES (2, 0, 500, 500)");
					galaxiesDB.execSQL("INSERT INTO galaxies (id, bought, reds, blues) VALUES (3, 0, 750, 750)");
					galaxiesDB.execSQL("INSERT INTO galaxies (id, bought, reds, blues) VALUES (4, 0, 750, 750)");
					galaxiesDB.execSQL("INSERT INTO galaxies (id, bought, reds, blues) VALUES (5, 0, 1500, 1000)");
					galaxiesDB.execSQL("INSERT INTO galaxies (id, bought, reds, blues) VALUES (6, 0, 1000, 1500)");
					galaxiesDB.execSQL("INSERT INTO galaxies (id, bought, reds, blues) VALUES (7, 0, 3000, 2000)");
					galaxiesDB.execSQL("INSERT INTO galaxies (id, bought, reds, blues) VALUES (8, 0, 2000, 3000)");
					galaxiesDB.execSQL("INSERT INTO galaxies (id, bought, reds, blues) VALUES (9, 0, 5000, 5000)");
				}		
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}	
		//stats database stuff
	    statsDB = DatabaseFactory.getNewDatabase("stats.db", 1, "CREATE TABLE IF NOT EXISTS stats " +
	    		"(games integer, totalPoints integer, avgPoints integer, totalOrbs, totalBlues integer, totalReds integer, totalLifes integer, " +
	    		"totalObjects integer, spentBlues integer, spentReds integer, spentLifes integer, ob0 integer, ob1 integer, ob2 integer, ob3 integer," +
	    		"ob4 integer, ob5 integer, ob6 integer, ob7 integer, ob8 integer, ob9 integer, timePlaying integer);", null);
	    statsDB.setupDatabase();
		try {
			statsDB.openOrCreateDatabase();
			statsDB.execSQL("CREATE TABLE IF NOT EXISTS stats (games integer, totalPoints integer, avgPoints integer, totalOrbs, totalBlues integer, " +
				"totalReds integer, totalLifes integer, totalObjects integer, spentBlues integer, spentReds integer, spentLifes integer, ob0 integer, " +
				"ob1 integer, ob2 integer, ob3 integer, ob4 integer, ob5 integer, ob6 integer, ob7 integer, ob8 integer, ob9 integer, timePlaying integer);");
			cursor = statsDB.rawQuery("SELECT COUNT(games) FROM stats;");
			if(cursor != null && cursor.next())
				if(String.valueOf(cursor.getInt(0)).equals("0")){
					statsDB.execSQL("INSERT INTO stats (games, totalPoints, avgPoints, totalOrbs, totalBlues, totalReds, totalLifes, totalObjects," +
							"spentBlues, spentReds, spentLifes, ob0, ob1, ob2, ob3, ob4, ob5, ob6, ob7, ob8, ob9, timePlaying) VALUES " +
							"(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)");
				}		
			cursor.close();
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}
		
		try {
			cursor = galaxiesDB.rawQuery("SELECT reds FROM galaxies WHERE id=1;");
			if(cursor != null && cursor.next())
				if(cursor.getInt(0) == 500){
					galaxiesDB.execSQL("UPDATE galaxies SET blues=" + 250 + ", reds=" + 250 + " WHERE id=1;");
					
					upgradesPriceDB.execSQL("UPDATE upgradesPrice SET id=17 WHERE id=16;");
					upgradesPriceDB.execSQL("UPDATE upgradesPrice SET id=16 WHERE id=15;");
					upgradesPriceDB.execSQL("UPDATE upgradesPrice SET id=15 WHERE id=14;");
					upgradesPriceDB.execSQL("UPDATE upgradesPrice SET id=14 WHERE id=13;");
					upgradesPriceDB.execSQL("UPDATE upgradesPrice SET id=13 WHERE id=12;");
					upgradesPriceDB.execSQL("UPDATE upgradesPrice SET id=12 WHERE id=11;");
					upgradesPriceDB.execSQL("UPDATE upgradesPrice SET id=11 WHERE id=10;");
					upgradesPriceDB.execSQL("UPDATE upgradesPrice SET id=10 WHERE id=9;");
					upgradesPriceDB.execSQL("UPDATE upgradesPrice SET id=9 WHERE id=8;");
					upgradesPriceDB.execSQL("UPDATE upgradesPrice SET id=8 WHERE id=7;");
					upgradesPriceDB.execSQL("UPDATE upgradesPrice SET id=7 WHERE id=6;");
					upgradesPriceDB.execSQL("UPDATE upgradesPrice SET id=6 WHERE id=5;");
					upgradesPriceDB.execSQL("UPDATE upgradesPrice SET id=5 WHERE id=4;");
					upgradesPriceDB.execSQL("UPDATE upgradesPrice SET id=4 WHERE id=3;");
					upgradesPriceDB.execSQL("UPDATE upgradesPrice SET id=3 WHERE id=2;");
					upgradesPriceDB.execSQL("UPDATE upgradesPrice SET id=2 WHERE id=1;");
					upgradesPriceDB.execSQL("UPDATE upgradesPrice SET id=1 WHERE id=0;");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (0, 0, 50)");
					upgradesPriceDB.execSQL("UPDATE upgradesPrice SET lifeBalls=250 WHERE id=9;");
					upgradesPriceDB.execSQL("UPDATE upgradesPrice SET lifeBalls=500 WHERE id=10;");
					upgradesPriceDB.execSQL("UPDATE upgradesPrice SET lifeBalls=50 WHERE id=14;");
					upgradesPriceDB.execSQL("UPDATE upgradesPrice SET lifeBalls=100 WHERE id=15;");
					upgradesPriceDB.execSQL("UPDATE upgradesPrice SET lifeBalls=250 WHERE id=16;");
					upgradesPriceDB.execSQL("UPDATE upgradesPrice SET lifeBalls=500 WHERE id=17;");
					
					numbersDB.execSQL("ALTER TABLE seleccionsAndNumbers ADD language integer");
					numbersDB.execSQL("INSERT INTO seleccionsAndNumbers (language) VALUES (1);");
					
				}
			cursor.close();

		} catch (SQLiteGdxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    this.setScreen(new Splash(this));
		
	}
	
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}	
	
	public void render() {
        super.render(); 
    }

    public void dispose() {
        batch.dispose();
        font.dispose();
        assetManager.dispose();
    }
    
    public int getStatusBarHeight(){
    	return statusBarHeight;
    }
	
    public int getNavigationBarHeight(){
    	return navigationBarHeight;
    }
    
    public void setNavigationBarHeight(int sbh){
    	 statusBarHeight = sbh;
    }
    
	public Database getDataBase(){
		return numbersDB;
	}
	
	public Database getUpgradesDataBase(){
		return upgradesDB;
	}
	
	public Database getShipsDataBase(){
		return shipsDB;
	}
	
	public Database getGalaxiesDataBase(){
		return galaxiesDB;
	}

	public Database getUpgradesPriceDataBase(){
		return upgradesPriceDB;
	}
	
	public Database getStatsDataBase(){
		return statsDB;
	}

	public static boolean isTutorial() {
		return tutorial;
	}

	public static void setTutorial(boolean tutorial) {
		Main.tutorial = tutorial;
	}
	
	public MainMenuScreen getMMS(){
		return mms;
	}
	
	public PlayScreen getPS(){
		return ps;
	}
	
	public void setMMS(MainMenuScreen mainScreen){
		mms = mainScreen;
	}
	
	public void setPS(PlayScreen playScreen){
		ps = playScreen;
	}

	public ActionResolver getActionResolver() {
		return actionResolver;
	}

	public static void setActionResolver(ActionResolver actionResolver) {
		Main.actionResolver = actionResolver;
	}

}
