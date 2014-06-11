
public class Sheep extends Thread {
	int sheepID;
	String sheepCall;

	SheepMap[] sheepMap;

	public Sheep(int id, String message) {
		this.sheepID = id;
		this.sheepCall = message;
	}

	public void run() {
		SheepPatrol sheeper = new SheepPatrol();
		try {
			Thread.sleep(this.sheepID * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		sheeper.beepSheep(sheepCall);
	}

}
