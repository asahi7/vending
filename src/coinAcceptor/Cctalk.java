package coinAcceptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Cctalk {
	public InputStream in;
	public OutputStream out;
	
	private String lastEvent = "";
	private final int[] denomination = new int[]{0, 5, 10, 20, 50, 100};

	private int[] buffer = new int[1024];
	private int tail = 0;
	
	public Cctalk(InputStream in, OutputStream out){
		this.in = in;
		this.out = out;
	}

	public static void delay(long millis){ // delete if Thread variant will be used
		long lastTime = System.currentTimeMillis();
		while(System.currentTimeMillis() - lastTime < millis);
	}	
	
	public boolean readMessage(){
		try{
			int b;
			int messageSize = -1;
			long lastTime = System.currentTimeMillis();
			tail = 0;
			while(true){
				while((b = in.read()) != -1){
					buffer[tail++] = b;
					if(tail == 2){
						messageSize = buffer[tail - 1] + 5;
					}
					lastTime = System.currentTimeMillis();
					if(tail == messageSize){
						break;
					}
				}
				if(tail == messageSize){
					break;
				}
				if(System.currentTimeMillis() - lastTime > 50){
					tail = 0;
					messageSize = -1;
					break;
				}
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
		int CRC = calculateCRC(message, messageSize);
		if(message[messageSize - 1] == CRC) return true;
		else return false;
	}
	
	public boolean waitACK(){
		if(readMessage() == false) return false;
		else{
			if(checkCRC(buffer, tail) == false) return false;
			if(! (buffer[0] == 1 && buffer[1] == 0 && buffer[2] == 2 && buffer[3] == 0)) return false;
			return true;
		}
	}	
	
	public boolean waitConfirmation(int[] message, int messageSize){
		if(readMessage() == false) return false;
		else{
			if(checkCRC(buffer, tail) == false || tail != messageSize) return false;
			for(int i = 0; i < messageSize - 1; i++){
				if(message[i] != buffer[i]) return false;
			}
			return true;
		}
	}
	
	public void simplePoll(){
		do{
			do{
				int[] message = new int[]{2, 0, 1, 254}; // Destination, Data Size, Source, Header
				sendMessage(prepareMessage(message, 5), 5);
			} while(! waitConfirmation(new int[]{2, 0, 1, 254}, 5));
		} while(! waitACK());
		System.out.println("ACK of simplePoll() is received");
	}

	public void acceptCoins(){
		do{
			do{
				int[] message = new int[]{2, 2, 1, 231, 255, 255};
				sendMessage(prepareMessage(message, 7), 7);
			} while(! waitConfirmation(new int[]{2, 2, 1, 231, 255, 255}, 7));
		} while(! waitACK());
		do{
			do{
				int[] message = new int[]{2, 1, 1, 228, 1};
				sendMessage(prepareMessage(message, 6), 6);
			} while(! waitConfirmation(new int[]{2, 1, 1, 228, 1}, 6));
		} while(! waitACK());		
		System.out.println("ACK of acceptCoins() is received");		
	}
	
	public int getLastAccepted(){
		do{
			do{
				int[] message = new int[]{2, 0, 1, 229};
				sendMessage(prepareMessage(message, 5), 5);
			} while(! waitConfirmation(new int[]{2, 0, 1, 229}, 5));
		} while(! readMessage() || ! checkCRC(buffer, tail) || ! (buffer[0] == 1 && buffer[1] == 11 && buffer[2] == 2 && buffer[3] == 0)); // Waiting for command response
		String newEvent = new String();
		for(int i = 4; i <= 14; i++){
			newEvent = newEvent + buffer[i];
		}
		if(! newEvent.equals(lastEvent)){
			lastEvent = newEvent;
			return denomination[buffer[5]];
		}
		delay(50);
		return -1;
	}
	
	public int calculateCRC(int[] message, int messageSize){
		int CRC = 0;
		int SUM = 0;
		for(int i = 0; i < messageSize - 1; i++){
			SUM += message[i];
		}
		SUM = SUM % 256;
		CRC = -SUM + 256;
		return CRC;
	}
	
	public byte[] prepareMessage(int[] rawMessage, int messageSize){
		int CRC = calculateCRC(rawMessage, messageSize);
		byte[] b = new byte[messageSize];
		for(int i = 0; i < messageSize - 1; i++){
			b[i] = (byte) rawMessage[i];
		}
		b[messageSize - 1] = (byte) CRC;
		return b;
	}
		
	public void sendMessage(byte[] message, int messageSize){
		try{
			out.write(message);
			out.flush();
		} catch(IOException e){
			e.printStackTrace();
		}

	}
	
}
