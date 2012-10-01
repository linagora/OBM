import com.excilys.ebi.gatling.app.Gatling
import com.excilys.ebi.gatling.core.config.GatlingPropertiesBuilder
import com.excilys.ebi.gatling.core.util.PathHelper.path2string

object Engine extends App {

	val props = new GatlingPropertiesBuilder
	props.dataDirectory(IDEPathHelper.dataDirectoryPath)
	props.resultsDirectory(IDEPathHelper.resultsDirectoryPath)
	props.requestBodiesDirectory(IDEPathHelper.requestBodiesDirectoryPath)
	props.binariesDirectory(IDEPathHelper.mavenBinariesDirectoryPath)

	Gatling.fromMap(props.build)
}
