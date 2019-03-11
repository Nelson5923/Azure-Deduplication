import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class IndexEntry implements Serializable {
    
    public int chunkSize;
    public int refCount;

    public IndexEntry(int chunkSize, int refCount) {
        this.chunkSize = chunkSize;
        this.refCount = refCount;
    }

    public IndexEntry() {
        chunkSize = 0;
        refCount = 0;
    }

    public void addRef() {
        this.refCount = this.refCount + 1;
    }
    
    public void minusRef() {
        this.refCount = this.refCount - 1;
    }
 
    public int getRefCount() {
        return this.refCount;
    }
    
    public int getChunkSize() {
        return this.chunkSize;
    }
    
}
