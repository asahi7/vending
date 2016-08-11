import gnu.io.CommPortIdentifier;  
   
import java.util.Enumeration;  
   
public class ListAvailablePorts {  
   
    public void list() {  
        Enumeration ports = CommPortIdentifier.getPortIdentifiers();  
          
        while(ports.hasMoreElements())  
            System.out.println(((CommPortIdentifier)ports.nextElement()).getName());  
    }  
   
    public static void main(String[] args) {  
        new ListAvailablePorts().list();  
    }  
}  