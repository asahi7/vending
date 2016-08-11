package coinAcceptor;
import gnu.io.SerialPort;
import gnu.io.CommPortIdentifier;


public class CoinAcceptor extends Thread { // Afterwards, we need to worry about the security of this class, that the other classes wouldn't be able to access it

	private Cctalk protocol;
	private volatile int currentMoneyAccepted = 0; // Correct? O.o
	
	private void connect(String comPortName) throws Exception{
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(comPortName);
		if(portIdentifier.isCurrentlyOwned()){
			System.out.println(comPortName + " is used by another application");
			throw new Exception("Coin acceptor's port is not free");
		}
		else{
			SerialPort serialPort = (SerialPort) portIdentifier.open("CoinAcceptor", 2000);
			serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			protocol = new Cctalk(serialPort.getInputStream(), serialPort.getOutputStream());
		}
	}
		
	public int getCurrentMoney(){
		return currentMoneyAccepted;
	}
	
	public void annulMoneyCounter(){
		currentMoneyAccepted = 0;
	}	
		
	public void run() { // if working as a thread, simply rename this method to run()
		try{
			connect("COM4"); // name of the COM-port
			protocol.simplePoll();
			protocol.acceptCoins();
			protocol.getLastAccepted();
			while(true){
				int coin = protocol.getLastAccepted();
				if(coin != -1) currentMoneyAccepted += coin;
			}
		} catch(InterruptedException e){
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
