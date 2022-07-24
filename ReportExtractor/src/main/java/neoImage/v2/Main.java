package neoImage.v2;

import java.awt.EventQueue;

public class Main {
	
	//Main class variables 
	private Data data;
	
	//The Main constructor
	public Main() {
		System.out.println("Entering Main() function");
		this.data = new Data();

	}
	
	//The entry point
	public static void main(String[] args) {
		
		//Object of Main class
		Main mProcess = new Main();
		
		//Parse the arguments
		if(args.length == 0) {
			args = new String[1];
			args[0] = "NULL";
		}
		switch (args[0]) {
		case "use_gui":
			{
				System.out.println("CommandLine arguement provided = " + args[0]);
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							MainWindow window = new MainWindow(mProcess.data);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
			break;
		case "use_database":
			{
				System.out.println("CommandLine arguement provided = " + args[0]);
				System.out.println("\tDatabase mode not written yet");
			}
			break;
		default:
			System.out.println("CommandLine arguement provided = " + args[0]);
			System.out.println("\t...incorrect arguement, something went wrong");
			System.out.println("\t...in Eclipse remember to set arguements in Run Configurations");
            break;
		}

	}
}
