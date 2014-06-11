
public class SuperBTree {

	static final int MAXENTRY = 3;
	static final int SIZE = 15;

	public static void main(String[] args) {
		RandomSeed rs = new RandomSeed(100);
		BTree bTreeRoot = new BTree();
		// BTree myNode;
		Fruit fruit;

		for (int i = 0; i < SIZE; i++) {
			System.out.print(" " + rs.randomNumber[i]);
		}
		System.out.println();

		for (int i = 0; i < SIZE; i++) {
			fruit = new Fruit(rs.randomNumber[i]);
			bTreeRoot = bTreeRoot.insertNewFruit(fruit, null, bTreeRoot, -1);
		}
		// bTreeRoot.traversalTree("MAIN", bTreeRoot, 0);

		for (int i = 0; i < SIZE; i++) {
			fruit = new Fruit(rs.randomNumber[i]);
			bTreeRoot.deleteFruit(fruit);

			System.out.println();
			bTreeRoot.traversalTree("# "+i+ " ", bTreeRoot, 0);
		}

		/*
		 * fruit = new Fruit(60); bTreeRoot.deleteFruit(fruit);
		 * 
		 * System.out.println(); bTreeRoot.traversalTree("SEC", bTreeRoot, 0);
		 */
	}

	public SuperBTree() {
		super();
	}

}
