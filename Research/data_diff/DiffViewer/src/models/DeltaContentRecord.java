package models;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DeltaContentRecord extends DbRecord implements Serializable {
	private static final long serialVersionUID = 1L;

	private long id;
	
	private DeltaRecord delta;

	private String contents;
	
	public void setId(long id)
	{
		this.id = id;
	}
	
	public long getId(){
		return id;
	}
	
	public void setDelta(DeltaRecord delta)
	{
		this.delta = delta;
	}
	
	public DeltaRecord getDelta(){
		return delta;
	}
	
	public String getContents(){
		return contents;
	}
	
	public void setContents(String contents)
	{
		this.contents = contents;
	}
	
	public String getTable()
	{
		return "delta_contents";
	}
	
}
