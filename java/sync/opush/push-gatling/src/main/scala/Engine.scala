import com.excilys.ebi.gatling.app.Gatling
import com.excilys.ebi.gatling.core.config.GatlingPropertiesBuilder

object Engine extends App {

	val props = new GatlingPropertiesBuilder
	props.dataDirectory(IDEPathHelper.dataDirectoryPath.toString)
	props.resultsDirectory(IDEPathHelper.resultsDirectoryPath.toString)
	props.requestBodiesDirectory(IDEPathHelper.requestBodiesDirectoryPath.toString)
	props.binariesDirectory(IDEPathHelper.mavenBinariesDirectoryPath.toString)

	Gatling.fromMap(props.build)
}
