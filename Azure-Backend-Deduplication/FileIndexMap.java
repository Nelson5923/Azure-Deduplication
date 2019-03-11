import java.io.Serializable;
import java.util.*;

public class FileIndexMap implements Serializable{
    
    public HashMap<String, IndexEntry> IndexMap;

    public FileIndexMap() {
        IndexMap = new HashMap<String, IndexEntry>();
    }

    public Boolean containsKey(String s){
        return IndexMap.containsKey(s);
    }

    public IndexEntry getEntry(String s){
        return IndexMap.get(s);
    }
    
    public void rmEntry(String s){
        IndexMap.remove(s);
    }
    
    public void putEntry(String s, IndexEntry fi){
        this.IndexMap.put(s, fi);
    }
    
    public Set<String> getKey(){
        return IndexMap.keySet();
    }
    
}
