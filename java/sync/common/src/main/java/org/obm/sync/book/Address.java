package org.obm.sync.book;

public class Address implements IMergeable {

	private String street;
	private String zipCode;
	private String expressPostal;
	private String town;
	private String country;
	private String state;

	public Address(String street, String zipCode, String expressPostal,
			String town, String country, String state) {
		super();
		this.street = street;
		this.zipCode = zipCode;
		this.expressPostal = expressPostal;
		this.town = town;
		this.country = country;
		this.state = state;
	}

	public String getCountry() {
		return country;
	}

	public String getExpressPostal() {
		return expressPostal;
	}

	public String getStreet() {
		return street;
	}

	public String getTown() {
		return town;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setExpressPostal(String expressPostal) {
		this.expressPostal = expressPostal;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public void setTown(String town) {
		this.town = town;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	@Override
	public void merge(IMergeable previous) {
		if (previous instanceof Address) {
			Address prev = (Address) previous;
			if (getStreet() == null && prev.getStreet() != null) {
				setStreet(prev.getStreet());
			}
			if (getCountry() == null && prev.getCountry() != null) {
				setCountry(prev.getCountry());
			}
			if (getExpressPostal() == null && prev.getExpressPostal() != null) {
				setExpressPostal(prev.getExpressPostal());
			}
			if (getZipCode() == null && prev.getZipCode() != null) {
				setZipCode(prev.getZipCode());
			}
			if (getTown() == null && prev.getTown() != null) {
				setTown(prev.getTown());
			}
		}
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

}
