/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hornerclienta2s15;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;


/**
 *  Robert Horner
 * 
 */
public class HornerClientA2S15 {

    static byte leapIndicator = 0;
    static byte version = 3;
    static byte mode = 3;
    static short stratum = 0;
    static byte pollInterval = 0;
    static byte precision = 0;
    static double rootDelay = 0;
    static double rootDispersion = 0;
    static byte[] referenceIdentifier = {0, 0, 0, 0};
    static double referenceTimestamp = 0;
    static double originateTimestamp = 0;
    static double receiveTimestamp = 0;
    static double transmitTimestamp = 0;
    
    public static final int SERVER_PORT = 33312;
    public static final int MAX_UDP_MESSAGE_SIZE = 65507;
    
    
    public static void main(String[] args) throws SocketException,UnknownHostException, IOException{
        
        /*
        
        //Code modified from Java SNTP client located at: http://support.ntp.org/bin/view/Support/JavaSntpClient
        
        String serverName= "0.north-america.pool.ntp.org";
        
	DatagramSocket socket = new DatagramSocket();
        
        InetAddress address = InetAddress.getByName(serverName);
	
        byte[] buf = toByteArray();
         
        mode = 3;
        transmitTimestamp = (System.currentTimeMillis()/1000.0) + 2208988800.0;
        
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 123);
        
        encodeTimestamp(packet.getData(), 40,(System.currentTimeMillis()/1000.0) + 2208988800.0);
	
        //validate ntp message
        //System.out.println(printNTPPacket());
	System.out.println("NTP Server Name: "+ serverName);
        System.out.println("NTP Server IP Address:PortNumber " + address.getHostAddress()+":123");
        System.out.println("Client IP Address:PortNumber " + InetAddress.getLocalHost()+ ":" + socket.getLocalPort());
        
        
        socket.send(packet);
        
        //System.out.println("NTP request sent, waiting for response...\n");
        
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        
        //System.out.println("Packet received");
        
        double destinationTimestamp = (System.currentTimeMillis()/1000.0) + 2208988800.0;
        
        decodeNtpMessage(packet.getData());
        
        double roundTripDelay = (destinationTimestamp-originateTimestamp) -
                    (transmitTimestamp-receiveTimestamp);

        double localClockOffset =
                    ((receiveTimestamp - originateTimestamp) +
                    (transmitTimestamp - destinationTimestamp)) / 2;
        
        System.out.println("Client NTP request left client at : " + timestampToString(originateTimestamp));
        System.out.println("NTP server received request at " + timestampToString(receiveTimestamp));
        System.out.println("Total time from client sending request to receiving response " +
                    new DecimalFormat("0.00").format(roundTripDelay*1000) + " ms");
        
        socket.close();
        */
        //begin file xfer, get filename from user
        System.out.println("Enter filename in the form: c:\\path\\filename.mp3");
        
        Scanner input = new Scanner(System.in);
        
        //For actual usage
        //String mp3Path = input.nextLine();
        
        //For test purposes
        String mp3Path = "/home/rob/Desktop/Link to Music/Alexandra\\ Stan\\ -\\ Mr.\\ Saxobeat.mp3";
        
        byte[] sData = new byte[1024];
        
        sData = mp3Path.getBytes();
        
        DatagramPacket sPacket = new DatagramPacket(sData, sData.length, InetAddress.getLocalHost(), SERVER_PORT);
        
        DatagramSocket cSocket =  new DatagramSocket();
        
        cSocket.send(sPacket);
        
        //get response from server
        byte[] rData = new byte[1024];        
        DatagramPacket rPacket = new DatagramPacket(rData, 1024);
        cSocket.receive(rPacket);
        //response is in string form, either negative value (failure) or positive value(success)
        String response = new String(rPacket.getData());
        String trimmedResponse = response.trim();
        //Convert from String to int
        int fileSize = Integer.parseInt(trimmedResponse);
        
        //remove path from filename
        int index_of_last_file_separator = mp3Path.lastIndexOf("\\") + 1;
        System.out.println(mp3Path + "length = " + mp3Path.length()+ "\nlast index of \\ is " + index_of_last_file_separator);
        String fileName = mp3Path.substring(index_of_last_file_separator);
        System.out.println("Filename is: "+ fileName);
        
        //print size of file and time received
        double currentTimestamp = (System.currentTimeMillis()/1000.0) + 2208988800.0;
        System.out.println("(" + timestampToString(currentTimestamp) + ")" + "Size of " + fileName + " = " + fileSize + "bytes.");
        
            
        if(fileSize < 0){
            System.err.println("Invalid path, file not found on Server");
        }
        else{
            
            //verify receipt of filesize
            transmitSuccess(cSocket);
            
            
            //write to  byte[]
            //byte[] fileBuffer = new byte[fileSize];
            //nahhhhh
            //or just create temporary file to write to
            File temp = File.createTempFile("COSC_350_temp_file", ".tmp");
            temp.deleteOnExit();
            FileOutputStream fOut = new FileOutputStream(temp);
            
            int index = 0;
            
            while(index < fileSize){
                if(index + MAX_UDP_MESSAGE_SIZE < fileSize){ //test for terminal 
                    rData = new byte[MAX_UDP_MESSAGE_SIZE];
                    rPacket = new DatagramPacket(rData, rData.length);
                    cSocket.receive(rPacket);
                    //write((b, off, len)
                    fOut.write(rData);
                    //compute number of bytes received
                    index += rData.length + 1;
                    //output number of bytes received
                    System.out.println(index + "\\" + fileSize + " bytes received");
                    //
                    //send success message to server
                    transmitSuccess(cSocket);   
                }
                else{
                    int size_of_last_read = fileSize - index;
                    rData = new byte[size_of_last_read];
                    rPacket = new DatagramPacket(rData, rData.length);
                    cSocket.receive(rPacket);
                    //write((b, off, len)
                    fOut.write(rData);
                    System.out.println("Success!");
                    break;
                }
                //prompt for filename
                
                
                
                
            }
            
            System.out.println("Enter filename to save in the form:\n " +
                        "c:\\path\\filename.mp3");
            //diagnostic
            mp3Path = "~/Desktop/Mr_Saxobeat.mp3";
            //actual query
            //mp3Path = input.nextLine();

            //create new file
            File newFile = new File(mp3Path);
            FileChannel tempFileChannel = new FileInputStream(temp).getChannel();
            FileChannel newFileChannel = new FileOutputStream(newFile).getChannel();

            newFileChannel.transferFrom(tempFileChannel, 0, temp.length());
            //close socket
            cSocket.close();
            //annnnd done
            System.out.println("File Received");
            System.out.println(temp.getName() + ""  + temp.getAbsolutePath());
        }
        
    }
    public static void transmitSuccess(DatagramSocket cSocket) throws UnknownHostException, IOException{
        
        //byte representation of a '1' for success
        String success = "success";
        byte[] message = new byte[1024];
        message = success.getBytes();
        
        DatagramPacket sPacket = new DatagramPacket(message, message.length,InetAddress.getLocalHost(), SERVER_PORT);
        
        cSocket.send(sPacket);
        
    }  
        /*
        System.out.println("NTP server: " + serverName);
            System.out.println(printNTPPacket());

            System.out.println("Dest. timestamp:     " +
                    timestampToString(destinationTimestamp));

            System.out.println("Round-trip delay: " +
                    new DecimalFormat("0.00").format(roundTripDelay*1000) + " ms");

            System.out.println("Local clock offset: " +
                    new DecimalFormat("0.00").format(localClockOffset*1000) + " ms");
        */

            
    
    
    public static byte[] toByteArray()
	{
       
        // All bytes are automatically set to 0
        byte[] p = new byte[48];

        p[0] = (byte) (leapIndicator << 6 | version << 3 | mode);
        p[1] = (byte) stratum;
        p[2] = (byte) pollInterval;
        p[3] = (byte) precision;

        // root delay is a signed 16.16-bit FP, in Java an int is 32-bits
        int l = (int) (rootDelay * 65536.0);
        p[4] = (byte) ((l >> 24) & 0xFF);
        p[5] = (byte) ((l >> 16) & 0xFF);
        p[6] = (byte) ((l >> 8) & 0xFF);
        p[7] = (byte) (l & 0xFF);

        // root dispersion is an unsigned 16.16-bit FP, in Java there are no
        // unsigned primitive types, so we use a long which is 64-bits 
        long ul = (long) (rootDispersion * 65536.0);
        p[8] = (byte) ((ul >> 24) & 0xFF);
        p[9] = (byte) ((ul >> 16) & 0xFF);
        p[10] = (byte) ((ul >> 8) & 0xFF);
        p[11] = (byte) (ul & 0xFF);

        p[12] = referenceIdentifier[0];
        p[13] = referenceIdentifier[1];
        p[14] = referenceIdentifier[2];
        p[15] = referenceIdentifier[3];

        encodeTimestamp(p, 16, referenceTimestamp);
        encodeTimestamp(p, 24, originateTimestamp);
        encodeTimestamp(p, 32, receiveTimestamp);
        encodeTimestamp(p, 40, transmitTimestamp);

        return p; 
    }
    public static String printNTPPacket()
	{
		String precisionStr =
			new DecimalFormat("0.#E0").format(Math.pow(2, precision));
			
		return "Leap indicator: " + leapIndicator + "\n" +
			"Version: " + version + "\n" +
			"Mode: " + mode + "\n" +
			"Stratum: " + stratum + "\n" +
			"Poll: " + pollInterval + "\n" +
			"Precision: " + precision + " (" + precisionStr + " seconds)\n" + 
			"Root delay: " + new DecimalFormat("0.00").format(rootDelay*1000) + " ms\n" +
			"Root dispersion: " + new DecimalFormat("0.00").format(rootDispersion*1000) + " ms\n" + 
			"Reference identifier: " + referenceIdentifierToString(referenceIdentifier, stratum, version) + "\n" +
			"Reference timestamp: " + timestampToString(referenceTimestamp) + "\n" +
			"Originate timestamp: " + timestampToString(originateTimestamp) + "\n" +
			"Receive timestamp:   " + timestampToString(receiveTimestamp) + "\n" +
			"Transmit timestamp:  " + timestampToString(transmitTimestamp);
	}
    public static short unsignedByteToShort(byte b)
	{
		if((b & 0x80)==0x80) return (short) (128 + (b & 0x7f));
		else return (short) b;
	}
    public static double decodeTimestamp(byte[] array, int pointer)
	{
		double r = 0.0;
		
		for(int i=0; i<8; i++)
		{
			r += unsignedByteToShort(array[pointer+i]) * Math.pow(2, (3-i)*8);
		}
		
		return r;
	}
    public static void encodeTimestamp(byte[] array, int pointer, double timestamp)
	{
		// Converts a double into a 64-bit fixed point
		for(int i=0; i<8; i++)
		{
			// 2^24, 2^16, 2^8, .. 2^-32
			double base = Math.pow(2, (3-i)*8);
			
			// Capture byte value
			array[pointer+i] = (byte) (timestamp / base);

			// Subtract captured value from remaining total
			timestamp = timestamp - (double) (unsignedByteToShort(array[pointer+i]) * base);
		}
		
		// From RFC 2030: It is advisable to fill the non-significant
		// low order bits of the timestamp with a random, unbiased
		// bitstring, both to avoid systematic roundoff errors and as
		// a means of loop detection and replay detection.
		array[7] = (byte) (Math.random()*255.0);
	}
    public static String timestampToString(double timestamp)
	{
		if(timestamp==0) return "0";
		
		// timestamp is relative to 1900, utc is used by Java and is relative
		// to 1970 
		double utc = timestamp - (2208988800.0);
		
		// milliseconds
		long ms = (long) (utc * 1000.0);
		
		// date/time
		String date = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date(ms));
		
		// fraction
		double fraction = timestamp - ((long) timestamp);
		String fractionSting = new DecimalFormat(".000000").format(fraction);
		
		return date + fractionSting;
	}
    public static String referenceIdentifierToString(byte[] ref, short stratum, byte version)
	{
		// From the RFC 2030:
		// In the case of NTP Version 3 or Version 4 stratum-0 (unspecified)
		// or stratum-1 (primary) servers, this is a four-character ASCII
		// string, left justified and zero padded to 32 bits.
		if(stratum==0 || stratum==1)
		{
			return new String(ref);
		}
		
		// In NTP Version 3 secondary servers, this is the 32-bit IPv4
		// address of the reference source.
		else if(version==3)
		{
			return unsignedByteToShort(ref[0]) + "." +
				unsignedByteToShort(ref[1]) + "." +
				unsignedByteToShort(ref[2]) + "." +
				unsignedByteToShort(ref[3]);
		}
		
		// In NTP Version 4 secondary servers, this is the low order 32 bits
		// of the latest transmit timestamp of the reference source.
		else if(version==4)
		{
			return "" + ((unsignedByteToShort(ref[0]) / 256.0) + 
				(unsignedByteToShort(ref[1]) / 65536.0) +
				(unsignedByteToShort(ref[2]) / 16777216.0) +
				(unsignedByteToShort(ref[3]) / 4294967296.0));
		}
		
		return "";
	}
    public static void decodeNtpMessage(byte[] array)
	{
		// See the packet format diagram in RFC 2030 for details 
		leapIndicator = (byte) ((array[0] >> 6) & 0x3);
		version = (byte) ((array[0] >> 3) & 0x7);
		mode = (byte) (array[0] & 0x7);
		stratum = unsignedByteToShort(array[1]);
		pollInterval = array[2];
		precision = array[3];
		
		rootDelay = (array[4] * 256.0) + 
			unsignedByteToShort(array[5]) +
			(unsignedByteToShort(array[6]) / 256.0) +
			(unsignedByteToShort(array[7]) / 65536.0);
		
		rootDispersion = (unsignedByteToShort(array[8]) * 256.0) + 
			unsignedByteToShort(array[9]) +
			(unsignedByteToShort(array[10]) / 256.0) +
			(unsignedByteToShort(array[11]) / 65536.0);
		
		referenceIdentifier[0] = array[12];
		referenceIdentifier[1] = array[13];
		referenceIdentifier[2] = array[14];
		referenceIdentifier[3] = array[15];
		
		referenceTimestamp = decodeTimestamp(array, 16);
		originateTimestamp = decodeTimestamp(array, 24);
		receiveTimestamp = decodeTimestamp(array, 32);
		transmitTimestamp = decodeTimestamp(array, 40);
                
                
	}
}
