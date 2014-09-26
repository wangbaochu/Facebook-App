package com.baochu.androidassignment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.baochu.androidassignment.UiOOMHandler.UiRunnable;
import com.facebook.FacebookRequestError;
import com.facebook.Session;
import com.facebook.SessionDefaultAudience;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

public class Utils {

    /** The permission to read different field of user profile */
    public static final String PROFILE_PERMISSION  = "public_profile";
    public static final String EAMIL_PERMISSION    = "email"; 
    public static final String BIRTHDAY_PERMISSION = "user_birthday";
    public static final String LOCATION_PERMISSION = "user_location";
    public static final String LIKES_PERMISSION    = "user_likes";
    /** The permission to read user's status update message */
    public static final String STATUS_PERMISSION   = "user_status";
    public static final String STREAM_PERMISSION   = "read_stream";
    /** The permissions to read user photos */
    public static final String PHOTO_PERMISSION    = "user_photos";
    
    private static final Uri M_FACEBOOK_URL = Uri.parse("http://m.facebook.com");
    
    public static final int BUFFER_SIZE_DEFAULT_FOR_FILE = 8192;

    public static List<String> getAllNecessaryReadPermission() {
        List<String> permissions = new ArrayList<String>();
        permissions.add(Utils.PROFILE_PERMISSION);
        permissions.add(Utils.BIRTHDAY_PERMISSION);
        permissions.add(Utils.EAMIL_PERMISSION);
        permissions.add(Utils.LOCATION_PERMISSION);
        permissions.add(Utils.LIKES_PERMISSION);
        permissions.add(Utils.STATUS_PERMISSION);
        permissions.add(Utils.STREAM_PERMISSION);
        permissions.add(Utils.PHOTO_PERMISSION);
        return permissions;
    }
    
    public static List<String> getProfileReadPermission() {
        List<String> permissions = new ArrayList<String>();
        permissions.add(Utils.PROFILE_PERMISSION);
        permissions.add(Utils.BIRTHDAY_PERMISSION);
        permissions.add(Utils.EAMIL_PERMISSION);
        permissions.add(Utils.LOCATION_PERMISSION);
        permissions.add(Utils.LIKES_PERMISSION);
        return permissions;
    }
    
    public static List<String> getUserStatusReadPermission(){
        List<String> permissions = new ArrayList<String>();
        permissions.add(Utils.STATUS_PERMISSION);
        permissions.add(Utils.STREAM_PERMISSION);
        return permissions;
    }
    
    public static List<String> getUserAlbumPhotoReadPermission(){
        List<String> permissions = new ArrayList<String>();
        permissions.add(Utils.PHOTO_PERMISSION);
        return permissions;
    }
    
    public static byte[] getByteArray(final InputStream is) throws IOException {
        byte[] buf = new byte[BUFFER_SIZE_DEFAULT_FOR_FILE];
        int bytesRead;
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        while ((bytesRead = is.read(buf)) != -1) {
            os.write(buf, 0, bytesRead);
        }
        return os.toByteArray();
    }
    
    /**
     * Dismiss the soft keyboard. 
     * @param view any view associated with the window for which the keyboard should be dismissed
     */
    public static void closeSoftKeyboard(final View view) {
        if (null == view) {
            return;
        }
        final InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        if (null != imm) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    public static void updateImageWithDrawable(final Bitmap bitmap,
            final ImageView imageView) {
         UiOOMHandler oomHandler = new UiOOMHandler((Activity) imageView.getContext(), 
                 new UiRunnable(new Runnable() {
                     @Override
                     public void run () {
                         if (null == bitmap) {
                             imageView.setImageResource(R.drawable.noimage);
                         } else {
                             imageView.setImageBitmap(bitmap);
                         }
                     }
                 }, true));
         oomHandler.execute();
     }

    /** Request new permission */
    public static void requestPermissions(Activity activity, Session session, List<String> permissions) {
        if (session != null && permissions != null && permissions.size() > 0) {
            Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(activity, permissions);
            newPermissionsRequest.setDefaultAudience(SessionDefaultAudience.FRIENDS).setRequestCode(RequestCodes.REAUTH_ACTIVITY_CODE);
            session.requestNewReadPermissions(newPermissionsRequest);
        }
    }
    
    /** Handle the error response */
    public static void handleError(final Activity activity, final FacebookRequestError error, final List<String> permissions) {
        DialogInterface.OnClickListener listener = null;
        String dialogBody = null;

        if (error == null) {
            dialogBody = activity.getString(R.string.error_dialog_default_text);
        } else {
            switch (error.getCategory()) {
            case AUTHENTICATION_RETRY:
                // tell the user what happened by getting the message id, and
                // retry the operation later
                String userAction = (error.shouldNotifyUser()) ? "" : activity.getString(error.getUserActionMessageId());
                dialogBody = activity.getString(R.string.error_authentication_retry, userAction);
                listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, M_FACEBOOK_URL);
                        activity.startActivity(intent);
                    }
                };
                break;

            case AUTHENTICATION_REOPEN_SESSION:
                // close the session and reopen it.
                dialogBody = activity.getString(R.string.error_authentication_reopen);
                listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Session session = Session.getActiveSession();
                        if (session != null && !session.isClosed()) {
                            session.closeAndClearTokenInformation();
                        }
                    }
                };
                break;

            case PERMISSION:
                // request the publish permission
                dialogBody = activity.getString(R.string.error_permission);
                listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermissions(activity, Session.getActiveSession(), permissions);
                    }
                };
                break;

            case SERVER:
            case THROTTLING:
                // this is usually temporary, don't clear the fields, and
                // ask the user to try again
                dialogBody = activity.getString(R.string.error_server);
                break;

            case BAD_REQUEST:
                // this is likely a coding error, ask the user to file a bug
                dialogBody = activity.getString(R.string.error_bad_request, error.getErrorMessage());
                break;

            case OTHER:
            case CLIENT:
            default:
                // an unknown issue occurred, this could be a code error, or
                // a server side issue, log the issue, and either ask the
                // user to retry, or file a bug
                dialogBody = activity.getString(R.string.error_unknown, error.getErrorMessage());
                break;
            }
        }

        String title = error.getErrorUserTitle();
        String message = error.getErrorUserMessage();
        if (message == null) {
            message = dialogBody;
        }
        if (title == null) {
            title = activity.getResources().getString(R.string.error_dialog_title);
        }

        new AlertDialog.Builder(activity)
        .setPositiveButton(R.string.error_dialog_button_text, listener)
        .setTitle(title)
        .setMessage(message)
        .show();
    }
    
    /** Create and show the progress dialog */
    public static AlertDialog showProgressDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(R.layout.progress_bar);
        return dialog;
    }
}
