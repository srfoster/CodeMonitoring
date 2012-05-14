
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import models.AttributeRecord;
import models.DbRecord;
import models.DeltaContentRecord;
import models.DeltaRecord;
import models.FileRecord;
import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;

public class DiffViewer {
	
	List<DbRecord> deltas;
	Iterator deltas_iterator;
	
	HashMap<Long,Info> info;
	
	boolean paused = true;
	
	int count = 0;  
	
	public static void main(String[] args)
	{
		(new DiffViewer()).init();
	}
	
	public void init() {
		info = new HashMap<Long,Info>();
		
		deltas = (new DeltaContentRecord()).all();
		deltas_iterator = deltas.iterator();
		
		while(deltas_iterator.hasNext())
		{
			nextDelta();
		}
	}
	
	
	public void nextDelta()
	{
		count++;
		System.out.println(count + " of " + deltas.size());
		DeltaContentRecord content = (DeltaContentRecord) deltas_iterator.next();
		DeltaRecord delta = content.getDelta();
		AttributeRecord attribute = delta.getAttribute();
		FileRecord file = attribute.getFile();
		
	//	System.out.println("Handling:");
	//	System.out.println("  " + attribute.getName());
	//	System.out.println("  " + content.getContents());

		if(attribute.getName().equals("status")) { //File status changed
			
			if(content.getContents().toLowerCase().equals("Opened".toLowerCase())){        //File opened
				
				Info i = info.get(file.getId());
				
				if(i != null) { //We know about the opened file
					i.status = "Open";
				} else { //We don't know about the opened file (i.e. we don't know it's name yet).
					
					// We'll just ignore this and wait for the first name/content change.
					System.out.println();
				}
				
			}else if(content.getContents().toLowerCase().equals("Closed".toLowerCase())){  //File closed
				
				Info i = info.get(file.getId());
				
				
				if(i != null)
				{
					i.status = "Closed";
				} else { //Closing a file we don't know about.  We'll ignore this for now.
					System.out.println();
				}
				
				
			}else if(content.getContents().toLowerCase().equals("Created".toLowerCase())){ //File created
				
				// We'll ignore this for now.  We can display this event somehow if we decide to care.
				
			}else if(content.getContents().toLowerCase().equals("Deleted".toLowerCase())){ //File deleted.  Treat like close.
				Info i = info.get(file.getId());
				
				if(i != null)
				{
					i.status = "Closed";
				} else { //Closing a file we don't know about.  We'll ignore this for now.
					System.out.println();
				}
				
			}else{
				throw new RuntimeException("Unknown status change");
			}
			
		} else if (attribute.getName().equals("name")) { //File name changed (or file was moved)
			
			// Execute a rename/move within sessions/ directory
			
			Info i = info.get(file.getId());
			
			if(i != null)
			{
				//We'll ignore the rename.
				//If we decide to care, we should:
				// 1) Display the event in some kind of popup.
				// 2) Move the file inside of sessions.
				// 3) Change the file_name attribute of the info object that file.getId() maps to.
				System.out.println();
			} else {

				Info i_new = new Info();
				i_new.file_name = content.getContents();
				i_new.status = "Open";
				//System.out.println(content.getContents());
				info.put(file.getId(), i_new);
			}
			
		} else if (attribute.getName().equals("code")) { //File's contents changed
			
			//info.get(file.getId());
			Info i = info.get(file.getId());
			handleDelta(content, i);
			
		}
		

	}
	
	private void handleDelta(DeltaContentRecord delta_content, Info info)
	{
		String delta = delta_content.getContents();
				
		if(delta.equals(""))
			return;
		//System.out.println(delta);
		
		

        
        Patch patch = DiffUtils.parseUnifiedDiff(Arrays.asList(delta.split("\n")));
        


        
        if(patch.getDeltas().size() == 0)
        {
            info.setContent(delta, delta_content);
        	
            return;
        }
        
        List result = null;
		try {
			String content = info.getContent();
			String[] lines = null;
			if(content.equals("\n"))
				lines = new String[]{""};  //Jumping through hoops to appease DiffUtlis.
			else
				lines = content.split("\n");
			result = DiffUtils.patch(Arrays.asList(lines), patch);
		} catch (PatchFailedException e) {
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		if(result == null) return;
		
        String new_text = "";
        
        for(Object o : result)
        {
        	new_text += ((String) o) + "\n";
        }

        info.setContent(new_text, delta_content);
	}
	
	public String filesOpen()
	{
		String file_string = "";
		
		for(Long l : info.keySet())
		{
			Info i = info.get(l);
			if(i.status.equals("Open"))
				file_string += " " + i.file_name;
		}
		
		return file_string;
	}

	
	private class Info
	{		
		public String file_name;
		public String status;
		private String content;
		private boolean first = true;
		
		
		public Info()
		{
			content = "";
		}
		
		public void setContent(String content, DeltaContentRecord delta_content)
		{
			this.content = content;

			String[] split_name = file_name.split("\\/");
			String short_name = split_name[split_name.length - 1];
			
			(new File("output/" + short_name)).mkdir(); // I assume this'll fail if it exists.

			String file_name = String.format("output/" + short_name + "/" + short_name + "_" + "%03d", getLastFileNumber("output/" + short_name + "/")+1);

			String preamble = "//Time : " + delta_content.getDelta().getCreatedat().toString() + "\n";
			preamble += "//Files Open :"+ filesOpen()+" \n";			
			
			try{
				  // Create file 
				  FileWriter fstream = new FileWriter(file_name);
				  BufferedWriter out = new BufferedWriter(fstream);
				  out.write(preamble + content);
				  //Close the output stream
				  out.close();
			}catch (Exception e){//Catch exception if any
				  System.err.println("Error: " + e.getMessage());
			}
			
			if(first)
			{
				first = false;
				return;
			}
			
			String last_file_name = String.format("output/" + short_name + "/" + short_name + "_" + "%03d", getLastFileNumber("output/" + short_name + "/")-1);

			preamble += "/*AST Changes :\n" + smartDiff(file_name, last_file_name) + "*/";

			//Ugly: Save the file twice, so we can save smart diff info.
			try{
				  // Create file 
				  FileWriter fstream = new FileWriter(file_name);
				  BufferedWriter out = new BufferedWriter(fstream);
				  out.write(preamble + content);
				  //Close the output stream
				  out.close();
			}catch (Exception e){//Catch exception if any
				  System.err.println("Error: " + e.getMessage());
			}
			
			count++;
		}
		
		public String smartDiff(String file_name1, String file_name2){
			SmartDiffer differ = new SmartDiffer();
			
			try {
				differ.handleFile(file_name1, file_name2);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String ret = "";
			for(String key : differ.getAggregations().keySet())
			{
				ret += (key + " = " + differ.getAggregations().get(key)) + "\n";
			}
			
			return ret;
		}
		
		public int getLastFileNumber(String dir_name)
		{
			File dir = new File(dir_name);

			String[] children = dir.list();
			
			int max = -1;

		    for (int i=0; i<children.length; i++) {
		        // Get filename of file or directory
		        String filename = children[i];
		        
		        int current = Integer.parseInt(filename.split("java_")[1]);
		        		
		        if(current > max)
		        	max = current;
		    }

		    return max;
		}
		
		public String getContent()
		{
			return content;
		}
	}

}
