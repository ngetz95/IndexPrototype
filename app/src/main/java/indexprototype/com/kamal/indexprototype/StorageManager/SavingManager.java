package indexprototype.com.kamal.indexprototype.StorageManager;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import indexprototype.com.kamal.indexprototype.StoriesBank;
import indexprototype.com.kamal.indexprototype.Story;

/**
 * A class that saves stories and their information into internal memory.
 * A folder is created for each story. Each folder contains a JSONObjecr
 * representation of the story.
 * @author Kamal Kamalaldin
 * @version 12/15/2014
 */
public class SavingManager {

    private JSONArray storiesJsonArray;
    private Context mContext;

    /**
     * A public constructor
     * @param context The application's context.
     */
    public SavingManager(Context context){
        mContext = context;
    }



    /**
     * Saves List of stories into memory. Each story is saved as a JSONObjct
     * into its own folder
     * @param storiesList A <Code>List</Code> of stories.
     * @return boolean  True if all the stories were saved successfully to memory.
     * False if any story was not saved successfully.
     */
    public boolean saveStoriesToMemory(List<Story> storiesList){
        Log.d("SavingManager", "Stories in database: " );
        StoriesBank.printStories();
        boolean allSuccess = true;
        for(Story story: storiesList){
            boolean oneSucess = saveStory(story);
            if (!oneSucess)
                allSuccess = false;
            Log.d("SavingManager",  story.getTitle() + " was saved: " + oneSucess);
        }

        return allSuccess;
    }


    /**
     * Saves a story in JSON format in folder named after the story's title,
     * without spaces or "/" characters.
     * @param story The story to be saved to memeory.
     * @return boolean  true if the JSONObject was saved successfully, false
     * if any errors were encountered.
     */
    private boolean saveStory(Story story){

        Writer writer = null;

        try {
            JSONObject JSONstory = story.toJSON();
            File storiesFile = mContext.getDir(STORIES_FILE_PATH, Context.MODE_APPEND);
            File storyFile = new File(storiesFile, story.getCondensedTitle());
            boolean createdDirectory = storyFile.mkdir();
            Log.d("SavingManager", "Directory " + storyFile + " was created with success: " + createdDirectory);

            File storyJSONFile = new File(storyFile, STORY_FILE_NAME);
//            createdDirectory = storyJSONFile.mkdir();
//            Log.d("SavingManager", "Directory " + storyJSONFile + " was created with success: " + createdDirectory);

            OutputStream outputStream = new FileOutputStream(storyJSONFile);
            writer = new OutputStreamWriter(outputStream);
            writer.write(JSONstory.toString());


        } catch (JSONException e) {
            Log.e("SavingManager", "The file was unable to be created");
            e.printStackTrace();
            return false;
        } catch (FileNotFoundException e) {
            Log.e("SavingManager", "The file was unable to be created");
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            Log.e("SavingManager", "The file was unable to be created");
            e.printStackTrace();
            return false;
        } finally {
            if(writer!=null)
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
//        Log.e("SavingManager", "The file was unable to be created");
//        e.printStackTrace();
//        return false;
//        Log.d("SavingManager", "Story saved to file " + mContext.getFilesDir() + "/" + STORIES_FILE_PATH + "/" + filename + " successfully.");
        return true;
    }


    public final static String STORIES_FILE_PATH = "Stories";
    public final static String STORY_FILE_NAME = "story";

}
