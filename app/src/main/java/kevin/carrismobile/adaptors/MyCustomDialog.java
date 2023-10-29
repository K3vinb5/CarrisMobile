package kevin.carrismobile.adaptors;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.NonNull;

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
}
