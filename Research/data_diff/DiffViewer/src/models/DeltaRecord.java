package models;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class DeltaRecord extends DbRecord implements Serializable {
	private static final long serialVersionUID = 1L;

	private long id;
	
	private String createdat;
	
	private Set contents = new HashSet<DeltaContentRecord>();

	private AttributeRecord attribute;
	
	public void setId(long id)
	{
		this.id = id;
	}
	
	public long getId(){
		return id;
	}
	
	public void setContents(Set contents)
	{
		this.contents = contents;
	}
	
	public Set getContents(){
		return contents;
	}
	
	public DeltaContentRecord getContent()
	{
		for(Object o : contents)
		{
			return (DeltaContentRecord) o;
		}
		
		return null;
	}
	
	public AttributeRecord getAttribute(){
		return attribute;
	}
	
	public void setAttribute(AttributeRecord attribute)
	{
		this.attribute = attribute;
	}

	public String getCreatedat() {
		return createdat;
	}
	
	public void setCreatedat(String c) {
		createdat = c;
	}
	
	public Date getCreatedAt(){
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		
		try {
			return df.parse(getCreatedat());
		} catch (ParseException e) {
			return null;
		}
	}
	
	public String getTable()
	{
		return "deltas";
	}
}
