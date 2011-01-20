package org.obm.sync.mailingList;

import java.util.LinkedList;
import java.util.List;

public class MailingList {

	private Integer id;
	private String name;
	private List<MLEmail> emails;
	
	public MailingList() {
		emails = new LinkedList<MLEmail>();
	}

	public MailingList(String name, Integer id) {
		this.name = name;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public List<MLEmail> getEmails() {
		return emails;
	}

	public void addEmail(MLEmail email) {
		this.emails.add(email);
	}
	

	public void addEmails(List<MLEmail> emails) {
		this.emails.addAll(emails);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((emails == null) ? 0 : emails.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MailingList other = (MailingList) obj;
		if (emails == null) {
			if (other.emails != null)
				return false;
		} else if (!emails.equals(other.emails))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append("MailingList ");
		ret.append(name);
		ret.append("' (");
		ret.append(id);
		ret.append(")\n");
		for(MLEmail e : emails){
			ret.append("	");
			ret.append(e.toString());
			ret.append("\n");
		}
		
		return ret.toString();
	}
}
