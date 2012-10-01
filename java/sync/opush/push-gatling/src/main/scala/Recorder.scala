import com.excilys.ebi.gatling.core.util.PathHelper.path2string
import com.excilys.ebi.gatling.recorder.config.RecorderOptions
import com.excilys.ebi.gatling.recorder.controller.RecorderController

import IDEPathHelper.{ requestBodiesDirectoryPath, recorderOutputDirectoryPath }

object Recorder extends App {

	RecorderController(new RecorderOptions(
		outputFolder = Some(IDEPathHelper.recorderOutputDirectoryPath),
		simulationPackage = Some("org.obm.opush"),
		requestBodiesFolder = Some(IDEPathHelper.requestBodiesDirectoryPath)))
}