package models;

import java.io.Serializable;

public class SessionRecord extends DbRecord implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long id = null;
	
	public void setId(Long id)
	{
		this.id = id;
	}
	
	public Long getId(){
		return id;
	}

	public FileRecord newFile() {
	    FileRecord file = new FileRecord();
	    file.setSessionid(getId());
	    file.init();
	    return file;
	}
	
	public String getTable()
	{
		return "sessions";
	}
}
