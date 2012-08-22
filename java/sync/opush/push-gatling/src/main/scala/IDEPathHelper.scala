import scala.tools.nsc.io.File
import scala.tools.nsc.io.Path
object IDEPathHelper {

	val gatlingConfUrl = getClass.getClassLoader.getResource("gatling.conf").getPath
	val projectRootDir = File(gatlingConfUrl).parents(1)

	val mavenSourcesDirectoryPath = projectRootDir / "src" / "main" / "scala"
	val mavenResourcesDirectoryPath = projectRootDir / "src" / "main" / "resources"
	val mavenTargetDirectoryPath = projectRootDir / "target"
	val mavenBinariesDirectoryPath = mavenTargetDirectoryPath / "classes"

	val dataDirectoryPath = mavenResourcesDirectoryPath / "data"
	val requestBodiesDirectoryPath = mavenResourcesDirectoryPath / "request-bodies"

	val recorderOutputDirectoryPath = mavenSourcesDirectoryPath
	val resultsDirectoryPath = mavenTargetDirectoryPath / "gatling-results"
}