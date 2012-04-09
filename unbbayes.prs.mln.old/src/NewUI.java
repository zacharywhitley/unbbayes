import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.util.Hashtable;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import tuffy.parse.CommandOptions;
import tuffy.parse.ConfigLexer;
import tuffy.parse.ConfigParser;
import tuffy.util.Config;
import tuffy.util.ExceptionMan;
import tuffy.util.FileMan;
import tuffy.util.UIMan;


public class NewUI {

	protected static boolean silent = false;
	
	public static void setSilent(boolean v){
		silent = v;
	}
	
	private static PrintStream dribbleStream = null;
    public static String dribbleFileName = null;
	
    public static void println(String... strings){
		if(silent) return;
		if(Config.console_line_header != null){
			System.out.print("@" + Config.console_line_header + " ");
		}
		for(String s : strings){
			System.out.print(s);
			writeToDribbleFile(s);
		}
		System.out.println();
		writeToDribbleFile("\n");
	}
    
	public static void createDribbleFile(String fileName) {
        closeDribbleFile();
        try {
            FileOutputStream outStream = new FileOutputStream(fileName);
            dribbleStream = new PrintStream(outStream, false); // No auto-flush (can slow down code).
            dribbleFileName = fileName;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.err.println("Unable to open file for logging:\n " + fileName + ".\nError message: " + e.getMessage());
        }
    }
	
	public static void writeToDribbleFile(String str) {
        if (dribbleStream != null) { 
        	dribbleStream.print(str);
        }
    }
	
	public static void closeDribbleFile() {
        dribbleFileName = null;
        if (dribbleStream == null) { return; }
        dribbleStream.close();
        dribbleStream = null;
    }
	
	public static CommandOptions processOptions(CommandOptions opt){

		if(opt.pathConf != null){
			Config.path_conf = opt.pathConf;
		}
		boolean okconf = UIMan.parseConfigFile(Config.path_conf);
		//if(!okconf){
		//	return null;
		//}
		
		
		Config.mcsat_sample_para = opt.mcsatPara;
		Config.avoid_breaking_hard_clauses = opt.avoidBreakingHardClauses;
		Config.output_prolog_format = opt.outputProlog;
		
		Config.max_threads = opt.maxThreads;
		//Config.use_atom_blocking = opt.block;
		
		Config.evidDBSchema = opt.evidDBSchema;
		Config.dbNeedTranslate = opt.dbNeedTranslate;
		
		Config.disable_partition = opt.disablePartition;
		Config.output_files_in_gzip = opt.outputGz;
		if(Config.output_files_in_gzip && !opt.fout.toLowerCase().endsWith(".gz")){
			opt.fout += ".gz";
		}
		
		Config.mcsatDumpPeriodSeconds = opt.mcsatDumpPeriodSec;
		Config.timeout = opt.timeout;

		Config.marginal_output_min_prob = opt.minProb;
		/*
		if(opt.timeout > 0){
			Config.timeout = opt.timeout;
		}
		*/
		Config.dir_out = FileMan.getParentDir(opt.fout);
		Config.file_stats = opt.fout + ".stats";
		//Config.file_stats = Config.dir_out + "/tuffy_stats.txt";
		
		/*
		if(opt.reportingFreq > 0 && opt.marginal == false){
			Config.num_tries_per_periodic_flush = opt.reportingFreq;
		}
		
		*/
		Config.mark_all_atoms_active = opt.activateAllAtoms;
		Config.keep_db_data = opt.keepData;
		
		Config.console_line_header = opt.consoleLineHeader;
		
		if(opt.fDribble != null){
			createDribbleFile(opt.fDribble);
		}

		if(opt.fquery == null && opt.queryAtoms == null){
			System.err.println("Please specify queries with -q or -queryFiles");
			return null;
		}

		Config.verbose_level = opt.verboseLevel;
		
		return opt;
		
	}
	
	public static CommandOptions parseCommand(String[] args){
		CommandOptions opt = new CommandOptions();
		CmdLineParser parser = new CmdLineParser(opt);
		try{
			parser.parseArgument(args);
			if(opt.showHelp){
				UIMan.println("USAGE:");
	            parser.printUsage(System.out);
	            return null;
			}
		}catch(CmdLineException e){
			System.err.println(e.getMessage());
			UIMan.println("USAGE:");//alterar mais tarde.
            parser.printUsage(System.out);
            return null;
		}

		return processOptions(opt);
	}
	
	public static boolean parseConfigFile(String fconf){
		try {
			FileInputStream fis = null;
			try{
				fis = new FileInputStream(fconf);
			}catch(Exception e){
				System.out.println("Failed to open config file.");
				System.err.println(e.getMessage());
				return false;
			}
			ANTLRInputStream input = new ANTLRInputStream(fis);
			ConfigLexer lexer = new ConfigLexer(input);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			ConfigParser parser = new ConfigParser(tokens);
			try{
				parser.config();
			}catch(Exception e){
				System.out.println("Ill-formed config file: " + fconf);
				System.err.println(e.getMessage());
				return false;
			}
			Hashtable<String, String> map = parser.map;
			String value;
			
			value = map.get("db_url");
			if(value == null){
				ExceptionMan.die("missing db_url in config file " + fconf);
			}else{
				Config.db_url = value.trim();
			}
	
			value = map.get("db_username");
			if(value == null){
				//Config.db_username = "tuffer";
				ExceptionMan.die("missing db_username in config file " + fconf);
			}else{
				Config.db_username = value.trim();
			}
	
			value = map.get("db_password");
			if(value == null){
				//Config.db_password = "tuffer";
				ExceptionMan.die("missing db_password in config file " + fconf);
			}else{
				Config.db_password = value.trim();
			}

			value = map.get("dir_working");
			if(value != null){
				Config.dir_working = value.trim().replace('\\', '/');
			}
			String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
			String user = System.getProperty("user.name").toLowerCase().replaceAll("\\W", "_");
			String machine = java.net.InetAddress.getLocalHost().getHostName().toLowerCase().replaceAll("\\W", "_");
			
			String prod = Config.product_line;
			Config.dir_working += "/" + prod + "_" + machine + "_" + user + "_" + pid;
			
			if(Config.evidDBSchema == null){
				Config.db_schema = prod + "_" + machine + "_" + user + "_" + pid;
			}else{
				Config.db_schema = Config.evidDBSchema;
			}
			
			String curDir = System.getProperty("user.dir");
			
		
			println("Database schema     = " + Config.db_schema);
			println("Current directory   = " + curDir);
			println("Temporary directory = " + Config.dir_working);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}
	
}
