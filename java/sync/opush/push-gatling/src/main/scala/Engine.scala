import com.excilys.ebi.gatling.app.{ Options, Gatling }
import com.excilys.ebi.gatling.core.util.PathHelper.path2string

object Engine extends App {

	new Gatling(Options(
		dataDirectoryPath = Some(IDEPathHelper.dataDirectoryPath),
		resultsDirectoryPath = Some(IDEPathHelper.resultsDirectoryPath),
		requestBodiesDirectoryPath = Some(IDEPathHelper.requestBodiesDirectoryPath),
		simulationBinariesDirectoryPath = Some(IDEPathHelper.mavenBinariesDirectoryPath))).start
}