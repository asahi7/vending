package billAcceptor;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

public class BillAcceptor extends Thread{ // Now it is not in Thread Mode yet..
	
	private Ccnet protocol;
	private volatile int currentMoneyAccepted = 0; // Correct? O.o
	
	private void connect(String comPortName) throws Exception{
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(comPortName);
		if(portIdentifier.isCurrentlyOwned()){
			System.out.println(comPortName + " is used by another application");
			throw new Exception("Coin acceptor's port is not free");
		}
		else{
			SerialPort serialPort = (SerialPort) portIdentifier.open("BillAcceptor", 2000);
			serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			protocol = new Ccnet(serialPort.getInputStream(), serialPort.getOutputStream());
		}
	}
		
	public int getCurrentMoney(){
		return currentMoneyAccepted;
	}
	
	public void annulMoneyCounter(){
		currentMoneyAccepted = 0;
	} 		

	public void run(){
		try{
			connect("COM7"); // name of the COM-port
			protocol.reset();
			Ccnet.delay(10000);
			protocol.getBillTable();
			protocol.getID();
			protocol.getStatus();
			System.out.println("Starting point..");
			while(true){
				String description = new String("");
				while((description = protocol.poll()).equals("NOT TIME"));
				//System.out.println(description);
				if(protocol.MONEY_IN_ESCROW == 1){
					protocol.stackBill();
					while(true){
						description = protocol.poll();
						if(! (description.equals("NOT TIME") || description.equals("Stacking"))) break;
					}
					//System.out.println(description);
					if(protocol.BILL_STACKED != 1 || protocol.BILL_RETURNED == 1){
						System.out.println("Serious Error occured..");
						break;
					}
					else{
						currentMoneyAccepted += protocol.lastAccepted;
					}
				}
				protocol.getBill();
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}
		
}
