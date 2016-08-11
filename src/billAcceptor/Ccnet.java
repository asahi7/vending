package billAcceptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Ccnet {

	public InputStream in;
	public OutputStream out;
	private final int POLYNOMIAL = 0x08408;
	private final int[] denomination = new int[]{0, 200, 500, 1000, 2000, 5000};
	private long lastCommand = System.currentTimeMillis();
	public int MONEY_IN_ESCROW = 0;
	public int BILL_STACKED = 0;
	public int BILL_RETURNED = 0;
	public int lastAccepted = 0;
	private long last_poll = System.currentTimeMillis();
	private int[] buffer = new int[1024];
	private int tail = 0;	
	
	public Ccnet(InputStream in, OutputStream out){
		this.in = in;
		this.out = out;
	}
	
	String pollResponse(int z1, int z2){
		String description = new String("");
		switch(z1){
		    case 0x10: description = "Power up";
		               break;
		    case 0x11: description = "Power up with bill in validator";
		               break;
		    case 0x12: description = "Power up with bill in stacker";
		               break;
		    case 0x13: description = "Initialize";
		               break;
		    case 0x14: description = "Idling";
		               break;
		    case 0x15: description = "Accepting";
		               break;
		    case 0x17: description = "Stacking";
		               break;
		    case 0x18: description = "Returning";
		               break;
		    case 0x19: description = "Unit disabled";
		               break;
		    case 0x1A: description = "Holding";
		               break;
		    case 0x1B: description = "Device busy";
		               break;
		               
		    case 0x1C: 
		               switch(z2){
		                 case 0x60: description = "Insertion error";
		                            break;
		                 case 0x61: description = "Dielectric error";
		                            break;
		                 case 0x62: description = "Previously inserted bill remains in head";
		                            break;
		                 case 0x63: description = "Compensation error";
		                            break;
		                 case 0x64: description = "Bill transport error";
		                            break;
		                 case 0x65: description = "Identification error";
		                            break;
		                 case 0x66: description = "Verification error";
		                            break;
		                 case 0x67: description = "Optic sensor error";
		                            break;
		                 case 0x68: description = "Return by inhibit denomination error";
		                            break;
		                 case 0x69: description = "Capacitance error";
		                            break;
		                 case 0x6A: description = "Operation error";
		                            break;
		                 case 0x6C: description = "Length error";
		                            break;
		                 case 0x92: description = "Bill taken was treated as a barcode but no reliable data can be read from it";
		                            break;
		                 case 0x6D: description = "Banknote UV properties do not meet the predefined criteria";
		                            break;
		                 case 0x93: description = "Barcode data was read (at list partially) but s consistent";
		                            break;
		                 case 0x94: description = "Barcode was not read as no synchronization was established";
		                            break;
		                 case 0x95: description = "Barcode was read but trailing data is corrupt";
		                            break;
		               }
		               break;
		               
		    case 0x41: description = "Drop cassete full";
		               break;
		    case 0x42: description = "Drop cassete out of position";
		               break;
		    case 0x43: description = "Validator jammed";
		               break;
		    case 0x44: description = "Drop cassete jammed";
		               break;
		    case 0x45: description = "Cheated";
		               break;
		    case 0x46: description = "Pause";
		               break;
		    case 0x47: switch(z2){
		                 case 0x50: description = "Drop cassete motor fauilure";
		                 break; 
		                 case 0x51: description = "Transport motor speed failure";
		                 break; 
		                 case 0x52: description = "Transport motor failure";
		                 break; 
		                 case 0x53: description = "Aligning motor motor fauilure";
		                 break; 
		                 case 0x54: description = "Initial cassete status failure";
		                 break; 
		                 case 0x55: description = "One of the optic sensors has failed to provide its response";
		                 break; 
		                 case 0x56: description = "Inductive sensor failed to respond";
		                 break; 
		                 case 0x57: description = "Capacitance sensor failed to respond";
		                 break; 
		               }
		               break;
		    case 0x80:
		              switch(z2){
		                  case 0x01: description = denomination[1] + " tenge in escrow position";
		                  			 BILL_STACKED = 0;
		                  			 BILL_RETURNED = 0;
		                  			 MONEY_IN_ESCROW = 1;
		                             break;
		                  case 0x02: description = denomination[2] + " tenge in escrow position";
               			 			 BILL_STACKED = 0;
               			 			 BILL_RETURNED = 0;
               			 			 MONEY_IN_ESCROW = 1;
		                             break;
		                  case 0x03: description = denomination[3] + " tenge in escrow position";
               			 			 BILL_STACKED = 0;
               			 			 BILL_RETURNED = 0;
		                  			 MONEY_IN_ESCROW = 1;
		                             break;
		                  case 0x04: description = denomination[4] + " tenge in escrow position";
               			 			 BILL_STACKED = 0;
               			 			 BILL_RETURNED = 0;
		                  			 MONEY_IN_ESCROW = 1;
		                             break;
		                  case 0x05: description = denomination[5] + " tenge in escrow position";
               			 			 BILL_STACKED = 0;
               			 			 BILL_RETURNED = 0;
		                  			 MONEY_IN_ESCROW = 1;
		                             break;
		               }
		               break;
		    case 0x81: switch(z2){
		                  case 0x01: description = denomination[1] + " tenge in stack position";
		                  			 BILL_STACKED = 1;
		                  			 lastAccepted = denomination[1];
		                  			 MONEY_IN_ESCROW = 0;
		                             break;
		                  case 0x02: description = denomination[2] + " tenge in stack position";
		                  			 BILL_STACKED = 1;
		                  			 lastAccepted = denomination[2];
		                             MONEY_IN_ESCROW = 0;
		                             break;
		                  case 0x03: description = denomination[3] + " tenge in stack position";
		                  			 BILL_STACKED = 1;
		                  			 lastAccepted = denomination[3];
		                             MONEY_IN_ESCROW = 0;
		                             break;
		                  case 0x04: description = denomination[4] + " tenge in stack position";
		                  			 BILL_STACKED = 1;
		                  			 lastAccepted = denomination[4];
		                             MONEY_IN_ESCROW = 0;
		                             break;
		                  case 0x05: description = denomination[5] + " tenge in stack position";
		                  			 BILL_STACKED = 1;
		                  			 lastAccepted = denomination[5];
		                             MONEY_IN_ESCROW = 0;
		                             break;
		               }
		               break;
		    case 0x82: switch(z2){
		                  case 0x01: description = denomination[1] + " tenge returned";
		                  			 BILL_RETURNED = 1;
		                             MONEY_IN_ESCROW = 0;
		                             break;
		                  case 0x02: description = denomination[2] + " tenge returned";
		                  			 BILL_RETURNED = 1;
		                             MONEY_IN_ESCROW = 0;
		                             break;
		                  case 0x03: description = denomination[3] + " tenge returned";
		                  			 BILL_RETURNED = 1;
		                             MONEY_IN_ESCROW = 0;
		                             break;
		                  case 0x04: description = denomination[4] + " tenge returned";
		                  			 BILL_RETURNED = 1;
		                             MONEY_IN_ESCROW = 0;
		                             break;
		                  case 0x05: description = denomination[5] + " tenge returned";
		                  			 BILL_RETURNED = 1;
		                             MONEY_IN_ESCROW = 0;
		                             break;
		               }
		               break;
		}
		return description;
	}        	
	
	public static void delay(long millis){ // delete if Thread variant will be used
		long lastTime = System.currentTimeMillis();
		while(System.currentTimeMillis() - lastTime < millis);
	}	
	
	public boolean readMessage(int tResponse){ // Check :3
		try{
			int b;
			int messageSize = -1;
			long lastTime = -1;
			long firstLaunch = System.currentTimeMillis();
			tail = 0;
			while(true){
				while((b = in.read()) != -1){
					if(lastTime == -1) lastTime = System.currentTimeMillis();
					buffer[tail++] = b;
					if(tail == 3){
						messageSize = buffer[tail - 1];
					}
					if(tail == messageSize){
						break;
					}
					if(System.currentTimeMillis() - firstLaunch > tResponse) return false;
				}
				if(tail == messageSize){
					break;
				}
				if(lastTime != -1 && System.currentTimeMillis() - lastTime > 5){ // Maximum time allowed between bytes
					tail = 0;
					messageSize = -1;
					break;
				}
				if(System.currentTimeMillis() - firstLaunch > tResponse) return false; // Check this -_-'
			}
			if(tail == 0 || messageSize == -1){
				return false;
			}
			if(messageSize == tail){
				return true;
			}
		} catch(IOException e){
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean checkCRC(int[] message, int messageSize){
		int CRC = (message[messageSize - 1] << 8 ) | (message[messageSize - 2] & 0xff);
		if(CRC == getCRC(message, messageSize)) return true;
		return false;
	}
	
	public boolean waitACK(){
		if(checkCRC(buffer, tail) == false) return false;
		if(! (buffer[0] == 0x02 && buffer[1] == 0x03 && buffer[2] == 0x06 && buffer[3] == 0x00)) return false;
		return true;
	}	
	
	public boolean waitNAK(){
		if(checkCRC(buffer, tail) == false) return false;
		if(! (buffer[0] == 0x02 && buffer[1] == 0x03 && buffer[2] == 0x06 && buffer[3] == 0xff)) return false;
		return true;
	}	

	public boolean waitIC(){
		if(checkCRC(buffer, tail) == false) return false;
		if(! (buffer[0] == 0x02 && buffer[1] == 0x03 && buffer[2] == 0x06 && buffer[3] == 0x30)) return false;
		return true;
	}	
	
	public boolean checkResponse(){
		if(checkCRC(buffer, tail) == false) return false;
		if(! (buffer[0] == 0x02 && buffer[1] == 0x03)) return false;
		return true;
	}

	public void getBillTable(){
		do{
			while(System.currentTimeMillis() - lastCommand < 20);
			int[] data = new int[]{0x00};
			sendMessage(prepareMessage(0x41, data, 1));
			if(readMessage(100) == false) {
				continue;
			}
			if(checkResponse() == false) continue;
			if(waitNAK() == true){
				lastCommand = System.currentTimeMillis();
				continue;
			}			
			for(int i = 0; i < 24; i++){
				String country = new String("");
				int firstByte = 0, zeroes = 0;
				for(int j = 0; j < 5; j++){
					int ch = buffer[3 + i * 5 + j];
				    if(j == 0) firstByte = ch;
				    if(j == 1) country += (char)ch;
				    if(j == 2) country += (char)ch;
				    if(j == 3) country += (char)ch;
				    if(j == 4) zeroes = ch;
				}
				int res = 0;
				if((zeroes & (1 << 7)) == 0){
					int pow = 1;
				    for(int x = 0; x < 7; x++){
				    	if((zeroes & (1 << x)) != 0){
				    		res += pow;
				        }
				        pow *= 2;
				    }
				}
				if(firstByte != 0){
					System.out.print(firstByte);
					for(int x = 0; x < res; x++) System.out.print("0");
					System.out.println(" " + country);
				}
			}
			sendMessage(prepareMessage(-1, data, 1));
			lastCommand = System.currentTimeMillis();
			break;
		} while(true);
	}	
	
	public String poll(){
		if(System.currentTimeMillis() - last_poll < 200) return "NOT TIME"; 
		do{
			while(System.currentTimeMillis() - lastCommand < 20);
			int[] data = new int[]{0x00};
			sendMessage(prepareMessage(0x33, data, 1));
			if(readMessage(10) == false) continue;
			if(checkResponse() == false) continue;
			if(waitNAK() == true){
				lastCommand = System.currentTimeMillis();
				last_poll = System.currentTimeMillis();
				return "NOT TIME";
			}						
			sendMessage(prepareMessage(-1, data, 1));
			last_poll = System.currentTimeMillis();
			lastCommand = System.currentTimeMillis();
			String description = new String("");
			int messageSize = buffer[2];
			if(messageSize == 6){
				description = pollResponse(buffer[3], 0);
			}
			else if(messageSize == 7){
			    description = pollResponse(buffer[3], buffer[4]);
			}
			return description;
		} while(true);
	}	
	
	public void getBill(){
		while(MONEY_IN_ESCROW == 0 && poll().equals("NOT TIME"));
		if(MONEY_IN_ESCROW == 1) return;
		do{
			while(System.currentTimeMillis() - lastCommand < 20);
			int[] data = new int[]{0xff, 0xff, 0xff, 0xff, 0xff, 0xff};
			sendMessage(prepareMessage(0x34, data, 6));
			if(readMessage(10) == false) continue;
			if(checkResponse() == false) continue;
			if(waitACK() == true){
				lastCommand = System.currentTimeMillis();
				break;
			}
			if(waitNAK() == true){
				lastCommand = System.currentTimeMillis();
				continue;
			}			
			lastCommand = System.currentTimeMillis();
			
		} while(true);
	}		

	public void stackBill(){
		if(MONEY_IN_ESCROW == 0) return;
		do{
			while(System.currentTimeMillis() - lastCommand < 20);
			int[] data = new int[]{0x00};
			sendMessage(prepareMessage(0x35, data, 0));
			if(readMessage(10) == false) continue;
			if(checkResponse() == false) continue;
			if(waitACK() == true){
				lastCommand = System.currentTimeMillis();
				break;
			}
			if(waitNAK() == true){
				lastCommand = System.currentTimeMillis();
				continue;
			}			
			lastCommand = System.currentTimeMillis();
		} while(true);
	}			
	
	public void getStatus(){
		do{
			while(System.currentTimeMillis() - lastCommand < 20); // problem in lastCommand
			int[] data = new int[]{0x00};
			sendMessage(prepareMessage(0x31, data, 1));
			if(readMessage(10) == false) continue;
			if(checkResponse() == false) continue;
			if(waitNAK() == true){
				lastCommand = System.currentTimeMillis();
				continue;
			}			
			int z1, z2, z3;
			z1 = buffer[3];
			z2 = buffer[4];
			z3 = buffer[5];
			System.out.println(z1);
			System.out.println(z2);
			System.out.println(z3);			
			sendMessage(prepareMessage(-1, data, 1));
			lastCommand = System.currentTimeMillis();
			break;
		} while(true);
	}	
	
	public void getID(){
		do{
			while(System.currentTimeMillis() - lastCommand < 20);
			int[] data = new int[]{0x00};
			sendMessage(prepareMessage(0x37, data, 1));
			if(readMessage(200) == false) continue;
			if(checkResponse() == false) continue;
			if(waitNAK() == true){
				lastCommand = System.currentTimeMillis();
				continue;
			}			
			String partNum = new String("");
			String serialNum = new String("");
			for(int i = 0; i < 15; i++){
				partNum = partNum + (char)buffer[i + 3]; 
			}
			for(int i = 0; i < 12; i++){
				serialNum = serialNum + (char)buffer[i + 3 + 15];
			}
			System.out.println(partNum);
			System.out.println(serialNum);			
			sendMessage(prepareMessage(-1, data, 1)); // data length = 1, data = 0x00
			lastCommand = System.currentTimeMillis();
			break;
			
		} while(true);
	}

	public void reset(){
		do{
			while(System.currentTimeMillis() - lastCommand < 20);
			int[] data = new int[]{0x00};
			sendMessage(prepareMessage(0x30, data, 0));
			if(readMessage(100) == false) continue;
			if(waitACK() == true){
				lastCommand = System.currentTimeMillis();
				break;
			}
			if(waitNAK() == true){
				lastCommand = System.currentTimeMillis();
				continue;
			}			
		} while(true);
		System.out.println("ACK!");
	}
	
	public int getCRC(int[] message, int messageSize){
		  int CRC = 0;
		  char j;
		  for(int i = 0; i < messageSize - 2; i++){
		    CRC ^= message[i];
		    for(j = 0; j < 8; j++){
		      if((CRC & 0x0001) != 0) {CRC >>= 1; CRC ^= POLYNOMIAL;}
		      else CRC >>= 1;
		    }
		  }
		  return CRC;
	}
	
	public byte[] prepareMessage(int command, int[] data, int dataSize){
		int messageSize = 6 + dataSize;
		if(command == -1){
			// then it's ACK response from controller
			messageSize--;
		}
		int[] rawMessage = new int[messageSize];
		rawMessage[0] = 0x02;
		rawMessage[1] = 0x03;
		rawMessage[2] = messageSize;
		if(command != -1) rawMessage[3] = command;
		for(int i = 0; i < dataSize; i++){
			if(command != -1) rawMessage[i + 4] = data[i];
			else rawMessage[i + 3] = data[i];
 		}
		int CRC = getCRC(rawMessage, messageSize);
		byte[] b = new byte[messageSize];
		for(int i = 0; i < messageSize - 2; i++){
			b[i] = (byte) rawMessage[i];
		}
		int CRC1 = (CRC & 0xFF);
		int CRC2 = ((CRC & 0xFF00) >> 8);
		b[messageSize - 2] = (byte) CRC1;
		b[messageSize - 1] = (byte) CRC2;		
		return b;
	}
		
	public void sendMessage(byte[] message){
		try{
			out.write(message);
			out.flush();
		} catch(IOException e){
			e.printStackTrace();
		}

	}	
	
}
