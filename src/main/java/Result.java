import java.util.Comparator;

public class Result {
	String name;
	int count;
	
	public Result(String name, int count) {
		this.name = name;
		this.count = count;
	}
}

class CitedAuthorComparator implements Comparator<Result> { 
    public int compare(Result a, Result b) {
    	return b.count - a.count;
    }
} 
