import com.excilys.ebi.gatling.recorder.config.RecorderOptions
import com.excilys.ebi.gatling.recorder.controller.RecorderController

import IDEPathHelper.{ requestBodiesDirectoryPath, recorderOutputDirectoryPath }

object Recorder extends App {

	RecorderController(new RecorderOptions(
		outputFolder = Some(IDEPathHelper.recorderOutputDirectoryPath.toString),
		simulationPackage = Some("org.obm.opush"),
		requestBodiesFolder = Some(IDEPathHelper.requestBodiesDirectoryPath.toString)))
}