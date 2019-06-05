import java.io.*;
import java.util.*;
import java.math.*;
import java.util.Scanner;

//This is a class that reads in the random-numbers.txt file using Scanner
//and allows for easy reading of the numbers

class rand_int{

	//these are the filename and scanner object fields that allow for reading
	File filename;
	Scanner input;

	//this constructor attempts to read in the random numbers file, if so it initiliazes the scanner object
	rand_int(){

		filename = new File("random-numbers.txt");

		try{

			input = new Scanner(filename);
		}
		catch (FileNotFoundException ex){

			System.out.println();

			System.err.printf("Error: the file cannot be opened.\n");
			System.out.println();

			System.exit(1);
		}
	}

	//this method just returns the next integer in the file
	int nextInt(){

		int x = input.nextInt();

		return x;
	}
}