import java.util.*;

// structure to hold variable types in Expressions()
public class StoreTypes{
	public ArrayList<String> types;
	// addition to initial code
	public String reg;

	public StoreTypes(){
		this.types = new ArrayList<String>();
	}

	public void addType(String type){
		this.types.add(type);
	}
}