package models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class AttributeRecord extends DbRecord implements Serializable{
	private static final long serialVersionUID = 1L;

	private long fileid;
	private long id;
	private String name;
	private FileRecord file;

	private Set deltas = new HashSet<DeltaRecord>();

	
	public void setFileid(long file_id)
	{
		this.fileid = file_id;
	}
	
	public long getFileid(){
		return fileid;
	}
	
	public FileRecord getFile()
	{
		return file;
	}
	
	public void setFile(FileRecord file)
	{
		this.file = file;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setId(long id)
	{
		this.id = id;
	}
	
	public long getId(){
		return id;
	}
	
	public Set getDeltas()
	{
		return deltas;
	}
	
	public void setDeltas(Set d)
	{
		deltas = d;
	}

	public DeltaRecord getLatestDelta() {
		DeltaRecord max = null;
		for(Object o : deltas)
		{
			DeltaRecord d = (DeltaRecord) o;
			
			Date current_time = d.getCreatedAt();
			Date max_time = max == null ? null : max.getCreatedAt();
			
			if(max == null || current_time.compareTo(max_time) > 0)
			{
				max = d;
			}
				
		}
			
		return max;
	}
	
	public String getTable()
	{
		return "attributes";
	}
}
