package com.habijabi.mealplanner;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v7.widget.Toolbar;
import android.content.ClipboardManager;
import android.view.View;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLDataException;
import java.util.ArrayList;

public class CreateRecipe extends Activity {
    Uri uriSavedImage1;
    String photo_name, EXTRA = "message", columns, values;
    Uri uriSavedImage = Uri.parse("file:///sdcard/ArtRage/blah.png");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);

        Intent intent = getIntent();
        columns = intent.getStringExtra("Tot_col");
        values = intent.getStringExtra("Tot_val");
        ActionBar actionBar = getActionBar();
    }


    public void recipe_pic(View view) throws IOException {
        EditText editText = (EditText) findViewById(R.id.recipe_name);
        String recipe_name = editText.getText().toString();
        if (recipe_name.length() == 0) {
            Toast toast = Toast.makeText(this, "Please enter a valid recipe name", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        photo_name = recipe_name.replaceAll("\\s+", "");
        /* creating folder*/
        File dir = new File("/sdcard/Mealplanner");
        try{
            if(dir.mkdir()) {
                System.out.println("Directory created");
            } else {
                System.out.println("Directory is not created");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        /*folder created*/



        uriSavedImage = Uri.parse("file:///sdcard/Mealplanner/"+ photo_name + ".png");
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
                    startActivityForResult(intent, 1);
                    //    startActivity(intent);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
                    startActivityForResult(intent, 1);
                    // startActivity(intent);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();


    }


    public void paste(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard.getPrimaryClip() != null) {
            ClipData.Item pastetext = clipboard.getPrimaryClip().getItemAt(0);
            EditText editText = (EditText) findViewById(R.id.createRecipe);
            editText.append(pastetext.coerceToText(this));
        } else {
            Toast toast = Toast.makeText(this, "There is nothing to paste!", Toast.LENGTH_SHORT);
            toast.show();
        }

    }


    public void save(View view) throws FileNotFoundException {
        ////////////////
       // new RotateImageClass().execute();
////////




        ////////////////////////////
        uriSavedImage1 = Uri.parse(uriSavedImage.toString());
        EditText editText = (EditText) findViewById(R.id.recipe_name);
        String recipe_name = editText.getText().toString();
        if (recipe_name.length() <= 0) {
            Toast toast = Toast.makeText(this, "Please enter a valid recipe name", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        EditText editText1 = (EditText) findViewById(R.id.createRecipe);
        String description = editText1.getText().toString();
        if (description.length() <= 10) {
            Toast toast = Toast.makeText(this, "Recipe description should be of more than 10 letters", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        ArrayList<String> params=new ArrayList<String>();
        params.add(recipe_name);
        params.add(description);
        params.add(uriSavedImage1.toString());
        params.add(values);
        new SaveRecipeClass().execute(params);
    }

    private class RotateImageClass extends AsyncTask<ArrayList<String>, Void, Boolean> {
        protected void onPreExecute() {
            ProgressDialog dialog= ProgressDialog.show(CreateRecipe.this, "Saving", "Saving this recipe in the database!");
        }

        protected Boolean doInBackground(ArrayList<String>... params) {

            ExifInterface exif = null;
            try {
               // exif = new ExifInterface("/sdcard/MealPlanner/" + photo_name + ".png");
                exif = new ExifInterface("some");
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            int rotationAngle = 0;
            if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("6")) {
                rotationAngle = 90; //90
            } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("8")) {
                rotationAngle = 270; //270
            } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("3")) {
                rotationAngle = 180; //180
            } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("0")) {
                rotationAngle = 90; //90
            }
            Bitmap bmp = BitmapFactory.decodeFile("/sdcard/MealPlanner/" + photo_name + ".png");
            Matrix matrix = new Matrix();
            matrix.postRotate(rotationAngle);
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            //learn content provider for more info
            OutputStream os = null;
            try {
                os = getContentResolver().openOutputStream(uriSavedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
            return true;
        }

        protected void onPostExecute(Boolean success) {
            if (!success) {
                Toast toast = Toast.makeText(CreateRecipe.this, "Issues with Image file.", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }























    private class SaveRecipeClass extends AsyncTask<ArrayList<String>, Void, Boolean> {
        protected void onPreExecute() {
        }

        protected Boolean doInBackground(ArrayList<String>...params) {

            SQLiteOpenHelper starbuzzdb1 = new RecipeDatabase(CreateRecipe.this);
            String recipe_name=params[0].get(0), description=params[0].get(1),uriSavedImage1=params[0].get(2),values=params[0].get(3);

            try {
                SQLiteDatabase db = starbuzzdb1.getWritableDatabase();
                String insert = "INSERT INTO RECIPE(NAME,DESCRIPTION,IMAGE_RESOURCE_ID" + columns + ")VALUES('" + recipe_name + "','" + description + "','" + uriSavedImage1 + "'" + values + ");";
                db.execSQL(insert);
                db.close();
                return true;
            } catch (SQLiteException e) {
                return false;
            }
        }


        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast toast = Toast.makeText(CreateRecipe.this, "The recipe is added.", Toast.LENGTH_SHORT);
                toast.show();
                Intent intent = new Intent(CreateRecipe.this, MainActivity.class);
                startActivity(intent);
            } else {
                Toast toast = Toast.makeText(CreateRecipe.this, "this database is unavailable", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}

