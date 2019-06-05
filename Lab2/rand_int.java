import java.io.*;
import java.util.*;
import java.math.*;
import java.util.Scanner;

class rand_int{

	File filename;
	Scanner input;

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

	int randomOS(int U){

		int x = (1 + (input.nextInt() % U));

		return x;
	}
}