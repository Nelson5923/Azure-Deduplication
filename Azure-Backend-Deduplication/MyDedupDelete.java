import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.util.*;

public class MyDedupDelete {
    
    public static void LocalDelete(String fileToDelete) {

       try {
            
            FileOutputStream fos = null;
            ObjectOutputStream oos = null;
            FileInputStream fis = null;
            ObjectInputStream ois = null;
           
            /* Create a Directory */
            
            File dir = new File("data");

            if (!dir.exists()) {
                if (!dir.mkdir()) {
                    System.err.println("Directory does not exist.");
                    return;
                }
            }
            
            if (!dir.isDirectory()) {
                System.err.println("Not a direcetory");
                return;
            }
            
            /* Create a Index File */

            File indexf = new File(dir.getName() + "/" + "mydedup.index");
            Boolean isNewIndexFile = indexf.createNewFile();
            FileIndexMap IndexMap = new FileIndexMap();
            
            if (!isNewIndexFile) {
                fis = new FileInputStream(indexf.getAbsolutePath());
                ois = new ObjectInputStream(fis);
                IndexMap = (FileIndexMap) ois.readObject();
                ois.close();
                fis.close();
            }

            /* Create a File Recipe */
            
            File recipef = new File(dir.getName() + "/" + "mydedup.recipe");
            Boolean isNewRecipesFile = recipef.createNewFile();
            FileRecipeMap RecipeMap = new FileRecipeMap();
            
            if (!isNewRecipesFile) {
                
                fis = new FileInputStream(recipef.getAbsolutePath());
                ois = new ObjectInputStream(fis);
                RecipeMap = (FileRecipeMap) ois.readObject();
                ois.close();
                fis.close();
                
            }
            
            if (!RecipeMap.containsKey(fileToDelete)) {
                System.err.println("Error: File not exists. (RecipeMap)");
                return;
            }
            
            List<String> FileRecipe = RecipeMap.getRecipe(fileToDelete);
            
            for (String FileChunk : FileRecipe) {
                
                IndexMap.getEntry(FileChunk).minusRef();
                
                if(IndexMap.getEntry(FileChunk).getRefCount() == 0){
                    IndexMap.rmEntry(FileChunk);
                    if (!FileChunk.matches("ZeroChunk:[0-9].*")){
                        File f = new File(dir.getName() + "/" + FileChunk);
                        f.delete();     
                    }
                }
          
            }
            
            RecipeMap.rmRecipe(fileToDelete);
            
            System.out.println("Logging: File is deleted (rmRecipe)");
            
            FileOutputStream indexfos = new FileOutputStream(indexf.getAbsolutePath());
            ObjectOutputStream indexoos = new ObjectOutputStream(indexfos);
            
            indexoos.writeObject(IndexMap);
            indexoos.close();
            indexfos.close();
            
            FileOutputStream recipefos = new FileOutputStream(recipef.getAbsolutePath());
            ObjectOutputStream recipeoos =  new ObjectOutputStream(recipefos);
            
            recipeoos.writeObject(RecipeMap);
            recipeoos.close();
            recipefos.close();

        }catch (Exception e) {
            System.err.println(e.getMessage());
        }
          
    }
    
    public static void AzureDelete(String fileToDelete, AzureInfo ai) {

       try {
            
            FileOutputStream fos = null;
            ObjectOutputStream oos = null;
            FileInputStream fis = null;
            ObjectInputStream ois = null;
           
            /* Create a Directory */
            
            File dir = new File("data");

            if (!dir.exists()) {
                if (!dir.mkdir()) {
                    System.err.println("Directory does not exist.");
                    return;
                }
            }
            
            if (!dir.isDirectory()) {
                System.err.println("Not a direcetory");
                return;
            }
                   
             /* Create a Index File */

            ai.download("mydedup.index",dir.getName());
            File indexf = new File(dir.getName() + "/" + "mydedup.index");
            Boolean isNewIndexFile = indexf.createNewFile();
            FileIndexMap IndexMap = new FileIndexMap();
            
            if (!isNewIndexFile) {
                fis = new FileInputStream(indexf.getAbsolutePath());
                ois = new ObjectInputStream(fis);
                IndexMap = (FileIndexMap) ois.readObject();
                ois.close();
                fis.close();
            }
            
            /* Create a File Recipe */
            
            ai.download("mydedup.recipe",dir.getName());
            File recipef = new File(dir.getName() + "/" + "mydedup.recipe");
            Boolean isNewRecipesFile = recipef.createNewFile();
            FileRecipeMap RecipeMap = new FileRecipeMap();
            
            if (!isNewRecipesFile) {
                
                fis = new FileInputStream(recipef.getAbsolutePath());
                ois = new ObjectInputStream(fis);
                RecipeMap = (FileRecipeMap) ois.readObject();
                ois.close();
                fis.close();
                
            }
            
            if (!RecipeMap.containsKey(fileToDelete)) {
                System.err.println("Error: File not exists. (RecipeMap)");
                return;
            }
            
            List<String> FileRecipe = RecipeMap.getRecipe(fileToDelete);
            
            for (String FileChunk : FileRecipe) {
                
                IndexMap.getEntry(FileChunk).minusRef();
                
                if(IndexMap.getEntry(FileChunk).getRefCount() == 0){
                    
                    IndexMap.rmEntry(FileChunk);
                    if (!FileChunk.matches("ZeroChunk:[0-9].*")){
                        ai.delete(FileChunk);      
                    }
                    
                }
          
            }
            
            RecipeMap.rmRecipe(fileToDelete);
            System.out.println("Logging: File is deleted (rmRecipe)");
            
            FileOutputStream indexfos = new FileOutputStream(indexf.getAbsolutePath());
            ObjectOutputStream indexoos = new ObjectOutputStream(indexfos);
            
            indexoos.writeObject(IndexMap);
            indexoos.close();
            indexfos.close();
            
            FileOutputStream recipefos = new FileOutputStream(recipef.getAbsolutePath());
            ObjectOutputStream recipeoos =  new ObjectOutputStream(recipefos);
            
            recipeoos.writeObject(RecipeMap);
            recipeoos.close();
            recipefos.close();
           
            File source = new File(dir.getName() + "/" + "mydedup.index");
            ai.uploadFromFile(source.getAbsolutePath(), "mydedup.index");
            source = new File(dir.getName() + "/" + "mydedup.recipe");
            ai.uploadFromFile(source.getAbsolutePath(), "mydedup.recipe");
            MyDedupTools.removeAll(dir);

        }catch (Exception e) {
                    System.err.println(e.getMessage());
        }
          
    }

}