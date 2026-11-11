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
    public com.spaceship.handlers.NeonBatch stageBatch;
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
		// the Back button navigates inside the game (see onBack in the screens) instead of closing it
		com.badlogic.gdx.Gdx.input.setCatchKey(com.badlogic.gdx.Input.Keys.BACK, true);
	
		assetManager = new com.spaceship.handlers.GameAssetManager();
		batch = new com.spaceship.handlers.NeonBatch();
		stageBatch = new com.spaceship.handlers.NeonBatch();
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
			try{
				// the music switch was split from the sound switch: existing saves start with music as they had sound
				numbersDB.execSQL("ALTER TABLE seleccionsAndNumbers ADD COLUMN music integer DEFAULT 1;");
				numbersDB.execSQL("UPDATE seleccionsAndNumbers SET music = sound;");
			}catch (SQLiteGdxException e) {
				// column already there
			}
			cursor = numbersDB.rawQuery("SELECT COUNT(highScore) FROM seleccionsAndNumbers;");
			if(cursor != null && cursor.next())
				if(String.valueOf(cursor.getInt(0)).equals("0")){ 
					// first run: the game speaks the phone's language (Spanish if it is Spanish, English otherwise)
					int startLanguage = "es".equals(java.util.Locale.getDefault().getLanguage()) ? 0 : 1;
					numbersDB.execSQL("INSERT INTO seleccionsAndNumbers (highScore, lifeBalls, blueBalls, redBalls, ffSelected, shipSelected, " +
							"galaxySelected, sound, music, vibration, controls, language, ships, galaxies, upgrades)" +
							" VALUES (0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, " + startLanguage + ", 0, 0, 0)");		// first install: sound FX on, music off
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
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (0, 0, 25)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (1, 0, 50)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (2, 0, 100)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (3, 0, 150)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (4, 0, 100)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (5, 0, 150)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (6, 0, 50)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (7, 0, 100)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (8, 0, 150)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (9, 0, 100)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (10, 0, 150)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (11, 0, 100)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (12, 0, 150)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (13, 0, 200)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (14, 0, 25)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (15, 0, 50)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (16, 0, 100)");
					upgradesPriceDB.execSQL("INSERT INTO upgradesPrice (id, bought, lifeBalls) VALUES (17, 0, 150)");
					
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
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (5, 0, 0, 60)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (6, 0, 50, 50)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (7, 0, 60, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (8, 0, 50, 50)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (9, 0, 0, 70)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (10, 0, 70, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (11, 0, 80, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (12, 0, 60, 60)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (13, 0, 60, 60)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (14, 0, 0, 80)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (15, 0, 0, 100)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (16, 0, 100, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (17, 0, 0, 100)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (18, 0, 100, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (19, 0, 0, 120)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (20, 0, 120, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (21, 0, 100, 60)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (22, 0, 80, 80)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (23, 0, 130, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (24, 0, 0, 130)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (25, 0, 70, 100)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (26, 0, 0, 150)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (27, 0, 150, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (28, 0, 150, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (29, 0, 0, 150)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (30, 0, 110, 110)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (31, 0, 110, 110)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (32, 0, 110, 110)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (33, 0, 110, 110)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (34, 0, 150, 90)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (35, 0, 0, 200)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (36, 0, 200, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (37, 0, 0, 200)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (38, 0, 200, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (39, 0, 130, 150)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (40, 0, 150, 130)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (41, 0, 130, 150)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (42, 0, 160, 140)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (43, 0, 0, 300)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (44, 0, 300, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (45, 0, 0, 300)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (46, 0, 300, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (47, 0, 210, 110)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (48, 0, 200, 120)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (49, 0, 225, 130)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (50, 0, 0, 400)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (51, 0, 400, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (52, 0, 175, 250)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (53, 0, 500, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (54, 0, 0, 500)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (55, 0, 625, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (56, 0, 0, 625)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (57, 0, 260, 260)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (58, 0, 260, 260)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (59, 0, 225, 375)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (60, 0, 375, 225)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (61, 0, 0, 750)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (62, 0, 750, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (63, 0, 0, 1000)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (64, 0, 1000, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (65, 0, 500, 500)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (66, 0, 500, 500)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (67, 0, 600, 650)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (68, 0, 1000, 750)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (69, 0, 750, 1000)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (70, 0, 0, 2000)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (71, 0, 2000, 0)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (72, 0, 1600, 1200)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (73, 0, 2000, 2000)");
					shipsDB.execSQL("INSERT INTO ships (id, bought, reds, blues) VALUES (74, 0, 2000, 2000)");
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
					galaxiesDB.execSQL("INSERT INTO galaxies (id, bought, reds, blues) VALUES (1, 0, 50, 50)");
					galaxiesDB.execSQL("INSERT INTO galaxies (id, bought, reds, blues) VALUES (2, 0, 100, 100)");
					galaxiesDB.execSQL("INSERT INTO galaxies (id, bought, reds, blues) VALUES (3, 0, 150, 150)");
					galaxiesDB.execSQL("INSERT INTO galaxies (id, bought, reds, blues) VALUES (4, 0, 150, 150)");
					galaxiesDB.execSQL("INSERT INTO galaxies (id, bought, reds, blues) VALUES (5, 0, 250, 200)");
					galaxiesDB.execSQL("INSERT INTO galaxies (id, bought, reds, blues) VALUES (6, 0, 200, 250)");
					galaxiesDB.execSQL("INSERT INTO galaxies (id, bought, reds, blues) VALUES (7, 0, 400, 300)");
					galaxiesDB.execSQL("INSERT INTO galaxies (id, bought, reds, blues) VALUES (8, 0, 300, 400)");
					galaxiesDB.execSQL("INSERT INTO galaxies (id, bought, reds, blues) VALUES (9, 0, 500, 500)");
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
		// upgrade prices were lowered: bring existing saves to the current price list (safe to repeat, bought flags are untouched)
		try {
			int[][] upgradePrices = {{0,25},{1,50},{2,100},{3,150},{4,100},{5,150},{6,50},{7,100},{8,150},{9,100},{10,150},{11,100},{12,150},{13,200},{14,25},{15,50},{16,100},{17,150}};
			for (int[] up : upgradePrices)
				upgradesPriceDB.execSQL("UPDATE upgradesPrice SET lifeBalls=" + up[1] + " WHERE id=" + up[0] + ";");
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}

		// ship prices were lowered: bring existing saves to the current price list (safe to repeat, bought flags are untouched)
		try {
			int[][] shipPrices = {{1,50,0},{2,0,50},{3,50,0},{4,0,50},{5,0,60},{6,50,50},{7,60,0},{8,50,50},{9,0,70},{10,70,0},{11,80,0},{12,60,60},{13,60,60},{14,0,80},{15,0,100},{16,100,0},{17,0,100},{18,100,0},{19,0,120},{20,120,0},{21,100,60},{22,80,80},{23,130,0},{24,0,130},{25,70,100},{26,0,150},{27,150,0},{28,150,0},{29,0,150},{30,110,110},{31,110,110},{32,110,110},{33,110,110},{34,150,90},{35,0,200},{36,200,0},{37,0,200},{38,200,0},{39,130,150},{40,150,130},{41,130,150},{42,160,140},{43,0,300},{44,300,0},{45,0,300},{46,300,0},{47,210,110},{48,200,120},{49,225,130},{50,0,400},{51,400,0},{52,175,250},{53,500,0},{54,0,500},{55,625,0},{56,0,625},{57,260,260},{58,260,260},{59,225,375},{60,375,225},{61,0,750},{62,750,0},{63,0,1000},{64,1000,0},{65,500,500},{66,500,500},{67,600,650},{68,1000,750},{69,750,1000},{70,0,2000},{71,2000,0},{72,1600,1200},{73,2000,2000},{74,2000,2000}};
			for (int[] sp : shipPrices)
				shipsDB.execSQL("UPDATE ships SET reds=" + sp[1] + ", blues=" + sp[2] + " WHERE id=" + sp[0] + ";");
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}

		// galaxy prices were lowered: bring existing saves to the current price list (safe to repeat, bought flags are untouched)
		try {
			int[][] galaxyPrices = {{1, 50, 50}, {2, 100, 100}, {3, 150, 150}, {4, 150, 150}, {5, 250, 200}, {6, 200, 250}, {7, 400, 300}, {8, 300, 400}, {9, 500, 500}};
			for (int[] g : galaxyPrices)
				galaxiesDB.execSQL("UPDATE galaxies SET reds=" + g[1] + ", blues=" + g[2] + " WHERE id=" + g[0] + ";");
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}

		
	    this.setScreen(new Splash(this));
		
	}
	
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}	
	
	private final com.spaceship.handlers.Bloom bloom = new com.spaceship.handlers.Bloom();

	public void render() {
		// the menu and the game are drawn through the bloom pass (the splash has its own light effects)
		com.badlogic.gdx.Screen before = getScreen();
		if(!(before instanceof PlayScreen))
			com.spaceship.handlers.Bloom.fringe = 0f;
		boolean glow = (before instanceof MainMenuScreen || before instanceof PlayScreen) && bloom.begin();
        super.render(); 
		if(glow){
			bloom.end();
			bloom.watch(com.badlogic.gdx.Gdx.graphics.getDeltaTime());
		}
        if(com.badlogic.gdx.Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.BACK)){
        	com.badlogic.gdx.Screen s = getScreen();
        	if(s instanceof MainMenuScreen)
        		((MainMenuScreen) s).onBack();
        	else if(s instanceof PlayScreen)
        		((PlayScreen) s).onBack();
        }
    }

    public void dispose() {
        bloom.dispose();
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
