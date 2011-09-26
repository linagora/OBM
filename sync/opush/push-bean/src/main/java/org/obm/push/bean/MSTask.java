package org.obm.push.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import com.google.common.base.Objects;

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
	public final int hashCode(){
		return Objects.hashCode(subject, importance, utcStartDate, startDate, UtcDueDate, 
				dueDate, categories, recurrence, complete, dateCompleted, sensitivity, 
				reminderTime, reminderSet, description);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof MSTask) {
			MSTask that = (MSTask) object;
			return Objects.equal(this.subject, that.subject)
				&& Objects.equal(this.importance, that.importance)
				&& Objects.equal(this.utcStartDate, that.utcStartDate)
				&& Objects.equal(this.startDate, that.startDate)
				&& Objects.equal(this.UtcDueDate, that.UtcDueDate)
				&& Objects.equal(this.dueDate, that.dueDate)
				&& Objects.equal(this.categories, that.categories)
				&& Objects.equal(this.recurrence, that.recurrence)
				&& Objects.equal(this.complete, that.complete)
				&& Objects.equal(this.dateCompleted, that.dateCompleted)
				&& Objects.equal(this.sensitivity, that.sensitivity)
				&& Objects.equal(this.reminderTime, that.reminderTime)
				&& Objects.equal(this.reminderSet, that.reminderSet)
				&& Objects.equal(this.description, that.description);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("subject", subject)
			.add("importance", importance)
			.add("utcStartDate", utcStartDate)
			.add("startDate", startDate)
			.add("UtcDueDate", UtcDueDate)
			.add("dueDate", dueDate)
			.add("categories", categories)
			.add("recurrence", recurrence)
			.add("complete", complete)
			.add("dateCompleted", dateCompleted)
			.add("sensitivity", sensitivity)
			.add("reminderTime", reminderTime)
			.add("reminderSet", reminderSet)
			.add("description", description)
			.toString();
	}
	
}
