
public class BTree {
	Fruit[] fruits;
	BTree[] nextNode;
	int entrySize;
	boolean isLeaf;

	BTree() {
		entrySize = 0;
		isLeaf = true;
		fruits = new Fruit[3];
		nextNode = new BTree[4];
	}

	BTree(Fruit fruit) {
		entrySize = 1;
		fruits = new Fruit[3];
		nextNode = new BTree[4];
		fruits[0] = fruit;
	}

	BTree splitNode(BTree parent, BTree me, int parentOffset) {
		// create 2 new single fruit node
		BTree leftNode = new BTree(me.fruits[0]);
		BTree rightNode = new BTree(me.fruits[2]);
		leftNode.isLeaf = me.isLeaf;
		rightNode.isLeaf = me.isLeaf;

		/* If not leaf, add nextNode information */
		if (!me.isLeaf) {
			leftNode.nextNode[0] = me.nextNode[0];
			leftNode.nextNode[1] = me.nextNode[1];
			rightNode.nextNode[0] = me.nextNode[2];
			rightNode.nextNode[1] = me.nextNode[3];
		}

		if (parent == null) {
			/* Split root */
			BTree newRoot = new BTree(me.fruits[1]);
			newRoot.nextNode[0] = leftNode;
			newRoot.nextNode[1] = rightNode;
			newRoot.isLeaf = false;
			return newRoot;
		} else {
			/* Add new node at parent level */
			shiftFruit(me.fruits[1], parent, parentOffset);
			for (int i = parent.entrySize; i > parentOffset + 1; i--) {
				parent.nextNode[i] = parent.nextNode[i - 1];
			}
			parent.nextNode[parentOffset] = leftNode;
			parent.nextNode[parentOffset + 1] = rightNode;
			return parent;
		}
	}

	void shiftFruit(Fruit fruit, BTree me, int offset) {
		int i;
		if (offset < 0) {
			for (i = 0; i < me.entrySize; i++) {
				if (fruit.id < me.fruits[i].id)
					break;
			}
		} else {
			i = offset;
		}
		Fruit[] newFruits = new Fruit[me.entrySize + 1];
		for (int j = me.entrySize; j > i; j--) {
			newFruits[j] = me.fruits[j - 1];
		}
		newFruits[i] = fruit;
		for (int j = 0; j < i; j++) {
			newFruits[j] = me.fruits[j];
		}
		me.fruits = newFruits;
		me.entrySize++;
	}

	BTree insertNewFruit(Fruit fruit, BTree parent, BTree me, int parentOffset) {
		/* Current node full already, then split */
		if (me.entrySize == 3) {
			parent = splitNode(parent, me, parentOffset);
			int i;
			for (i = 0; i < parent.entrySize; i++) {
				if (fruit.id < parent.fruits[i].id)
					break;
			}
			/* Recursive after split full node */
			parent = insertNewFruit(fruit, parent, parent.nextNode[i], i);
			return parent;
		}

		/* directly insert to leaf */
		if (me.isLeaf) {
			// insert leaf node
			shiftFruit(fruit, me, -1);
			if (parent == null)
				return me;
			else
				return parent;
		}

		// Go to next level, recursive
		int i;
		for (i = 0; i < me.entrySize; i++) {
			if (fruit.id < me.fruits[i].id)
				break;
		}
		insertNewFruit(fruit, me, me.nextNode[i], i);
		if (parent == null)
			return me;
		else
			return parent;
	}

	BTree[] findFruit(Fruit fruit, BTree me, BTree parent) {
		int i;
		BTree[] retTree = new BTree[2];
		/* If found in current node, return */
		for (i = 0; i < me.entrySize; i++) {
			if (me.fruits[i].id == fruit.id) {
				retTree[0] = parent;
				retTree[1] = me;
				return retTree;

			}
		}

		/* Not in current code, leaf already, return NOT found */
		if (me.isLeaf) {
			retTree[0] = null;
			retTree[1] = null;
			return retTree;

		}

		/* Find entry to next level */
		for (i = 0; i < me.entrySize; i++) {
			if (fruit.id < me.fruits[i].id)
				break;
		}

		/* Recursive to next level */
		return findFruit(fruit, me.nextNode[i], me);
	}

	void deleteFruitAtSimpleLeaf(Fruit fruit, BTree me) {
		int i;
		for (i = 0; i < me.entrySize; i++) {
			if (me.fruits[i].id == fruit.id)
				break;
		}
		for (int j = i; j < me.entrySize - 1; j++) {
			me.fruits[j] = me.fruits[j + 1];
		}
		me.entrySize--;
	}

	int siblingCount(Fruit fruit, BTree me, BTree parent, int myIndex) {
		if (myIndex > 0)
			if (parent.nextNode[myIndex - 1].entrySize > 1)
				return myIndex - 1;
		if (myIndex < parent.entrySize)
			if (parent.nextNode[myIndex + 1].entrySize > 1)
				return myIndex + 1;
		return -1;
	}

	void deleteFruitByStealSibling(Fruit fruit, BTree me, BTree parent,
			int siblingIndex, int myIndex) {
		BTree siblingNode = parent.nextNode[siblingIndex];
		if (siblingIndex > myIndex) {
			me.fruits[0] = parent.fruits[myIndex];
			parent.fruits[myIndex] = siblingNode.fruits[0];
			siblingNode.deleteFruitAtSimpleLeaf(siblingNode.fruits[0],
					siblingNode);
		} else {
			me.fruits[0] = parent.fruits[myIndex - 1];
			parent.fruits[myIndex - 1] = siblingNode.fruits[siblingNode.entrySize - 1];
			;
			siblingNode.deleteFruitAtSimpleLeaf(
					siblingNode.fruits[siblingNode.entrySize - 1], siblingNode);
		}
	}

	void deleteFruitByStealParent(Fruit fruit, BTree me, BTree parent,
			int myIndex) {
		if (myIndex < parent.entrySize) {
			me.entrySize = 2;
			Fruit[] newFruits = new Fruit[me.entrySize];
			newFruits[0] = parent.fruits[myIndex];
			newFruits[1] = parent.nextNode[myIndex + 1].fruits[0];
			me.fruits = newFruits;
			for (int i = myIndex; i < parent.entrySize - 1; i++) {
				parent.fruits[i] = parent.fruits[i + 1];
				parent.nextNode[i + 1] = parent.nextNode[i + 2];
			}
			parent.entrySize--;
		} else {
			Fruit[] newFruits = new Fruit[2];
			newFruits[1] = parent.fruits[myIndex - 1];
			newFruits[0] = parent.nextNode[myIndex - 1].fruits[0];
			parent.nextNode[myIndex - 1].fruits = newFruits;
			parent.nextNode[myIndex - 1].entrySize = 2;
			parent.entrySize--;
		}
	}

	void deleteFruitByMergeParent(Fruit fruit, BTree me, BTree parent,
			int myIndex) {
		parent.entrySize = 2;
		parent.isLeaf = true;
		Fruit[] newFruits = new Fruit[2];
		if (myIndex == 0) {
			newFruits[0] = parent.fruits[0];
			newFruits[1] = parent.nextNode[1].fruits[0];
		} else {
			newFruits[1] = parent.fruits[0];
			newFruits[0] = parent.nextNode[0].fruits[0];
		}
		parent.fruits = newFruits;
	}

	void deleteFruitAtLeaf(Fruit fruit, BTree parent, BTree me, int myIndex) {
		int siblingIndex;
		if (me.entrySize > 1) {
			me.deleteFruitAtSimpleLeaf(fruit, me);
		} else {
			/* Pick up sibling node with entrySize >= 2 */
			siblingIndex = parent.siblingCount(fruit, me, parent, myIndex);
			if (siblingIndex >= 0)
				parent.deleteFruitByStealSibling(fruit, me, parent,
						siblingIndex, myIndex);
			else if (parent.entrySize > 1)
				parent.deleteFruitByStealParent(fruit, me, parent, myIndex);
			else
				parent.deleteFruitByMergeParent(fruit, me, parent, myIndex);
		}

	}

	void deleteFruitAtNode(Fruit fruit, BTree me) {
		int meIndex, i, leafMeIndex;
		BTree leafParent, leafMe;

		for (i = 0; i < me.entrySize; i++)
			if (fruit.id == me.fruits[i].id)
				break;
		meIndex = i;

		/* Find minimum bigger fruit from leaf */
		leafParent = me;
		leafMe = me.nextNode[meIndex + 1];
		leafMeIndex = meIndex + 1;
		while (!leafMe.isLeaf) {
			leafParent = leafMe;
			leafMe = leafMe.nextNode[0];
			leafMeIndex = 0;
		}

		me.fruits[meIndex] = leafMe.fruits[0];

		fruit = leafMe.fruits[0];

		System.out.println(" leafmeindex " + leafMeIndex);

		deleteFruitAtLeaf(fruit, leafParent, leafMe, leafMeIndex);

	}

	void deleteFruitAtRoot(Fruit fruit, BTree me) {
		if (me.isLeaf) {
			if (me.entrySize == 1) {
				me.entrySize = 0;
			} else {
				me.deleteFruitAtSimpleLeaf(fruit, me);
			}
		} else {
			deleteFruitAtNode(fruit, me);
		}
	}

	BTree deleteFruit(Fruit fruit) {
		BTree me, parent;
		int myIndex;
		BTree retFromFind[];

		retFromFind = this.findFruit(fruit, this, null);
		parent = retFromFind[0];
		me = retFromFind[1];

		if (me == null && parent == null)
			return null;

		if (parent == null && me != null) {
			deleteFruitAtRoot(fruit, me);
		} else {
			int i;
			for (i = 0; i < parent.entrySize; i++)
				if (fruit.id < parent.fruits[i].id)
					break;
			myIndex = i;
			// me = parent.nextNode[myIndex];
			if (me.isLeaf) {
				deleteFruitAtLeaf(fruit, parent, me, myIndex);
			} else {
				deleteFruitAtNode(fruit, me);
			}
		}
		return parent;
	}

	void traversalTree(String tag, BTree node, int level) {
		if (node == null) {
			System.out.println();
			return;
		}

		System.out.print(tag + " level: " + level + " isleaf " + node.isLeaf
				+ " entry# " + node.entrySize + " -- ");
		for (int i = 0; i < node.entrySize; i++)
			System.out.print("-" + node.fruits[i].id);
		System.out.println();
		if (!node.isLeaf) {
			for (int i = 0; i <= node.entrySize; i++)
				traversalTree(tag, node.nextNode[i], level + 1);
		}
		// System.out.println();
	}

	int[] first7Fruit(BTree node, int level, int startIndex, int[] retInt) {
		if (node == null || startIndex >= 7) {
			return retInt;
		}

		int level2size=level*10000 +  node.entrySize*100 ;
		for (int i = 0; i < node.entrySize; i++) 
                    if (startIndex < 7 ) retInt[startIndex++]= level2size + node.fruits[i].id;  
		if (!node.isLeaf) {
			for (int i = 0; i <= node.entrySize; i++) {
	                     retInt=first7Fruit( node.nextNode[i], level + 1, startIndex, retInt);
                             startIndex = retInt[7];
                         }
		}

                retInt[7] = startIndex; /* last unit store index */
                return retInt;
	}
}
