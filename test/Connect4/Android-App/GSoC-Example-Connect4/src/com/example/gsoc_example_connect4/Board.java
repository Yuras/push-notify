package com.example.gsoc_example_connect4;

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.SharedPreferences;
import android.util.Log;

// This class represents game state.
public class Board {
	private Integer[] positions;
	SharedPreferences prefs;
	int turn= 0;
	String actualPlayer;

	//Main constructor.
	public Board(SharedPreferences pref,boolean newgame,boolean firstPlayer){
		prefs = pref;
		positions = new Integer[42];
		if(newgame)
			setNewGame(firstPlayer);
        else
        	readState();
	}
 
	//Recovers game state.
	public void readState(){
		String gameState= prefs.getString("board",null);
		turn= prefs.getInt("turn",0);
        if(gameState == null)
        	setNewGame(true);
        else{
        	try{
        		JSONArray json= new JSONArray(gameState);
           		positions = new Integer[42];
            	for(int i=0;i<42;i++){
            		positions[i] = json.getInt(i);	
            	}
        	}catch(JSONException e){Log.i("tag", "JSON EXCEPTION!!");setNewGame(true);}
        }
	}
	
	final private Integer[] newGame = {
            R.drawable.empty , R.drawable.empty, R.drawable.empty , R.drawable.empty ,
            R.drawable.empty , R.drawable.empty, R.drawable.empty , R.drawable.empty ,
            R.drawable.empty , R.drawable.empty, R.drawable.empty , R.drawable.empty ,
            R.drawable.empty , R.drawable.empty, R.drawable.empty , R.drawable.empty ,
            R.drawable.empty , R.drawable.empty, R.drawable.empty , R.drawable.empty ,
            R.drawable.empty , R.drawable.empty, R.drawable.empty , R.drawable.empty ,
            R.drawable.empty , R.drawable.empty, R.drawable.empty , R.drawable.empty ,
            R.drawable.empty , R.drawable.empty, R.drawable.empty , R.drawable.empty ,
            R.drawable.empty , R.drawable.empty, R.drawable.empty , R.drawable.empty ,
            R.drawable.empty , R.drawable.empty, R.drawable.empty , R.drawable.empty ,
            R.drawable.empty , R.drawable.empty
    };

	public void setNewGame(boolean firstPlayer){
		for(int i=0;i<42;i++){
    		positions[i] = newGame[i];	
    	}
		if(firstPlayer)
			turn = 1;
		else
			turn = 2;
		save();
	}
	
	public int getPosition(int pos){
    	return positions[pos];
    }
	
	private void setPosition(int pos,int player){
		int color;
		if (player == 1)
			color= R.drawable.red;
		else{
			if (player == 2)
				color= R.drawable.yellow;
			else
				return ;
		}
		positions[pos] = color;
    }
	
	public int getTurn(){
		return turn;
	}
	
	public Boolean newMovement(int column,int player){
		if(column > 6 || column <0 || positions[column]!=R.drawable.empty)
			return false;
		if(turn == player){
    		for(int i=column;i<42;i+=7){
    			if((i<35 && positions[i+7]!=R.drawable.empty) ||  (i>=35)){
					setPosition(i,player);
					break;
    			}
    		}
    		turn=(turn%2)+1;
    		save();
    		return true;
		}
		return false;
	}
	
	//Go back on last movement.
	public void cancelMovement(int column){
		if(column > 6 || column <0)
			return ;
		for(int i=column;i<42;i+=7){
			if((i<35 && positions[i+7]!=R.drawable.empty) ||  (i>=35)){
					if(positions[i+7] == R.drawable.red)
						positions[i+7]=R.drawable.empty;
					break;
			}
		}
		turn=1;
		save();
	}
	
	//Saves game state.
	public void save(){
		SharedPreferences.Editor editor = prefs.edit();
		JSONArray json = new JSONArray(Arrays.asList(positions));
		editor.putString("board", json.toString());
		editor.putInt("turn", turn);
		editor.commit();
	}
}
