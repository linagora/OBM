package org.obm.push.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class MSTask implements IApplicationData, Serializable {

	@Override
	public PIMDataType getType() {
		return PIMDataType.TASKS;
	}

	private String subject;
	private Integer importance;
	private Date utcStartDate;
	private Date startDate;
	private Date UtcDueDate;
	private Date dueDate;
	private List<String> categories;
	private Recurrence recurrence;
	private Boolean complete;
	private Date dateCompleted;
	private CalendarSensitivity sensitivity;
	private Date reminderTime;
	private Boolean reminderSet;
	private String description;
	
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Integer getImportance() {
		return importance;
	}

	public void setImportance(Integer importance) {
		this.importance = importance;
	}

	public Date getUtcStartDate() {
		return utcStartDate;
	}

	public void setUtcStartDate(Date utcStartDate) {
		this.utcStartDate = utcStartDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getUtcDueDate() {
		return UtcDueDate;
	}

	public void setUtcDueDate(Date utcDueDate) {
		UtcDueDate = utcDueDate;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	public Recurrence getRecurrence() {
		return recurrence;
	}

	public void setRecurrence(Recurrence recurrence) {
		this.recurrence = recurrence;
	}

	public Boolean getComplete() {
		return complete;
	}

	public void setComplete(Boolean complete) {
		this.complete = complete;
	}

	public Date getDateCompleted() {
		return dateCompleted;
	}

	public void setDateCompleted(Date dateCompleted) {
		this.dateCompleted = dateCompleted;
	}

	public CalendarSensitivity getSensitivity() {
		return sensitivity;
	}

	public void setSensitivity(CalendarSensitivity sensitivity) {
		this.sensitivity = sensitivity;
	}

	public Date getReminderTime() {
		return reminderTime;
	}

	public void setReminderTime(Date reminderTime) {
		this.reminderTime = reminderTime;
	}

	public Boolean getReminderSet() {
		return reminderSet;
	}

	public void setReminderSet(Boolean reminderSet) {
		this.reminderSet = reminderSet;
	}
	

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((UtcDueDate == null) ? 0 : UtcDueDate.hashCode());
		result = prime * result
				+ ((categories == null) ? 0 : categories.hashCode());
		result = prime * result
				+ ((complete == null) ? 0 : complete.hashCode());
		result = prime * result
				+ ((dateCompleted == null) ? 0 : dateCompleted.hashCode());
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((dueDate == null) ? 0 : dueDate.hashCode());
		result = prime * result
				+ ((importance == null) ? 0 : importance.hashCode());
		result = prime * result
				+ ((recurrence == null) ? 0 : recurrence.hashCode());
		result = prime * result
				+ ((reminderSet == null) ? 0 : reminderSet.hashCode());
		result = prime * result
				+ ((reminderTime == null) ? 0 : reminderTime.hashCode());
		result = prime * result
				+ ((sensitivity == null) ? 0 : sensitivity.hashCode());
		result = prime * result
				+ ((startDate == null) ? 0 : startDate.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
		result = prime * result
				+ ((utcStartDate == null) ? 0 : utcStartDate.hashCode());
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
		MSTask other = (MSTask) obj;
		if (UtcDueDate == null) {
			if (other.UtcDueDate != null)
				return false;
		} else if (!UtcDueDate.equals(other.UtcDueDate))
			return false;
		if (categories == null) {
			if (other.categories != null)
				return false;
		} else if (!categories.equals(other.categories))
			return false;
		if (complete == null) {
			if (other.complete != null)
				return false;
		} else if (!complete.equals(other.complete))
			return false;
		if (dateCompleted == null) {
			if (other.dateCompleted != null)
				return false;
		} else if (!dateCompleted.equals(other.dateCompleted))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (dueDate == null) {
			if (other.dueDate != null)
				return false;
		} else if (!dueDate.equals(other.dueDate))
			return false;
		if (importance == null) {
			if (other.importance != null)
				return false;
		} else if (!importance.equals(other.importance))
			return false;
		if (recurrence == null) {
			if (other.recurrence != null)
				return false;
		} else if (!recurrence.equals(other.recurrence))
			return false;
		if (reminderSet == null) {
			if (other.reminderSet != null)
				return false;
		} else if (!reminderSet.equals(other.reminderSet))
			return false;
		if (reminderTime == null) {
			if (other.reminderTime != null)
				return false;
		} else if (!reminderTime.equals(other.reminderTime))
			return false;
		if (sensitivity == null) {
			if (other.sensitivity != null)
				return false;
		} else if (!sensitivity.equals(other.sensitivity))
			return false;
		if (startDate == null) {
			if (other.startDate != null)
				return false;
		} else if (!startDate.equals(other.startDate))
			return false;
		if (subject == null) {
			if (other.subject != null)
				return false;
		} else if (!subject.equals(other.subject))
			return false;
		if (utcStartDate == null) {
			if (other.utcStartDate != null)
				return false;
		} else if (!utcStartDate.equals(other.utcStartDate))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MSTask [UtcDueDate=" + UtcDueDate + ", categories="
				+ categories + ", complete=" + complete + ", dateCompleted="
				+ dateCompleted + ", description=" + description + ", dueDate="
				+ dueDate + ", importance=" + importance + ", recurrence="
				+ recurrence + ", reminderSet=" + reminderSet
				+ ", reminderTime=" + reminderTime + ", sensitivity="
				+ sensitivity + ", startDate=" + startDate + ", subject="
				+ subject + ", utcStartDate=" + utcStartDate + "]";
	}
}
