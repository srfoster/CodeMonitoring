package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;

public class FileRecord extends DbRecord implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final int CREATED = 0;
	public static final int OPENED = 1;
	public static final int CLOSED = 2;
	public static final int DELETED = 3;

	private long sessionid;
	private long id;
	
	private Set<AttributeRecord> attributes = new HashSet<AttributeRecord>();

	
	public void init()
	{
	    AttributeRecord status = new AttributeRecord();
	    status.setName("status");
	    
	    AttributeRecord name = new AttributeRecord();
	    name.setName("name");
	    
	    AttributeRecord code = new AttributeRecord();
	    code.setName("code");
	    
	    AttributeRecord last_edit = new AttributeRecord();
	    last_edit.setName("last_edit");
	    
	    name.setFile(this);
	    code.setFile(this);
	    status.setFile(this);
	    last_edit.setFile(this);
	    
	    save();
	    name.save();
	    code.save();
	    status.save();
	    last_edit.save();
	    
	    reload();
	}
	
	public void setSessionid(long session_id)
	{
		this.sessionid = session_id;
	}
	
	public long getSessionid(){
		return sessionid;
	}
	
	public void setId(long id)
	{
		this.id = id;
	}
	
	public long getId(){
		return id;
	}
	
	public void setAttributes(Set attrs)
	{
		this.attributes = attrs;
	}
	
	public Set getAttributes(){
		return attributes;
	}

	public AttributeRecord getAttribute(String name) {
		for(AttributeRecord r : attributes)
		{
			if(r.getName().toLowerCase().equals(name.toLowerCase()))
				return r;
		}
		
		return null;
	}

	/*
	public void setFileName(FQN file_name) {
	    DeltaRecord name_delta = new DeltaRecord();
	    name_delta.setAttribute(getNameAttribute());
	    name_delta.save();
	    
	    DeltaContentRecord name_contents = new DeltaContentRecord();
	    name_contents.setContents(file_name.toString());
	    name_contents.setDelta(name_delta);
	    name_contents.save();
	    
	    name_delta.reload();
	    getNameAttribute().reload();
	    
	    reload();
	    
	    String s = getNameString();
	}
	*/

	public void setStatus(int s) {
		
		String status_string = "";
		
		if(s == DELETED) {
			status_string = "Deleted";
		} else if (s == OPENED) {
			status_string = "Opened";
		} else if (s == CREATED) {
			status_string = "Created";
		} else if (s == CLOSED) {
			status_string = "Closed";
		} else {
			throw new RuntimeException("Status not found");
		}
		
	    DeltaRecord status_delta = new DeltaRecord();
	    status_delta.setAttribute(getStatusAttribute());
	    status_delta.save();
	    
	    DeltaContentRecord status_contents = new DeltaContentRecord();
	    status_contents.setContents(status_string);
	    status_contents.setDelta(status_delta);
	    status_contents.save();
	    
	    status_delta.reload();
	    getStatusAttribute().reload();
	}
	
	public void setLastEdit(String edit)
	{
	    DeltaRecord edit_delta = new DeltaRecord();
	    edit_delta.setAttribute(getAttribute("last_edit"));
	    edit_delta.save();
	    
	    DeltaContentRecord status_contents = new DeltaContentRecord();
	    status_contents.setContents(edit);
	    status_contents.setDelta(edit_delta);
	    status_contents.save();
	    
	    edit_delta.reload();
	    getAttribute("last_edit").reload();
		
	}

	//We'll make this set the current code string to a diff of the last string and code_string.
	// (Unless code_string is the first delta.  In which case we diff with an empty string or just save the text.)
	//If we implement a getCode() -- we should patch the file's original contents with all of the subsequent diffs.
	//  Need unit tests for all of this stuff.
	public void setCode(String code_string) {
		String string = "";
		
		if(getCode() == null)
		{
			string = code_string;
		} else {
			String left_string = getCode();
			String right_string = code_string;
			
	        List<String> original = Arrays.asList(left_string.split("\n"));
	        List<String> revised  = Arrays.asList(right_string.split("\n"));
	        
	        // Compute diff. Get the Patch object. Patch is the container for computed deltas.
	        Patch patch = DiffUtils.diff(original, revised);
	        
	        List<String> unified_diff = DiffUtils.generateUnifiedDiff("before","after",original,patch,0);
			
	        for(String s : unified_diff)
	        	string += s + "\n"; //Set it to the diff of code_string and getCode().
		}
		
		
		DeltaRecord code_delta = new DeltaRecord();
	    code_delta.setAttribute(getCodeAttribute());
	    code_delta.save();
	    
	    DeltaContentRecord code_contents = new DeltaContentRecord();

		code_contents.setContents(string);
		
		
	    code_contents.setDelta(code_delta);
	    code_contents.save();
	    
	    code_delta.reload();
	    getCodeAttribute().reload();
	}
	
	public String getCode()
	{
	    reload();

		AttributeRecord code = getAttribute("code");
		
		Set<DeltaRecord> deltas = code.getDeltas();
		
		if(deltas.size() == 0)
			return null;
		
		List<DeltaRecord> ordered_deltas = new ArrayList(deltas);
		Collections.sort(ordered_deltas, new Comparator<DeltaRecord>(){

			@Override
			public int compare(DeltaRecord f, DeltaRecord s) {
				return f.getCreatedAt().compareTo(s.getCreatedAt());
			}
			
		});
		
		String original_code = ordered_deltas.get(0).getContent().getContents();
		
		if(ordered_deltas.size() == 1)
			return original_code;
		
		List<String> original_code_lines = Arrays.asList(original_code.split("\n"));
		
		for(int i = 1; i < ordered_deltas.size(); i++)
		{
			DeltaRecord current = ordered_deltas.get(i);
			
			String diff_string = current.getContent().getContents();
			
			Patch patch = DiffUtils.parseUnifiedDiff(Arrays.asList(diff_string.split("\n")));
			
			List<String> patched_code_lines = null;
			
			try {
				patched_code_lines = (List<String>) DiffUtils.patch(original_code_lines, patch);
			} catch (PatchFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			original_code_lines = patched_code_lines;
			
		}
		
		
		original_code = original_code_lines.get(0);
		for(int i = 1; i < original_code_lines.size(); i++)
			original_code += "\n" + original_code_lines.get(i);
		
		return original_code;
		
	}
	
	public String getLastEdit()
	{
	    reload();

		AttributeRecord status = getAttribute("last_edit");
		DeltaRecord delta = status.getLatestDelta();
		if(delta == null)
			return null;
		DeltaContentRecord content = delta.getContent();
		return content.getContents();
	}

	public String getStatusString()
	{
	    reload();

		AttributeRecord status = getAttribute("status");
		DeltaRecord delta = status.getLatestDelta();
		if(delta == null)
			return null;
		DeltaContentRecord content = delta.getContent();
		return content.getContents();
	}
	
	public int getStatus() {
		//Get the status attribute,
		//get the delta with the highest created_at timestamp
		//get its delta_content,
		//check the string against "Created","Opened","Closed","Deleted"

		
		String s = getStatusString();
		
		if(s.toLowerCase().equals("Created".toLowerCase()))
			return CREATED;
		if(s.toLowerCase().equals("Opened".toLowerCase()))
			return OPENED;
		if(s.toLowerCase().equals("Closed".toLowerCase()))
			return CLOSED;
		if(s.toLowerCase().equals("Deleted".toLowerCase()))
			return DELETED;
		
		return -1;
	}

	public String getNameString() {
	    reload();

		AttributeRecord name = getAttribute("name");
		DeltaRecord delta = name.getLatestDelta();
		if(delta == null)
			return null;
		DeltaContentRecord content = delta.getContent();
		return content.getContents();
	}
	
	public Object getContents() {
	    reload();

		AttributeRecord status = getAttribute("code");
		DeltaRecord delta = status.getLatestDelta();
		if(delta == null)
			return null;
		DeltaContentRecord content = delta.getContent();
		return content.getContents();
	}
	
	public String getTable()
	{
		return "files";
	}

	private AttributeRecord getNameAttribute()
	{
		return getAttribute("name");
	}
	
	private AttributeRecord getStatusAttribute()
	{
		return getAttribute("status");
	}
	
	private AttributeRecord getCodeAttribute()
	{
		return getAttribute("code");
	}
	

}
