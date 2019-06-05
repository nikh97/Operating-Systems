import java.io.*;
import java.util.*;
import java.math.*;
import java.util.Scanner;

public class TwoPassLinker{

	public static void main(String[] args) throws FileNotFoundException{

		if (args.length != 1){
				System.out.println();

				System.err.printf("Error: incorrect usage.\n");
				System.err.printf("Usage Error: the program expects file name as an argument.\n");
				System.out.println();

				System.exit(1);
			}

		File filename = new File(args[0]);

		if (!filename.exists() || !filename.canRead()){

			System.out.println();

			System.err.printf("Error: the file \"" + args[0] + "\" cannot be opened.\n");
			System.out.println();

			System.exit(1);
		}

	    Scanner input = new Scanner(filename);

        int num_modules = input.nextInt();

        ArrayList<Integer> base = new ArrayList<Integer>();

        base.add(0);

        ArrayList<String> symbols = new ArrayList<String>();
        ArrayList<Integer> values = new ArrayList<Integer>();
        ArrayList<String> addresses = new ArrayList<String>();
        ArrayList<String> symbol_map = new ArrayList<String>();
        ArrayList<String> syms_used = new ArrayList<String>();
        ArrayList<Integer> def_mod = new ArrayList<Integer>();


        for(int i = 0; i < num_modules; i++){

        	int num_def = input.nextInt();

        	for (int j = 1; j <= (num_def*2); j += 2){

        		String symbol = input.next();

        		int value = input.nextInt() + base.get(i);

        		if(symbols.contains(symbol)){

        			int index = symbols.indexOf(symbol);
        			values.set(index, value);
        			def_mod.set(index, i);

        			symbol_map.set(index, symbol + "=" + Integer.toString(value) + " Error: This variable is multiply defined; last value used.");

        		}else{

	        		symbols.add(symbol);
	        		values.add(value);
	        		def_mod.add(i);

	        		symbol_map.add(symbol + "=" + Integer.toString(value));
	        	}
	        }

        	int num_iterations = input.nextInt();
        	String current = input.next();
        	
        	if(num_iterations > 0){
	        	for(int t = 0; t < num_iterations; t++){

					while(!current.equals("-1")){

						current = input.next();
					}

					current = input.next();
				}
			}


			int num_addresses = Integer.parseInt(current);
			base.add(num_addresses + base.get(i));

			for (int t = 0; t < num_addresses; t++){

				current = input.next();
			}
        }

        for (int v = 0; v < symbols.size(); v++){

        	if(values.get(v) >= base.get(def_mod.get(v) + 1)){

        		int new_val = base.get(def_mod.get(v) + 1) - 1;

        		values.set(v, new_val);
        		symbol_map.set(v, symbols.get(v) + "=" + new_val + " Error: Definition exceeds module size; last word in module used.");
        	}
        }
        
        input.close();


        input = new Scanner(filename);

        num_modules = input.nextInt();

		for (int i = 0; i < num_modules; i++){

			int num_def = input.nextInt();

			String current;

        	for (int j = 1; j <= (num_def*2); j++){

        		current = input.next();
     			
	        }

			Integer size = base.get(i+1) - base.get(i);
			ArrayList<String> uses = new ArrayList<String>();
			ArrayList<String> err_messages = new ArrayList<String>();

			for(int s = 0; s < size; s++){

				uses.add("N/A");
			}

			for(int s = 0; s < size; s++){

				err_messages.add("N/A");
			}

			int num_iterations = input.nextInt();

			int counter;

			for(int j = 0; j < num_iterations; j++){

				String sym = input.next();

				if(!syms_used.contains(sym)){

					syms_used.add(sym);
				}

				counter = input.nextInt();

				while(counter != -1){

					if(!uses.get(counter).equals("N/A")){

						err_messages.set(counter, " Error: Multiple variables used in instruction; all but last ignored.");
					}

					uses.set(counter, sym);
					counter = input.nextInt();
				}
			}

			size = input.nextInt();

			for(int x = 1; x <= size; x++){

				String address = input.next();

				if(address.charAt(4) == '1'){

					addresses.add(address.substring(0,4));

				} else if(address.charAt(4) == '2'){

					String word = address.substring(1,4);
					int word1 = Integer.parseInt(word);


					if(word1 > 299){

						addresses.add(address.substring(0,1) + "299" + " Error: Absolute address exceeds machine size; largest legal value used.");

					}else{

						addresses.add(address.substring(0,4));
					}

				} else if(address.charAt(4) == '3'){

					int relative = Integer.parseInt(address.substring(0,4));

					int base_ad = base.get(i);

					addresses.add(Integer.toString(relative+base_ad));

				} else{

					String used_sym = uses.get(x - 1);

					int sym_index;
					int value_of_sym;

					if(!symbols.contains(used_sym)){

						value_of_sym = 111;
						String value_string = Integer.toString(value_of_sym);
						addresses.add(address.charAt(0) + value_string + " Error: " + used_sym +" is not defined; 111 used.");


					} else{

						sym_index = symbols.indexOf(used_sym);
						value_of_sym = values.get(sym_index);

						String value_string = Integer.toString(value_of_sym);

						while(value_string.length() != 3){

							value_string = "0" + value_string;
						}

						if(!err_messages.get(x-1).equals("N/A")){

							addresses.add(address.charAt(0) + value_string + err_messages.get(x-1));
						} else{

							addresses.add(address.charAt(0) + value_string);
						}

					}

				}

			}

		}
		
		System.out.println();
		System.out.println("Symbol Table");

		for(int i = 0; i < symbol_map.size(); i++){

			System.out.println(symbol_map.get(i));
		}

		System.out.println();
		System.out.println("Memory Map");

		for(int i = 0; i < addresses.size(); i++){

			System.out.println(String.format("%-3s",i + ":") + " " + addresses.get(i));

		}

		System.out.println();

		for(int i = 0; i < symbols.size(); i++){

			if(!syms_used.contains(symbols.get(i))){

				System.out.println("Warning: "+ symbols.get(i) +" was defined in module " + def_mod.get(i) +" but never used.");
			}
		}

		System.out.println();

	}
}