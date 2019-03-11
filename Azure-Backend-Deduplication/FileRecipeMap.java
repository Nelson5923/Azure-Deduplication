import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileRecipeMap implements Serializable{
    
    public HashMap<String, List<String>> RecipeMap;

    public FileRecipeMap() {
        this.RecipeMap= new HashMap<String, List<String>>();
    }
    
    public Boolean containsKey(String s){
        return RecipeMap.containsKey(s);
    }
    
    public void putRecipe(String s, List<String> ls){
        RecipeMap.put(s,ls);
    }
    
    public List<String> getRecipe(String s){
        return RecipeMap.get(s);
    }
    
    public void rmRecipe(String s){
        RecipeMap.remove(s);
    }
    
}