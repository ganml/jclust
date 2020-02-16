package clustering.main;

import org.apache.commons.cli.*;
import clustering.util.AlgorithmRunner;

public class AlgorithmRunnerMain {

	public static void main(String[] args) {
		try{
			Options options = new Options();
			Option help = new Option( "help", "print this message" );
			options.addOption(help);
			options.addOption("paramfile", true, "file name of the input data");
			options.addOption("numthread", true, "number of threads");
			options.addOption("prefix", true, "file name prefix of the output data");
			options.addOption("vindex", true, "validation index (e.g., RunTime:CorrectedRandIndex)");
			options.addOption("outputlevel", true, "output level (0,1,2,3)");
			
			CommandLineParser parser = new DefaultParser();		
			CommandLine cmd = parser.parse( options, args);
			if(cmd.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "jclust", options );
				return;
			}
			
			String paramfile = "";
			if(cmd.hasOption("paramfile")) {
				paramfile = cmd.getOptionValue("paramfile");
			} else {
				System.out.println("Please input a datafile");
				return;
			}
			
			String prefix = paramfile.replaceFirst("[.][^.]+$", "");
			if(cmd.hasOption("prefix")) {
				prefix = cmd.getOptionValue("prefix");
			} 
			
			String vindex = "RunTime";
			if(cmd.hasOption("vindex")) {
				vindex = cmd.getOptionValue("vindex");
			} 
			
			
			int numthread = 1;
			if(cmd.hasOption("numthread")) {
				numthread = Integer.parseInt(cmd.getOptionValue("numthread"));
			}
			
			int outputlevel = 1;
			if(cmd.hasOption("outputlevel")) {
				outputlevel = Integer.parseInt(cmd.getOptionValue("outputlevel"));
			}
			
			AlgorithmRunner ar = new AlgorithmRunner(paramfile, prefix, numthread,
					outputlevel, vindex);
			ar.run();
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}

	}

}
