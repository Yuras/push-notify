package com.example.gsoc_example_connect4;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

//Simple message to show a new invitation to play.
public class NewInvitation extends DialogFragment {
	MainActivity activityHost;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {        
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage("You received an invitation to play against: " + activityHost.getActualPlayer())
    		   .setNegativeButton("Reject"   ,new DialogInterface.OnClickListener() {
    				   public void onClick(DialogInterface dialog, int which) {
    					   activityHost.sendCancel();
    				   }
    		   })
    		   .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
    			   public void onClick(DialogInterface dialog, int which) {
    				   ;
    			   }
    		   });
        return builder.create();
    }

	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activityHost = (MainActivity) activity;
    }
}
