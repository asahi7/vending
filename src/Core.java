import coinAcceptor.CoinAcceptor;
import billAcceptor.BillAcceptor;

public class Core {

	private static Thread cAcceptor = new CoinAcceptor();
	
	private static Thread bAcceptor = new BillAcceptor();
	
	public static void main(String[] args) {
		cAcceptor.start();
		bAcceptor.start();
		try{
			Thread.sleep(10000);
			while(true){
				int coins = ((CoinAcceptor) cAcceptor).getCurrentMoney();
				int bills = ((BillAcceptor) bAcceptor).getCurrentMoney();
				System.out.println("CurrentMoneyEntered: " + (coins + bills));
				Thread.sleep(1000);
			}
		} catch(InterruptedException e){
			e.printStackTrace();
		}
	}	
	
}
