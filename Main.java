import java.io.*;
import java.util.*;
import visitor.*;
import syntaxtree.*;

class Main {
    public static void main (String [] args){
    	if(args.length < 1){
		    System.err.println("Usage: java [MainClassName] [file1] [file2] ... [fileN]");
		    System.exit(1);
		}
		int count = 0;
		FileInputStream fis = null;
		while(count < args.length){
			try{
			    fis = new FileInputStream(args[count]);
			    MiniJavaParser parser = new MiniJavaParser(fis);
			    System.err.println("Program parsed successfully.");
			    fillSTVisitor stVisitor = new fillSTVisitor();
			    Goal root = parser.Goal();
			    root.accept(stVisitor);

			    TCVisitor checkVisitor = new TCVisitor(stVisitor.symtable);
			    root.accept(checkVisitor, null);
			    System.err.println("Program typechecked successfully.");

			    String ll = stVisitor.symtable.printLLCode();
			    
				LLVMVisitor llVisitor = new LLVMVisitor(stVisitor.symtable, ll);
				root.accept(llVisitor, null);

				// strips path , keeps only filename without extension
				int start_pos = args[count].lastIndexOf("/");
				int last_pos = args[count].lastIndexOf(".");
				// Otherwise return the string, up to the dot.

				String filename = args[count].substring(start_pos+1, last_pos);
				String directory = "output";
				File file = new File(directory);
				if (!file.exists()){
					if (file.mkdir())
						System.err.println("Directory with name " + directory + " created!");
					else
						System.err.println("Error in creating directory with name " + directory);
					
				}
				// save produced code in directory named "output" and adds to filename the "ll" extension
				PrintWriter writer = new PrintWriter(file + "/" + filename + ".ll");
		        writer.println(llVisitor.getLLCode());
		        
		        writer.close();

				System.out.println(llVisitor.getLLCode());
			}
			catch(ParseException ex){
			    System.out.println(ex.getMessage());
			}
			catch(FileNotFoundException ex){
			    System.err.println(ex.getMessage());
			}
			catch (Exception e) {
				System.err.println("\n****************************************");
				System.err.println("ERROR IN TYPE CHECKING -> INPUT FILE " + (count + 1) + " : " + args[count]);
				System.err.println(e.getMessage());
				System.err.println("****************************************\n");
			}
			finally{
			    try{
					if(fis != null) fis.close();
			    }
			    catch(IOException ex){
				System.err.println(ex.getMessage());
			    }
			}
			count++;
		}
    }
}
