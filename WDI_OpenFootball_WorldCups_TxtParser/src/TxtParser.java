import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;

public class TxtParser {
	public String path_main = "/Users/TimoSturm/Desktop/world-cup-master";
	public ArrayList<String> paths_cups = new ArrayList<String>();
	public ArrayList<ArrayList<String>> paths_cup_countries = new ArrayList<ArrayList<String>>();
	public String path_cup = "/1930--uruguay/squads/";
	public String fileName_out = "/Users/TimoSturm/Desktop/results/results.csv";
	private String columns = "cup_host,cup_year,countryName_full,FIFACountryCode,numberOfPlayers,"
			+ "player_shirtNumber,player_position,player_name,player_caps,player_club";
	
	// Information:
	private String cup_year = "";
	private String cup_host = "";
	private String countryName_full = "";
	private String fifaCountryCode = "";
	private String numberOfPlayers = "-1";
	private ArrayList<String> player_shirtNumbers = new ArrayList<String>();
	private ArrayList<String> player_positions = new ArrayList<String>();
	private ArrayList<String> player_names = new ArrayList<String>();
	private ArrayList<String> player_caps = new ArrayList<String>();
	private ArrayList<String> player_clubs = new ArrayList<String>();
	
	public static void main(String[] args){
		
		TxtParser parser = new TxtParser();
		
		// check if file already exists. If yes, then it's an old version and will be deleted.
		File file = new File(parser.fileName_out);
		if(file.isFile()){ 
			file.delete();
		}
		
		// parse all files
		parser.parseAllTxtFiles();
		
	}
	
	public void parseAllTxtFiles(){
		// get file names of all files/directories contained in the main directory (path_main)
		File file = new File(path_main);
		String[] names = file.list();
		
		// loop through all files/directories that contain a number smaller equal to 2014 (= directories containing data of past cups)
		for(int i=0; i< names.length; i++){
			String current_name = names[i];
			if(current_name.matches("[0-9]+.*") && Integer.valueOf(current_name.substring(0, 4)) <= 2014){
				String newPath_cup = "/" + current_name + "/squads/";
				paths_cups.add(newPath_cup);
				
				// list & loop through all files/directories that are directly contained in the current directory and remove those that do not contain "-" in their name from the list
				File current_file = new File(path_main + newPath_cup);
				String[] current_file_names = current_file.list();
				ArrayList<String> validCountryFileNames = new ArrayList<String>();
				for(int j=0; j<current_file_names.length; j++){
					if(current_file_names[j].matches(".*-.*")){
						validCountryFileNames.add(current_file_names[j]);
					}
				}
				paths_cup_countries.add(validCountryFileNames);
			}
		}
		
		// parse all txt files
		for(int i=0; i<paths_cups.size(); i++){
			ArrayList<String> current_path_cup_country = paths_cup_countries.get(i);
			for(int j=0; j<current_path_cup_country.size(); j++){
				try {
					parseTxtFile(path_main + paths_cups.get(i) + current_path_cup_country.get(j));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public void parseTxtFile(String path) throws IOException{
		cup_year = path.replaceAll("[^0-9]", "");
		cup_host = path.split("/")[path.split("/").length - 3].replaceAll("[0-9]", "").replace("-", "");
		
		BufferedReader in = new BufferedReader(new FileReader(path));
		String line;
		int counter_line = 1;
		
		while((line = in.readLine()) != null) {
		    if(counter_line == 2){
		    	String[] countryNames = line.split(" ");
		    	countryName_full = countryNames[1];
		    	for(int i=2; i<countryNames.length; i++){
		    		if(!countryNames[i].contains("(")){
		    			countryName_full = countryName_full + " " + countryNames[i];
		    		}
		    		else{
				    	fifaCountryCode = countryNames[i].replace("(", "").replace(")", "");
		    		}
		    	}
		    }
		    else if(counter_line == 3){
		    	ArrayList<String> splitResult = new ArrayList<String>(Arrays.asList(line.split(" ")));
		    	splitResult.removeAll(Arrays.asList("", null));
		    	numberOfPlayers = splitResult.get(2);
		    }
		    else if(counter_line > 3){
		    	ArrayList<String> splitResult = new ArrayList<String>(Arrays.asList(line.split(" ")));
		    	splitResult.removeAll(Arrays.asList("", null));
		    	if(!splitResult.isEmpty()){
			    	player_shirtNumbers.add(splitResult.get(0).replace("(", "").replace(")", ""));
			    	player_positions.add(splitResult.get(1));
			    	
			    	int i = 2;
			    	String name_player = splitResult.get(i);
			    	i++;
			    	while(true){
			    		if (splitResult.get(i).contains("#")){
			    			break;
			    		}
			    		else{
			    			name_player = name_player + " " + splitResult.get(i);
			    		}
			    		i++;
			    	}
			    	player_names.add(name_player);
			    	player_caps.add(splitResult.get(++i).replace(",", ""));
			    	
			    	
			    	String name_club = splitResult.get(++i);
			    	i++;
			    	while(i < splitResult.size()){
			    		name_club = name_club + " " + splitResult.get(i);
			    		i++;
			    	}
			    	player_clubs.add(name_club);
		    	}
		    }
		    counter_line++;
		}
		in.close();
		
		System.out.println("cup_year: '" + cup_year + "'");
		System.out.println("cup_place: '" + cup_host + "'");
		System.out.println("countryName_full: '" + countryName_full + "'");
		System.out.println("countryName_short: '" + fifaCountryCode + "'");
		System.out.println("numberOfPlayers: '" + numberOfPlayers + "'");
		System.out.println("player_numbers: " + player_shirtNumbers);
		System.out.println("player_positions: " + player_positions);
		System.out.println("player_names: " + player_names);
		System.out.println("player_club_numbers: " + player_caps);
		System.out.println("player_clubs: " + player_clubs);
		
		createCSV();
	}
	
	private void createCSV(){

		try {
			File file = new File(fileName_out);
			
			if(!file.isFile()) { 
				Writer writer = new BufferedWriter(new OutputStreamWriter(
			              new FileOutputStream(fileName_out), "utf-8"));
				writer.write(columns + "\n");
				writer.close();
			}
			
			FileWriter fw = new FileWriter(fileName_out,true);
			String newLine = "";
			for(int i=0; i< player_names.size(); i++){
				 newLine = cup_host + "," + cup_year + "," + countryName_full + "," + fifaCountryCode + "," + numberOfPlayers + "," + 
						 player_shirtNumbers.get(i) + "," + player_positions.get(i) + "," + player_names.get(i) + "," + 
						 player_caps.get(i) + "," + player_clubs.get(i);
				 fw.write(newLine + "\n");	//appends the string to the file
			}
		    fw.close();
		    
			player_shirtNumbers = new ArrayList<String>();
			player_positions = new ArrayList<String>();
			player_names = new ArrayList<String>();
			player_caps = new ArrayList<String>();
			player_clubs = new ArrayList<String>();
		    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}
	
	
}
