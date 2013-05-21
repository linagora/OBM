package com.linagora.obm.ui.page;

import java.util.List;

import org.openqa.selenium.By.ByCssSelector;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverCommandProcessor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.google.inject.Inject;
import com.linagora.obm.ui.service.Services.Logout;
import com.linagora.obm.ui.url.ServiceUrlMapping;

public abstract class RootPage implements Page {

	@Inject protected ServiceUrlMapping mapping;
	@Inject protected PageFactory pageFactory;
	protected final WebDriver driver;
	private String timeout = "30";
	
	@FindBy(name="displayMessageOk")
	private List<WebElement> messagesOk;
	@FindBy(name="displayMessageInfo")
	private List<WebElement> messagesInfo;
	@FindBy(name="displayMessageWarning")
	private List<WebElement> messagesWarning;
	@FindBy(name="displayMessageError")
	private List<WebElement> messagesError;
	@FindBy(id="bannerLogoutLink")
	private WebElement logoutLink;

	public RootPage(WebDriver driver) {
		this.driver = driver;
	}
	
	@Override
	public String currentTitle() {
		return driver.getTitle();
	}	

	public LoginPage logout() {
		elLogoutLink().click();
		return pageFactory.create(driver, LoginPage.class);
	}

	public LoginPage logoutByUrl() {
		driver.get(mapping.lookup(Logout.class).toExternalForm());
		return pageFactory.create(driver, LoginPage.class);
	}

	public List<WebElement> elMessagesOk() {
		return messagesOk;
	}

	public List<WebElement> elMessagesInfo() {
		return messagesInfo;
	}

	public List<WebElement> elMessagesWarning() {
		return messagesWarning;
	}

	public List<WebElement> elMessagesError() {
		return messagesError;
	}

	public WebElement elLogoutLink() {
		return logoutLink;
	}
	
	protected boolean clickCheckbox(WebElement field, boolean hasToBeClicked) {
		if (hasToBeClicked) {
			field.click();
		}
		return hasToBeClicked;
	}
	
	public WebElement getDivByTitle(String title) {
		return driver.findElement(new ByCssSelector("div[title*='" + title + "']"));
	}
	
	public void waitForPageToLoad() {
		WebDriverCommandProcessor webDriverCommandProcessor = new WebDriverCommandProcessor(driver.getCurrentUrl(), driver);
		webDriverCommandProcessor.doCommand("waitForPageToLoad", new String[] {timeout});
	}
}
