package pbting.com.net.download;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class CreatorData {

	public static void main(String[] args) {
		String fileName = "D:/demo.txt" ;
		int count = 100000;
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));){
			while(count-- > 0){
				writer.write("I am from chainaads asfdfgdgdf gfgf gfgf ffgg->"+count);
				writer.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
