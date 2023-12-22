package kevin.carrismobile.custom;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.NonNull;

import kevin.carrismobile.fragments.MainActivity;
import kevin.carrismobile.fragments.StopFavoritesFragment;

public class MyCustomDialog extends Dialog {
    public MyCustomDialog(@NonNull Context context) {
        super(context);
    }

    public static AlertDialog createOkButtonDialog(Context context, String title, String message){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setMessage(message);
        builder1.setTitle(title);
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        return builder1.create();
    }

    public static AlertDialog createYesAndNoButtonDialogStopFavorite(Context context, String title, String message, Activity activity){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setMessage(message);
        builder1.setTitle(title);
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity mainActivity = (MainActivity) activity;
                        StopFavoritesFragment fragment = (StopFavoritesFragment) ((MainActivity) activity).stopFavoritesFragment;
                        fragment.setRemoveListSelectionDecision(true);
                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MainActivity mainActivity = (MainActivity) activity;
                StopFavoritesFragment fragment = (StopFavoritesFragment) ((MainActivity) activity).stopFavoritesFragment;
                fragment.setRemoveListSelectionDecision(false);
                dialogInterface.cancel();
            }
        });

        return builder1.create();
    }
}
