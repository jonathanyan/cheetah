
import java.util.Random;

public class RandomSeed {
	int[] randomNumber;

	RandomSeed(int size) {
		this.randomNumber = new int[size];
		for (int i = 0; i < size; i++) {
			Random rand = new Random(i);
			randomNumber[i] = rand.nextInt(size);
		}
	}
}
