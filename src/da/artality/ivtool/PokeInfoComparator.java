package da.artality.ivtool;

import java.util.Comparator;

/**
 * Standard comparator for PokeInfo that compares in the following order: <br />
 * <br/>
 * 1. Pokemon ID (ascending)<br/>
 * 2. IV percentage (descending)<br/>
 * 3. CP (descending)
 * 
 * @author Tsunamii
 *
 */
public class PokeInfoComparator implements Comparator<PokeInfo> {

	@Override
	public int compare(PokeInfo p1, PokeInfo p2) {
		int i = p1.getNr() - p2.getNr();
		if (i != 0) {
			return i;
		}
		i = p2.getIvPerc() - p1.getIvPerc();
		if (i != 0) {
			return i;
		}
		return p2.getCp() - p1.getCp();
	}
}
