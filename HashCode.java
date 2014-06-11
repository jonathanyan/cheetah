
public class HashCode {
	int hashCode(String key) {
		int hashVal = 0;
		for (int i = 0; i < key.length(); i++) {
			hashVal = (127 * hashVal + key.charAt(i)) % 15485867;
		}
		return hashVal;
	}

	int hashCode(int key) {
		return ((key * 17077 + 9013) % 15485867);
	}
}
