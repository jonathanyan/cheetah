
public class SuperSheep {

	public static void main(String[] args) {
		String[] msg={"aa", "bb", "cc","dd"};
		Sheep[] sheepers = new Sheep[4];
		int i = 0;
		for (String oneMsg : msg) {
			sheepers[i++] = new Sheep(i, oneMsg); 
		}
		
		for (Sheep sheep: sheepers) {
			sheep.start();
		}
	}

	public SuperSheep() {
		super();
	}
}
